#!/usr/bin/env bash
# Generate samples, run build_runner, and execute all tests
# Usage: ./scripts/test-samples.sh [--skip-generate] [--skip-build]
#   --skip-generate: Skip sample generation (use existing samples)
#   --skip-build: Skip build_runner (use existing .g.dart files)

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/samples/generated"

# Parse arguments
SKIP_GENERATE=false
SKIP_BUILD=false
for arg in "$@"; do
    case $arg in
        --skip-generate) SKIP_GENERATE=true ;;
        --skip-build) SKIP_BUILD=true ;;
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
echo "======================================"
echo ""

TOTAL_TESTS=0
FAILED_SPECS=()

for spec in "${ALL_SPECS[@]}"; do
    spec_dir="$OUTPUT_DIR/$spec"
    if [ -d "$spec_dir" ]; then
        result=$(cd "$spec_dir" && flutter test 2>&1) || true
        
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

echo ""
echo "======================================"
echo "Test Summary"
echo "======================================"
echo "Total tests passed: $TOTAL_TESTS"

if [ ${#FAILED_SPECS[@]} -eq 0 ]; then
    echo "All samples passed! ✓"
    exit 0
else
    echo "Failed samples: ${FAILED_SPECS[*]}"
    exit 1
fi
