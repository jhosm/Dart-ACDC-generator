#!/usr/bin/env bash
# Generate sample clients using all test OpenAPI specs
# Usage: ./scripts/generate-samples.sh [spec-name]
#   spec-name: Optional. If provided, only generate that spec (petstore, minimal, composition, file-upload, enums)

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CLI_JAR="$PROJECT_ROOT/openapi-generator-cli.jar"
GENERATOR_JAR=$(find "$PROJECT_ROOT/generator/target" -name "dart-acdc-generator-*.jar" -type f | head -n 1)
OUTPUT_DIR="$PROJECT_ROOT/samples/generated"

echo "======================================"
echo "Generating Sample Dart-ACDC Clients"
echo "======================================"
echo ""

# Check if generator JAR exists
if [ ! -f "$GENERATOR_JAR" ]; then
    echo "Error: Generator JAR not found at $GENERATOR_JAR"
    echo "Run ./scripts/build.sh first"
    exit 1
fi

# Check if OpenAPI Generator CLI JAR exists
if [ ! -f "$CLI_JAR" ]; then
    echo "Error: OpenAPI Generator CLI JAR not found at $CLI_JAR"
    echo "Download it from: https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.10.0/openapi-generator-cli-7.10.0.jar"
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Function to generate a single spec
generate_spec() {
    local spec_name=$1
    local config_file="$PROJECT_ROOT/bin/configs/dart-acdc-${spec_name}.yaml"

    if [ ! -f "$config_file" ]; then
        echo "Error: Config file not found: $config_file"
        return 1
    fi

    echo "Generating $spec_name..."
    java -cp "$CLI_JAR:$GENERATOR_JAR" \
        org.openapitools.codegen.OpenAPIGenerator \
        generate \
        -g dart-acdc \
        -c "$config_file"
    echo "✓ $spec_name generated"
    echo ""
}

# List of all specs
ALL_SPECS=("petstore" "minimal" "composition" "file-upload" "enums")

# If a specific spec is provided, generate only that one
if [ -n "$1" ]; then
    SPEC_NAME="$1"
    echo "Generating only: $SPEC_NAME"
    echo ""
    generate_spec "$SPEC_NAME"
else
    # Generate all specs
    echo "Generating all sample specs..."
    echo ""
    for spec in "${ALL_SPECS[@]}"; do
        generate_spec "$spec"
    done
fi

echo "======================================"
echo "Generation complete!"
echo "Output directory: $OUTPUT_DIR"
echo "======================================"
echo ""
echo "Next steps:"
echo "  1. cd samples/generated/<spec-name>"
echo "  2. dart pub get"
echo "  3. dart analyze"
