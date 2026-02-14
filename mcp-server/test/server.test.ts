import { describe, it, expect } from 'vitest';
import { spawn } from 'node:child_process';
import { join } from 'node:path';

const SERVER_PATH = join(import.meta.dirname, '..', 'dist', 'index.js');

/**
 * Send a JSON-RPC request to the server process over stdin and collect the response.
 */
function sendJsonRpc(
  proc: ReturnType<typeof spawn>,
  request: Record<string, unknown>,
): Promise<Record<string, unknown>> {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      proc.kill();
      reject(new Error('Server response timed out'));
    }, 10_000);

    let buffer = '';
    proc.stdout!.on('data', (chunk: Buffer) => {
      buffer += chunk.toString();
      // JSON-RPC messages are newline-delimited
      const lines = buffer.split('\n');
      for (const line of lines) {
        if (!line.trim()) continue;
        try {
          const parsed = JSON.parse(line);
          clearTimeout(timeout);
          resolve(parsed);
        } catch {
          // Incomplete JSON, continue buffering
        }
      }
    });

    proc.stderr!.on('data', (chunk: Buffer) => {
      // MCP servers may log to stderr; that's fine
    });

    const msg = JSON.stringify(request);
    proc.stdin!.write(msg + '\n');
  });
}

describe('MCP Server Integration', () => {
  it('responds to initialize and lists all three tools', async () => {
    const proc = spawn('node', [SERVER_PATH], {
      stdio: ['pipe', 'pipe', 'pipe'],
      env: { ...process.env },
    });

    try {
      // Step 1: Initialize
      const initResponse = await sendJsonRpc(proc, {
        jsonrpc: '2.0',
        id: 1,
        method: 'initialize',
        params: {
          protocolVersion: '2024-11-05',
          capabilities: {},
          clientInfo: { name: 'test-client', version: '1.0.0' },
        },
      });

      expect(initResponse).toHaveProperty('result');

      // Step 2: Send initialized notification
      proc.stdin!.write(
        JSON.stringify({
          jsonrpc: '2.0',
          method: 'notifications/initialized',
        }) + '\n',
      );

      // Step 3: List tools
      const toolsResponse = await sendJsonRpc(proc, {
        jsonrpc: '2.0',
        id: 2,
        method: 'tools/list',
        params: {},
      });

      expect(toolsResponse).toHaveProperty('result');
      const result = (toolsResponse as { result: { tools: Array<{ name: string; inputSchema: unknown }> } }).result;
      const toolNames = result.tools.map((t) => t.name);

      expect(toolNames).toContain('generate');
      expect(toolNames).toContain('list-options');
      expect(toolNames).toContain('validate');
      expect(result.tools).toHaveLength(3);

      // Verify generate tool has an input schema with required fields
      const generateTool = result.tools.find((t) => t.name === 'generate');
      expect(generateTool?.inputSchema).toBeDefined();
      const schema = generateTool!.inputSchema as { properties: Record<string, unknown>; required: string[] };
      expect(schema.properties).toHaveProperty('inputSpec');
      expect(schema.properties).toHaveProperty('outputDir');

      // Verify validate tool has inputSpec param
      const validateTool = result.tools.find((t) => t.name === 'validate');
      expect(validateTool?.inputSchema).toBeDefined();
      const vSchema = validateTool!.inputSchema as { properties: Record<string, unknown> };
      expect(vSchema.properties).toHaveProperty('inputSpec');
    } finally {
      proc.kill();
    }
  });
});
