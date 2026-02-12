import { z } from 'zod';
import { writeFile, rm, access, readdir } from 'node:fs/promises';
import { join, resolve } from 'node:path';
import { constants } from 'node:fs';
import { runCli, createTempDir, RunnerError } from '../utils/runner.js';
import type { CallToolResult } from '@modelcontextprotocol/sdk/types.js';

export const GenerateInputSchema = {
  inputSpec: z.string().describe('Path to the OpenAPI specification file (YAML or JSON)'),
  outputDir: z.string().describe('Directory where the generated Dart package will be written'),

  // Package metadata
  pubName: z.string().optional().describe('Package name for pubspec.yaml (derived from OpenAPI info.title if not provided)'),
  pubVersion: z.string().optional().describe('Package version for pubspec.yaml').default('1.0.0'),
  pubDescription: z.string().optional().describe('Package description for pubspec.yaml'),
  pubAuthor: z.string().optional().describe('Package author for pubspec.yaml'),
  pubHomepage: z.string().optional().describe('Package homepage URL for pubspec.yaml'),

  // ACDC feature toggles
  enableAuthentication: z.boolean().optional().describe('Enable OAuth 2.1 authentication with automatic token refresh').default(true),
  enableCaching: z.boolean().optional().describe('Enable two-tier caching (memory + disk) with encryption').default(true),
  enableLogging: z.boolean().optional().describe('Enable configurable logging with sensitive data redaction').default(true),
  enableOfflineSupport: z.boolean().optional().describe('Enable offline detection and support').default(true),
  enableCertificatePinning: z.boolean().optional().describe('Enable certificate pinning for enhanced security').default(false),

  // Authentication options
  defaultTokenRefreshUrl: z.string().optional().describe('Default token refresh URL (e.g., https://api.example.com/auth/refresh)'),
  useSecureTokenStorage: z.boolean().optional().describe('Enable secure token storage using platform-specific secure storage').default(true),
  refreshThresholdMinutes: z.number().optional().describe('Minutes before token expiration to trigger refresh').default(5),

  // Cache options
  defaultCacheTtlHours: z.number().optional().describe('Default cache time-to-live in hours').default(1),
  cacheDiskSizeMb: z.number().optional().describe('Maximum disk cache size in megabytes').default(20),
  encryptCache: z.boolean().optional().describe('Enable cache encryption using AES-256').default(true),
  enableUserCacheIsolation: z.boolean().optional().describe('Enable user-specific cache isolation').default(true),

  // Logging options
  defaultLogLevel: z.enum(['none', 'error', 'warning', 'info', 'debug', 'verbose']).optional().describe('Default logging level').default('info'),
  redactSensitiveData: z.boolean().optional().describe('Enable automatic redaction of sensitive data in logs').default(true),

  // Code style options
  serializationLibrary: z.enum(['json_serializable', 'freezed']).optional().describe('Serialization library for model classes').default('json_serializable'),
  generateInterfaces: z.boolean().optional().describe('Generate abstract interface classes for remote data sources').default(true),
  dataSourceSuffix: z.string().optional().describe("Suffix for generated API class names (e.g., 'RemoteDataSource')").default('RemoteDataSource'),
  generateBarrelExports: z.boolean().optional().describe('Generate barrel export files').default(true),
};

type GenerateInput = {
  [K in keyof typeof GenerateInputSchema]: z.infer<(typeof GenerateInputSchema)[K]>;
};

/**
 * Build a YAML config string from typed parameters.
 * Only includes non-default/non-undefined values to keep the config minimal.
 */
