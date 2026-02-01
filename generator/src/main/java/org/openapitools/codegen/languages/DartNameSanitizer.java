package org.openapitools.codegen.languages;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing names and identifiers according to Dart naming conventions.
 * 
 * This class handles:
 * - Package name sanitization following Dart pub conventions
 * - Reserved word escaping for variables and models
 * - Case conversion (camelCase to snake_case)
 */
public class DartNameSanitizer {

    // Constants for default values and suffixes
    private static final String DEFAULT_PACKAGE_NAME = "openapi_client";
    private static final String RESERVED_WORD_VAR_SUFFIX = "_";
    private static final String NUMERIC_PACKAGE_PREFIX = "api_";

    // Pre-compiled regex patterns for performance
    // Package name sanitization patterns
    private static final Pattern PATTERN_SPACES_HYPHENS = Pattern.compile("[ -]");
    private static final Pattern PATTERN_NON_ALPHANUMERIC_PACKAGE = Pattern.compile("[^a-z0-9_]");
    private static final Pattern PATTERN_CONSECUTIVE_UNDERSCORES = Pattern.compile("_+");
    private static final Pattern PATTERN_LEADING_TRAILING_UNDERSCORES = Pattern.compile("^_+|_+$");
    private static final Pattern PATTERN_STARTS_WITH_DIGIT = Pattern.compile("^[0-9].*");

    // Underscore conversion patterns
    private static final Pattern PATTERN_LOWERCASE_UPPERCASE = Pattern.compile("([a-z0-9])([A-Z])");
    private static final Pattern PATTERN_UPPERCASE_SEQUENCE = Pattern.compile("([A-Z])([A-Z][a-z])");

    /**
     * Sanitizes a package name to follow Dart pub package naming conventions.
     *
     * Rules:
     * - Convert to lowercase
     * - Replace spaces and hyphens with underscores
     * - Remove all characters except a-z, 0-9, and _
     * - Collapse consecutive underscores to single underscore
     * - Remove leading/trailing underscores
     * - Prefix with 'api_' if name starts with a digit
     * - Use 'openapi_client' if sanitization results in empty string
     *
     * @param name the package name to sanitize
     * @return the sanitized package name following Dart conventions
     */
    public String sanitizePubName(String name) {
        if (name == null || name.isEmpty()) {
            return DEFAULT_PACKAGE_NAME;
        }

        // Convert to lowercase
        String sanitized = name.toLowerCase();

        // Replace spaces and hyphens with underscores
        sanitized = PATTERN_SPACES_HYPHENS.matcher(sanitized).replaceAll("_");

        // Remove all characters except a-z, 0-9, and _
        sanitized = PATTERN_NON_ALPHANUMERIC_PACKAGE.matcher(sanitized).replaceAll("");

        // Collapse consecutive underscores to single underscore
        sanitized = PATTERN_CONSECUTIVE_UNDERSCORES.matcher(sanitized).replaceAll("_");

        // Remove leading/trailing underscores
        sanitized = PATTERN_LEADING_TRAILING_UNDERSCORES.matcher(sanitized).replaceAll("");

        // If empty after sanitization, use default
        if (sanitized.isEmpty()) {
            return DEFAULT_PACKAGE_NAME;
        }

        // Prefix with 'api_' if name starts with a digit
        if (PATTERN_STARTS_WITH_DIGIT.matcher(sanitized).matches()) {
            sanitized = NUMERIC_PACKAGE_PREFIX + sanitized;
        }

        return sanitized;
    }

    /**
     * Escapes reserved words by appending an underscore suffix.
     * Used for property/variable names.
     *
     * @param name the name to escape
     * @return the name with underscore suffix
     */
    public String escapeReservedWord(String name) {
        return name + RESERVED_WORD_VAR_SUFFIX;
    }

    /**
     * Converts a name from camelCase or PascalCase to snake_case.
     * This is commonly used for Dart file naming conventions.
     *
     * Examples:
     * - "UserProfile" -> "user_profile"
     * - "HTTPResponse" -> "http_response"
     * - "simpleValue" -> "simple_value"
     *
     * @param name the name to convert
     * @return the snake_case version of the name
     */
    public String toSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Insert underscore before uppercase letters (except at the start)
        // and convert to lowercase
        String result = PATTERN_LOWERCASE_UPPERCASE.matcher(name).replaceAll("$1_$2");
        result = PATTERN_UPPERCASE_SEQUENCE.matcher(result).replaceAll("$1_$2");
        result = result.toLowerCase();

        return result;
    }
}
