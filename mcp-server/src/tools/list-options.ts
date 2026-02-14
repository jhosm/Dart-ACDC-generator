import { runCli, RunnerError } from '../utils/runner.js';
import type { CallToolResult } from '@modelcontextprotocol/sdk/types.js';

export async function handleListOptions(): Promise<CallToolResult> {
  try {
    const result = await runCli(['config-help', '-g', 'dart-acdc']);
    const output = [result.stdout, result.stderr].filter(Boolean).join('\n');

    if (result.exitCode !== 0) {
      return {
        content: [{ type: 'text', text: `Failed to list options (exit code ${result.exitCode}):\n${output}` }],
        isError: true,
      };
    }

    return {
      content: [{ type: 'text', text: output }],
    };
  } catch (err) {
    if (err instanceof RunnerError) {
      return {
        content: [{ type: 'text', text: `Error: ${err.message}` }],
        isError: true,
      };
    }
    throw err;
  }
}
