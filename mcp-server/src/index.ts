#!/usr/bin/env node

import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { GenerateInputSchema, handleGenerate } from './tools/generate.js';
import { handleListOptions } from './tools/list-options.js';
import { ValidateInputSchema, handleValidate } from './tools/validate.js';

const server = new McpServer({
  name: 'dart-acdc',
  version: '0.1.0',
});

server.tool(
  'generate',
  'Generate a Dart-ACDC API client from an OpenAPI specification. Produces a complete Dart package with authentication, caching, logging, and offline support pre-configured.',
  GenerateInputSchema,
  (args) => handleGenerate(args),
);

server.tool(
  'list-options',
  'List all available configuration options for the dart-acdc generator, including types, descriptions, and default values.',
  async () => handleListOptions(),
);

server.tool(
  'validate',
  'Validate an OpenAPI specification file for compatibility with the dart-acdc generator. Returns validation errors or warnings.',
  ValidateInputSchema,
  (args) => handleValidate(args),
);

const transport = new StdioServerTransport();
await server.connect(transport);
