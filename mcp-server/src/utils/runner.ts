import { execFile } from 'node:child_process';
import { readdir, stat, access, mkdtemp } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { constants } from 'node:fs';
import type { ExecFileException, ExecFileOptions } from 'node:child_process';

const CLI_JAR_NAME = 'openapi-generator-cli.jar';
const TIMEOUT_MS = 120_000; // 2 minutes
const MAIN_CLASS = 'org.openapitools.codegen.OpenAPIGenerator';
const CLI_VERSION = '7.10.0';
const CLI_DOWNLOAD_URL = `https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${CLI_VERSION}/openapi-generator-cli-${CLI_VERSION}.jar`;

export class RunnerError extends Error {
  constructor(
    message: string,
    public readonly code: string,
  ) {
    super(message);
    this.name = 'RunnerError';
  }
}

export interface RunResult {
  stdout: string;
  stderr: string;
  exitCode: number;
}

/**
 * Promise wrapper around execFile that resolves on any exit (including non-zero)
 * and only rejects on timeout/kill or spawn failure.
 */
async function exec(
  command: string,
  args: string[],
  options?: ExecFileOptions,
): Promise<RunResult> {
  return new Promise<RunResult>((resolve, reject) => {
    execFile(command, args, options ?? {}, (error: ExecFileException | null, stdout, stderr) => {
      if (error?.killed) {
        reject(
          new RunnerError(
            'CLI process timed out after 2 minutes and was killed.',
            'CLI_TIMEOUT',
          ),
        );
        return;
      }
      resolve({
        stdout: String(stdout ?? ''),
        stderr: String(stderr ?? ''),
        exitCode: typeof error?.code === 'number' ? error.code : error ? 1 : 0,
      });
    });
  });
}

/**
 * Check if running on Windows and fail early.
 */
export function checkPlatform(): void {
  if (process.platform === 'win32') {
    throw new RunnerError(
      'Windows is not currently supported. Please use macOS or Linux.',
      'WINDOWS_NOT_SUPPORTED',
    );
  }
}

/**
 * Check that Java is available on PATH.
 */
export async function checkJava(): Promise<void> {
  const result = await exec('java', ['--version']).catch(() => null);
  if (!result || result.exitCode !== 0) {
    throw new RunnerError(
      'Java is not installed or not on PATH. Java 21+ is required. ' +
        'Run `java --version` to verify your installation.',
      'JAVA_NOT_FOUND',
    );
  }
}

/**
 * Resolve the project root directory.
 *
 * Priority:
 * 1. DART_ACDC_PROJECT_ROOT environment variable
 * 2. Infer from this file's location (mcp-server/dist/utils/runner.js → ../../..)
 */
export function resolveProjectRoot(): string {
  const envRoot = process.env.DART_ACDC_PROJECT_ROOT;
  if (envRoot) return envRoot;

  // At runtime: <project>/mcp-server/dist/utils/runner.js
  const thisDir = dirname(fileURLToPath(import.meta.url));
  return join(thisDir, '..', '..', '..');
}

/**
 * Find the OpenAPI Generator CLI JAR.
 */
export async function findCliJar(projectRoot: string): Promise<string> {
  const envPath = process.env.OPENAPI_CLI_JAR;
  if (envPath) {
    await assertFileReadable(envPath, 'OPENAPI_CLI_JAR');
    return envPath;
  }

  const conventionPath = join(projectRoot, CLI_JAR_NAME);
  try {
    await access(conventionPath, constants.R_OK);
    return conventionPath;
  } catch {
    throw new RunnerError(
      `OpenAPI Generator CLI JAR not found at ${conventionPath}. ` +
        `Download it from: ${CLI_DOWNLOAD_URL}\n` +
        'Or set the OPENAPI_CLI_JAR environment variable to the JAR path.',
      'CLI_JAR_NOT_FOUND',
    );
  }
}

/**
 * Check if a filename is a main generator JAR (excludes classifier JARs like -sources, -javadoc).
 */
function isMainGeneratorJar(name: string): boolean {
  return (
    name.startsWith('dart-acdc-generator-') &&
    name.endsWith('.jar') &&
    !name.endsWith('-sources.jar') &&
    !name.endsWith('-javadoc.jar') &&
    !name.endsWith('-tests.jar')
  );
}

/**
 * Find the dart-acdc generator JAR in generator/target/.
 * When multiple JARs exist, selects the most recently modified.
 */
export async function findGeneratorJar(projectRoot: string): Promise<string> {
  const envPath = process.env.DART_ACDC_GENERATOR_JAR;
  if (envPath) {
    await assertFileReadable(envPath, 'DART_ACDC_GENERATOR_JAR');
    return envPath;
  }

  const targetDir = join(projectRoot, 'generator', 'target');
  return findNewestJar(targetDir);
}

