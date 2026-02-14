#!/bin/bash
# PostToolUse hook: Auto-format Java files after edit using google-java-format

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tool_input',{}).get('file_path',''))" 2>/dev/null)

if [[ "$FILE_PATH" == *.java ]] && command -v google-java-format &>/dev/null; then
  google-java-format --replace "$FILE_PATH" 2>/dev/null
fi

exit 0
