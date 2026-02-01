package org.openapitools.codegen.languages;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility class for handling Dart enum generation.
 * 
 * This class handles:
 * - Enum variable name generation from values
 * - Enum collision resolution with numeric suffixes
 * - Reserved word handling for enum values
 * - Numeric value prefixing
 */
public class DartEnumHandler {

    // Constants for enum defaults
    private static final String DEFAULT_ENUM_VALUE = "empty";
    private static final String NUMERIC_ENUM_PREFIX = "value";
    private static final String RESERVED_WORD_VAR_SUFFIX = "_";

    // Pre-compiled regex patterns
    private static final Pattern PATTERN_NUMERIC_VALUE = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern PATTERN_NON_DIGITS = Pattern.compile("[^0-9]");
    private static final Pattern PATTERN_SEPARATORS = Pattern.compile("[-_./\\s]+");
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PATTERN_NON_ALPHANUMERIC_CAMEL = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern PATTERN_STARTS_WITH_DIGIT = Pattern.compile("^[0-9].*");

    /**
     * Set of reserved words that need escaping.
     */
    private final Set<String> reservedWords;

    /**
     * Constructor.
     *
     * @param reservedWords set of Dart reserved words
     */
    public DartEnumHandler(Set<String> reservedWords) {
        this.reservedWords = reservedWords;
    }

    /**
     * Converts an enum value to a valid Dart identifier using camelCase.
     *
     * Rules:
     * - Convert to camelCase
     * - Remove/replace invalid characters
     * - Prefix numeric values with 'value'
     * - Use 'empty' for empty/invalid values
     * - Escape reserved words with underscore suffix
     *
     * @param value    the enum value (e.g., "ACTIVE", "in-progress", "123")
     * @param datatype the data type (e.g., "string", "integer")
     * @return a valid Dart enum variable name (e.g., "active", "inProgress", "value123")
     */
    public String toEnumVarName(String value, String datatype) {
        if (value == null || value.isEmpty()) {
            return DEFAULT_ENUM_VALUE;
        }

        // Check if value is numeric
        if (PATTERN_NUMERIC_VALUE.matcher(value).matches()) {
            // Numeric value - prefix with 'value' and remove any decimals/negatives
            String sanitized = PATTERN_NON_DIGITS.matcher(value).replaceAll("");
            if (sanitized.isEmpty()) {
                sanitized = "0";
            }
            return NUMERIC_ENUM_PREFIX + sanitized;
        }

        // Convert to camelCase
        String identifier = toCamelCase(value);

        // If empty after sanitization, use 'empty'
        if (identifier.isEmpty()) {
            return DEFAULT_ENUM_VALUE;
        }

        // If starts with digit, prefix with 'value'
        if (PATTERN_STARTS_WITH_DIGIT.matcher(identifier).matches()) {
            identifier = NUMERIC_ENUM_PREFIX + capitalize(identifier);
        }

        // Handle reserved words - suffix with underscore for enum values
        if (isReservedWord(identifier)) {
            identifier = identifier + RESERVED_WORD_VAR_SUFFIX;
        }

        return identifier;
    }

    /**
     * Creates enum variable definitions with collision resolution.
     * If multiple values map to the same identifier, they get numeric suffixes.
     *
     * @param values   the list of enum values
     * @param datatype the data type (e.g., "string", "integer")
     * @return list of enum variable maps with name, value, and isString properties
     */
    public List<Map<String, Object>> createEnumVars(List<Object> values, String datatype) {
        List<Map<String, Object>> enumVars = new ArrayList<>();
        Map<String, Integer> nameCount = new HashMap<>();

        for (Object value : values) {
            String valueStr = String.valueOf(value);
            String baseName = toEnumVarName(valueStr, datatype);

            // Handle collision resolution
            String finalName;
            if (nameCount.containsKey(baseName)) {
                int count = nameCount.get(baseName) + 1;
                nameCount.put(baseName, count);
                finalName = baseName + count;
            } else {
                nameCount.put(baseName, 1);
                finalName = baseName;
            }

            Map<String, Object> enumVar = Map.of(
                    "name", finalName,
                    "value", valueStr,
                    "isString", "string".equalsIgnoreCase(datatype));
            enumVars.add(enumVar);
        }

        return enumVars;
    }

    /**
     * Converts a string to camelCase, handling various input formats.
     *
     * @param input the string to convert
     * @return camelCase version of the string
     */
    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Replace various separators with spaces
        String processed = PATTERN_SEPARATORS.matcher(input).replaceAll(" ").trim();

        if (processed.isEmpty()) {
            return "";
        }

        // Split into words
        String[] words = PATTERN_WHITESPACE.split(processed);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }

            if (i == 0) {
                // First word: lowercase
                result.append(word.toLowerCase());
            } else {
                // Subsequent words: capitalize first letter
                result.append(capitalize(word.toLowerCase()));
            }
        }

        // Remove any remaining non-alphanumeric characters
        String sanitized = PATTERN_NON_ALPHANUMERIC_CAMEL.matcher(result.toString()).replaceAll("");

        return sanitized;
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Checks if a name is a reserved word (case-insensitive).
     *
     * @param name the name to check
     * @return true if reserved, false otherwise
     */
    private boolean isReservedWord(String name) {
        if (name == null || reservedWords == null) {
            return false;
        }
        return reservedWords.contains(name.toLowerCase());
    }
}