export function buildConfigYaml(params: GenerateInput): string {
  const lines: string[] = [
    `generatorName: dart-acdc`,
    `inputSpec: "${params.inputSpec}"`,
    `outputDir: "${params.outputDir}"`,
  ];

  const additionalProperties: string[] = [];

  const stringProps: (keyof GenerateInput)[] = [
    'pubName', 'pubVersion', 'pubDescription', 'pubAuthor', 'pubHomepage',
    'defaultTokenRefreshUrl', 'defaultLogLevel', 'serializationLibrary', 'dataSourceSuffix',
  ];

  const boolProps: (keyof GenerateInput)[] = [
    'enableAuthentication', 'enableCaching', 'enableLogging', 'enableOfflineSupport',
    'enableCertificatePinning', 'useSecureTokenStorage', 'encryptCache',
    'enableUserCacheIsolation', 'redactSensitiveData', 'generateInterfaces',
    'generateBarrelExports',
  ];

  const numericAsStringProps: (keyof GenerateInput)[] = [
    'refreshThresholdMinutes', 'defaultCacheTtlHours', 'cacheDiskSizeMb',
  ];

  for (const key of stringProps) {
    const val = params[key];
    if (val !== undefined) {
      additionalProperties.push(`    ${key}: "${val}"`);
    }
  }

  for (const key of boolProps) {
    const val = params[key];
    if (val !== undefined) {
      additionalProperties.push(`    ${key}: ${val}`);
    }
  }

  // These are numeric in our schema but the Java CLI expects string values
  for (const key of numericAsStringProps) {
    const val = params[key];
    if (val !== undefined) {
      additionalProperties.push(`    ${key}: "${val}"`);
    }
  }

  if (additionalProperties.length > 0) {
    lines.push('additionalProperties:');
    lines.push(...additionalProperties);
  }

  return lines.join('\n') + '\n';
}

export async function handleGenerate(args: GenerateInput): Promise<CallToolResult> {
  const inputSpec = resolve(args.inputSpec);
  const outputDir = resolve(args.outputDir);
  const resolvedArgs = { ...args, inputSpec, outputDir };

  // Check inputSpec exists
  try {
    await access(inputSpec, constants.R_OK);
  } catch {
    return {
      content: [{ type: 'text', text: `Error: Input spec not found: ${inputSpec}` }],
      isError: true,
    };
  }

  // Check outputDir writability (check parent if dir doesn't exist)
  let outputDirWarning = '';
  try {
    await access(outputDir, constants.W_OK);
    // Directory exists — check if non-empty
    const entries = await readdir(outputDir);
    if (entries.length > 0) {
      outputDirWarning = `Warning: Output directory "${outputDir}" is not empty. Existing files may be overwritten.\n\n`;
    }
  } catch {
    // Directory doesn't exist — check parent is writable
    const parentDir = resolve(outputDir, '..');
    try {
      await access(parentDir, constants.W_OK);
    } catch {
      return {
        content: [{ type: 'text', text: `Error: Output directory is not writable: ${outputDir}` }],
        isError: true,
      };
    }
  }

  let tmpDir: string | undefined;
  try {
    tmpDir = await createTempDir();
    const configPath = join(tmpDir, 'config.yaml');
    const configYaml = buildConfigYaml(resolvedArgs);
    await writeFile(configPath, configYaml);

    const result = await runCli(['generate', '-c', configPath]);
    const output = [result.stdout, result.stderr].filter(Boolean).join('\n');

    if (result.exitCode !== 0) {
      return {
        content: [{ type: 'text', text: `Generation failed (exit code ${result.exitCode}):\n${output}` }],
        isError: true,
      };
    }

    const nextSteps = [
      `cd ${outputDir}`,
      'dart pub get',
      'dart run build_runner build --delete-conflicting-outputs',
      'dart analyze',
    ].join('\n  ');

    return {
      content: [{
        type: 'text',
        text: `${outputDirWarning}Generated Dart-ACDC client at: ${outputDir}\n\nNext steps:\n  ${nextSteps}\n\n--- CLI Output ---\n${output}`,
      }],
    };
  } catch (err) {
    if (err instanceof RunnerError && err.code === 'CLI_TIMEOUT') {
      // Clean up partial output on timeout
      try {
        await rm(outputDir, { recursive: true, force: true });
      } catch {
        // Best-effort cleanup
      }
    }

    if (err instanceof RunnerError) {
      return {
        content: [{ type: 'text', text: `Error: ${err.message}` }],
        isError: true,
      };
    }
    throw err;
  } finally {
    if (tmpDir) {
      await rm(tmpDir, { recursive: true, force: true }).catch(() => {});
    }
  }
}
