package org.openapitools.codegen.languages;

import org.openapitools.codegen.*;
import org.openapitools.codegen.model.ModelsMap;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Dart-ACDC OpenAPI Generator
 *
 * Generates Dart API clients with full Dart-ACDC integration (Authentication, Caching, Debugging, Client).
 *
 * @see <a href="https://github.com/jhosm/Dart-ACDC">Dart-ACDC Library</a>
 */
public class DartAcdcGenerator extends DefaultCodegen implements CodegenConfig {

    /**
     * Dart reserved keywords that require escaping.
     * These cannot be used as identifiers in Dart code.
     */
    protected static final Set<String> DART_RESERVED_WORDS = new HashSet<>(Arrays.asList(
        // Keywords
        "abstract", "as", "assert", "async", "await",
        "break", "case", "catch", "class", "const",
        "continue", "covariant", "default", "deferred", "do",
        "dynamic", "else", "enum", "export", "extends",
        "extension", "external", "factory", "false", "final",
        "finally", "for", "Function", "get", "hide",
        "if", "implements", "import", "in", "interface",
        "is", "late", "library", "mixin", "new",
        "null", "on", "operator", "part", "required",
        "rethrow", "return", "set", "show", "static",
        "super", "switch", "sync", "this", "throw",
        "true", "try", "typedef", "var", "void",
        "while", "with", "yield"
    ));

