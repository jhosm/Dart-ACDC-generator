package org.openapitools.codegen.languages;

import org.openapitools.codegen.*;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.utils.ModelUtils;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DartAcdcGenerator.class);

    // Constants for content types
    private static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String MEDIA_TYPE_KEY = "mediaType";

    // Constants for vendor extensions
    private static final String VENDOR_EXTENSION_IS_MULTIPART = "x-is-multipart";
    private static final String VENDOR_EXTENSION_IS_MULTIPART_CONTEXT = "x-is-multipart-context";
    private static final String VENDOR_EXTENSION_IS_MULTIPART_FILE = "x-is-multipart-file";
    private static final String VENDOR_EXTENSION_DART_IMPORT = "x-dart-import";

    // Constants for Dart types and imports
    private static final String DART_TYPE_MULTIPART_FILE = "MultipartFile";
    private static final String DART_TYPE_LIST_INT = "List<int>";
    private static final String DART_IMPORT_DIO = "package:dio/dio.dart";

    // Constants for default values
    private static final String DEFAULT_PACKAGE_NAME = "openapi_client";
    private static final String DEFAULT_ENUM_VALUE = "empty";
    private static final String NUMERIC_ENUM_PREFIX = "value";
    private static final String RESERVED_WORD_MODEL_SUFFIX = "Model";
    private static final String RESERVED_WORD_VAR_SUFFIX = "_";
    private static final String NUMERIC_PACKAGE_PREFIX = "api_";

    // Pre-compiled regex patterns for performance
    // Package name sanitization patterns
    private static final Pattern PATTERN_SPACES_HYPHENS = Pattern.compile("[ -]");
    private static final Pattern PATTERN_NON_ALPHANUMERIC_PACKAGE = Pattern.compile("[^a-z0-9_]");
    private static final Pattern PATTERN_CONSECUTIVE_UNDERSCORES = Pattern.compile("_+");
    private static final Pattern PATTERN_LEADING_TRAILING_UNDERSCORES = Pattern.compile("^_+|_+$");
    private static final Pattern PATTERN_STARTS_WITH_DIGIT = Pattern.compile("^[0-9].*");

    // Enum value patterns
    private static final Pattern PATTERN_NUMERIC_VALUE = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern PATTERN_NON_DIGITS = Pattern.compile("[^0-9]");

    // CamelCase conversion patterns
    private static final Pattern PATTERN_SEPARATORS = Pattern.compile("[-_./\\s]+");
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PATTERN_NON_ALPHANUMERIC_CAMEL = Pattern.compile("[^a-zA-Z0-9]");

    // Underscore conversion patterns
    private static final Pattern PATTERN_LOWERCASE_UPPERCASE = Pattern.compile("([a-z0-9])([A-Z])");
    private static final Pattern PATTERN_UPPERCASE_SEQUENCE = Pattern.compile("([A-Z])([A-Z][a-z])");

    /**
     * ThreadLocal to track whether we're currently processing a multipart/form-data request body.
     * This allows context-aware type mapping for file/binary types.
     */
    private static final ThreadLocal<Boolean> IS_MULTIPART_CONTEXT = ThreadLocal.withInitial(() -> false);

    /**
     * Map to track which schemas should extend sealed classes.
     * Key: schema name (e.g., "Dog"), Value: parent sealed class name (e.g., "Animal")
     */
    private final Map<String, String> sealedClassExtensions = new HashMap<>();

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
        apiTemplateFiles.put("remote_data_source.mustache", "_remote_data_source.dart");
        apiTemplateFiles.put("remote_data_source_impl.mustache", "_remote_data_source_impl.dart");
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

        // Barrel export files
        supportingFiles.add(new SupportingFile("models.mustache", "lib/models", "models.dart"));
        supportingFiles.add(new SupportingFile("remote_data_sources.mustache", "lib/remote_data_sources", "remote_data_sources.dart"));
        supportingFiles.add(new SupportingFile("library.mustache", "lib", "{{pubName}}.dart"));

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
            "DateTime"
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
        typeMapping.put("binary", DART_TYPE_LIST_INT);
        typeMapping.put("ByteArray", DART_TYPE_LIST_INT);
        // Note: "file" type mapping is context-aware - see getTypeDeclaration()
        // In multipart/form-data context: MultipartFile
        // In non-multipart context: List<int>
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
        return name + RESERVED_WORD_VAR_SUFFIX;
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
            return sanitized + RESERVED_WORD_MODEL_SUFFIX;
        }

        return sanitized;
    }

    /**
     * Returns the file name for a model.
     * Converts model names to snake_case following Dart file naming conventions.
     *
     * @param name the model name (from OpenAPI schema)
     * @return the file name in snake_case (without extension)
     */
    @Override
    public String toModelFilename(String name) {
        // Convert the model name to snake_case for Dart file naming conventions
        // e.g., "UserProfile" -> "user_profile"
        return underscore(toModelName(name));
    }

    /**
     * Generates the import statement for a model reference.
     * Creates proper package-relative import paths for Dart.
     *
     * @param name the model name
     * @return the import path (e.g., "package:my_api/models/user.dart")
     */
    @Override
    public String toModelImport(String name) {
        // Get the pubName from additional properties, or use default
        String pubName = (String) additionalProperties.get("pubName");
        if (pubName == null || pubName.isEmpty()) {
            pubName = DEFAULT_PACKAGE_NAME;
        }

        // Convert model name to filename
        String filename = toModelFilename(name);

        // Generate Dart package import path
        // Format: package:{pubName}/models/{filename}.dart
        return "package:" + pubName + "/models/" + filename + ".dart";
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
     * Converts a string from PascalCase/camelCase to snake_case.
     * Used for generating Dart file names from model class names.
     *
     * @param name the name in PascalCase or camelCase
     * @return the name in snake_case
     */
    private String underscore(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Insert underscore before uppercase letters (except at the start)
        // and convert to lowercase
        // e.g., "UserProfile" -> "user_profile", "HTTPResponse" -> "http_response"
        String result = PATTERN_LOWERCASE_UPPERCASE.matcher(name).replaceAll("$1_$2");
        result = PATTERN_UPPERCASE_SEQUENCE.matcher(result).replaceAll("$1_$2");
        result = result.toLowerCase();

        return result;
    }

    /**
     * Preprocesses the OpenAPI specification to flatten allOf compositions and detect circular references.
     * This runs before model generation to optimize the schema structure for code generation.
     *
     * @param openAPI the OpenAPI specification
     */
    @Override
    public void preprocessOpenAPI(io.swagger.v3.oas.models.OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        LOGGER.info("Preprocessing OpenAPI spec for allOf composition");

        Map<String, Schema> schemas = extractSchemas(openAPI);
        if (schemas == null) {
            LOGGER.info("No schemas to preprocess");
            return;
        }

        LOGGER.info("Found {} schemas to process", schemas.size());

        // First pass: flatten allOf compositions
        flattenAllOfCompositions(schemas);

        // Second pass: detect and mark circular references
        detectAllCircularReferences(schemas);
    }

    /**
     * Safely extracts schemas from the OpenAPI specification.
     *
     * @param openAPI the OpenAPI specification
     * @return the schemas map, or null if not available
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Schema> extractSchemas(io.swagger.v3.oas.models.OpenAPI openAPI) {
        if (openAPI == null || openAPI.getComponents() == null) {
            return null;
        }
        return openAPI.getComponents().getSchemas();
    }

    /**
     * Flattens all allOf compositions in the schema map.
     *
     * @param schemas the schemas to process (modified in place)
     */
    @SuppressWarnings("rawtypes")
    private void flattenAllOfCompositions(Map<String, Schema> schemas) {
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema schema = entry.getValue();

            if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
                LOGGER.info("Processing allOf for schema: {}", schemaName);
                Schema flattenedSchema = composeAllOfSchemaPreprocess(schemaName, schema, schemas);
                schemas.put(schemaName, flattenedSchema);
                LOGGER.info("Replaced schema {} with flattened version", schemaName);
            }
        }
    }

    /**
     * Detects circular references in all schemas and marks affected properties as nullable.
     *
     * @param schemas all schemas to check for circular references
     */
    @SuppressWarnings("rawtypes")
    private void detectAllCircularReferences(Map<String, Schema> schemas) {
        LOGGER.info("Detecting circular references");
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema schema = entry.getValue();
            detectCircularReferences(schemaName, schema, schemas, new HashSet<>());
        }
    }

    /**
     * Detects circular references in a schema by traversing the property graph.
     * Marks properties involved in circular references as nullable.
     *
     * @param schemaName the current schema name
     * @param schema the current schema
     * @param allSchemas all available schemas for reference resolution
     * @param visitedPath set of schema names in the current path (for cycle detection)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void detectCircularReferences(String schemaName, Schema schema, Map<String, Schema> allSchemas, Set<String> visitedPath) {
        if (schema == null || schema.getProperties() == null) {
            return;
        }

        // Add current schema to path
        visitedPath.add(schemaName);

        Map<String, Schema> properties = (Map<String, Schema>) schema.getProperties();

        for (Map.Entry<String, Schema> propEntry : properties.entrySet()) {
            String propName = propEntry.getKey();
            Schema propSchema = propEntry.getValue();

            // Check if this property references another schema
            String referencedSchemaName = null;

            if (propSchema.get$ref() != null) {
                // Direct reference
                String ref = propSchema.get$ref();
                referencedSchemaName = extractSchemaNameFromRef(ref);
            } else if ("array".equals(propSchema.getType()) && propSchema.getItems() != null && propSchema.getItems().get$ref() != null) {
                // Array of references
                String ref = propSchema.getItems().get$ref();
                referencedSchemaName = extractSchemaNameFromRef(ref);
            }

            if (referencedSchemaName != null) {
                // Check if this creates a cycle
                if (visitedPath.contains(referencedSchemaName)) {
                    // Circular reference detected!
                    LOGGER.info("Circular reference detected: {} -> {} (in property '{}')", schemaName, referencedSchemaName, propName);

                    // Mark property as nullable
                    propSchema.setNullable(true);
                    LOGGER.info("Marked property '{}' in schema '{}' as nullable", propName, schemaName);
                } else {
                    // Continue traversal
                    Schema referencedSchema = allSchemas.get(referencedSchemaName);
                    if (referencedSchema != null) {
                        // Create a new visited set for this branch (copy current path)
                        Set<String> newPath = new HashSet<>(visitedPath);
                        detectCircularReferences(referencedSchemaName, referencedSchema, allSchemas, newPath);
                    }
                }
            }
        }
    }

    /**
     * Composes an allOf schema during preprocessing by merging all properties.
     * Handles nested composition (allOf containing oneOf/anyOf references).
     *
     * @param name the schema name
     * @param schema the schema with allOf
     * @param allSchemas all available schemas for $ref resolution
     * @return a new flattened schema
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Schema composeAllOfSchemaPreprocess(String name, Schema schema, Map<String, Schema> allSchemas) {
        if (schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
            return schema;
        }

        LOGGER.info("Composing allOf schema for: {}", name);

        Map<String, Schema> mergedProperties = new LinkedHashMap<>();
        Set<String> mergedRequired = new LinkedHashSet<>();

        // Process each schema in the allOf array
        List<Schema> allOfSchemas = (List<Schema>) schema.getAllOf();
        LOGGER.info("allOf has {} schemas to merge", allOfSchemas.size());

        for (Schema allOfSchema : allOfSchemas) {
            processAllOfElement(name, allOfSchema, allSchemas, mergedProperties, mergedRequired);
        }

        // Create and configure the composed schema
        Schema composedSchema = createComposedSchema(schema, mergedProperties, mergedRequired);

        LOGGER.info("Composed allOf schema for '{}': {} properties, {} required",
                    name, mergedProperties.size(), mergedRequired.size());

        return composedSchema;
    }

    /**
     * Processes a single element from an allOf array, merging properties or handling nested composition.
     *
     * @param parentName the parent schema name
     * @param allOfSchema the allOf element schema
     * @param allSchemas all available schemas for $ref resolution
     * @param mergedProperties accumulator for merged properties
     * @param mergedRequired accumulator for merged required properties
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processAllOfElement(String parentName, Schema allOfSchema, Map<String, Schema> allSchemas,
                                      Map<String, Schema> mergedProperties, Set<String> mergedRequired) {
        Schema resolvedSchema = allOfSchema;
        String referencedSchemaName = null;

        // Resolve $ref if present
        if (allOfSchema.get$ref() != null) {
            String ref = allOfSchema.get$ref();
            referencedSchemaName = extractSchemaNameFromRef(ref);
            resolvedSchema = allSchemas.get(referencedSchemaName);

            if (resolvedSchema == null) {
                LOGGER.warn("Unable to resolve $ref: {} in allOf for schema: {}", ref, parentName);
                return;
            }
        }

        // Handle nested composition (allOf containing oneOf/anyOf)
        if (isCompositionSchema(resolvedSchema) && referencedSchemaName != null) {
            handleNestedComposition(referencedSchemaName, mergedProperties, mergedRequired);
            return;
        }

        // Regular object schema: Merge properties
        mergeSchemaProperties(parentName, resolvedSchema, mergedProperties);

        // Merge required arrays
        if (resolvedSchema.getRequired() != null) {
            mergedRequired.addAll(resolvedSchema.getRequired());
        }
    }

    /**
     * Checks if a schema is a composition schema (oneOf or anyOf).
     *
     * @param schema the schema to check
     * @return true if the schema has oneOf or anyOf
     */
    @SuppressWarnings("rawtypes")
    private boolean isCompositionSchema(Schema schema) {
        return (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) ||
               (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty());
    }

    /**
     * Handles nested composition by creating a property typed as the composition schema.
     *
     * @param referencedSchemaName the name of the composition schema
     * @param mergedProperties accumulator for merged properties
     * @param mergedRequired accumulator for merged required properties
     */
    @SuppressWarnings("rawtypes")
    private void handleNestedComposition(String referencedSchemaName, Map<String, Schema> mergedProperties,
                                          Set<String> mergedRequired) {
        LOGGER.info("Detected nested composition: allOf contains oneOf/anyOf reference to '{}'", referencedSchemaName);

        // Create a property with type = the referenced schema name
        Schema propertySchema = new Schema();
        propertySchema.set$ref("#/components/schemas/" + referencedSchemaName);

        // Use camelCase schema name as property name
        String propertyName = toCamelCase(referencedSchemaName);
        mergedProperties.put(propertyName, propertySchema);
        mergedRequired.add(propertyName);

        LOGGER.info("Created property '{}' typed as '{}'", propertyName, referencedSchemaName);
    }

    /**
     * Merges properties from a resolved schema into the merged properties map.
     *
     * @param parentName the parent schema name (for logging)
     * @param resolvedSchema the schema to merge from
     * @param mergedProperties accumulator for merged properties
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mergeSchemaProperties(String parentName, Schema resolvedSchema, Map<String, Schema> mergedProperties) {
        if (resolvedSchema.getProperties() == null) {
            return;
        }

        Map<String, Schema> properties = (Map<String, Schema>) resolvedSchema.getProperties();
        for (Map.Entry<String, Schema> propEntry : properties.entrySet()) {
            String propName = propEntry.getKey();
            Schema propSchema = propEntry.getValue();

            // Check for property conflict
            if (mergedProperties.containsKey(propName)) {
                logPropertyConflict(parentName, propName, mergedProperties.get(propName), propSchema);
            }

            // Last definition wins
            mergedProperties.put(propName, propSchema);
        }
    }

    /**
     * Logs a warning when a property conflict is detected during allOf merging.
     *
     * @param schemaName the schema name
     * @param propertyName the conflicting property name
     * @param existingSchema the existing property schema
     * @param newSchema the new property schema
     */
    @SuppressWarnings("rawtypes")
    private void logPropertyConflict(String schemaName, String propertyName, Schema existingSchema, Schema newSchema) {
        String existingType = existingSchema.getType();
        String newType = newSchema.getType();

        if (!Objects.equals(existingType, newType)) {
            LOGGER.warn("Property conflict in allOf for schema '{}': property '{}' " +
                       "has different types ({} vs {}). Using last definition.",
                       schemaName, propertyName, existingType, newType);
        }
    }

    /**
     * Creates the final composed schema from merged data.
     *
     * @param originalSchema the original schema with allOf
     * @param mergedProperties the merged properties
     * @param mergedRequired the merged required properties
     * @return the composed schema
     */
    @SuppressWarnings("rawtypes")
    private Schema createComposedSchema(Schema originalSchema, Map<String, Schema> mergedProperties,
                                         Set<String> mergedRequired) {
        Schema composedSchema = new Schema();

        // Apply merged properties and required
        if (!mergedProperties.isEmpty()) {
            composedSchema.setProperties(mergedProperties);
        }

        if (!mergedRequired.isEmpty()) {
            composedSchema.setRequired(new ArrayList<>(mergedRequired));
        }

        // Copy other relevant attributes from the original schema
        composedSchema.setType(originalSchema.getType() != null ? originalSchema.getType() : "object");

        if (originalSchema.getDescription() != null) {
            composedSchema.setDescription(originalSchema.getDescription());
        }

        if (originalSchema.getTitle() != null) {
            composedSchema.setTitle(originalSchema.getTitle());
        }

        return composedSchema;
    }

    /**
     * Overrides fromModel to properly handle standalone enum schemas and composition schemas (oneOf/anyOf).
     * allOf composition is now handled in preprocessOpenAPI() which runs before model generation.
     *
     * For enum schemas:
     * - Ensures that schemas with enum values are properly processed as enums
     *   with populated allowableValues and enumVars for template rendering.
     *
     * For oneOf/anyOf schemas:
     * - Detects composition and marks the model appropriately
     * - Processes alternatives and discriminator information
     * - Adds metadata for sealed class generation in templates
     *
     * @param name the model name
     * @param schema the schema
     * @return the codegen model
     */
    @Override
    public CodegenModel fromModel(String name, Schema schema) {
        CodegenModel model = super.fromModel(name, schema);

        // Check if this schema has enum values and no properties (standalone enum)
        if (schema != null && schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            if (schema.getProperties() == null || schema.getProperties().isEmpty()) {
                model.isEnum = true;
                // Note: allowableValues and enumVars are processed in postProcessModels
            }
        }

        // Check for oneOf composition
        if (schema != null && schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            processOneOfComposition(name, schema, model);
        }

        // Check for anyOf composition
        if (schema != null && schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            processAnyOfComposition(name, schema, model);
        }

        // Check if this model should extend a sealed class
        String parentSealedClass = sealedClassExtensions.get(model.classname);
        if (parentSealedClass != null) {
            model.parent = parentSealedClass;
            model.vendorExtensions.put("x-extends-sealed-class", true);
            model.vendorExtensions.put("x-sealed-parent", parentSealedClass);
            model.vendorExtensions.put("x-sealed-parent-filename", toModelFilename(parentSealedClass));
            LOGGER.info("Model {} will extend sealed class {}", model.classname, parentSealedClass);
        }

        return model;
    }

    /**
     * Processes a oneOf composition schema and adds metadata to the CodegenModel.
     *
     * @param name the schema name
     * @param schema the schema with oneOf
     * @param model the codegen model to update
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processOneOfComposition(String name, Schema schema, CodegenModel model) {
        LOGGER.info("Processing oneOf composition for schema: {}", name);

        // Mark this model as a oneOf composition
        model.vendorExtensions.put("x-is-one-of", true);

        // Process discriminator if present
        processDiscriminator(name, schema, model);

        // Process oneOf alternatives
        List<Schema> oneOfSchemas = (List<Schema>) schema.getOneOf();
        List<Map<String, Object>> alternatives = processCompositionAlternatives(name, oneOfSchemas, "oneOf");

        // Track which schemas should extend this sealed class
        registerSealedClassExtensions(name, oneOfSchemas);

        model.vendorExtensions.put("x-one-of-alternatives", alternatives);
        LOGGER.info("Processed oneOf for '{}': {} alternatives", name, alternatives.size());
    }

    /**
     * Processes an anyOf composition schema and adds metadata to the CodegenModel.
     * anyOf is treated identically to oneOf without discriminator.
     *
     * @param name the schema name
     * @param schema the schema with anyOf
     * @param model the codegen model to update
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processAnyOfComposition(String name, Schema schema, CodegenModel model) {
        LOGGER.info("Processing anyOf composition for schema: {}", name);

        // Mark this model as an anyOf composition
        model.vendorExtensions.put("x-is-any-of", true);

        // anyOf never has discriminator (treat as try-each)
        model.vendorExtensions.put("x-has-discriminator", false);

        // Process anyOf alternatives (same as oneOf)
        List<Schema> anyOfSchemas = (List<Schema>) schema.getAnyOf();
        List<Map<String, Object>> alternatives = processCompositionAlternatives(name, anyOfSchemas, "anyOf");

        // Track which schemas should extend this sealed class
        registerSealedClassExtensions(name, anyOfSchemas);

        model.vendorExtensions.put("x-any-of-alternatives", alternatives);
        LOGGER.info("Processed anyOf for '{}': {} alternatives", name, alternatives.size());
    }

    /**
     * Processes discriminator information for oneOf schemas.
     * Extracts discriminator property name and mapping metadata.
     *
     * @param name the schema name
     * @param schema the schema (may have discriminator)
     * @param model the codegen model to update
     */
    @SuppressWarnings("rawtypes")
    private void processDiscriminator(String name, Schema schema, CodegenModel model) {
        if (schema.getDiscriminator() == null) {
            model.vendorExtensions.put("x-has-discriminator", false);
            return;
        }

        String discriminatorPropertyName = schema.getDiscriminator().getPropertyName();

        // Validate discriminator property name
        if (discriminatorPropertyName == null || discriminatorPropertyName.isEmpty()) {
            LOGGER.warn("Discriminator property name is null or empty for schema: {}", name);
            model.vendorExtensions.put("x-has-discriminator", false);
            return;
        }

        model.vendorExtensions.put("x-has-discriminator", true);
        model.vendorExtensions.put("x-discriminator-name", discriminatorPropertyName);

        // Process discriminator mapping
        if (schema.getDiscriminator().getMapping() != null) {
            List<Map<String, Object>> discriminatorMapping = new ArrayList<>();
            Map<String, String> mapping = schema.getDiscriminator().getMapping();

            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String mappingKey = entry.getKey();
                String schemaRef = entry.getValue();

                // Extract schema name from $ref (e.g., "#/components/schemas/Dog" -> "Dog")
                String schemaName = extractSchemaNameFromRef(schemaRef);
                // Use the actual schema name as the subclass name (e.g., "Dog", not "AnimalDog")
                String subclassName = toModelName(schemaName);

                Map<String, Object> mappingEntry = Map.of(
                    "mappingKey", mappingKey,
                    "schemaName", schemaName,
                    "subclassName", subclassName
                );
                discriminatorMapping.add(mappingEntry);
            }

            model.vendorExtensions.put("x-discriminator-mapping", discriminatorMapping);
        }
    }

    /**
     * Processes composition alternatives (oneOf/anyOf) into a list of metadata maps.
     * Handles references, primitive types, and inline schemas.
     *
     * @param parentName the parent schema name
     * @param schemas the list of alternative schemas
     * @param compositionType the composition type ("oneOf" or "anyOf") for logging
     * @return list of alternative metadata maps
     */
    @SuppressWarnings("rawtypes")
    private List<Map<String, Object>> processCompositionAlternatives(String parentName, List<Schema> schemas, String compositionType) {
        List<Map<String, Object>> alternatives = new ArrayList<>();

        for (int i = 0; i < schemas.size(); i++) {
            Schema alternativeSchema = schemas.get(i);
            boolean hasNext = i < schemas.size() - 1;
            Map<String, Object> alternative = createAlternativeMetadata(parentName, alternativeSchema, i, hasNext, compositionType);

            if (alternative != null) {
                alternatives.add(alternative);
            }
        }

        return alternatives;
    }

    /**
     * Creates metadata for a single composition alternative.
     *
     * @param parentName the parent schema name
     * @param schema the alternative schema
     * @param index the index of this alternative
     * @param hasNext whether there are more alternatives after this one
     * @param compositionType the composition type ("oneOf" or "anyOf") for logging
     * @return metadata map, or null if the schema is invalid
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Object> createAlternativeMetadata(String parentName, Schema schema, int index, boolean hasNext, String compositionType) {
        if (schema.get$ref() != null) {
            // Reference to another schema
            String ref = schema.get$ref();
            String schemaName = extractSchemaNameFromRef(ref);
            // Use the actual schema name as the subclass name (e.g., "Dog", not "AnimalDog")
            String subclassName = toModelName(schemaName);

            return Map.of(
                "parentClassName", parentName,
                "isRef", true,
                "schemaName", schemaName,
                "subclassName", subclassName,
                "importPath", toModelImport(schemaName),
                "hasNext", hasNext
            );
        } else if (schema.getType() != null) {
            // Inline schema (primitive or object)
            String type = schema.getType();
            boolean isPrimitive = isPrimitiveType(type);

            if (isPrimitive) {
                // Wrapper class for primitive
                String dartType = getTypeDeclaration(schema);
                String wrapperName = toModelName(parentName + capitalize(dartType));

                return Map.of(
                    "parentClassName", parentName,
                    "isPrimitive", true,
                    "dartType", dartType,
                    "subclassName", wrapperName,
                    "hasNext", hasNext
                );
            } else {
                // Inline complex type - use Option naming
                String subclassName = toModelName(parentName + "Option" + (index + 1));
                return Map.of(
                    "parentClassName", parentName,
                    "isInline", true,
                    "subclassName", subclassName,
                    "index", index + 1,
                    "hasNext", hasNext
                );
            }
        } else {
            // Schema has neither $ref nor type - log warning and skip
            LOGGER.warn("{} schema at index {} has neither $ref nor type for schema '{}'. Skipping.",
                       compositionType, index, parentName);
            return null;
        }
    }

    /**
     * Registers schemas referenced in oneOf/anyOf to extend the parent sealed class.
     *
     * @param parentName the parent sealed class name
     * @param schemas the list of referenced schemas
     */
    @SuppressWarnings("rawtypes")
    private void registerSealedClassExtensions(String parentName, List<Schema> schemas) {
        for (Schema schema : schemas) {
            if (schema.get$ref() != null) {
                // Only register for references (not inline primitives or objects)
                String ref = schema.get$ref();
                String childSchemaName = extractSchemaNameFromRef(ref);
                String childModelName = toModelName(childSchemaName);
                sealedClassExtensions.put(childModelName, parentName);
                LOGGER.info("Registered {} to extend sealed class {}", childModelName, parentName);
            }
        }
    }

    /**
     * Checks if an OpenAPI type is a primitive type (string, integer, number, boolean).
     * Arrays and objects are not considered primitive.
     *
     * @param type the OpenAPI type to check
     * @return true if the type is primitive (string/integer/number/boolean), false otherwise
     */
    private boolean isPrimitiveType(String type) {
        return type != null && OPENAPI_PRIMITIVE_TYPES.contains(type);
    }

    /**
     * Safely extracts the schema name from a $ref string.
     * Handles refs without '/' gracefully and validates input.
     *
     * @param ref the $ref string (e.g., "#/components/schemas/Pet")
     * @return the schema name (e.g., "Pet"), or "UnknownSchema" if extraction fails
     */
    private String extractSchemaNameFromRef(String ref) {
        if (ref == null || ref.isEmpty()) {
            LOGGER.warn("Received null or empty $ref");
            return "UnknownSchema";
        }

        int lastSlashIndex = ref.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            LOGGER.warn("Malformed $ref without '/': {}", ref);
            return ref; // Return the whole string as fallback
        }

        if (lastSlashIndex == ref.length() - 1) {
            LOGGER.warn("Malformed $ref ends with '/': {}", ref);
            return "UnknownSchema";
        }

        return ref.substring(lastSlashIndex + 1);
    }

    /**
     * Post-processes models to ensure enum data is properly structured for templates.
     * Also ensures that properties requiring special imports (like MultipartFile) have
     * their imports properly tracked.
     * This runs after all model processing and right before template rendering.
     *
     * @param objs the models map containing all model data
     * @return the processed models map
     */
    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        ModelsMap result = super.postProcessModels(objs);

        // Process each model to add enum variables with collision-resistant naming
        for (ModelMap modelMap : result.getModels()) {
            CodegenModel model = modelMap.getModel();

            // Fix model imports: convert from simple model names to proper import paths
            if (model.imports != null && !model.imports.isEmpty()) {
                Set<String> fixedImports = new HashSet<>();
                for (Object importObj : model.imports) {
                    if (importObj instanceof String) {
                        String importStr = (String) importObj;
                        // Check if this is already a full path (starts with package:)
                        if (importStr.startsWith("package:")) {
                            fixedImports.add(importStr);
                        } else {
                            // Convert model name to import path
                            String importPath = toModelImport(importStr);
                            if (importPath != null && !importPath.isEmpty()) {
                                fixedImports.add(importPath);
                            }
                        }
                    } else if (importObj instanceof Map) {
                        // Similar to operations fix - extract import path from map
                        Map<?, ?> importMap = (Map<?, ?>) importObj;
                        Object importPath = importMap.get("import");
                        if (importPath != null) {
                            fixedImports.add(importPath.toString());
                        }
                    }
                }
                model.imports.clear();
                model.imports.addAll(fixedImports);
            }

            if (model.isEnum && model.allowableValues != null) {
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) model.allowableValues.get("values");

                if (values != null && !values.isEmpty()) {
                    // Determine datatype from model
                    String datatype = model.dataType != null ? model.dataType : "string";

                    // Create enumVars with collision-resistant naming
                    List<Map<String, Object>> enumVars = createEnumVars(values, datatype);
                    model.allowableValues.put("enumVars", enumVars);
                }
            }

            // Scan model properties for any that require special imports (e.g., MultipartFile)
            if (model.vars != null) {
                for (CodegenProperty prop : model.vars) {
                    if (prop.vendorExtensions.containsKey(VENDOR_EXTENSION_DART_IMPORT)) {
                        String dartImport = (String) prop.vendorExtensions.get(VENDOR_EXTENSION_DART_IMPORT);
                        if (dartImport != null && !dartImport.isEmpty()) {
                            model.imports.add(dartImport);
                        }
                    }

                    // Add imports for oneOf/anyOf composition properties
                    if (prop.vendorExtensions.containsKey("x-is-composition-property")) {
                        // The property's complexType contains the model name that needs to be imported
                        String importPath = toModelImport(prop.complexType);
                        if (importPath != null && !importPath.isEmpty()) {
                            model.imports.add(importPath);
                        }
                    }
                }
            }

            // Add imports for oneOf/anyOf sealed class alternatives
            if (model.vendorExtensions.containsKey("x-is-one-of")) {
                addAlternativeImports(model, "x-one-of-alternatives");
            }
            if (model.vendorExtensions.containsKey("x-is-any-of")) {
                addAlternativeImports(model, "x-any-of-alternatives");
            }

            // Clean up imports: keep only valid package imports
            // Note: Final cleanup happens in postProcessAllModels() after base class adds more imports
            if (model.imports != null && !model.imports.isEmpty()) {
                Set<String> validImports = new HashSet<>();
                for (Object importObj : model.imports) {
                    String importStr = importObj.toString();
                    // Only keep imports that start with "package:" and don't reference primitive types
                    if (importStr.startsWith("package:") && !isPrimitiveTypeImport(importStr)) {
                        validImports.add(importStr);
                    }
                }
                model.imports = new TreeSet<>(validImports);
            }
        }

        return result;
    }

    /**
     * Post-processes all models after individual model processing.
     *
     * This is the final cleanup stage before template rendering. The base class
     * (DefaultCodegen.postProcessAllModels) adds simple-name imports based on
     * model references, which we need to convert to proper package imports.
     *
     * @param objs the map of all models
     * @return the processed models map
     */
    @Override
    public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> objs) {
        Map<String, ModelsMap> result = super.postProcessAllModels(objs);

        // Final cleanup: Remove simple-name imports added by base class
        // The base class adds imports like "Cat", "Dog" based on model references.
        // We need to ensure only valid package imports (package:xxx/models/yyy.dart) remain.
        for (Map.Entry<String, ModelsMap> entry : result.entrySet()) {
            ModelsMap modelsMap = entry.getValue();
            for (ModelMap modelMap : modelsMap.getModels()) {
                CodegenModel model = modelMap.getModel();

                if (model.imports != null && !model.imports.isEmpty()) {
                    Set<String> validImports = new TreeSet<>();
                    for (Object importObj : model.imports) {
                        String importStr = importObj.toString();
                        // Only keep imports that start with "package:" and don't reference primitive types
                        if (importStr.startsWith("package:") && !isPrimitiveTypeImport(importStr)) {
                            validImports.add(importStr);
                        }
                    }
                    model.imports = new TreeSet<>(validImports);
                }
            }
        }

        return result;
    }

    /**
     * OpenAPI primitive types (used for oneOf/anyOf composition logic).
     * These are the standard OpenAPI primitive types that can appear in schemas.
     */
    private static final Set<String> OPENAPI_PRIMITIVE_TYPES = Set.of(
        "string", "integer", "number", "boolean"
    );

    /**
     * Primitive type file names that should never be imported.
     * These are used to filter out invalid imports to non-existent primitive type files.
     * Includes both OpenAPI types and Dart-specific type names.
     */
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
        "string", "integer", "number", "boolean", "int", "double", "num", "array", "object"
    );

    /**
     * Checks if an import path references a primitive type file that doesn't exist.
     *
     * @param importPath the import path to check
     * @return true if this is an invalid primitive type import
     */
    private boolean isPrimitiveTypeImport(String importPath) {
        if (importPath == null || !importPath.contains("/models/")) {
            return false;
        }

        // Extract the filename (e.g., "string.dart" from "package:foo/models/string.dart")
        int lastSlash = importPath.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String filename = importPath.substring(lastSlash + 1);
        // Remove .dart extension
        if (filename.endsWith(".dart")) {
            filename = filename.substring(0, filename.length() - 5);
        }

        return PRIMITIVE_TYPES.contains(filename);
    }

    /**
     * Adds imports for oneOf/anyOf sealed class alternatives.
     * Extracts import paths from alternative metadata and adds them to model.imports.
     *
     * @param model the codegen model
     * @param alternativesKey the vendor extension key containing alternatives ("x-one-of-alternatives" or "x-any-of-alternatives")
     */
    @SuppressWarnings("unchecked")
    private void addAlternativeImports(CodegenModel model, String alternativesKey) {
        Object alternativesObj = model.vendorExtensions.get(alternativesKey);
        if (!(alternativesObj instanceof List)) {
            return;
        }

        List<Map<String, Object>> alternatives = (List<Map<String, Object>>) alternativesObj;
        for (Map<String, Object> alternative : alternatives) {
            // Only add imports for reference types (not primitives or inline schemas)
            Object isRef = alternative.get("isRef");
            if (Boolean.TRUE.equals(isRef)) {
                Object importPathObj = alternative.get("importPath");
                if (importPathObj instanceof String) {
                    String importPath = (String) importPathObj;
                    if (!importPath.isEmpty()) {
                        model.imports.add(importPath);
                        LOGGER.info("Added import for sealed class alternative: {}", importPath);
                    }
                }
            }
        }
    }

    /**
     * Creates enumVars list with collision-resistant naming.
     * Handles collisions by appending numeric suffixes (e.g., value, value2, value3).
     *
     * @param values the enum values from the schema
     * @param datatype the datatype for toEnumVarName processing
     * @return list of enumVar maps with 'name' and 'value' keys
     */
    private List<Map<String, Object>> createEnumVars(List<Object> values, String datatype) {
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
                "isString", "string".equalsIgnoreCase(datatype)
            );
            enumVars.add(enumVar);
        }

        return enumVars;
    }

    /**
     * Detects if the given content map contains multipart/form-data media type.
     *
     * @param content the content map from a request body
     * @return true if multipart/form-data is present, false otherwise
     */
    private boolean isMultipartContent(Content content) {
        if (content == null) {
            return false;
        }
        return content.containsKey(CONTENT_TYPE_MULTIPART_FORM_DATA);
    }

    /**
     * Overrides the base implementation to detect multipart/form-data context
     * and set ThreadLocal context for property processing.
     *
     * @param name the parameter name
     * @param requestBody the request body specification
     * @param imports the imports set
     * @param bodyParameterName the body parameter name
     * @return the CodegenParameter with multipart context information
     */
    @Override
    public CodegenParameter fromRequestBody(RequestBody requestBody, Set<String> imports, String bodyParameterName) {
        try {
            // Detect if this is a multipart/form-data request BEFORE calling super
            if (requestBody != null) {
                Content content = requestBody.getContent();
                boolean isMultipart = isMultipartContent(content);

                if (isMultipart) {
                    // Set ThreadLocal context for property processing
                    IS_MULTIPART_CONTEXT.set(true);
                }
            }

            CodegenParameter parameter = super.fromRequestBody(requestBody, imports, bodyParameterName);

            if (parameter == null) {
                return parameter;
            }

            // Mark the parameter with multipart context information
            if (IS_MULTIPART_CONTEXT.get()) {
                parameter.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_CONTEXT, true);

                // If this parameter itself is a file/binary type, mark it specifically
                if (parameter.isBinary || "file".equals(parameter.baseType)) {
                    parameter.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
                }
            }

            return parameter;
        } finally {
            // Always clear ThreadLocal after processing to avoid memory leaks
            IS_MULTIPART_CONTEXT.remove();
        }
    }

    /**
     * Overrides type declaration to provide context-aware mapping for file/binary types.
     *
     * In multipart/form-data context: binary/file → MultipartFile
     * In non-multipart context: binary/file → List<int>
     *
     * @param schema the schema
     * @return the Dart type declaration
     */
    @Override
    public String getTypeDeclaration(Schema schema) {
        if (schema == null) {
            return super.getTypeDeclaration(schema);
        }

        // Check if this is a binary/file type
        String format = schema.getFormat();
        String type = schema.getType();

        boolean isBinary = "string".equals(type) && "binary".equals(format);

        if (isBinary) {
            // For binary types, we need to check the context
            // However, at this point we don't have access to the parameter context
            // So we'll use the default mapping (List<int>) here
            // The multipart-specific mapping will be handled in fromProperty
            return DART_TYPE_LIST_INT;
        }

        return super.getTypeDeclaration(schema);
    }

    /**
     * Overrides fromProperty to apply context-aware type mapping for binary/file properties.
     *
     * Checks the ThreadLocal context to determine if we're in a multipart/form-data request,
     * and maps binary/file types accordingly:
     * - Multipart context: type=string,format=binary → MultipartFile
     * - Non-multipart context: type=string,format=binary → List<int>
     *
     * @param name the property name
     * @param schema the property schema
     * @param required whether the property is required
     * @param schemaIsFromAdditionalProperties whether this schema comes from additionalProperties
     * @return the CodegenProperty with correct type based on context
     */
    @Override
    public CodegenProperty fromProperty(String name, Schema schema, boolean required, boolean schemaIsFromAdditionalProperties) {
        CodegenProperty property = super.fromProperty(name, schema, required, schemaIsFromAdditionalProperties);

        if (property == null || schema == null) {
            return property;
        }

        // Check if this is a binary type (type=string, format=binary)
        String format = schema.getFormat();
        String type = schema.getType();
        boolean isBinary = "string".equals(type) && "binary".equals(format);

        if (isBinary && IS_MULTIPART_CONTEXT.get()) {
            // We're in multipart/form-data context - use MultipartFile
            property.dataType = DART_TYPE_MULTIPART_FILE;
            property.datatypeWithEnum = DART_TYPE_MULTIPART_FILE;
            property.baseType = DART_TYPE_MULTIPART_FILE;
            property.isBinary = true;

            // Mark for template usage
            property.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
            property.vendorExtensions.put(VENDOR_EXTENSION_DART_IMPORT, DART_IMPORT_DIO);
        }
        // else: non-multipart context - keep the default List<int> from typeMapping

        // Check if this property references a oneOf/anyOf/allOf composition
        // These need custom JSON converters since they're abstract/sealed classes
        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            property.vendorExtensions.put("x-is-one-of-property", true);
        } else if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            property.vendorExtensions.put("x-is-any-of-property", true);
        } else if (schema.get$ref() != null) {
            // Check if this $ref points to a oneOf/anyOf schema by checking the dataType
            // against our tracking maps
            String refName = property.dataType;
            if (refName != null) {
                // Check if this type is a sealed class parent (oneOf/anyOf schema)
                boolean isCompositionType = sealedClassExtensions.containsValue(refName);
                if (isCompositionType) {
                    property.vendorExtensions.put("x-is-composition-property", true);
                }
            }
        }

        return property;
    }

    /**
     * Post-processes operations to apply context-aware type mapping for file parameters.
     * Also ensures that parameters requiring special imports (like MultipartFile) have
     * their imports properly tracked.
     *
     * For operations with multipart/form-data request bodies, this method changes
     * binary parameters from List<int> to MultipartFile and adds the necessary imports.
     *
     * This method also filters imports to only include models actually used in operation
     * signatures (parameters, return types) to avoid unused imports.
     *
     * @param objs the operations map
     * @param allModels all models for cross-referencing
     * @return the processed operations map
     */
    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        OperationsMap result = super.postProcessOperationsWithModels(objs, allModels);

        // Get the operations list to track which models are actually used
        OperationMap operations = result.getOperations();
        List<CodegenOperation> ops = operations.getOperation();

        // Track which models are actually used across all operations
        Set<String> usedModelImports = new HashSet<>();

        // First pass: identify which models are actually used
        for (CodegenOperation operation : ops) {
            // Add imports for return types
            if (operation.returnType != null && !operation.returnType.equals("void")) {
                String returnModelImport = getModelImportFromType(operation.returnBaseType != null ? operation.returnBaseType : operation.returnType);
                if (returnModelImport != null) {
                    usedModelImports.add(returnModelImport);
                }
            }

            // Add imports for parameters
            if (operation.allParams != null) {
                for (CodegenParameter param : operation.allParams) {
                    String paramModelImport = getModelImportFromType(param.baseType != null ? param.baseType : param.dataType);
                    if (paramModelImport != null) {
                        usedModelImports.add(paramModelImport);
                    }
                }
            }
        }

        // Fix imports: OpenAPI Generator populates the imports list with Map objects
        // Extract the actual import paths from these maps and filter to only used models
        List<?> imports = (List<?>) result.get("imports");
        if (imports != null && !imports.isEmpty()) {
            List<String> fixedImports = new ArrayList<>();
            for (Object importObj : imports) {
                String importPath = null;
                if (importObj instanceof String) {
                    importPath = (String) importObj;
                } else if (importObj instanceof Map) {
                    Map<?, ?> importMap = (Map<?, ?>) importObj;
                    Object importPathObj = importMap.get("import");
                    if (importPathObj != null) {
                        importPath = importPathObj.toString();
                    }
                }

                // Only add valid package imports that:
                // 1. Don't reference primitive types
                // 2. Are actually used in operation signatures
                if (importPath != null &&
                    importPath.startsWith("package:") &&
                    !isPrimitiveTypeImport(importPath) &&
                    usedModelImports.contains(importPath)) {
                    fixedImports.add(importPath);
                }
            }
            result.put("imports", fixedImports);
        }

        // Second pass: process operations for multipart and other special handling
        for (CodegenOperation operation : ops) {
            // Convert HTTP method to lowercase for Dio method calls (GET -> get, POST -> post, etc.)
            if (operation.httpMethod != null) {
                operation.httpMethod = operation.httpMethod.toLowerCase();
            }

            // Check if this operation has multipart/form-data content
            boolean isMultipartOperation = operation.hasConsumes && operation.consumes != null &&
                    operation.consumes.stream().anyMatch(consume -> {
                        Object mediaType = consume.get(MEDIA_TYPE_KEY);
                        return mediaType instanceof String && CONTENT_TYPE_MULTIPART_FORM_DATA.equals(mediaType);
                    });

            if (isMultipartOperation) {
                // Mark the operation
                operation.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART, true);

                // Add MultipartFile import for this operation
                operation.imports.add(DART_IMPORT_DIO);

                // Process all parameters to change binary types to MultipartFile
                if (operation.allParams != null) {
                    for (CodegenParameter param : operation.allParams) {
                        // Check if this is a binary parameter (List<int> indicates binary)
                        if (param.isBinary || DART_TYPE_LIST_INT.equals(param.dataType)) {
                            // Change to MultipartFile for multipart context
                            param.dataType = DART_TYPE_MULTIPART_FILE;
                            param.datatypeWithEnum = DART_TYPE_MULTIPART_FILE;
                            param.baseType = DART_TYPE_MULTIPART_FILE;
                            param.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
                            param.vendorExtensions.put(VENDOR_EXTENSION_DART_IMPORT, DART_IMPORT_DIO);

                            // Update in all parameter lists
                            updateParameterInLists(operation, param);
                        }
                    }
                }
            }

            // Scan all parameters for any that require special imports
            if (operation.allParams != null) {
                for (CodegenParameter param : operation.allParams) {
                    if (param.vendorExtensions.containsKey(VENDOR_EXTENSION_DART_IMPORT)) {
                        String dartImport = (String) param.vendorExtensions.get(VENDOR_EXTENSION_DART_IMPORT);
                        if (dartImport != null && !dartImport.isEmpty()) {
                            operation.imports.add(dartImport);
                        }
                    }
                }
            }

            // Fix array return types: Ensure List<T> has generic parameter
            if (operation.returnType != null && operation.isArray && operation.returnBaseType != null) {
                // Only fix if returnType is missing the generic parameter
                if ("List".equals(operation.returnType)) {
                    operation.returnType = "List<" + operation.returnBaseType + ">";
                }
                // Ensure isListContainer is set for template
                operation.vendorExtensions.put("isListContainer", true);
            }
        }

        return result;
    }

    /**
     * Synchronizes parameter type information across all parameter lists (allParams, bodyParams, formParams).
     * Required because OpenAPI Generator creates separate instances for each list.
     *
     * @param operation the operation containing the parameter lists
     * @param param the parameter with updated type information to propagate
     */
    private void updateParameterInLists(CodegenOperation operation, CodegenParameter param) {
        // Update in bodyParams if present
        if (operation.bodyParams != null) {
            for (CodegenParameter p : operation.bodyParams) {
                if (p.paramName.equals(param.paramName)) {
                    p.dataType = param.dataType;
                    p.datatypeWithEnum = param.datatypeWithEnum;
                    p.baseType = param.baseType;
                }
            }
        }

        // Update in formParams if present
        if (operation.formParams != null) {
            for (CodegenParameter p : operation.formParams) {
                if (p.paramName.equals(param.paramName)) {
                    p.dataType = param.dataType;
                    p.datatypeWithEnum = param.datatypeWithEnum;
                    p.baseType = param.baseType;
                }
            }
        }
    }

    /**
     * Converts a type name to its corresponding model import path.
     * Filters out primitive types and language-specific primitives.
     *
     * @param typeName the type name (e.g., "Pet", "List", "int")
     * @return the model import path, or null if not a model type
     */
    private String getModelImportFromType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }

        // Skip language-specific primitives
        if (languageSpecificPrimitives.contains(typeName)) {
            return null;
        }

        // Skip special Dart types that aren't models
        if (typeName.equals("MultipartFile") || typeName.equals("List") ||
            typeName.equals("Map") || typeName.equals("void")) {
            return null;
        }

        // Skip types with generic parameters (e.g., "List<int>", "Map<String, dynamic>")
        if (typeName.contains("<") || typeName.contains(">")) {
            return null;
        }

        // Convert model name to import path
        return toModelImport(typeName);
    }

}
