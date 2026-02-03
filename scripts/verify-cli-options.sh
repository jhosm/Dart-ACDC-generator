#!/bin/bash
set -e

# verify-cli-options.sh
# Verifies that CLI options actually affect generated code by:
# 1. Generating samples with custom config values
# 2. Checking generated files contain expected values

echo "=========================================="
echo "CLI Options Integration Test"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED_CHECKS=0
PASSED_CHECKS=0

# Function to check if a file contains expected content
check_file_contains() {
    local file=$1
    local pattern=$2
    local description=$3

    if [ ! -f "$file" ]; then
        echo -e "${RED}✗ FAIL${NC}: File not found: $file"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi

    if grep -q "$pattern" "$file"; then
        echo -e "${GREEN}✓ PASS${NC}: $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}: $description"
        echo "  Expected pattern: $pattern"
        echo "  In file: $file"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# Step 1: Generate sample with default values (petstore)
echo "Step 1: Generating sample with DEFAULT config values..."
./scripts/generate-samples.sh petstore
echo ""

# Step 2: Verify default values in petstore sample
echo "Step 2: Verifying DEFAULT config values in generated code..."
check_file_contains \
    "samples/generated/petstore/lib/config/cache_config.dart" \
    "this.ttl = const Duration(hours: 1)" \
    "Default cache TTL is 1 hour"

check_file_contains \
    "samples/generated/petstore/lib/config/log_config.dart" \
    "this.level = LogLevel.info" \
    "Default log level is info"

check_file_contains \
    "samples/generated/petstore/lib/config/cache_config.dart" \
    "this.encryptCache = true" \
    "Default cache encryption is true"

check_file_contains \
    "samples/generated/petstore/lib/config/cache_config.dart" \
    "this.maxDiskCacheSizeMB = 20" \
    "Default disk cache size is 20MB"

check_file_contains \
    "samples/generated/petstore/lib/config/cache_config.dart" \
    "this.userIsolation = true" \
    "Default user cache isolation is true"

check_file_contains \
    "samples/generated/petstore/lib/config/log_config.dart" \
    "this.redactSensitiveData = true" \
    "Default sensitive data redaction is true"

check_file_contains \
    "samples/generated/petstore/lib/config/auth_config.dart" \
    "this.refreshThreshold = 5 \* 60" \
    "Default refresh threshold is 5 minutes"

check_file_contains \
    "samples/generated/petstore/lib/config/auth_config.dart" \
    "this.useSecureStorage = true" \
    "Default secure token storage is true"

echo ""

# Step 3: Generate sample with custom values
echo "Step 3: Generating sample with CUSTOM config values..."
./scripts/generate-samples.sh custom-config-test
echo ""

# Step 4: Verify custom values in custom-config-test sample
echo "Step 4: Verifying CUSTOM config values in generated code..."
check_file_contains \
    "samples/generated/custom-config-test/lib/config/cache_config.dart" \
    "this.ttl = const Duration(hours: 24)" \
    "Custom cache TTL is 24 hours (not default 1)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/log_config.dart" \
    "this.level = LogLevel.debug" \
    "Custom log level is debug (not default info)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/cache_config.dart" \
    "this.encryptCache = false" \
    "Custom cache encryption is false (not default true)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/cache_config.dart" \
    "this.maxDiskCacheSizeMB = 50" \
    "Custom disk cache size is 50MB (not default 20)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/cache_config.dart" \
    "this.userIsolation = false" \
    "Custom user cache isolation is false (not default true)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/log_config.dart" \
    "this.redactSensitiveData = false" \
    "Custom sensitive data redaction is false (not default true)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/auth_config.dart" \
    "this.refreshThreshold = 10 \* 60" \
    "Custom refresh threshold is 10 minutes (not default 5)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/auth_config.dart" \
    "this.useSecureStorage = false" \
    "Custom secure token storage is false (not default true)"

check_file_contains \
    "samples/generated/custom-config-test/lib/config/auth_config.dart" \
    "this.tokenRefreshUrl = 'https://api.example.com/auth/refresh'" \
    "Custom token refresh URL is set"

echo ""

# Step 5: Build and test generated code
echo "Step 5: Building and testing generated code..."
echo ""

echo "Building custom-config-test sample..."
cd samples/generated/custom-config-test
if dart pub get && dart run build_runner build --delete-conflicting-outputs; then
    echo -e "${GREEN}✓ PASS${NC}: Custom config sample builds successfully"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
else
    echo -e "${RED}✗ FAIL${NC}: Custom config sample failed to build"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
fi
cd ../../..

echo ""

# Step 6: Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED_CHECKS${NC}"
echo -e "${RED}Failed: $FAILED_CHECKS${NC}"
echo ""

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}✓ All CLI option integration tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some CLI option integration tests failed${NC}"
    exit 1
fi
