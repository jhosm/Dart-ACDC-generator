#!/bin/bash
# PreToolUse hook: Block edits to generated code in samples/generated/
# Generated code should only be modified by running ./scripts/generate-samples.sh

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('file_path',''))" 2>/dev/null)

if [[ "$FILE_PATH" == */samples/generated/* ]]; then
  echo "BLOCKED: This file is auto-generated. Edit the Mustache template in generator/src/main/resources/dart-acdc/ instead, then run ./scripts/generate-samples.sh" >&2
  exit 2
fi

exit 0
