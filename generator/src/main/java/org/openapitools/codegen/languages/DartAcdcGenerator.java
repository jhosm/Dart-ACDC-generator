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
 * Generates Dart API clients with full Dart-ACDC integration (Authentication,
 * Caching, Debugging, Client).
 *
 * @see <a href="https://github.com/jhosm/Dart-ACDC">Dart-ACDC Library</a>
 */
public class DartAcdcGenerator extends DefaultCodegen implements CodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartAcdcGenerator.class);

    // Constants for content types and media
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
    private static final String RESERVED_WORD_MODEL_SUFFIX = "Model";

    /**
     * Central registry for schema storage and lookup.
     */
    private final DartSchemaRegistry schemaRegistry = new DartSchemaRegistry();

    /**
     * Helper for name sanitization and case conversion.
     */
    private final DartNameSanitizer nameSanitizer = new DartNameSanitizer();

    /**
     * Helper for type mapping and multipart context management.
     */
    private final DartTypeMapper typeMapper = new DartTypeMapper();

    /**
     * Helper for test data generation.
     * Initialized lazily after schemaRegistry and languageSpecificPrimitives are populated.
     */
    private DartTestDataGenerator testDataGenerator;

    /**
     * Helper for enum handling.
     * Initialized lazily after reserved words are set.
     */
    private DartEnumHandler enumHandler;

    /**
     * Configuration manager for CLI options and generator settings.
     */
    private final DartGeneratorConfig config;

    /**
     * Helper for allOf composition flattening.
     */
    private final DartAllOfFlattener allOfFlattener = new DartAllOfFlattener();

    /**
     * Helper for circular reference detection.
     */
    private final DartCircularReferenceDetector circularReferenceDetector = new DartCircularReferenceDetector();

    /**
     * Coordinator for schema preprocessing (allOf flattening and circular reference detection).
     */
    private final DartSchemaPreprocessor schemaPreprocessor;

    /**
     * Factory for creating and configuring CodegenModel instances.
     * Initialized lazily after discriminatorProcessor is available.
     */
    private DartModelFactory modelFactory;

    /**
     * Enricher for composition alternative metadata.
     * Initialized lazily after discriminatorProcessor and testDataGenerator are available.
     */
    private DartModelEnricher modelEnricher;

    /**
     * Processor for model post-processing (imports, enums, cleanup).
     * Initialized lazily after enumHandler is available.
     */
    private DartModelPostProcessor modelPostProcessor;

    /**
     * Resolver for final import resolution and deduplication.
     * Initialized lazily.
     */
    private DartModelImportResolver modelImportResolver;

    /**
     * Processor for oneOf composition handling.
     * Initialized lazily after modelEnricher is available.
     */
    private DartOneOfProcessor oneOfProcessor;

    /**
     * Processor for anyOf composition handling.
     * Initialized lazily after modelEnricher is available.
     */
    private DartAnyOfProcessor anyOfProcessor;

    /**
     * Processor for discriminator and sealed class handling.
     * Initialized lazily after testDataGenerator is available.
     */
    private DartDiscriminatorProcessor discriminatorProcessor;

    /**
     * Factory for creating request body parameters.
     * Initialized lazily.
     */
    private DartRequestBodyFactory requestBodyFactory;

    /**
     * Factory for creating properties from schemas.
     * Initialized lazily.
     */
    private DartPropertyFactory propertyFactory;

    /**
     * Resolver for type declarations with context awareness.
     * Initialized eagerly.
     */
    private DartTypeResolver typeResolver;

    /**
     * Resolver for operation-level import filtering.
     * Initialized lazily after languageSpecificPrimitives are populated.
     */
    private DartOperationImportResolver operationImportResolver;

    /**
     * Enricher for adding test metadata to operations.
     * Initialized lazily after testDataGenerator is available.
     */
    private DartOperationEnricher operationEnricher;

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
            "while", "with", "yield",
            // ACDC-specific types to prevent naming collisions
            "AcdcConfig", "AuthConfig", "CacheConfig", "LogConfig",
            "OfflineConfig", "SecurityConfig", "CertificatePinningConfig",
            "AcdcClientBuilder", "AcdcException", "AcdcAuthException",
            "AcdcNetworkException", "AcdcServerException", "AcdcSecurityException",
            "TokenProvider", "AcdcLogDelegate", "LogLevel"));

    /**
     * Constructor - configures the generator with Dart-ACDC specific settings.
     */
    public DartAcdcGenerator() {
        super();

        // Set reserved words for the generator
        reservedWords.addAll(DART_RESERVED_WORDS);

        // Initialize configuration manager and register CLI options
        config = new DartGeneratorConfig(nameSanitizer);
        cliOptions.addAll(config.registerCliOptions());

        // Initialize schema preprocessor with its dependencies
        schemaPreprocessor = new DartSchemaPreprocessor(schemaRegistry, allOfFlattener, circularReferenceDetector);

        // Initialize type resolver
        typeResolver = new DartTypeResolver(typeMapper);

        // Basic configuration
        outputFolder = "generated-code/dart-acdc";
        modelTemplateFiles.put("model.mustache", ".dart");
        modelTestTemplateFiles.put("test/model_test.mustache", "_test.dart");
        apiTemplateFiles.put("remote_data_source.mustache", "_remote_data_source.dart");
        apiTemplateFiles.put("remote_data_source_impl.mustache", "_remote_data_source_impl.dart");
        apiTestTemplateFiles.put("test/api_test.mustache", "_test.dart");
        embeddedTemplateDir = templateDir = "dart-acdc";

        // Enable enum generation as separate models
        setLegacyDiscriminatorBehavior(false);

        // Package configuration following Flutter/Dart conventions
        apiPackage = "lib.remote_data_sources";
        modelPackage = "lib.models";

        // Supporting files
        // Note: dart_acdc imports are handled directly in templates:
        // - api_client.mustache imports 'package:dart_acdc/dart_acdc.dart' as acdc
        // - remote_data_source*.mustache import 'package:dart_acdc/dart_acdc.dart'
        // - *config.mustache files import dart_acdc for types like LogLevel, TokenProvider
        supportingFiles.add(new SupportingFile("pubspec.mustache", "", "pubspec.yaml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("analysis_options.mustache", "", "analysis_options.yaml"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("api_client.mustache", "lib", "api_client.dart"));

        // Barrel export files
        supportingFiles.add(new SupportingFile("models.mustache", "lib/models", "models.dart"));
        supportingFiles.add(new SupportingFile("remote_data_sources.mustache", "lib/remote_data_sources",
                "remote_data_sources.dart"));
        // library.mustache barrel file is registered in processOpts() where pubName is resolved

        // Config supporting files
        supportingFiles.add(new SupportingFile("config.mustache", "lib/config", "config.dart"));
        supportingFiles.add(new SupportingFile("acdc_config.mustache", "lib/config", "acdc_config.dart"));
        supportingFiles.add(new SupportingFile("auth_config.mustache", "lib/config", "auth_config.dart"));
        supportingFiles.add(new SupportingFile("cache_config.mustache", "lib/config", "cache_config.dart"));
        supportingFiles.add(new SupportingFile("log_config.mustache", "lib/config", "log_config.dart"));
        supportingFiles.add(new SupportingFile("offline_config.mustache", "lib/config", "offline_config.dart"));
        supportingFiles.add(new SupportingFile("security_config.mustache", "lib/config", "security_config.dart"));

        // Test supporting files
        supportingFiles.add(new SupportingFile("test/test_helpers.mustache", "test", "test_helpers.dart"));

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
                "Map",
                "Set",
                "dynamic"));

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
     * Returns the folder where API test files are generated.
     * Places test files in the standard Dart test/ directory.
     *
     * @return path to the test directory
     */
    @Override
    public String apiTestFileFolder() {
        return outputFolder + "/test";
    }

    /**
     * Returns the folder where model test files are generated.
     * Places test files in the standard Dart test/ directory.
     *
     * @return path to the test directory
     */
    @Override
    public String modelTestFileFolder() {
        return outputFolder + "/test";
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
        // Delegate to DartNameSanitizer
        return nameSanitizer.escapeReservedWord(name);
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

        // If the sanitized name is a Dart reserved keyword (case-insensitive check),
        // suffix with "Model"
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
        // Get the pubName from config (after processOpts) or additionalProperties (during tests)
        String pubName = config.getPubName();

        // Fallback to additionalProperties if config hasn't been processed yet (e.g., in tests)
        if (pubName == null || pubName.equals(DEFAULT_PACKAGE_NAME)) {
            Object pubNameProp = additionalProperties.get("pubName");
            if (pubNameProp instanceof String && !((String) pubNameProp).isEmpty()) {
                pubName = (String) pubNameProp;
            }
        }

        // Convert model name to filename
        String filename = toModelFilename(name);

        // Generate Dart package import path
        // Format: package:{pubName}/models/{filename}.dart
        return "package:" + pubName + "/models/" + filename + ".dart";
    }

    /**
     * Sanitizes a package name to follow Dart pub package naming conventions.
     * Delegates to DartNameSanitizer.
     *
     * @param name the package name to sanitize
     * @return the sanitized package name following Dart conventions
     */
    protected String sanitizePubName(String name) {
        return nameSanitizer.sanitizePubName(name);
    }

    /**
     * Processes additional properties and applies sanitization where needed.
     * Also derives smart defaults from the OpenAPI specification (info.title, info.version, info.description).
     */
    @Override
    public void processOpts() {
        super.processOpts();

        // Process all configuration options using the config manager
        config.processOptions(additionalProperties, openAPI);
        config.applyToAdditionalProperties(additionalProperties);

        // Register the main barrel export file now that pubName is resolved
        String sanitizedPubName = config.getPubName();
        supportingFiles.add(new SupportingFile("library.mustache", "lib", sanitizedPubName + ".dart"));
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
     * @param value    the original enum value
     * @param datatype the data type (e.g., String, int)
     * @return the sanitized enum identifier
     */
    @Override
    public String toEnumVarName(String value, String datatype) {
        return getEnumHandler().toEnumVarName(value, datatype);
    }

    /**
     * Converts a string from PascalCase/camelCase to snake_case.
     * Used for generating Dart file names from model class names.
     *
     * @param name the name in PascalCase or camelCase
     * @return the name in snake_case
     */
    private String underscore(String name) {
        // Delegate to DartNameSanitizer
        return nameSanitizer.toSnakeCase(name);
    }

    /**
     * Converts a string to camelCase by lowercasing the first letter.
     *
     * @param str the string to convert
     * @return camelCase version
     */
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Preprocesses the OpenAPI specification to flatten allOf compositions and
     * detect circular references.
     * This runs before model generation to optimize the schema structure for code
     * generation.
     *
     * @param openAPI the OpenAPI specification
     */
    @Override
    public void preprocessOpenAPI(io.swagger.v3.oas.models.OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        schemaPreprocessor.preprocess(openAPI);
    }


    /**
     * Overrides fromModel to properly handle standalone enum schemas and
     * composition schemas (oneOf/anyOf).
     * allOf composition is now handled in preprocessOpenAPI() which runs before
     * model generation.
     *
     * Delegates to DartModelFactory for coordinated model creation.
     *
     * For enum schemas:
     * - Ensures that schemas with enum values are properly processed as enums
     * with populated allowableValues and enumVars for template rendering.
     *
     * For oneOf/anyOf schemas:
     * - Detects composition and marks the model appropriately
     * - Processes alternatives and discriminator information
     * - Adds metadata for sealed class generation in templates
     *
     * @param name   the model name
     * @param schema the schema
     * @return the codegen model
     */
    @Override
    public CodegenModel fromModel(String name, Schema schema) {
        return getModelFactory().createModel(name, schema);
    }

    /**
     * Creates a base CodegenModel using the parent generator's fromModel.
     * This is a delegation point for DartModelFactory.
     *
     * @param name   the model name
     * @param schema the schema
     * @return the base CodegenModel
     */
    @SuppressWarnings("rawtypes")
    CodegenModel createBaseModel(String name, Schema schema) {
        return super.fromModel(name, schema);
    }

    /**
     * Processes oneOf composition for a model.
     * Delegation point for DartModelFactory.
     *
     * @param name   the schema name
     * @param schema the schema with oneOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings("rawtypes")
    void processOneOfCompositionForFactory(String name, Schema schema, CodegenModel model) {
        processOneOfComposition(name, schema, model);
    }

    /**
     * Processes anyOf composition for a model.
     * Delegation point for DartModelFactory.
     *
     * @param name   the schema name
     * @param schema the schema with anyOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings("rawtypes")
    void processAnyOfCompositionForFactory(String name, Schema schema, CodegenModel model) {
        processAnyOfComposition(name, schema, model);
    }

    /**
     * Processes a oneOf composition schema and adds metadata to the CodegenModel.
     * Delegates to DartOneOfProcessor.
     *
     * @param name   the schema name
     * @param schema the schema with oneOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processOneOfComposition(String name, Schema schema, CodegenModel model) {
        getOneOfProcessor().processOneOf(name, schema, model);
    }

    /**
     * Processes an anyOf composition schema and adds metadata to the CodegenModel.
     * Delegates to DartAnyOfProcessor.
     *
     * @param name   the schema name
     * @param schema the schema with anyOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processAnyOfComposition(String name, Schema schema, CodegenModel model) {
        getAnyOfProcessor().processAnyOf(name, schema, model);
    }


    /**
     * Post-processes models to ensure enum data is properly structured for templates.
     * Delegates to DartModelPostProcessor for actual processing.
     *
     * @param objs the models map containing all model data
     * @return the processed models map
     */
    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        ModelsMap result = super.postProcessModels(objs);
        return getModelPostProcessor().postProcess(result);
    }

    /**
     * Post-processes all models after individual model processing.
     * Delegates to DartModelImportResolver for final import cleanup.
     *
     * This is the final cleanup stage before template rendering. The base class
     * (DefaultCodegen.postProcessAllModels) adds simple-name imports based on
     * model references, which need to be filtered out.
     *
     * @param objs the map of all models
     * @return the processed models map with cleaned imports
     */
    @Override
    public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> objs) {
        Map<String, ModelsMap> result = super.postProcessAllModels(objs);
        return getModelImportResolver().resolveAllImports(result);
    }

    /**
     * OpenAPI primitive types (used for oneOf/anyOf composition logic).
     * These are the standard OpenAPI primitive types that can appear in schemas.
     */
    private static final Set<String> OPENAPI_PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean");

    /**
     * Overrides the base implementation to detect multipart/form-data context
     * and set ThreadLocal context for property processing.
     * Delegates to DartRequestBodyFactory.
     *
     * @param requestBody       the request body specification
     * @param imports           the imports set
     * @param bodyParameterName the body parameter name
     * @return the CodegenParameter with multipart context information
     */
    @Override
    public CodegenParameter fromRequestBody(RequestBody requestBody, Set<String> imports, String bodyParameterName) {
        return getRequestBodyFactory().createFromRequestBody(requestBody, imports, bodyParameterName);
    }

    /**
     * Delegation point for DartRequestBodyFactory to access parent's fromRequestBody.
     * Package-private for use by DartRequestBodyFactory only.
     *
     * @param requestBody       the request body specification
     * @param imports           the imports set
     * @param bodyParameterName the body parameter name
     * @return the CodegenParameter from parent generator
     */
    CodegenParameter superFromRequestBody(RequestBody requestBody, Set<String> imports, String bodyParameterName) {
        return super.fromRequestBody(requestBody, imports, bodyParameterName);
    }

    /**
     * Delegation point for DartPropertyFactory to access parent's fromProperty.
     * Package-private for use by DartPropertyFactory only.
     *
     * @param name                             the property name
     * @param schema                           the property schema
     * @param required                         whether the property is required
     * @param schemaIsFromAdditionalProperties whether this schema comes from additionalProperties
     * @return the CodegenProperty from parent generator
     */
    CodegenProperty superFromProperty(String name, Schema schema, boolean required, boolean schemaIsFromAdditionalProperties) {
        return super.fromProperty(name, schema, required, schemaIsFromAdditionalProperties);
    }

    /**
     * Overrides type declaration to provide context-aware mapping for file/binary
     * types. Delegates binary type checking to DartTypeResolver.
     *
     * In multipart/form-data context: binary/file → MultipartFile
     * In non-multipart context: binary/file → List&lt;int&gt;
     *
     * @param schema the schema
     * @return the Dart type declaration
     */
    @Override
    public String getTypeDeclaration(Schema schema) {
        if (schema == null) {
            return super.getTypeDeclaration(schema);
        }

        // Delegate binary type resolution to DartTypeResolver
        String binaryType = typeResolver.resolveBinaryType(schema);
        if (binaryType != null) {
            return binaryType;
        }

        return super.getTypeDeclaration(schema);
    }

    /**
     * Overrides fromProperty to apply context-aware type mapping for binary/file
     * properties. Delegates to DartPropertyFactory.
     *
     * Checks the ThreadLocal context to determine if we're in a multipart/form-data
     * request,
     * and maps binary/file types accordingly:
     * - Multipart context: type=string,format=binary → MultipartFile
     * - Non-multipart context: type=string,format=binary → List<int>
     *
     * Also detects composition schemas (oneOf/anyOf) and marks them for custom JSON
     * converter generation.
     *
     * @param name                             the property name
     * @param schema                           the property schema
     * @param required                         whether the property is required
     * @param schemaIsFromAdditionalProperties whether this schema comes from
     *                                         additionalProperties
     * @return the CodegenProperty with correct type based on context
     */
    @Override
    public CodegenProperty fromProperty(String name, Schema schema, boolean required,
            boolean schemaIsFromAdditionalProperties) {
        return getPropertyFactory().createFromProperty(name, schema, required, schemaIsFromAdditionalProperties);
    }

    /**
     * Gets or initializes the test data generator.
     * Lazily creates the generator with current modelSchemas and primitives.
     *
     * @return the test data generator instance
     */
    private DartTestDataGenerator getTestDataGenerator() {
        if (testDataGenerator == null) {
            testDataGenerator = new DartTestDataGenerator(schemaRegistry.getAllSchemas(), languageSpecificPrimitives);
        }
        return testDataGenerator;
    }

    /**
     * Gets or initializes the enum handler.
     * Lazily creates the handler with current reserved words.
     *
     * @return the enum handler instance
     */
    private DartEnumHandler getEnumHandler() {
        if (enumHandler == null) {
            enumHandler = new DartEnumHandler(reservedWords);
        }
        return enumHandler;
    }

    /**
     * Gets or initializes the model enricher.
     * Lazily creates the enricher with required dependencies.
     *
     * @return the model enricher instance
     */
    private DartModelEnricher getModelEnricher() {
        if (modelEnricher == null) {
            modelEnricher = new DartModelEnricher(this, getTestDataGenerator(), getDiscriminatorProcessor());
        }
        return modelEnricher;
    }

    /**
     * Gets or initializes the model post-processor.
     * Lazily creates the processor with required dependencies.
     *
     * @return the model post-processor instance
     */
    private DartModelPostProcessor getModelPostProcessor() {
        if (modelPostProcessor == null) {
            modelPostProcessor = new DartModelPostProcessor(this, getEnumHandler());
        }
        return modelPostProcessor;
    }

    /**
     * Gets or initializes the model import resolver.
     * Lazily creates the resolver instance.
     *
     * @return the model import resolver instance
     */
    private DartModelImportResolver getModelImportResolver() {
        if (modelImportResolver == null) {
            modelImportResolver = new DartModelImportResolver();
        }
        return modelImportResolver;
    }

    /**
     * Gets or initializes the oneOf processor.
     * Lazily creates the processor with required dependencies.
     *
     * @return the oneOf processor instance
     */
    private DartOneOfProcessor getOneOfProcessor() {
        if (oneOfProcessor == null) {
            oneOfProcessor = new DartOneOfProcessor(getDiscriminatorProcessor(), getModelEnricher());
        }
        return oneOfProcessor;
    }

    /**
     * Gets or initializes the anyOf processor.
     * Lazily creates the processor with required dependencies.
     *
     * @return the anyOf processor instance
     */
    private DartAnyOfProcessor getAnyOfProcessor() {
        if (anyOfProcessor == null) {
            anyOfProcessor = new DartAnyOfProcessor(getDiscriminatorProcessor(), getModelEnricher());
        }
        return anyOfProcessor;
    }

    /**
     * Gets or initializes the discriminator processor.
     * Lazily creates the processor with required dependencies.
     *
     * @return the discriminator processor instance
     */
    private DartDiscriminatorProcessor getDiscriminatorProcessor() {
        if (discriminatorProcessor == null) {
            discriminatorProcessor = new DartDiscriminatorProcessor(this, getTestDataGenerator());
        }
        return discriminatorProcessor;
    }

    /**
     * Gets or initializes the model factory.
     * Lazily creates the factory with required dependencies.
     *
     * @return the model factory instance
     */
    private DartModelFactory getModelFactory() {
        if (modelFactory == null) {
            modelFactory = new DartModelFactory(this, getDiscriminatorProcessor());
        }
        return modelFactory;
    }

    /**
     * Gets or initializes the request body factory.
     * Lazily creates the factory with required dependencies.
     *
     * @return the request body factory instance
     */
    private DartRequestBodyFactory getRequestBodyFactory() {
        if (requestBodyFactory == null) {
            requestBodyFactory = new DartRequestBodyFactory(this, typeMapper);
        }
        return requestBodyFactory;
    }

    /**
     * Gets or initializes the property factory.
     * Lazily creates the factory with required dependencies.
     *
     * @return the property factory instance
     */
    private DartPropertyFactory getPropertyFactory() {
        if (propertyFactory == null) {
            propertyFactory = new DartPropertyFactory(this, typeMapper, getDiscriminatorProcessor());
        }
        return propertyFactory;
    }

    /**
     * Gets or initializes the operation import resolver.
     * Lazily creates the resolver with required dependencies.
     *
     * @return the operation import resolver instance
     */
    private DartOperationImportResolver getOperationImportResolver() {
        if (operationImportResolver == null) {
            operationImportResolver = new DartOperationImportResolver(this, languageSpecificPrimitives);
        }
        return operationImportResolver;
    }

    /**
     * Gets or initializes the operation enricher.
     * Lazily creates the enricher with required dependencies.
     *
     * @return the operation enricher instance
     */
    private DartOperationEnricher getOperationEnricher() {
        if (operationEnricher == null) {
            operationEnricher = new DartOperationEnricher(getTestDataGenerator(), languageSpecificPrimitives);
        }
        return operationEnricher;
    }

    /**
     * Post-processes operations to apply context-aware type mapping for file
     * parameters.
     * Also ensures that parameters requiring special imports (like MultipartFile)
     * have
     * their imports properly tracked.
     *
     * For operations with multipart/form-data request bodies, this method changes
     * binary parameters from List<int> to MultipartFile and adds the necessary
     * imports.
     *
     * This method also filters imports to only include models actually used in
     * operation
     * signatures (parameters, return types) to avoid unused imports.
     *
     * Additionally, adds test metadata to operations and parameters for test
     * template generation.
     *
     * @param objs      the operations map
     * @param allModels all models for cross-referencing
     * @return the processed operations map
     */
    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        OperationsMap result = super.postProcessOperationsWithModels(objs, allModels);

        // Get the operations list to track which models are actually used
        OperationMap operations = result.getOperations();
        List<CodegenOperation> ops = operations.getOperation();

        // Resolve and filter imports to only include models used in operation signatures
        getOperationImportResolver().resolveImports(result, ops);

        // Process operations for type fixing, multipart handling, and HTTP method normalization
        for (CodegenOperation operation : ops) {
            // Convert HTTP method to lowercase for Dio method calls (GET -> get, POST -> post)
            if (operation.httpMethod != null) {
                operation.httpMethod = operation.httpMethod.toLowerCase();
            }

            // Fix returnType and returnBaseType to use proper Dart PascalCase class names.
            // OpenAPI Generator may set these to raw schema names (e.g., "ping_200_response")
            // but Dart classes are generated with PascalCase (e.g., "Ping200Response").
            if (operation.returnType != null && !operation.returnType.isEmpty()) {
                if (!languageSpecificPrimitives.contains(operation.returnType) &&
                        !operation.returnType.equals("void") &&
                        !operation.returnType.contains("<") &&
                        !operation.returnType.contains(">")) {
                    String fixedReturnType = toModelName(operation.returnType);
                    if (!fixedReturnType.equals(operation.returnType)) {
                        LOGGER.info("Fixed returnType from '{}' to '{}' for operation {}",
                                operation.returnType, fixedReturnType, operation.operationId);
                        operation.returnType = fixedReturnType;
                    }
                }
            }
            if (operation.returnBaseType != null && !operation.returnBaseType.isEmpty()) {
                if (!languageSpecificPrimitives.contains(operation.returnBaseType) &&
                        !operation.returnBaseType.equals("void") &&
                        !operation.returnBaseType.contains("<") &&
                        !operation.returnBaseType.contains(">")) {
                    String fixedBaseType = toModelName(operation.returnBaseType);
                    if (!fixedBaseType.equals(operation.returnBaseType)) {
                        LOGGER.info("Fixed returnBaseType from '{}' to '{}' for operation {}",
                                operation.returnBaseType, fixedBaseType, operation.operationId);
                        operation.returnBaseType = fixedBaseType;
                    }
                }
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

        // Enrich operations with test metadata AFTER all type conversions are complete
        // This ensures test values match final parameter types (e.g., MultipartFile not List<int>)
        getOperationEnricher().enrichOperations(ops);

        return result;
    }

    /**
     * Synchronizes parameter type information across all parameter lists
     * (allParams, bodyParams, formParams).
     * Required because OpenAPI Generator creates separate instances for each list.
     *
     * @param operation the operation containing the parameter lists
     * @param param     the parameter with updated type information to propagate
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

}
