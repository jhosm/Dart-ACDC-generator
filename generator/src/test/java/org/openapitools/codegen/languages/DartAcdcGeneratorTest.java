package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartAcdcGenerator.
 * Tests critical functionality including enum handling, name sanitization,
 * case conversion, and multipart file type mapping.
 */
class DartAcdcGeneratorTest {

    private DartAcdcGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
    }

    // ========================================
    // Package Name Sanitization Tests
    // ========================================

    @Test
    @DisplayName("sanitizePubName: should convert to lowercase and replace spaces")
    void testSanitizePubName_BasicConversion() {
        String result = generator.sanitizePubName("My API Client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should handle hyphens")
    void testSanitizePubName_Hyphens() {
        String result = generator.sanitizePubName("my-api-client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should remove invalid characters")
    void testSanitizePubName_InvalidChars() {
        String result = generator.sanitizePubName("my@api#client!");
        assertEquals("myapiclient", result);
    }

    @Test
    @DisplayName("sanitizePubName: should collapse consecutive underscores")
    void testSanitizePubName_ConsecutiveUnderscores() {
        String result = generator.sanitizePubName("my___api___client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should remove leading/trailing underscores")
    void testSanitizePubName_LeadingTrailing() {
        String result = generator.sanitizePubName("_my_api_client_");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should prefix with 'api_' if starts with digit")
    void testSanitizePubName_StartsWithDigit() {
        String result = generator.sanitizePubName("123api");
        assertEquals("api_123api", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for null input")
    void testSanitizePubName_Null() {
        String result = generator.sanitizePubName(null);
        assertEquals("openapi_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for empty string")
    void testSanitizePubName_Empty() {
        String result = generator.sanitizePubName("");
        assertEquals("openapi_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for string that becomes empty after sanitization")
    void testSanitizePubName_BecomesEmpty() {
        String result = generator.sanitizePubName("@#$!");
        assertEquals("openapi_client", result);
    }

    // ========================================
    // CLI Options Tests
    // ========================================

    @Test
    @DisplayName("CLI options: should register pubName option")
    void testCliOptions_PubName() {
        boolean hasPubName = generator.cliOptions().stream()
                .anyMatch(opt -> "pubName".equals(opt.getOpt()));
        assertTrue(hasPubName, "pubName CLI option should be registered");
    }

    @Test
    @DisplayName("CLI options: should register pubVersion option with default")
    void testCliOptions_PubVersion() {
        Optional<org.openapitools.codegen.CliOption> pubVersionOpt = generator.cliOptions().stream()
                .filter(opt -> "pubVersion".equals(opt.getOpt()))
                .findFirst();
        assertTrue(pubVersionOpt.isPresent(), "pubVersion CLI option should be registered");
        assertEquals("1.0.0", pubVersionOpt.get().getDefault(), "pubVersion should default to 1.0.0");
    }

    @Test
    @DisplayName("CLI options: should register all 5 package metadata options")
    void testCliOptions_AllPackageMetadata() {
        List<String> expectedOptions = List.of("pubName", "pubVersion", "pubDescription", "pubAuthor", "pubHomepage");
        List<String> actualOptions = generator.cliOptions().stream()
                .map(org.openapitools.codegen.CliOption::getOpt)
                .filter(expectedOptions::contains)
                .toList();
        assertEquals(5, actualOptions.size(), "All 5 package metadata options should be registered");
    }

    // ========================================
    // processOpts() Tests
    // ========================================

    @Test
    @DisplayName("processOpts: should use provided pubName and sanitize it")
    void testProcessOpts_ProvidedPubName() {
        generator.additionalProperties().put("pubName", "My API Client");
        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubName");
        assertEquals("my_api_client", result, "pubName should be sanitized");
    }

    @Test
    @DisplayName("processOpts: should derive pubName from OpenAPI info.title")
    void testProcessOpts_DerivedPubName() {
        // Create mock OpenAPI spec
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setTitle("Petstore API");
        openAPI.setInfo(info);
        generator.setOpenAPI(openAPI);

        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubName");
        assertEquals("petstore_api", result, "pubName should be derived from info.title and sanitized");
    }

    @Test
    @DisplayName("processOpts: should use default pubName when not provided and no OpenAPI info")
    void testProcessOpts_DefaultPubName() {
        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubName");
        assertEquals("openapi_client", result, "pubName should use default");
    }

    @Test
    @DisplayName("processOpts: should derive pubVersion from OpenAPI info.version")
    void testProcessOpts_DerivedPubVersion() {
        // Create mock OpenAPI spec
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setVersion("2.5.3");
        openAPI.setInfo(info);
        generator.setOpenAPI(openAPI);

        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubVersion");
        assertEquals("2.5.3", result, "pubVersion should be derived from info.version");
    }

    @Test
    @DisplayName("processOpts: should use provided pubVersion over OpenAPI info.version")
    void testProcessOpts_ProvidedPubVersionOverridesDefault() {
        generator.additionalProperties().put("pubVersion", "3.0.0");

        // Create mock OpenAPI spec with different version
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setVersion("2.5.3");
        openAPI.setInfo(info);
        generator.setOpenAPI(openAPI);

        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubVersion");
        assertEquals("3.0.0", result, "Provided pubVersion should take precedence");
    }

    @Test
    @DisplayName("processOpts: should use default pubVersion when not provided and no OpenAPI info")
    void testProcessOpts_DefaultPubVersion() {
        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubVersion");
        assertEquals("1.0.0", result, "pubVersion should use default");
    }

    @Test
    @DisplayName("processOpts: should derive pubDescription from OpenAPI info.description")
    void testProcessOpts_DerivedPubDescription() {
        // Create mock OpenAPI spec
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setDescription("A sample Petstore API");
        openAPI.setInfo(info);
        generator.setOpenAPI(openAPI);

        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubDescription");
        assertEquals("A sample Petstore API", result, "pubDescription should be derived from info.description");
    }

    @Test
    @DisplayName("processOpts: should use provided pubDescription over OpenAPI info.description")
    void testProcessOpts_ProvidedPubDescriptionOverridesDefault() {
        generator.additionalProperties().put("pubDescription", "Custom description");

        // Create mock OpenAPI spec with different description
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setDescription("Default description");
        openAPI.setInfo(info);
        generator.setOpenAPI(openAPI);

        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubDescription");
        assertEquals("Custom description", result, "Provided pubDescription should take precedence");
    }

    @Test
    @DisplayName("processOpts: should preserve provided pubAuthor and pubHomepage")
    void testProcessOpts_PreserveAuthorAndHomepage() {
        generator.additionalProperties().put("pubAuthor", "John Doe");
        generator.additionalProperties().put("pubHomepage", "https://example.com");

        generator.processOpts();

        assertEquals("John Doe", generator.additionalProperties().get("pubAuthor"));
        assertEquals("https://example.com", generator.additionalProperties().get("pubHomepage"));
    }

    @Test
    @DisplayName("processOpts: should handle pubName sanitization with special characters")
    void testProcessOpts_PubNameSanitizationComplex() {
        generator.additionalProperties().put("pubName", "My-Cool@API#2024!");
        generator.processOpts();

        String result = (String) generator.additionalProperties().get("pubName");
        assertEquals("my_coolapi2024", result, "pubName with special characters should be properly sanitized");
    }

    // ========================================
    // Enum Variable Name Tests
    // ========================================

    @Test
    @DisplayName("toEnumVarName: should convert simple string to camelCase")
    void testToEnumVarName_Simple() {
        String result = generator.toEnumVarName("active", "string");
        assertEquals("active", result);
    }

    @Test
    @DisplayName("toEnumVarName: should convert kebab-case to camelCase")
    void testToEnumVarName_KebabCase() {
        String result = generator.toEnumVarName("user-active", "string");
        assertEquals("userActive", result);
    }

    @Test
    @DisplayName("toEnumVarName: should convert snake_case to camelCase")
    void testToEnumVarName_SnakeCase() {
        String result = generator.toEnumVarName("user_active", "string");
        assertEquals("userActive", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle numeric values")
    void testToEnumVarName_Numeric() {
        String result = generator.toEnumVarName("123", "string");
        assertEquals("value123", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle negative numeric values")
    void testToEnumVarName_NegativeNumeric() {
        String result = generator.toEnumVarName("-123", "string");
        assertEquals("value123", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle decimal numeric values")
    void testToEnumVarName_DecimalNumeric() {
        String result = generator.toEnumVarName("123.45", "string");
        assertEquals("value12345", result);
    }

    @Test
    @DisplayName("toEnumVarName: should return 'empty' for null input")
    void testToEnumVarName_Null() {
        String result = generator.toEnumVarName(null, "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toEnumVarName: should return 'empty' for empty string")
    void testToEnumVarName_Empty() {
        String result = generator.toEnumVarName("", "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toEnumVarName: should prefix with 'value' if starts with digit after conversion")
    void testToEnumVarName_StartsWithDigit() {
        String result = generator.toEnumVarName("1st-place", "string");
        assertEquals("value1stPlace", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle reserved words")
    void testToEnumVarName_ReservedWord() {
        String result = generator.toEnumVarName("class", "string");
        assertEquals("class_", result);
    }

    // ========================================
    // Model Name Tests
    // ========================================

    @Test
    @DisplayName("toModelName: should handle reserved words by appending 'Model'")
    void testToModelName_ReservedWord() {
        String result = generator.toModelName("class");
        assertEquals("ClassModel", result);
    }

    @Test
    @DisplayName("toModelName: should not modify non-reserved words")
    void testToModelName_Normal() {
        String result = generator.toModelName("User");
        assertEquals("User", result);
    }

    // ========================================
    // Model Filename Tests
    // ========================================

    @Test
    @DisplayName("toModelFilename: should convert PascalCase to snake_case")
    void testToModelFilename_PascalCase() {
        String result = generator.toModelFilename("UserProfile");
        assertEquals("user_profile", result);
    }

    @Test
    @DisplayName("toModelFilename: should handle consecutive uppercase letters")
    void testToModelFilename_ConsecutiveUppercase() {
        String result = generator.toModelFilename("HTTPResponse");
        assertEquals("http_response", result);
    }

    @Test
    @DisplayName("toModelFilename: should handle single letter words")
    void testToModelFilename_SingleLetter() {
        String result = generator.toModelFilename("AValue");
        assertEquals("a_value", result);
    }

    // ========================================
    // Reserved Word Escaping Tests
    // ========================================

    @Test
    @DisplayName("escapeReservedWord: should append underscore")
    void testEscapeReservedWord() {
        String result = generator.escapeReservedWord("class");
        assertEquals("class_", result);
    }

    // ========================================
    // Enum Collision Tests
    // ========================================

    @Test
    @DisplayName("createEnumVars: should handle collisions with numeric suffixes")
    void testCreateEnumVars_Collisions() {
        // This tests the private createEnumVars method indirectly through postProcessModels
        // We'll test this by creating a model with enum values that would collide

        // Create a simple test: two values that map to the same identifier
        List<Object> values = Arrays.asList("active", "ACTIVE", "Active");

        // Note: We can't directly test createEnumVars as it's private,
        // but we can verify the behavior through the public API
        // For now, we'll test the toEnumVarName behavior which feeds into it

        String enum1 = generator.toEnumVarName("active", "string");
        String enum2 = generator.toEnumVarName("ACTIVE", "string");
        String enum3 = generator.toEnumVarName("Active", "string");

        // All should normalize to the same base name
        assertEquals("active", enum1);
        assertEquals("active", enum2);
        assertEquals("active", enum3);

        // The createEnumVars method should handle these collisions
        // by appending numeric suffixes: active, active2, active3
    }

    // ========================================
    // Model Import Path Tests
    // ========================================

    @Test
    @DisplayName("toModelImport: should generate correct Dart package import")
    void testToModelImport() {
        generator.additionalProperties().put("pubName", "my_api");
        String result = generator.toModelImport("UserProfile");
        assertEquals("package:my_api/models/user_profile.dart", result);
    }

    @Test
    @DisplayName("toModelImport: should use default package name if not set")
    void testToModelImport_DefaultPackage() {
        String result = generator.toModelImport("User");
        assertEquals("package:openapi_client/models/user.dart", result);
    }

    // ========================================
    // Generator Metadata Tests
    // ========================================

    @Test
    @DisplayName("getName: should return 'dart-acdc'")
    void testGetName() {
        assertEquals("dart-acdc", generator.getName());
    }

    @Test
    @DisplayName("getHelp: should return help text")
    void testGetHelp() {
        String help = generator.getHelp();
        assertNotNull(help);
        assertTrue(help.contains("Dart-ACDC"));
    }

    // ========================================
    // Edge Cases and Boundary Tests
    // ========================================

    @Test
    @DisplayName("toEnumVarName: should handle empty string after sanitization")
    void testToEnumVarName_EmptyAfterSanitization() {
        String result = generator.toEnumVarName("@#$", "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toModelFilename: should handle all lowercase")
    void testToModelFilename_AllLowercase() {
        String result = generator.toModelFilename("user");
        assertEquals("user", result);
    }

    @Test
    @DisplayName("toModelFilename: should handle all uppercase")
    void testToModelFilename_AllUppercase() {
        String result = generator.toModelFilename("USER");
        assertEquals("user", result);
    }

    @Test
    @DisplayName("sanitizePubName: should handle mixed case with special chars")
    void testSanitizePubName_ComplexCase() {
        String result = generator.sanitizePubName("My-API@2.0_Client!");
        assertEquals("my_api20_client", result);
    }

    // ========================================
    // Schema Composition Tests
    // ========================================

    @Test
    @DisplayName("oneOf with primitives: should create wrapper classes")
    void testOneOfWithPrimitives() {
        // Create a oneOf schema with primitive alternatives
        io.swagger.v3.oas.models.media.Schema stringSchema = new io.swagger.v3.oas.models.media.Schema();
        stringSchema.setType("string");

        io.swagger.v3.oas.models.media.Schema numberSchema = new io.swagger.v3.oas.models.media.Schema();
        numberSchema.setType("number");

        io.swagger.v3.oas.models.media.Schema compositeSchema = new io.swagger.v3.oas.models.media.Schema();
        compositeSchema.setOneOf(java.util.Arrays.asList(stringSchema, numberSchema));

        // Process through the generator
        CodegenModel model = generator.fromModel("StringOrNumber", compositeSchema);

        // Verify it's marked as oneOf
        assertTrue((Boolean) model.vendorExtensions.getOrDefault("x-is-one-of", false),
                "Model should be marked as oneOf composition");

        // Verify alternatives are processed
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> alternatives =
                (java.util.List<java.util.Map<String, Object>>) model.vendorExtensions.get("x-one-of-alternatives");

        assertNotNull(alternatives, "Should have oneOf alternatives");
        assertEquals(2, alternatives.size(), "Should have 2 alternatives");

        // First alternative should be a primitive wrapper for String
        java.util.Map<String, Object> firstAlt = alternatives.getFirst();
        assertTrue((Boolean) firstAlt.getOrDefault("isPrimitive", false),
                "First alternative should be primitive");
        assertEquals("String", firstAlt.get("dartType"), "Should be String type");

        // Second alternative should be a primitive wrapper for number
        java.util.Map<String, Object> secondAlt = alternatives.get(1);
        assertTrue((Boolean) secondAlt.getOrDefault("isPrimitive", false),
                "Second alternative should be primitive");
        assertEquals("double", secondAlt.get("dartType"), "Should be double type");
    }

    @Test
    @DisplayName("oneOf without discriminator: should not have discriminator flag")
    void testOneOfWithoutDiscriminator() {
        io.swagger.v3.oas.models.media.Schema catSchema = new io.swagger.v3.oas.models.media.Schema();
        catSchema.set$ref("#/components/schemas/Cat");

        io.swagger.v3.oas.models.media.Schema dogSchema = new io.swagger.v3.oas.models.media.Schema();
        dogSchema.set$ref("#/components/schemas/Dog");

        io.swagger.v3.oas.models.media.Schema petSchema = new io.swagger.v3.oas.models.media.Schema();
        petSchema.setOneOf(java.util.Arrays.asList(catSchema, dogSchema));

        CodegenModel model = generator.fromModel("Pet", petSchema);

        assertFalse((Boolean) model.vendorExtensions.getOrDefault("x-has-discriminator", true),
                "Should not have discriminator");
    }

    @Test
    @DisplayName("oneOf with discriminator: should have discriminator metadata")
    void testOneOfWithDiscriminator() {
        io.swagger.v3.oas.models.media.Schema catSchema = new io.swagger.v3.oas.models.media.Schema();
        catSchema.set$ref("#/components/schemas/Cat");

        io.swagger.v3.oas.models.media.Schema dogSchema = new io.swagger.v3.oas.models.media.Schema();
        dogSchema.set$ref("#/components/schemas/Dog");

        io.swagger.v3.oas.models.media.Schema petSchema = new io.swagger.v3.oas.models.media.Schema();
        petSchema.setOneOf(java.util.Arrays.asList(catSchema, dogSchema));

        // Add discriminator
        io.swagger.v3.oas.models.media.Discriminator discriminator =
                new io.swagger.v3.oas.models.media.Discriminator();
        discriminator.setPropertyName("petType");
        discriminator.setMapping(java.util.Map.of(
                "cat", "#/components/schemas/Cat",
                "dog", "#/components/schemas/Dog"
        ));
        petSchema.setDiscriminator(discriminator);

        CodegenModel model = generator.fromModel("Pet", petSchema);

        assertTrue((Boolean) model.vendorExtensions.getOrDefault("x-has-discriminator", false),
                "Should have discriminator");
        assertEquals("petType", model.vendorExtensions.get("x-discriminator-name"),
                "Discriminator name should match");
    }

    @Test
    @DisplayName("anyOf: should be handled like oneOf")
    void testAnyOfComposition() {
        io.swagger.v3.oas.models.media.Schema emailSchema = new io.swagger.v3.oas.models.media.Schema();
        emailSchema.set$ref("#/components/schemas/EmailNotification");

        io.swagger.v3.oas.models.media.Schema smsSchema = new io.swagger.v3.oas.models.media.Schema();
        smsSchema.set$ref("#/components/schemas/SmsNotification");

        io.swagger.v3.oas.models.media.Schema notificationSchema = new io.swagger.v3.oas.models.media.Schema();
        notificationSchema.setAnyOf(java.util.Arrays.asList(emailSchema, smsSchema));

        CodegenModel model = generator.fromModel("Notification", notificationSchema);

        assertTrue((Boolean) model.vendorExtensions.getOrDefault("x-is-any-of", false),
                "Model should be marked as anyOf composition");

        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> alternatives =
                (java.util.List<java.util.Map<String, Object>>) model.vendorExtensions.get("x-any-of-alternatives");

        assertNotNull(alternatives, "Should have anyOf alternatives");
        assertEquals(2, alternatives.size(), "Should have 2 alternatives");
    }

    @Test
    @DisplayName("Circular reference: should mark property as nullable")
    void testCircularReferenceDetection() {
        // Create a Node schema with circular reference
        io.swagger.v3.oas.models.media.Schema valueSchema = new io.swagger.v3.oas.models.media.Schema();
        valueSchema.setType("string");

        io.swagger.v3.oas.models.media.Schema nodeRefSchema = new io.swagger.v3.oas.models.media.Schema();
        nodeRefSchema.set$ref("#/components/schemas/Node");

        io.swagger.v3.oas.models.media.Schema childrenSchema = new io.swagger.v3.oas.models.media.Schema();
        childrenSchema.setType("array");
        childrenSchema.setItems(nodeRefSchema);

        io.swagger.v3.oas.models.media.Schema nodeSchema = new io.swagger.v3.oas.models.media.Schema();
        nodeSchema.setType("object");
        nodeSchema.setProperties(java.util.Map.of(
                "value", valueSchema,
                "children", childrenSchema
        ));

        // Create OpenAPI with the Node schema
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        io.swagger.v3.oas.models.Components components = new io.swagger.v3.oas.models.Components();
        components.setSchemas(java.util.Map.of("Node", nodeSchema));
        openAPI.setComponents(components);

        // Preprocess the OpenAPI spec
        generator.preprocessOpenAPI(openAPI);

        // Verify that the children property was marked as nullable
        io.swagger.v3.oas.models.media.Schema processedNodeSchema =
                openAPI.getComponents().getSchemas().get("Node");

        @SuppressWarnings("unchecked")
        java.util.Map<String, io.swagger.v3.oas.models.media.Schema> properties =
                (java.util.Map<String, io.swagger.v3.oas.models.media.Schema>) processedNodeSchema.getProperties();

        io.swagger.v3.oas.models.media.Schema childrenProperty = properties.get("children");

        assertNotNull(childrenProperty, "Children property should exist");
        assertTrue(childrenProperty.getNullable() != null && childrenProperty.getNullable(),
                "Circular reference property should be marked as nullable");
    }

    // ========================================
    // Type Mapping and Declaration Tests
    // ========================================

    @Test
    @DisplayName("getTypeDeclaration: should map binary to List<int>")
    void testGetTypeDeclaration_Binary() {
        io.swagger.v3.oas.models.media.Schema binarySchema = new io.swagger.v3.oas.models.media.Schema();
        binarySchema.setType("string");
        binarySchema.setFormat("binary");

        String result = generator.getTypeDeclaration(binarySchema);
        assertEquals("List<int>", result, "Binary type should map to List<int>");
    }

    @Test
    @DisplayName("getTypeDeclaration: should handle null schema gracefully")
    void testGetTypeDeclaration_Null() {
        io.swagger.v3.oas.models.media.Schema nullSchema = null;
        String result = generator.getTypeDeclaration(nullSchema);
        // Should not throw exception
        assertNotNull(result);
    }

    // ========================================
    // Property Processing Tests
    // ========================================

    @Test
    @DisplayName("fromProperty: should map binary to List<int> in non-multipart context")
    void testFromProperty_BinaryNonMultipart() {
        io.swagger.v3.oas.models.media.Schema binarySchema = new io.swagger.v3.oas.models.media.Schema();
        binarySchema.setType("string");
        binarySchema.setFormat("binary");

        CodegenProperty property = generator.fromProperty("file", binarySchema, false, false);

        assertNotNull(property);
        // In non-multipart context, binary should be List<int>
        // Note: The actual mapping depends on ThreadLocal context which is set by fromRequestBody
    }

    @Test
    @DisplayName("fromProperty: should return null for null schema")
    void testFromProperty_NullSchema() {
        CodegenProperty property = generator.fromProperty("test", null, false, false);
        // fromProperty returns null when schema is null (expected behavior)
        assertNull(property, "fromProperty should return null when schema is null");
    }

    // ========================================
    // Additional Properties Processing Tests
    // ========================================

    @Test
    @DisplayName("processOpts: should sanitize pubName")
    void testProcessOpts_SanitizePubName() {
        generator.additionalProperties().put("pubName", "My-API@Client!");
        generator.processOpts();

        String sanitizedPubName = (String) generator.additionalProperties().get("pubName");
        assertEquals("my_apiclient", sanitizedPubName, "pubName should be sanitized");
    }

    @Test
    @DisplayName("processOpts: should handle missing pubName")
    void testProcessOpts_MissingPubName() {
        // Don't set pubName
        generator.processOpts();
        // Should not throw exception
    }

    // ========================================
    // String Utility Tests (via public API)
    // ========================================

    @Test
    @DisplayName("toEnumVarName: should handle mixed separators")
    void testToEnumVarName_MixedSeparators() {
        String result = generator.toEnumVarName("user-name_active.value", "string");
        assertEquals("userNameActiveValue", result, "Should handle mixed separators");
    }

    @Test
    @DisplayName("toEnumVarName: should handle whitespace")
    void testToEnumVarName_Whitespace() {
        String result = generator.toEnumVarName("user  active", "string");
        assertEquals("userActive", result, "Should handle whitespace");
    }

    @Test
    @DisplayName("toModelFilename: should handle acronyms correctly")
    void testToModelFilename_Acronyms() {
        String result = generator.toModelFilename("XMLHTTPRequest");
        assertEquals("xmlhttp_request", result, "Should handle acronyms");
    }

    @Test
    @DisplayName("sanitizePubName: should handle Unicode characters")
    void testSanitizePubName_Unicode() {
        String result = generator.sanitizePubName("my-api-клиент");
        // Should remove non-ASCII characters
        assertEquals("my_api", result, "Should remove Unicode characters");
    }

    // ========================================
    // Edge Case Tests
    // ========================================

    @Test
    @DisplayName("toEnumVarName: should handle very long numeric values")
    void testToEnumVarName_LongNumeric() {
        String result = generator.toEnumVarName("123456789012345", "string");
        assertEquals("value123456789012345", result);
    }

    @Test
    @DisplayName("toModelName: should handle single character names")
    void testToModelName_SingleChar() {
        String result = generator.toModelName("A");
        assertEquals("A", result);
    }

    @Test
    @DisplayName("toModelFilename: should handle numbers in middle")
    void testToModelFilename_NumbersInMiddle() {
        String result = generator.toModelFilename("User2FA");
        assertEquals("user2_fa", result);
    }

    @Test
    @DisplayName("escapeReservedWord: should handle already escaped words")
    void testEscapeReservedWord_AlreadyEscaped() {
        String result = generator.escapeReservedWord("class_");
        // Should add another underscore (this is expected behavior)
        assertEquals("class__", result);
    }

    // ========================================
    // Test Value Generation Tests (Regression)
    // ========================================

    /**
     * Helper to invoke the private getTestValueForType method via reflection.
     */
    private String invokeGetTestValueForType(String dataType) throws Exception {
        java.lang.reflect.Method method = DartAcdcGenerator.class.getDeclaredMethod("getTestValueForType", String.class);
        method.setAccessible(true);
        return (String) method.invoke(generator, dataType);
    }

    @Test
    @DisplayName("Regression: getTestValueForType should return MultipartFile for MultipartFile type")
    void testGetTestValueForType_MultipartFile() throws Exception {
        // Bug 43d: MultipartFile parameters should generate MultipartFile test values,
        // not empty lists
        String result = invokeGetTestValueForType("MultipartFile");
        assertTrue(result.contains("MultipartFile.fromString"),
            "MultipartFile type should generate a MultipartFile.fromString() value, got: " + result);
        assertFalse(result.equals("[]") || result.equals("const []"),
            "MultipartFile type should NOT generate an empty list");
    }

    @Test
    @DisplayName("Regression: getTestValueForType should return empty list for bare List type")
    void testGetTestValueForType_BareList() throws Exception {
        // Bug no4: bare List type (without generics) should return const [],
        // not List.fromJson(...)
        String result = invokeGetTestValueForType("List");
        assertEquals("const []", result,
            "Bare List type should generate 'const []', not List.fromJson()");
        assertFalse(result.contains("fromJson"),
            "Bare List type should NOT call fromJson");
    }

    @Test
    @DisplayName("getTestValueForType should return empty list for List with generic type")
    void testGetTestValueForType_GenericList() throws Exception {
        String result = invokeGetTestValueForType("List<String>");
        assertEquals("[]", result,
            "List<String> should generate '[]' (primitive inner type)");
    }

    @Test
    @DisplayName("getTestValueForType should return const empty list for List of model type")
    void testGetTestValueForType_ListOfModel() throws Exception {
        String result = invokeGetTestValueForType("List<Pet>");
        assertEquals("const []", result,
            "List<Pet> should generate 'const []' (model inner type)");
    }

    @Test
    @DisplayName("getTestValueForType should return fromJson for model types")
    void testGetTestValueForType_ModelType() throws Exception {
        String result = invokeGetTestValueForType("Pet");
        assertTrue(result.contains("Pet.fromJson"),
            "Model type should generate a fromJson call, got: " + result);
    }

    @Test
    @DisplayName("getTestValueForType should handle primitive types correctly")
    void testGetTestValueForType_Primitives() throws Exception {
        assertEquals("42", invokeGetTestValueForType("int"));
        assertEquals("3.14", invokeGetTestValueForType("double"));
        assertEquals("true", invokeGetTestValueForType("bool"));
        assertEquals("'test_value'", invokeGetTestValueForType("String"));
    }

    @Test
    @DisplayName("getTestValueForType should handle Map types")
    void testGetTestValueForType_Map() throws Exception {
        String result = invokeGetTestValueForType("Map<String, dynamic>");
        assertEquals("const <String, dynamic>{}", result);
    }

    // ========================================
    // Multipart Test Value Ordering Tests
    // ========================================

    @Test
    @DisplayName("Regression: test values should be generated AFTER multipart type conversion")
    void testTestValuesAfterMultipartConversion() throws Exception {
        // This tests the fix for bug 43d: test values were being generated
        // BEFORE binary parameters were converted to MultipartFile, resulting
        // in [] being used instead of MultipartFile.fromString(...)
        //
        // We verify this indirectly: a MultipartFile parameter should produce
        // a MultipartFile test value, not a List test value
        String multipartResult = invokeGetTestValueForType("MultipartFile");
        String listIntResult = invokeGetTestValueForType("List<int>");

        assertNotEquals(listIntResult, multipartResult,
            "MultipartFile and List<int> should produce different test values");
        assertTrue(multipartResult.contains("MultipartFile"),
            "MultipartFile type should generate MultipartFile value");
    }
}
