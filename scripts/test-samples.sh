#!/usr/bin/env bash
# Generate samples, run build_runner, and execute all tests with coverage
# Usage: ./scripts/test-samples.sh [options]
#   --skip-generate: Skip sample generation (use existing samples)
#   --skip-build: Skip build_runner (use existing .g.dart files)
#   --no-coverage: Disable code coverage (enabled by default)

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/samples/generated"
COVERAGE_DIR="$PROJECT_ROOT/coverage"

# Parse arguments
SKIP_GENERATE=false
SKIP_BUILD=false
COVERAGE=true
for arg in "$@"; do
    case $arg in
        --skip-generate) SKIP_GENERATE=true ;;
        --skip-build) SKIP_BUILD=true ;;
        --no-coverage) COVERAGE=false ;;
    esac
done

echo "======================================"
echo "Dart-ACDC Generator Test Suite"
echo "======================================"
echo ""

# Step 1: Generate samples
if [ "$SKIP_GENERATE" = false ]; then
    echo "Step 1: Generating samples..."
    "$SCRIPT_DIR/generate-samples.sh"
else
    echo "Step 1: Skipping generation (--skip-generate)"
    echo ""
fi

# Step 2: Run build_runner on each sample
ALL_SPECS=("petstore" "minimal" "composition" "file-upload" "enums" "reserved-words")

if [ "$SKIP_BUILD" = false ]; then
    echo "======================================"
    echo "Step 2: Running build_runner..."
    echo "======================================"
    echo ""
    
    for spec in "${ALL_SPECS[@]}"; do
        spec_dir="$OUTPUT_DIR/$spec"
        if [ -d "$spec_dir" ]; then
            echo "Building $spec..."
            (cd "$spec_dir" && flutter pub get > /dev/null 2>&1 && dart run build_runner build --delete-conflicting-outputs > /dev/null 2>&1)
            echo "✓ $spec built"
        fi
    done
    echo ""
else
    echo "Step 2: Skipping build_runner (--skip-build)"
    echo ""
fi

# Step 3: Run tests
echo "======================================"
echo "Step 3: Running tests..."
if [ "$COVERAGE" = true ]; then
    echo "(with coverage)"
    rm -rf "$COVERAGE_DIR"
    mkdir -p "$COVERAGE_DIR"
fi
echo "======================================"
echo ""

TOTAL_TESTS=0
FAILED_SPECS=()

for spec in "${ALL_SPECS[@]}"; do
    spec_dir="$OUTPUT_DIR/$spec"
    if [ -d "$spec_dir" ]; then
        if [ "$COVERAGE" = true ]; then
            result=$(cd "$spec_dir" && flutter test --coverage 2>&1) || true
            # Copy coverage file with spec name
            if [ -f "$spec_dir/coverage/lcov.info" ]; then
                cp "$spec_dir/coverage/lcov.info" "$COVERAGE_DIR/lcov-$spec.info"
            fi
        else
            result=$(cd "$spec_dir" && flutter test 2>&1) || true
        fi
        
        if echo "$result" | grep -q "All tests passed"; then
            count=$(echo "$result" | grep -oE '\+[0-9]+' | tail -1 | tr -d '+')
            echo "✓ $spec: $count tests passed"
            TOTAL_TESTS=$((TOTAL_TESTS + count))
        else
            echo "✗ $spec: FAILED"
            FAILED_SPECS+=("$spec")
        fi
    fi
done

# Merge coverage files if coverage was enabled
if [ "$COVERAGE" = true ]; then
    echo ""
    echo "Merging coverage reports..."
    
    # Combine all lcov files with fixed paths
    COMBINED="$COVERAGE_DIR/lcov.info"
    > "$COMBINED"
    for spec in "${ALL_SPECS[@]}"; do
        lcov_file="$COVERAGE_DIR/lcov-$spec.info"
        if [ -f "$lcov_file" ]; then
            # Rewrite paths to be relative to samples/generated/<spec>/
            sed "s|SF:lib/|SF:samples/generated/$spec/lib/|g" "$lcov_file" >> "$COMBINED"
        fi
    done
    
    # Generate HTML report if genhtml is available
    if command -v genhtml &> /dev/null; then
        echo "Generating HTML coverage report..."
        genhtml "$COMBINED" -o "$COVERAGE_DIR/html" --quiet
        echo "✓ HTML report: $COVERAGE_DIR/html/index.html"
    else
        echo "✓ LCOV report: $COMBINED"
        echo "  (Install lcov for HTML report: brew install lcov)"
    fi
fi

echo ""
echo "======================================"
echo "Test Summary"
echo "======================================"
echo "Total tests passed: $TOTAL_TESTS"

if [ "$COVERAGE" = true ] && [ -f "$COMBINED" ]; then
    # Extract coverage percentage
    LINES_FOUND=$(grep -h "^LF:" "$COMBINED" | cut -d: -f2 | awk '{sum+=$1} END {print sum}')
    LINES_HIT=$(grep -h "^LH:" "$COMBINED" | cut -d: -f2 | awk '{sum+=$1} END {print sum}')
    if [ "$LINES_FOUND" -gt 0 ]; then
        COVERAGE_PCT=$(echo "scale=1; $LINES_HIT * 100 / $LINES_FOUND" | bc)
        echo "Code coverage: $COVERAGE_PCT% ($LINES_HIT/$LINES_FOUND lines)"
    fi
fi

if [ ${#FAILED_SPECS[@]} -eq 0 ]; then
    echo "All samples passed! ✓"
    exit 0
else
    echo "Failed samples: ${FAILED_SPECS[*]}"
    exit 1
fi
