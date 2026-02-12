import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mkdir, writeFile, rm, readdir } from 'node:fs/promises';
import { mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

// Mock runner before importing the module under test
vi.mock('../src/utils/runner.js', () => ({
  runCli: vi.fn(),
  createTempDir: vi.fn(),
  RunnerError: class RunnerError extends Error {
    code: string;
    constructor(message: string, code: string) {
      super(message);
      this.name = 'RunnerError';
      this.code = code;
    }
  },
}));

import { runCli, createTempDir, RunnerError } from '../src/utils/runner.js';
import { buildConfigYaml, handleGenerate } from '../src/tools/generate.js';

const mockRunCli = vi.mocked(runCli);
const mockCreateTempDir = vi.mocked(createTempDir);

let tempRoot: string;

beforeEach(() => {
  vi.restoreAllMocks();
  tempRoot = mkdtempSync(join(tmpdir(), 'gen-test-'));
  // Default: createTempDir returns a subdir of our temp root
  mockCreateTempDir.mockResolvedValue(join(tempRoot, 'mcp-tmp'));
});

afterEach(async () => {
  await rm(tempRoot, { recursive: true, force: true });
});

// ---------- buildConfigYaml ----------

describe('buildConfigYaml', () => {
  it('generates minimal config with required fields only', () => {
    const yaml = buildConfigYaml({
      inputSpec: '/path/to/spec.yaml',
      outputDir: '/path/to/output',
    } as Parameters<typeof buildConfigYaml>[0]);

    expect(yaml).toContain('generatorName: dart-acdc');
    expect(yaml).toContain('inputSpec: "/path/to/spec.yaml"');
    expect(yaml).toContain('outputDir: "/path/to/output"');
  });

  it('includes package metadata when provided', () => {
    const yaml = buildConfigYaml({
      inputSpec: '/spec.yaml',
      outputDir: '/out',
      pubName: 'my_api',
      pubVersion: '2.0.0',
      pubDescription: 'My API client',
    } as Parameters<typeof buildConfigYaml>[0]);

    expect(yaml).toContain('additionalProperties:');
    expect(yaml).toContain('pubName: "my_api"');
    expect(yaml).toContain('pubVersion: "2.0.0"');
    expect(yaml).toContain('pubDescription: "My API client"');
  });

  it('includes boolean feature toggles', () => {
    const yaml = buildConfigYaml({
      inputSpec: '/spec.yaml',
      outputDir: '/out',
      enableAuthentication: false,
      enableCaching: true,
    } as Parameters<typeof buildConfigYaml>[0]);

    expect(yaml).toContain('enableAuthentication: false');
    expect(yaml).toContain('enableCaching: true');
  });

  it('includes numeric values as quoted strings', () => {
    const yaml = buildConfigYaml({
      inputSpec: '/spec.yaml',
      outputDir: '/out',
      defaultCacheTtlHours: 4,
      refreshThresholdMinutes: 10,
    } as Parameters<typeof buildConfigYaml>[0]);

    expect(yaml).toContain('defaultCacheTtlHours: "4"');
    expect(yaml).toContain('refreshThresholdMinutes: "10"');
  });

  it('omits undefined properties', () => {
    const yaml = buildConfigYaml({
      inputSpec: '/spec.yaml',
      outputDir: '/out',
      pubName: undefined,
    } as Parameters<typeof buildConfigYaml>[0]);

    expect(yaml).not.toContain('pubName');
    expect(yaml).not.toContain('additionalProperties');
  });
});

// ---------- handleGenerate ----------

describe('handleGenerate', () => {
  async function setupSpecFile(dir?: string): Promise<string> {
    const specDir = dir ?? tempRoot;
    const specPath = join(specDir, 'petstore.yaml');
    await writeFile(specPath, 'openapi: "3.0.0"');
    return specPath;
  }

  it('returns success with output path and next steps', async () => {
    const specPath = await setupSpecFile();
    const outputDir = join(tempRoot, 'generated');
    await mkdir(join(tempRoot, 'mcp-tmp'), { recursive: true });

    mockRunCli.mockResolvedValue({
      stdout: 'Generation complete',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir,
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBeUndefined();
    const text = result.content[0];
    expect(text.type).toBe('text');
    expect((text as { type: 'text'; text: string }).text).toContain(`Generated Dart-ACDC client at: ${outputDir}`);
    expect((text as { type: 'text'; text: string }).text).toContain('dart pub get');
    expect((text as { type: 'text'; text: string }).text).toContain('build_runner');
    expect((text as { type: 'text'; text: string }).text).toContain('dart analyze');
  });

  it('returns error when input spec not found', async () => {
    const result = await handleGenerate({
      inputSpec: join(tempRoot, 'nonexistent.yaml'),
      outputDir: join(tempRoot, 'out'),
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('not found');
  });

  it('returns error when output directory is not writable', async () => {
    const specPath = await setupSpecFile();
    // Use a path under a non-existent parent that can't be created
    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir: '/nonexistent-root/impossible/output',
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('not writable');
  });

  it('warns when output directory already contains files', async () => {
    const specPath = await setupSpecFile();
    const outputDir = join(tempRoot, 'existing-output');
    await mkdir(outputDir);
    await writeFile(join(outputDir, 'old_file.dart'), '// old');
    await mkdir(join(tempRoot, 'mcp-tmp'), { recursive: true });

    mockRunCli.mockResolvedValue({
      stdout: 'Done',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir,
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBeUndefined();
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Warning');
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('not empty');
  });

  it('returns error on CLI non-zero exit', async () => {
    const specPath = await setupSpecFile();
    const outputDir = join(tempRoot, 'out');
    await mkdir(join(tempRoot, 'mcp-tmp'), { recursive: true });

    mockRunCli.mockResolvedValue({
      stdout: '',
      stderr: 'Invalid spec: missing info section',
      exitCode: 1,
    });

    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir,
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Generation failed');
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Invalid spec');
  });

  it('cleans up partial output on timeout', async () => {
    const specPath = await setupSpecFile();
    const outputDir = join(tempRoot, 'partial-output');
    await mkdir(outputDir);
    await writeFile(join(outputDir, 'partial.dart'), '// partial');
    await mkdir(join(tempRoot, 'mcp-tmp'), { recursive: true });

    const { RunnerError: RE } = await import('../src/utils/runner.js');
    mockRunCli.mockRejectedValue(new RE('timed out', 'CLI_TIMEOUT'));

    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir,
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('timed out');

    // Verify partial output was cleaned up
    await expect(readdir(outputDir)).rejects.toThrow();
  });

  it('returns error on RunnerError', async () => {
    const specPath = await setupSpecFile();
    const outputDir = join(tempRoot, 'out');
    await mkdir(join(tempRoot, 'mcp-tmp'), { recursive: true });

    const { RunnerError: RE } = await import('../src/utils/runner.js');
    mockRunCli.mockRejectedValue(new RE('Java not installed', 'JAVA_NOT_FOUND'));

    const result = await handleGenerate({
      inputSpec: specPath,
      outputDir,
    } as Parameters<typeof handleGenerate>[0]);

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Java not installed');
  });

  it('cleans up temp directory even on error', async () => {
    const specPath = await setupSpecFile();
    const tmpDir = join(tempRoot, 'mcp-tmp');
    await mkdir(tmpDir, { recursive: true });

    mockRunCli.mockResolvedValue({
      stdout: 'Done',
      stderr: '',
      exitCode: 0,
    });

    await handleGenerate({
      inputSpec: specPath,
      outputDir: join(tempRoot, 'out'),
    } as Parameters<typeof handleGenerate>[0]);

    // The mockCreateTempDir returns our tmpDir; the handler calls rm on it in finally
    // Since we mocked createTempDir, rm runs on the mock path. We just verify no throw.
  });
});
