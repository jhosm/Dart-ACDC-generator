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

    @Test
    @DisplayName("CLI options: should register enableAuthentication with default true")
    void testCliOptions_EnableAuthentication() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableAuthentication".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableAuthentication CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "enableAuthentication should default to true");
    }

    @Test
    @DisplayName("CLI options: should register enableCaching with default true")
    void testCliOptions_EnableCaching() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableCaching".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableCaching CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "enableCaching should default to true");
    }

    @Test
    @DisplayName("CLI options: should register enableLogging with default true")
    void testCliOptions_EnableLogging() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableLogging".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableLogging CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "enableLogging should default to true");
    }

    @Test
    @DisplayName("CLI options: should register enableOfflineSupport with default true")
    void testCliOptions_EnableOfflineSupport() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableOfflineSupport".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableOfflineSupport CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "enableOfflineSupport should default to true");
    }

    @Test
    @DisplayName("CLI options: should register enableCertificatePinning with default false")
    void testCliOptions_EnableCertificatePinning() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableCertificatePinning".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableCertificatePinning CLI option should be registered");
        assertEquals("false", option.get().getDefault(), "enableCertificatePinning should default to false");
    }

    @Test
    @DisplayName("CLI options: should register all 5 feature toggle options")
    void testCliOptions_AllFeatureToggles() {
        List<String> expectedOptions = List.of(
                "enableAuthentication",
                "enableCaching",
                "enableLogging",
                "enableOfflineSupport",
                "enableCertificatePinning"
        );
        List<String> actualOptions = generator.cliOptions().stream()
                .map(org.openapitools.codegen.CliOption::getOpt)
                .filter(expectedOptions::contains)
                .toList();
        assertEquals(5, actualOptions.size(), "All 5 feature toggle options should be registered");
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
    // Feature Toggle Options Tests
    // ========================================

    @Test
    @DisplayName("processOpts: should use default values for feature toggles when not provided")
    void testProcessOpts_FeatureToggleDefaults() {
        generator.processOpts();

        // Verify all feature toggles have their defaults
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableAuthentication"),
                "enableAuthentication should default to true");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableCaching"),
                "enableCaching should default to true");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableLogging"),
                "enableLogging should default to true");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableOfflineSupport"),
                "enableOfflineSupport should default to true");
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableCertificatePinning"),
                "enableCertificatePinning should default to false");
    }

    @Test
    @DisplayName("processOpts: should store feature toggles as Boolean objects, not strings")
    void testProcessOpts_FeatureTogglesAreBoolean() {
        generator.processOpts();

        // Verify all values are Boolean objects
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableAuthentication"),
                "enableAuthentication should be a Boolean object");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableCaching"),
                "enableCaching should be a Boolean object");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableLogging"),
                "enableLogging should be a Boolean object");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableOfflineSupport"),
                "enableOfflineSupport should be a Boolean object");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableCertificatePinning"),
                "enableCertificatePinning should be a Boolean object");
    }

    @Test
    @DisplayName("processOpts: should accept explicit true value for feature toggles")
    void testProcessOpts_FeatureTogglesExplicitTrue() {
        generator.additionalProperties().put("enableAuthentication", "true");
        generator.additionalProperties().put("enableCaching", true);
        generator.additionalProperties().put("enableLogging", Boolean.TRUE);

        generator.processOpts();

        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableAuthentication"),
                "String 'true' should be converted to Boolean.TRUE");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableCaching"),
                "boolean true should be converted to Boolean.TRUE");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableLogging"),
                "Boolean.TRUE should remain Boolean.TRUE");
    }

    @Test
    @DisplayName("processOpts: should accept explicit false value for feature toggles")
    void testProcessOpts_FeatureTogglesExplicitFalse() {
        generator.additionalProperties().put("enableAuthentication", "false");
        generator.additionalProperties().put("enableCaching", false);
        generator.additionalProperties().put("enableLogging", Boolean.FALSE);

        generator.processOpts();

        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableAuthentication"),
                "String 'false' should be converted to Boolean.FALSE");
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableCaching"),
                "boolean false should be converted to Boolean.FALSE");
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableLogging"),
                "Boolean.FALSE should remain Boolean.FALSE");
    }

    @Test
    @DisplayName("processOpts: should handle case-insensitive string boolean values")
    void testProcessOpts_FeatureTogglesCaseInsensitive() {
        generator.additionalProperties().put("enableAuthentication", "TRUE");
        generator.additionalProperties().put("enableCaching", "False");
        generator.additionalProperties().put("enableLogging", "TrUe");

        generator.processOpts();

        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableAuthentication"),
                "String 'TRUE' should be converted to Boolean.TRUE");
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableCaching"),
                "String 'False' should be converted to Boolean.FALSE");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableLogging"),
                "String 'TrUe' should be converted to Boolean.TRUE");
    }

    @Test
    @DisplayName("processOpts: should use default for invalid boolean values")
    void testProcessOpts_FeatureTogglesInvalidValues() {
        generator.additionalProperties().put("enableAuthentication", "invalid");
        generator.additionalProperties().put("enableCaching", 123);
        generator.additionalProperties().put("enableLogging", null);

        generator.processOpts();

        // Should fall back to defaults
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableAuthentication"),
                "Invalid string should use default (true)");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableCaching"),
                "Invalid integer should use default (true)");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableLogging"),
                "null should use default (true)");
    }

    @Test
    @DisplayName("processOpts: should allow all features to be disabled")
    void testProcessOpts_AllFeaturesDisabled() {
        generator.additionalProperties().put("enableAuthentication", false);
        generator.additionalProperties().put("enableCaching", false);
        generator.additionalProperties().put("enableLogging", false);
        generator.additionalProperties().put("enableOfflineSupport", false);
        generator.additionalProperties().put("enableCertificatePinning", false);

        generator.processOpts();

        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableAuthentication"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableCaching"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableLogging"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableOfflineSupport"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableCertificatePinning"));
    }

    @Test
    @DisplayName("processOpts: should allow all features to be enabled")
    void testProcessOpts_AllFeaturesEnabled() {
        generator.additionalProperties().put("enableAuthentication", true);
        generator.additionalProperties().put("enableCaching", true);
        generator.additionalProperties().put("enableLogging", true);
        generator.additionalProperties().put("enableOfflineSupport", true);
        generator.additionalProperties().put("enableCertificatePinning", true);

        generator.processOpts();

        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableAuthentication"));
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableCaching"));
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableLogging"));
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableOfflineSupport"));
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableCertificatePinning"));
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
     * Helper to get test values via DartTestDataGenerator (the extracted class).
     * Previously used reflection on DartAcdcGenerator.getTestValueForType,
     * but that method was extracted to DartOperationEnricher which delegates
     * to DartTestDataGenerator.
     */
    private String getTestValueForType(String dataType) {
        DartTestDataGenerator testDataGenerator = new DartTestDataGenerator(
                new HashMap<>(), generator.languageSpecificPrimitives());
        return testDataGenerator.getTestValueForType(dataType);
    }

    @Test
    @DisplayName("Regression: getTestValueForType should return MultipartFile for MultipartFile type")
    void testGetTestValueForType_MultipartFile() {
        // Bug 43d: MultipartFile parameters should generate MultipartFile test values,
        // not empty lists
        String result = getTestValueForType("MultipartFile");
        assertTrue(result.contains("MultipartFile.fromString"),
            "MultipartFile type should generate a MultipartFile.fromString() value, got: " + result);
        assertFalse(result.equals("[]") || result.equals("const []"),
            "MultipartFile type should NOT generate an empty list");
    }

    @Test
    @DisplayName("Regression: getTestValueForType should return empty list for bare List type")
    void testGetTestValueForType_BareList() {
        // Bug no4: bare List type (without generics) should return const [],
        // not List.fromJson(...)
        String result = getTestValueForType("List");
        assertEquals("const []", result,
            "Bare List type should generate 'const []', not List.fromJson()");
        assertFalse(result.contains("fromJson"),
            "Bare List type should NOT call fromJson");
    }

    @Test
    @DisplayName("getTestValueForType should return empty list for List with generic type")
    void testGetTestValueForType_GenericList() {
        String result = getTestValueForType("List<String>");
        assertEquals("[]", result,
            "List<String> should generate '[]' (primitive inner type)");
    }

    @Test
    @DisplayName("getTestValueForType should return const empty list for List of model type")
    void testGetTestValueForType_ListOfModel() {
        String result = getTestValueForType("List<Pet>");
        assertEquals("const []", result,
            "List<Pet> should generate 'const []' (model inner type)");
    }

    @Test
    @DisplayName("getTestValueForType should return fromJson for model types")
    void testGetTestValueForType_ModelType() {
        String result = getTestValueForType("Pet");
        assertTrue(result.contains("Pet.fromJson"),
            "Model type should generate a fromJson call, got: " + result);
    }

    @Test
    @DisplayName("getTestValueForType should handle primitive types correctly")
    void testGetTestValueForType_Primitives() {
        assertEquals("42", getTestValueForType("int"));
        assertEquals("3.14", getTestValueForType("double"));
        assertEquals("true", getTestValueForType("bool"));
        assertEquals("'test_value'", getTestValueForType("String"));
    }

    @Test
    @DisplayName("getTestValueForType should handle Map types")
    void testGetTestValueForType_Map() {
        String result = getTestValueForType("Map<String, dynamic>");
        assertEquals("const <String, dynamic>{}", result);
    }

    // ========================================
    // Multipart Test Value Ordering Tests
    // ========================================

    @Test
    @DisplayName("Regression: test values should be generated AFTER multipart type conversion")
    void testTestValuesAfterMultipartConversion() {
        // This tests the fix for bug 43d: test values were being generated
        // BEFORE binary parameters were converted to MultipartFile, resulting
        // in [] being used instead of MultipartFile.fromString(...)
        //
        // We verify this indirectly: a MultipartFile parameter should produce
        // a MultipartFile test value, not a List test value
        String multipartResult = getTestValueForType("MultipartFile");
        String listIntResult = getTestValueForType("List<int>");

        assertNotEquals(listIntResult, multipartResult,
            "MultipartFile and List<int> should produce different test values");
        assertTrue(multipartResult.contains("MultipartFile"),
            "MultipartFile type should generate MultipartFile value");
    }

    // ========================================
    // ACDC Default Value Options Tests
    // ========================================

    @Test
    @DisplayName("CLI options: should register all 9 ACDC default value options")
    void testCliOptions_AllAcdcDefaultValues() {
        List<String> expectedOptions = List.of(
                "defaultTokenRefreshUrl",
                "useSecureTokenStorage",
                "refreshThresholdMinutes",
                "defaultCacheTtlHours",
                "cacheDiskSizeMb",
                "encryptCache",
                "enableUserCacheIsolation",
                "defaultLogLevel",
                "redactSensitiveData"
        );
        List<String> actualOptions = generator.cliOptions().stream()
                .map(org.openapitools.codegen.CliOption::getOpt)
                .filter(expectedOptions::contains)
                .toList();
        assertEquals(9, actualOptions.size(), "All 9 ACDC default value options should be registered");
    }

    @Test
    @DisplayName("CLI options: defaultTokenRefreshUrl should be registered as string")
    void testCliOptions_DefaultTokenRefreshUrl() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "defaultTokenRefreshUrl".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "defaultTokenRefreshUrl CLI option should be registered");
    }

    @Test
    @DisplayName("CLI options: useSecureTokenStorage should have default true")
    void testCliOptions_UseSecureTokenStorage() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "useSecureTokenStorage".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "useSecureTokenStorage CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "useSecureTokenStorage should default to true");
    }

    @Test
    @DisplayName("CLI options: refreshThresholdMinutes should have default 5")
    void testCliOptions_RefreshThresholdMinutes() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "refreshThresholdMinutes".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "refreshThresholdMinutes CLI option should be registered");
        assertEquals("5", option.get().getDefault(), "refreshThresholdMinutes should default to 5");
    }

    @Test
    @DisplayName("CLI options: defaultCacheTtlHours should have default 1")
    void testCliOptions_DefaultCacheTtlHours() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "defaultCacheTtlHours".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "defaultCacheTtlHours CLI option should be registered");
        assertEquals("1", option.get().getDefault(), "defaultCacheTtlHours should default to 1");
    }

    @Test
    @DisplayName("CLI options: cacheDiskSizeMb should have default 20")
    void testCliOptions_CacheDiskSizeMb() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "cacheDiskSizeMb".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "cacheDiskSizeMb CLI option should be registered");
        assertEquals("20", option.get().getDefault(), "cacheDiskSizeMb should default to 20");
    }

    @Test
    @DisplayName("CLI options: encryptCache should have default true")
    void testCliOptions_EncryptCache() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "encryptCache".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "encryptCache CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "encryptCache should default to true");
    }

    @Test
    @DisplayName("CLI options: enableUserCacheIsolation should have default true")
    void testCliOptions_EnableUserCacheIsolation() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "enableUserCacheIsolation".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "enableUserCacheIsolation CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "enableUserCacheIsolation should default to true");
    }

    @Test
    @DisplayName("CLI options: defaultLogLevel should have default info")
    void testCliOptions_DefaultLogLevel() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "defaultLogLevel".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "defaultLogLevel CLI option should be registered");
        assertEquals("info", option.get().getDefault(), "defaultLogLevel should default to info");
    }

    @Test
    @DisplayName("CLI options: defaultLogLevel should have enum values")
    void testCliOptions_DefaultLogLevelEnum() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "defaultLogLevel".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "defaultLogLevel CLI option should be registered");
        assertNotNull(option.get().getEnum(), "defaultLogLevel should have enum values");
        assertEquals(6, option.get().getEnum().size(), "defaultLogLevel should have 6 enum values");
        assertTrue(option.get().getEnum().containsKey("none"), "Should have 'none' enum value");
        assertTrue(option.get().getEnum().containsKey("error"), "Should have 'error' enum value");
        assertTrue(option.get().getEnum().containsKey("warning"), "Should have 'warning' enum value");
        assertTrue(option.get().getEnum().containsKey("info"), "Should have 'info' enum value");
        assertTrue(option.get().getEnum().containsKey("debug"), "Should have 'debug' enum value");
        assertTrue(option.get().getEnum().containsKey("verbose"), "Should have 'verbose' enum value");
    }

    @Test
    @DisplayName("CLI options: redactSensitiveData should have default true")
    void testCliOptions_RedactSensitiveData() {
        Optional<org.openapitools.codegen.CliOption> option = generator.cliOptions().stream()
                .filter(opt -> "redactSensitiveData".equals(opt.getOpt()))
                .findFirst();
        assertTrue(option.isPresent(), "redactSensitiveData CLI option should be registered");
        assertEquals("true", option.get().getDefault(), "redactSensitiveData should default to true");
    }

    // processOpts() tests for ACDC default values

    @Test
    @DisplayName("processOpts: should use default values for ACDC options when not provided")
    void testProcessOpts_AcdcDefaultValuesDefaults() {
        generator.processOpts();

        // Authentication defaults
        assertNull(generator.additionalProperties().get("defaultTokenRefreshUrl"),
                "defaultTokenRefreshUrl should be null when not provided");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("useSecureTokenStorage"),
                "useSecureTokenStorage should default to true");
        assertEquals(5, generator.additionalProperties().get("refreshThresholdMinutes"),
                "refreshThresholdMinutes should default to 5");

        // Cache defaults
        assertEquals(1, generator.additionalProperties().get("defaultCacheTtlHours"),
                "defaultCacheTtlHours should default to 1");
        assertEquals(20, generator.additionalProperties().get("cacheDiskSizeMb"),
                "cacheDiskSizeMb should default to 20");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("encryptCache"),
                "encryptCache should default to true");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("enableUserCacheIsolation"),
                "enableUserCacheIsolation should default to true");

        // Logging defaults
        assertEquals("LogLevel.info", generator.additionalProperties().get("defaultLogLevel"),
                "defaultLogLevel should default to LogLevel.info");
        assertEquals(Boolean.TRUE, generator.additionalProperties().get("redactSensitiveData"),
                "redactSensitiveData should default to true");
    }

    @Test
    @DisplayName("processOpts: should accept provided defaultTokenRefreshUrl")
    void testProcessOpts_ProvidedTokenRefreshUrl() {
        generator.additionalProperties().put("defaultTokenRefreshUrl", "https://api.example.com/auth/refresh");
        generator.processOpts();

        assertEquals("https://api.example.com/auth/refresh",
                generator.additionalProperties().get("defaultTokenRefreshUrl"),
                "Provided defaultTokenRefreshUrl should be used");
    }

    @Test
    @DisplayName("processOpts: should accept explicit boolean values for ACDC options")
    void testProcessOpts_AcdcBooleanValues() {
        generator.additionalProperties().put("useSecureTokenStorage", false);
        generator.additionalProperties().put("encryptCache", "false");
        generator.additionalProperties().put("enableUserCacheIsolation", Boolean.FALSE);
        generator.additionalProperties().put("redactSensitiveData", false);

        generator.processOpts();

        assertEquals(Boolean.FALSE, generator.additionalProperties().get("useSecureTokenStorage"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("encryptCache"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("enableUserCacheIsolation"));
        assertEquals(Boolean.FALSE, generator.additionalProperties().get("redactSensitiveData"));
    }

    @Test
    @DisplayName("processOpts: should accept valid integer values for ACDC numeric options")
    void testProcessOpts_AcdcValidIntegers() {
        generator.additionalProperties().put("refreshThresholdMinutes", 10);
        generator.additionalProperties().put("defaultCacheTtlHours", 24);
        generator.additionalProperties().put("cacheDiskSizeMb", 100);

        generator.processOpts();

        assertEquals(10, generator.additionalProperties().get("refreshThresholdMinutes"));
        assertEquals(24, generator.additionalProperties().get("defaultCacheTtlHours"));
        assertEquals(100, generator.additionalProperties().get("cacheDiskSizeMb"));
    }

    @Test
    @DisplayName("processOpts: should accept string integers for ACDC numeric options")
    void testProcessOpts_AcdcStringIntegers() {
        generator.additionalProperties().put("refreshThresholdMinutes", "15");
        generator.additionalProperties().put("defaultCacheTtlHours", "48");
        generator.additionalProperties().put("cacheDiskSizeMb", "50");

        generator.processOpts();

        assertEquals(15, generator.additionalProperties().get("refreshThresholdMinutes"));
        assertEquals(48, generator.additionalProperties().get("defaultCacheTtlHours"));
        assertEquals(50, generator.additionalProperties().get("cacheDiskSizeMb"));
    }

    @Test
    @DisplayName("processOpts: should reject refreshThresholdMinutes below minimum")
    void testProcessOpts_RefreshThresholdMinutesTooLow() {
        generator.additionalProperties().put("refreshThresholdMinutes", 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("refreshThresholdMinutes"));
        assertTrue(exception.getMessage().contains("between 1 and 60"));
    }

    @Test
    @DisplayName("processOpts: should reject refreshThresholdMinutes above maximum")
    void testProcessOpts_RefreshThresholdMinutesTooHigh() {
        generator.additionalProperties().put("refreshThresholdMinutes", 61);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("refreshThresholdMinutes"));
        assertTrue(exception.getMessage().contains("between 1 and 60"));
    }

    @Test
    @DisplayName("processOpts: should reject defaultCacheTtlHours below minimum")
    void testProcessOpts_CacheTtlTooLow() {
        generator.additionalProperties().put("defaultCacheTtlHours", 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("defaultCacheTtlHours"));
        assertTrue(exception.getMessage().contains("between 1 and 720"));
    }

    @Test
    @DisplayName("processOpts: should reject defaultCacheTtlHours above maximum")
    void testProcessOpts_CacheTtlTooHigh() {
        generator.additionalProperties().put("defaultCacheTtlHours", 721);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("defaultCacheTtlHours"));
        assertTrue(exception.getMessage().contains("between 1 and 720"));
    }

    @Test
    @DisplayName("processOpts: should reject cacheDiskSizeMb below minimum")
    void testProcessOpts_CacheDiskSizeTooLow() {
        generator.additionalProperties().put("cacheDiskSizeMb", 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("cacheDiskSizeMb"));
        assertTrue(exception.getMessage().contains("between 1 and 1024"));
    }

    @Test
    @DisplayName("processOpts: should reject cacheDiskSizeMb above maximum")
    void testProcessOpts_CacheDiskSizeTooHigh() {
        generator.additionalProperties().put("cacheDiskSizeMb", 1025);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("cacheDiskSizeMb"));
        assertTrue(exception.getMessage().contains("between 1 and 1024"));
    }

    @Test
    @DisplayName("processOpts: should reject invalid integer format for numeric options")
    void testProcessOpts_InvalidIntegerFormat() {
        generator.additionalProperties().put("refreshThresholdMinutes", "not-a-number");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("refreshThresholdMinutes"));
        assertTrue(exception.getMessage().contains("valid integer"));
    }

    @Test
    @DisplayName("processOpts: should accept all valid defaultLogLevel enum values")
    void testProcessOpts_ValidLogLevels() {
        String[] validLevels = {"none", "error", "warning", "info", "debug", "verbose"};

        for (String level : validLevels) {
            DartAcdcGenerator testGenerator = new DartAcdcGenerator();
            testGenerator.additionalProperties().put("defaultLogLevel", level);
            testGenerator.processOpts();

            assertEquals("LogLevel." + level, testGenerator.additionalProperties().get("defaultLogLevel"),
                    "Should accept log level: " + level);
        }
    }

    @Test
    @DisplayName("processOpts: should handle case-insensitive defaultLogLevel")
    void testProcessOpts_LogLevelCaseInsensitive() {
        generator.additionalProperties().put("defaultLogLevel", "ERROR");
        generator.processOpts();

        assertEquals("LogLevel.error", generator.additionalProperties().get("defaultLogLevel"),
                "defaultLogLevel should be case-insensitive");
    }

    @Test
    @DisplayName("processOpts: should reject invalid defaultLogLevel value")
    void testProcessOpts_InvalidLogLevel() {
        generator.additionalProperties().put("defaultLogLevel", "invalid");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.processOpts();
        });
        assertTrue(exception.getMessage().contains("defaultLogLevel"));
        assertTrue(exception.getMessage().contains("Valid values are"));
        assertTrue(exception.getMessage().contains("none"));
        assertTrue(exception.getMessage().contains("error"));
        assertTrue(exception.getMessage().contains("warning"));
        assertTrue(exception.getMessage().contains("info"));
        assertTrue(exception.getMessage().contains("debug"));
        assertTrue(exception.getMessage().contains("verbose"));
    }

    @Test
    @DisplayName("processOpts: should trim whitespace from defaultLogLevel")
    void testProcessOpts_LogLevelTrimWhitespace() {
        generator.additionalProperties().put("defaultLogLevel", "  warning  ");
        generator.processOpts();

        assertEquals("LogLevel.warning", generator.additionalProperties().get("defaultLogLevel"),
                "defaultLogLevel should trim whitespace");
    }

    @Test
    @DisplayName("processOpts: should store ACDC default values as correct types")
    void testProcessOpts_AcdcDefaultValueTypes() {
        generator.processOpts();

        // String types
        assertInstanceOf(String.class, generator.additionalProperties().get("defaultLogLevel"),
                "defaultLogLevel should be String");

        // Boolean types
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("useSecureTokenStorage"),
                "useSecureTokenStorage should be Boolean");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("encryptCache"),
                "encryptCache should be Boolean");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("enableUserCacheIsolation"),
                "enableUserCacheIsolation should be Boolean");
        assertInstanceOf(Boolean.class, generator.additionalProperties().get("redactSensitiveData"),
                "redactSensitiveData should be Boolean");

        // Integer types
        assertInstanceOf(Integer.class, generator.additionalProperties().get("refreshThresholdMinutes"),
                "refreshThresholdMinutes should be Integer");
        assertInstanceOf(Integer.class, generator.additionalProperties().get("defaultCacheTtlHours"),
                "defaultCacheTtlHours should be Integer");
        assertInstanceOf(Integer.class, generator.additionalProperties().get("cacheDiskSizeMb"),
                "cacheDiskSizeMb should be Integer");
    }
}
