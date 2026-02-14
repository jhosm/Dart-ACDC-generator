---
name: verify-pipeline
description: Run the full build, generate, test pipeline with smart change detection and failure diagnosis.
allowed-tools:
  - Bash
  - Read
  - Grep
  - Glob
---

# Full Pipeline Verification

Run the complete verification pipeline for the Dart-ACDC generator. Detects what changed and only runs the necessary stages.

## Pipeline Stages

### Stage 0: Detect Changes

Determine what changed since the last successful build:
```bash
# Get changed files (staged + unstaged)
changed=$(git diff --name-only HEAD 2>/dev/null; git diff --name-only --cached 2>/dev/null)
```

Classify changes:
- **Java changes**: Files in `generator/src/main/java/` — requires full pipeline
- **Template changes**: Files in `generator/src/main/resources/dart-acdc/` — skip Java build, run generate + test
- **Test template changes**: Files matching `*test*.mustache` — skip generate, re-run build_runner + tests
- **Script changes**: Files in `scripts/` — run full pipeline
- **Config changes**: Files in `configs/` — run generate + test
- **No code changes**: Skip pipeline, report current state

### Stage 1: Build (Java)

```bash
./scripts/build.sh
```

**On failure**: Report the Maven error, identify the failing class, show the relevant error snippet. Do NOT proceed to next stage.

### Stage 2: Generate Samples

```bash
./scripts/generate-samples.sh
```

**On failure**: Report which spec failed, show the generator error. Common issues:
- Template syntax errors (missing closing tags, undefined variables)
- Java NPE in model/operation processing
- Schema reference resolution failures

### Stage 3: Test Samples

```bash
./scripts/test-samples.sh --skip-generate
```

**On failure**: Report which sample spec's tests failed, show failing test names and assertions. Distinguish between:
- Dart compilation errors (generated code issue)
- build_runner failures (json_serializable issue)
- Test assertion failures (logic issue)
- Missing dependency errors (pubspec issue)

### Stage 4: Summary

Report:
```
Pipeline Results:
  Build:    PASS/FAIL/SKIPPED
  Generate: PASS/FAIL/SKIPPED (N specs)
  Test:     PASS/FAIL/SKIPPED (N tests, M% coverage)

Changes detected: [list of change categories]
Total time: Xs
```

If all stages pass, also run:
```bash
./scripts/verify-cli-options.sh
```

## Smart Skip Rules

| What Changed | Build | Generate | Test |
|---|---|---|---|
| Java source | RUN | RUN | RUN |
| Mustache templates | SKIP | RUN | RUN |
| Test templates only | SKIP | SKIP | RUN (with build_runner) |
| configs/ | SKIP | RUN | RUN |
| scripts/ | RUN | RUN | RUN |
| Dart test files | SKIP | SKIP | RUN |
| Nothing | SKIP | SKIP | SKIP (report state) |

## Error Diagnosis

When a stage fails, provide:
1. The exact error message
2. The likely root cause
3. Which file(s) to fix
4. A suggested fix if obvious
