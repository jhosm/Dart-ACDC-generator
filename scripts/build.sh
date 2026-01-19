#!/usr/bin/env bash
# Build the Dart-ACDC generator
# Usage: ./scripts/build.sh [--skip-tests]

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GENERATOR_DIR="$PROJECT_ROOT/generator"

echo "======================================"
echo "Building Dart-ACDC Generator"
echo "======================================"
echo ""

cd "$GENERATOR_DIR"

# Check if --skip-tests flag is provided
SKIP_TESTS=false
if [[ "$1" == "--skip-tests" ]]; then
    SKIP_TESTS=true
    echo "Skipping tests..."
fi

# Build with Maven
if [ "$SKIP_TESTS" = true ]; then
    mvn clean package -DskipTests
else
    mvn clean package
fi

# Verify JAR was created (find any version)
JAR_FILE=$(find "$GENERATOR_DIR/target" -name "dart-acdc-generator-*.jar" -type f | head -n 1)
if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
    echo ""
    echo "======================================"
    echo "Build successful!"
    echo "Generated JAR: $JAR_FILE"
    echo "======================================"
else
    echo ""
    echo "======================================"
    echo "Build failed: JAR file not found"
    echo "======================================"
    exit 1
fi

echo ""
echo "To generate samples, run: ./scripts/generate-samples.sh"