async function findNewestJar(targetDir: string): Promise<string> {
  let entries: string[];
  try {
    entries = await readdir(targetDir);
  } catch {
    throw new RunnerError(
      `Generator target directory not found: ${targetDir}. The generator may need to be built.`,
      'GENERATOR_JAR_NOT_FOUND',
    );
  }

  const jars = entries.filter(isMainGeneratorJar);
  if (jars.length === 0) {
    throw new RunnerError(
      `No dart-acdc generator JAR found in ${targetDir}. The generator may need to be built.`,
      'GENERATOR_JAR_NOT_FOUND',
    );
  }

  if (jars.length === 1) {
    return join(targetDir, jars[0]);
  }

  // Multiple JARs: pick the most recently modified
  let newest = jars[0];
  let newestMtime = 0;
  for (const jar of jars) {
    const info = await stat(join(targetDir, jar));
    if (info.mtimeMs > newestMtime) {
      newestMtime = info.mtimeMs;
      newest = jar;
    }
  }
  return join(targetDir, newest);
}

/**
 * Auto-build the generator JAR by running scripts/build.sh --skip-tests.
 */
export async function autoBuild(projectRoot: string): Promise<void> {
  const buildScript = join(projectRoot, 'scripts', 'build.sh');

  try {
    await access(buildScript, constants.X_OK);
  } catch {
    throw new RunnerError(
      `Build script not found or not executable: ${buildScript}. ` +
        'Maven 3.8+ is required to build the generator.',
      'BUILD_SCRIPT_NOT_FOUND',
    );
  }

  const result = await exec(buildScript, ['--skip-tests'], { timeout: TIMEOUT_MS });
  const output = result.stdout + result.stderr;

  if (result.exitCode !== 0) {
    if (output.includes('mvn: command not found') || output.includes('mvn: not found')) {
      throw new RunnerError(
        'Maven is not installed or not on PATH. Maven 3.8+ is required to build the generator.',
        'MAVEN_NOT_FOUND',
      );
    }
    throw new RunnerError(
      `Auto-build failed (exit code ${result.exitCode}):\n${output}`,
      'AUTO_BUILD_FAILED',
    );
  }
}

/**
 * Resolve both JAR paths, triggering auto-build if the generator JAR is missing.
 */
export async function resolveJars(
  projectRoot: string,
): Promise<{ cliJar: string; generatorJar: string }> {
  const cliJar = await findCliJar(projectRoot);

  let generatorJar: string;
  try {
    generatorJar = await findGeneratorJar(projectRoot);
  } catch (err) {
    if (err instanceof RunnerError && err.code === 'GENERATOR_JAR_NOT_FOUND') {
      await autoBuild(projectRoot);
      try {
        generatorJar = await findGeneratorJar(projectRoot);
      } catch {
        throw new RunnerError(
          'Auto-build completed successfully, but the generator JAR was still not found in ' +
            `${join(projectRoot, 'generator', 'target')}. ` +
            'The build may not have produced the expected artifact.',
          'JAR_NOT_FOUND_AFTER_BUILD',
        );
      }
    } else {
      throw err;
    }
  }

  return { cliJar, generatorJar };
}

/**
 * Create a unique temporary directory for a single invocation.
 */
export async function createTempDir(): Promise<string> {
  return mkdtemp(join(tmpdir(), 'dart-acdc-'));
}

/**
 * Run the OpenAPI Generator CLI with the given arguments.
 *
 * Performs all prerequisite checks (platform, Java, JAR discovery)
 * before spawning the Java process.
 */
export async function runCli(args: string[]): Promise<RunResult> {
  checkPlatform();
  await checkJava();

  const projectRoot = resolveProjectRoot();

  // If no env vars are set, verify the inferred project root looks valid
  const hasEnvConfig =
    process.env.DART_ACDC_PROJECT_ROOT ||
    process.env.OPENAPI_CLI_JAR ||
    process.env.DART_ACDC_GENERATOR_JAR;

  if (!hasEnvConfig) {
    try {
      await access(join(projectRoot, 'generator'), constants.R_OK);
    } catch {
      throw new RunnerError(
        'Could not determine project root. Set one of these environment variables:\n' +
          '  DART_ACDC_PROJECT_ROOT — path to the Dart-ACDC generator project root\n' +
          '  OPENAPI_CLI_JAR — path to openapi-generator-cli.jar\n' +
          '  DART_ACDC_GENERATOR_JAR — path to dart-acdc-generator-*.jar',
        'NO_CONFIG',
      );
    }
  }

  const { cliJar, generatorJar } = await resolveJars(projectRoot);
  const classpath = `${cliJar}:${generatorJar}`;

  return exec('java', ['-cp', classpath, MAIN_CLASS, ...args], {
    timeout: TIMEOUT_MS,
    killSignal: 'SIGKILL',
    maxBuffer: 10 * 1024 * 1024,
  });
}

async function assertFileReadable(filePath: string, envVarName: string): Promise<void> {
  try {
    await access(filePath, constants.R_OK);
  } catch {
    throw new RunnerError(
      `File specified by ${envVarName} not found: ${filePath}`,
      'FILE_NOT_FOUND',
    );
  }
}