    /**
     * Constructor - configures the generator with Dart-ACDC specific settings.
     */
    public DartAcdcGenerator() {
        super();

        // Set reserved words for the generator
        reservedWords.addAll(DART_RESERVED_WORDS);

        // Basic configuration
        outputFolder = "generated-code/dart-acdc";
        modelTemplateFiles.put("model.mustache", ".dart");
        apiTemplateFiles.put("api.mustache", ".dart");
        embeddedTemplateDir = templateDir = "dart-acdc";

        // Enable enum generation as separate models
        setLegacyDiscriminatorBehavior(false);

        // Package configuration following Flutter/Dart conventions
        apiPackage = "lib.remote_data_sources";
        modelPackage = "lib.models";

        // Supporting files
        supportingFiles.add(new SupportingFile("pubspec.mustache", "", "pubspec.yaml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("analysis_options.mustache", "", "analysis_options.yaml"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("api_client.mustache", "lib", "api_client.dart"));

        // Config supporting files
        supportingFiles.add(new SupportingFile("config.mustache", "lib/config", "config.dart"));
        supportingFiles.add(new SupportingFile("acdc_config.mustache", "lib/config", "acdc_config.dart"));
        supportingFiles.add(new SupportingFile("auth_config.mustache", "lib/config", "auth_config.dart"));
        supportingFiles.add(new SupportingFile("cache_config.mustache", "lib/config", "cache_config.dart"));
        supportingFiles.add(new SupportingFile("log_config.mustache", "lib/config", "log_config.dart"));
        supportingFiles.add(new SupportingFile("offline_config.mustache", "lib/config", "offline_config.dart"));
        supportingFiles.add(new SupportingFile("security_config.mustache", "lib/config", "security_config.dart"));

        // Language-specific primitives
        languageSpecificPrimitives = new HashSet<>(Arrays.asList(
            "String",
            "bool",
            "int",
            "double",
            "num",
            "Object",
            "DateTime",
            "List",
            "Map"
        ));

        // Type mappings: OpenAPI types -> Dart types
        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("number", "double");
        typeMapping.put("float", "double");
        typeMapping.put("double", "double");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "String");
        typeMapping.put("UUID", "String");
        typeMapping.put("date", "DateTime");
        typeMapping.put("DateTime", "DateTime");
        typeMapping.put("date-time", "DateTime");
        typeMapping.put("password", "String");
        typeMapping.put("binary", "List<int>");
        typeMapping.put("ByteArray", "List<int>");
        typeMapping.put("file", "MultipartFile");
        typeMapping.put("object", "Map<String, dynamic>");
        typeMapping.put("array", "List");
        typeMapping.put("map", "Map<String, dynamic>");
        typeMapping.put("AnyType", "Object");
    }

    /**
     * Returns the generator's unique identifier.
     * Used by OpenAPI Generator CLI with -g flag (e.g., -g dart-acdc).
     *
     * @return "dart-acdc"
     */
    @Override
    public String getName() {
        return "dart-acdc";
    }

    /**
     * Returns the help message describing this generator.
     *
     * @return Generator description
     */
    @Override
    public String getHelp() {
        return "Generates a Dart-ACDC client library with authentication, caching, debugging, and offline support.";
    }

    /**
     * Returns the generator type.
     *
     * @return CodegenType.CLIENT
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Escapes a reserved word by suffixing it with an underscore.
     * This is called for property/variable names.
     *
     * @param name the reserved word to escape
     * @return the escaped name with underscore suffix
     */
    @Override
    public String escapeReservedWord(String name) {
        // For property names, suffix with underscore
        return name + "_";
    }

    /**
     * Escapes reserved words in model/class names.
     * Dart reserved keywords are suffixed with "Model".
     *
     * @param name the model/class name
     * @return the escaped name if it's a reserved word, otherwise the original name
     */
    @Override
    public String toModelName(String name) {
        // First apply standard sanitization from parent class
        String sanitized = super.toModelName(name);

        // If the sanitized name is a Dart reserved keyword (case-insensitive check), suffix with "Model"
        if (isReservedWord(sanitized.toLowerCase())) {
            return sanitized + "Model";
        }

        return sanitized;
    }

    /**
     * Returns the file name for a model.
     * Ensures file names match the escaped model names for Dart conventions.
     *
     * @param name the model name (from OpenAPI schema)
     * @return the file name (without extension)
     */
    @Override
    public String toModelFilename(String name) {
        // Use the escaped model name for the file name to maintain Dart conventions
        return toModelName(name);
    }

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
    protected String sanitizePubName(String name) {
        if (name == null || name.isEmpty()) {
            return "openapi_client";
        }

        // Convert to lowercase
        String sanitized = name.toLowerCase();

        // Replace spaces and hyphens with underscores
        sanitized = sanitized.replaceAll("[ -]", "_");

        // Remove all characters except a-z, 0-9, and _
        sanitized = sanitized.replaceAll("[^a-z0-9_]", "");

        // Collapse consecutive underscores to single underscore
        sanitized = sanitized.replaceAll("_+", "_");

        // Remove leading/trailing underscores
        sanitized = sanitized.replaceAll("^_+|_+$", "");

        // If empty after sanitization, use default
        if (sanitized.isEmpty()) {
            return "openapi_client";
        }

        // Prefix with 'api_' if name starts with a digit
        if (sanitized.matches("^[0-9].*")) {
            sanitized = "api_" + sanitized;
        }

        return sanitized;
    }

    /**
     * Processes additional properties and applies sanitization where needed.
     *
     * @param objs the map of additional properties
     */
    @Override
    public void processOpts() {
        super.processOpts();

        // Sanitize pubName if provided
        if (additionalProperties.containsKey("pubName")) {
            String pubName = (String) additionalProperties.get("pubName");
            String sanitizedPubName = sanitizePubName(pubName);
            additionalProperties.put("pubName", sanitizedPubName);
        }
    }

    /**
     * Converts an enum value to a valid Dart identifier using camelCase.
     *
     * Rules:
     * - Convert to camelCase
     * - Remove/replace invalid characters
     * - Prefix numeric values with 'value'
     * - Handle empty strings as 'empty'
     *
     * @param value the original enum value
     * @param datatype the data type (e.g., String, int)
     * @return the sanitized enum identifier
     */
    @Override
    public String toEnumVarName(String value, String datatype) {
        if (value == null || value.isEmpty()) {
            return "empty";
        }

        // Check if value is numeric
        if (value.matches("^-?\\d+(\\.\\d+)?$")) {
            // Numeric value - prefix with 'value' and remove any decimals/negatives
            String sanitized = value.replaceAll("[^0-9]", "");
            if (sanitized.isEmpty()) {
                sanitized = "0";
            }
            return "value" + sanitized;
        }

        // Convert to camelCase
        String identifier = toCamelCase(value);

        // If empty after sanitization, use 'empty'
        if (identifier.isEmpty()) {
            return "empty";
        }

        // If starts with digit, prefix with 'value'
        if (identifier.matches("^\\d.*")) {
            identifier = "value" + capitalize(identifier);
        }

        // Handle reserved words - suffix with underscore for enum values
        if (isReservedWord(identifier)) {
            identifier = identifier + "_";
        }

        return identifier;
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
        String processed = input
            .replaceAll("[-_./\\s]+", " ")  // Replace separators with space
            .trim();

        if (processed.isEmpty()) {
            return "";
        }

        // Split into words
        String[] words = processed.split("\\s+");
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
        String sanitized = result.toString().replaceAll("[^a-zA-Z0-9]", "");

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
     * Overrides fromModel to properly handle enum schemas.
     * Ensures that schemas with enum values are marked as enums.
     *
     * @param name the model name
     * @param schema the schema
     * @return the codegen model
     */
    @Override
    public CodegenModel fromModel(String name, Schema schema) {
        CodegenModel model = super.fromModel(name, schema);

        // Check if this schema has enum values and no properties (simple enum)
        if (schema != null && schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            if (schema.getProperties() == null || schema.getProperties().isEmpty()) {
                model.isEnum = true;
            }
        }

        return model;
    }

}
