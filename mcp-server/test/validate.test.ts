import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { writeFile, rm } from 'node:fs/promises';
import { mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

vi.mock('../src/utils/runner.js', () => ({
  runCli: vi.fn(),
  RunnerError: class RunnerError extends Error {
    code: string;
    constructor(message: string, code: string) {
      super(message);
      this.name = 'RunnerError';
      this.code = code;
    }
  },
}));

import { runCli } from '../src/utils/runner.js';
import { handleValidate } from '../src/tools/validate.js';

const mockRunCli = vi.mocked(runCli);

let tempRoot: string;

beforeEach(() => {
  vi.clearAllMocks();
  tempRoot = mkdtempSync(join(tmpdir(), 'validate-test-'));
});

afterEach(async () => {
  await rm(tempRoot, { recursive: true, force: true });
});

describe('handleValidate', () => {
  it('returns success for a valid spec', async () => {
    const specPath = join(tempRoot, 'valid.yaml');
    await writeFile(specPath, 'openapi: "3.0.0"');

    mockRunCli.mockResolvedValue({
      stdout: 'No validation issues found.',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleValidate({ inputSpec: specPath });

    expect(result.isError).toBeUndefined();
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('No validation issues');
  });

  it('returns error for an invalid spec', async () => {
    const specPath = join(tempRoot, 'invalid.yaml');
    await writeFile(specPath, 'not: valid: openapi');

    mockRunCli.mockResolvedValue({
      stdout: '',
      stderr: 'Errors:\n  - missing required field: info',
      exitCode: 1,
    });

    const result = await handleValidate({ inputSpec: specPath });

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('missing required field');
  });

  it('returns success with warnings', async () => {
    const specPath = join(tempRoot, 'warn.yaml');
    await writeFile(specPath, 'openapi: "3.0.0"');

    mockRunCli.mockResolvedValue({
      stdout: 'Warnings:\n  - unused schema: Pet',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleValidate({ inputSpec: specPath });

    expect(result.isError).toBeUndefined();
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('valid with warnings');
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('unused schema');
  });

  it('returns error for non-existent file', async () => {
    const result = await handleValidate({
      inputSpec: join(tempRoot, 'doesnt-exist.yaml'),
    });

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('not found');
    // Should not have called runCli
    expect(mockRunCli).not.toHaveBeenCalled();
  });

  it('returns error for unsupported file format', async () => {
    const binPath = join(tempRoot, 'image.png');
    await writeFile(binPath, Buffer.from([0x89, 0x50, 0x4e, 0x47]));

    const result = await handleValidate({ inputSpec: binPath });

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Unsupported file format');
    expect(mockRunCli).not.toHaveBeenCalled();
  });

  it('accepts .json files', async () => {
    const specPath = join(tempRoot, 'spec.json');
    await writeFile(specPath, '{"openapi": "3.0.0"}');

    mockRunCli.mockResolvedValue({
      stdout: 'No validation issues found.',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleValidate({ inputSpec: specPath });
    expect(result.isError).toBeUndefined();
  });

  it('accepts .yml files', async () => {
    const specPath = join(tempRoot, 'spec.yml');
    await writeFile(specPath, 'openapi: "3.0.0"');

    mockRunCli.mockResolvedValue({
      stdout: 'No validation issues found.',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleValidate({ inputSpec: specPath });
    expect(result.isError).toBeUndefined();
  });

  it('returns error on RunnerError', async () => {
    const specPath = join(tempRoot, 'spec.yaml');
    await writeFile(specPath, 'openapi: "3.0.0"');

    const { RunnerError: RE } = await import('../src/utils/runner.js');
    mockRunCli.mockRejectedValue(new RE('Java not installed', 'JAVA_NOT_FOUND'));

    const result = await handleValidate({ inputSpec: specPath });

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Java not installed');
  });

  it('calls runCli with validate args', async () => {
    const specPath = join(tempRoot, 'check.yaml');
    await writeFile(specPath, 'openapi: "3.0.0"');

    mockRunCli.mockResolvedValue({ stdout: 'ok', stderr: '', exitCode: 0 });

    await handleValidate({ inputSpec: specPath });

    // Path gets resolved to absolute
    expect(mockRunCli).toHaveBeenCalledWith(['validate', '-i', expect.stringContaining('check.yaml')]);
  });
});
