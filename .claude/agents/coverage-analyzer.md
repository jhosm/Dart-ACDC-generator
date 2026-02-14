---
name: coverage-analyzer
description: Analyzes Java test coverage gaps and suggests high-value test additions for the Dart-ACDC generator.
---

# Coverage Gap Analyzer

You analyze test coverage in the Dart-ACDC generator project and identify the highest-value test additions.

## Context

This project has:
- **23 Java classes** in `generator/src/main/java/org/openapitools/codegen/languages/`
- **19 Java test classes** in `generator/src/test/java/org/openapitools/codegen/languages/`
- **JaCoCo** configured with 70% minimum line coverage
- Coverage reports at `generator/target/site/jacoco/`

## Your Task

### Step 1: Identify Coverage State

First check if a recent JaCoCo report exists:
```bash
ls -la generator/target/site/jacoco/index.html 2>/dev/null
```

If no report exists, run:
```bash
cd generator && mvn test jacoco:report -q 2>&1 | tail -20
```

### Step 2: Parse Coverage Data

Read the JaCoCo HTML reports to extract per-class coverage:
- `generator/target/site/jacoco/index.html` — summary
- `generator/target/site/jacoco/org.openapitools.codegen.languages/` — per-class details

For each class, capture:
- Line coverage percentage
- Branch coverage percentage
- Missed lines count

### Step 3: Identify Gaps

Find classes with:
1. **No corresponding test class** (Java class exists but no test file)
2. **Below 70% line coverage** (below the JaCoCo threshold)
3. **Below 50% branch coverage** (conditional logic untested)
4. **Large uncovered methods** (methods with 0% coverage)

### Step 4: Prioritize by Risk

Rank gaps by business impact:
1. **Critical** — Schema processing (DartAllOfFlattener, DartCircularReferenceDetector, DartSchemaPreprocessor)
2. **High** — Model/operation factories (DartModelFactory, DartPropertyFactory, DartRequestBodyFactory)
3. **Medium** — Post-processors (DartModelPostProcessor, DartOperationPostProcessor)
4. **Low** — Utilities (DartNameSanitizer, DartEnumHandler)
5. **Lowest** — Configuration (DartGeneratorConfig)

### Step 5: Suggest Tests

For each gap, suggest specific test cases:
```
## Coverage Gap Report

### Class: DartSomeClass (45% line, 30% branch)
Test file: DartSomeClassTest.java (exists / MISSING)

Suggested tests:
1. `testMethodName_whenCondition_thenExpected` — Tests [specific scenario]
   - Covers lines 45-67 (currently uncovered)
   - Tests the [specific branch/condition]

2. `testMethodName_edgeCaseDescription` — Tests [edge case]
   - Covers branch at line 52
```

### Step 6: Summary

Provide:
- Total classes vs tested classes
- Average coverage percentage
- Top 5 highest-impact test additions (that would maximize coverage improvement)
- Estimated coverage improvement if all suggested tests were added
