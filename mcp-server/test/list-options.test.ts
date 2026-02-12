import { describe, it, expect, vi, beforeEach } from 'vitest';

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

import { runCli, RunnerError } from '../src/utils/runner.js';
import { handleListOptions } from '../src/tools/list-options.js';

const mockRunCli = vi.mocked(runCli);

beforeEach(() => {
  vi.restoreAllMocks();
});

describe('handleListOptions', () => {
  it('returns CLI output on success', async () => {
    mockRunCli.mockResolvedValue({
      stdout: 'CONFIG OPTIONS\n  pubName\n    type: string\n  enableAuthentication\n    type: boolean',
      stderr: '',
      exitCode: 0,
    });

    const result = await handleListOptions();

    expect(result.isError).toBeUndefined();
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('pubName');
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('enableAuthentication');
  });

  it('calls runCli with config-help args', async () => {
    mockRunCli.mockResolvedValue({ stdout: 'output', stderr: '', exitCode: 0 });

    await handleListOptions();

    expect(mockRunCli).toHaveBeenCalledWith(['config-help', '-g', 'dart-acdc']);
  });

  it('returns error on non-zero exit code', async () => {
    mockRunCli.mockResolvedValue({
      stdout: '',
      stderr: 'Unknown generator: dart-acdc',
      exitCode: 1,
    });

    const result = await handleListOptions();

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Failed to list options');
  });

  it('returns error with CLI JAR not found message', async () => {
    const { RunnerError: RE } = await import('../src/utils/runner.js');
    mockRunCli.mockRejectedValue(
      new RE(
        'OpenAPI Generator CLI JAR not found. Download from: https://repo1.maven.org/...',
        'CLI_JAR_NOT_FOUND',
      ),
    );

    const result = await handleListOptions();

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('CLI JAR not found');
  });

  it('returns error on auto-build failure', async () => {
    const { RunnerError: RE } = await import('../src/utils/runner.js');
    mockRunCli.mockRejectedValue(
      new RE('Auto-build failed (exit code 1):\ncompilation error', 'AUTO_BUILD_FAILED'),
    );

    const result = await handleListOptions();

    expect(result.isError).toBe(true);
    expect((result.content[0] as { type: 'text'; text: string }).text).toContain('Auto-build failed');
  });
});
