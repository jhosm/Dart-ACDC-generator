import { z } from 'zod';
import { access } from 'node:fs/promises';
import { resolve } from 'node:path';
import { constants } from 'node:fs';
import { runCli, RunnerError } from '../utils/runner.js';
import type { CallToolResult } from '@modelcontextprotocol/sdk/types.js';

export const ValidateInputSchema = {
  inputSpec: z.string().describe('Path to the OpenAPI specification file to validate'),
};

type ValidateInput = {
  inputSpec: string;
};

export async function handleValidate(args: ValidateInput): Promise<CallToolResult> {
  const inputSpec = resolve(args.inputSpec);

  // Check file exists
  try {
    await access(inputSpec, constants.R_OK);
  } catch {
    return {
      content: [{ type: 'text', text: `Error: Spec file not found: ${inputSpec}` }],
      isError: true,
    };
  }

  // Check file extension
  const lower = inputSpec.toLowerCase();
  if (!lower.endsWith('.yaml') && !lower.endsWith('.yml') && !lower.endsWith('.json')) {
    return {
      content: [{ type: 'text', text: `Error: Unsupported file format. Expected a YAML (.yaml, .yml) or JSON (.json) file: ${inputSpec}` }],
      isError: true,
    };
  }

  try {
    const result = await runCli(['validate', '-i', inputSpec]);
    const output = [result.stdout, result.stderr].filter(Boolean).join('\n').trim();

    if (result.exitCode !== 0) {
      return {
        content: [{ type: 'text', text: output }],
        isError: true,
      };
    }

    // Check for warnings in output
    const hasWarnings = output.toLowerCase().includes('warn');

    if (hasWarnings) {
      return {
        content: [{ type: 'text', text: `Spec is valid with warnings:\n${output}` }],
      };
    }

    return {
      content: [{ type: 'text', text: output || `Spec is valid: ${inputSpec}` }],
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
