package org.openapitools.codegen.languages;

import org.openapitools.codegen.*;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationsMap;
import org.openapitools.codegen.model.OperationMap;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Operation;

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
     * ThreadLocal to track whether we're currently processing a multipart/form-data request body.
     * This allows context-aware type mapping for file/binary types.
     */
    private static final ThreadLocal<Boolean> IS_MULTIPART_CONTEXT = ThreadLocal.withInitial(() -> false);

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
            pubName = "openapi_client";
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
        String result = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                            .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                            .toLowerCase();

        return result;
    }

    /**
     * Overrides fromModel to properly handle standalone enum schemas.
     * Ensures that schemas with enum values are properly processed as enums
     * with populated allowableValues and enumVars for template rendering.
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

        return model;
    }

    /**
     * Post-processes models to ensure enum data is properly structured for templates.
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
        }

        return result;
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

            Map<String, Object> enumVar = new HashMap<>();
            enumVar.put("name", finalName);
            enumVar.put("value", valueStr);
            enumVar.put("isString", "string".equalsIgnoreCase(datatype));
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
        return content.containsKey("multipart/form-data");
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
                parameter.vendorExtensions.put("x-is-multipart-context", true);

                // If this parameter itself is a file/binary type, mark it specifically
                if (parameter.isBinary || "file".equals(parameter.baseType)) {
                    parameter.vendorExtensions.put("x-is-multipart-file", true);
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
            return "List<int>";
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
            property.dataType = "MultipartFile";
            property.datatypeWithEnum = "MultipartFile";
            property.baseType = "MultipartFile";
            property.isBinary = true;

            // Mark for template usage
            property.vendorExtensions.put("x-is-multipart-file", true);
            property.vendorExtensions.put("x-dart-import", "package:dio/dio.dart");
        }
        // else: non-multipart context - keep the default List<int> from typeMapping

        return property;
    }

    /**
     * Post-processes operations to apply context-aware type mapping for file parameters.
     *
     * For operations with multipart/form-data request bodies, this method changes
     * binary parameters from List<int> to MultipartFile.
     *
     * @param objs the operations map
     * @return the processed operations map
     */
    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        OperationsMap result = super.postProcessOperationsWithModels(objs, allModels);

        // Get the operations list
        OperationMap operations = result.getOperations();
        List<CodegenOperation> ops = operations.getOperation();

        for (CodegenOperation operation : ops) {
            // Check if this operation has multipart/form-data content
            boolean isMultipartOperation = operation.hasConsumes && operation.consumes != null &&
                    operation.consumes.stream().anyMatch(consume ->
                        "multipart/form-data".equals(consume.get("mediaType"))
                    );

            if (isMultipartOperation) {
                // Mark the operation
                operation.vendorExtensions.put("x-is-multipart", true);

                // Process all parameters to change binary types to MultipartFile
                if (operation.allParams != null) {
                    for (CodegenParameter param : operation.allParams) {
                        // Check if this is a binary parameter (List<int> indicates binary)
                        if (param.isBinary || "List<int>".equals(param.dataType)) {
                            // Change to MultipartFile for multipart context
                            param.dataType = "MultipartFile";
                            param.datatypeWithEnum = "MultipartFile";
                            param.baseType = "MultipartFile";
                            param.vendorExtensions.put("x-is-multipart-file", true);

                            // Update in all parameter lists
                            updateParameterInLists(operation, param);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Helper method to update a parameter in all parameter lists of an operation.
     *
     * @param operation the operation
     * @param param the parameter to update (by reference)
     */
    private void updateParameterInLists(CodegenOperation operation, CodegenParameter param) {
        // The parameter object is already updated by reference, but we need to ensure
        // consistency across all lists (bodyParams, formParams, etc.)

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

}
