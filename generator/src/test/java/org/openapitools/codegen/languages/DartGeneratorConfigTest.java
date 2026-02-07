package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CliOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DartGeneratorConfig Tests")
class DartGeneratorConfigTest {

    private DartGeneratorConfig config;
    private DartNameSanitizer nameSanitizer;
    private Map<String, Object> additionalProperties;

    @BeforeEach
    void setUp() {
        nameSanitizer = new DartNameSanitizer();
        config = new DartGeneratorConfig(nameSanitizer);
        additionalProperties = new HashMap<>();
    }

    // ========================================
    // CLI Option Registration Tests
    // ========================================

    @Test
    @DisplayName("registerCliOptions: should register all required options")
    void testRegisterCliOptions() {
        List<CliOption> options = config.registerCliOptions();

        // Should have at least 22 options (5 package + 5 features + 8 defaults + 4 code style)
        assertTrue(options.size() >= 22, "Should register at least 22 CLI options");

        // Verify key options exist
        assertTrue(options.stream().anyMatch(o -> o.getOpt().equals("pubName")));
        assertTrue(options.stream().anyMatch(o -> o.getOpt().equals("enableAuthentication")));
        assertTrue(options.stream().anyMatch(o -> o.getOpt().equals("defaultLogLevel")));
    }

    // ========================================
    // Package Metadata Processing Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should use provided pubName")
    void testProcessOptions_ProvidedPubName() {
        additionalProperties.put("pubName", "my_api_client");
        config.processOptions(additionalProperties, null);

        assertEquals("my_api_client", config.getPubName());
    }

    @Test
    @DisplayName("processOptions: should derive pubName from OpenAPI info.title")
    void testProcessOptions_DerivePubNameFromTitle() {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setTitle("Awesome API");
        openAPI.setInfo(info);

        config.processOptions(additionalProperties, openAPI);

        assertEquals("awesome_api", config.getPubName());
    }

    @Test
    @DisplayName("processOptions: should use default pubName if not provided")
    void testProcessOptions_DefaultPubName() {
        config.processOptions(additionalProperties, null);

        assertEquals("openapi_client", config.getPubName());
    }

    @Test
    @DisplayName("processOptions: should derive pubVersion from OpenAPI info.version")
    void testProcessOptions_DerivePubVersion() {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setVersion("2.5.3");
        openAPI.setInfo(info);

        config.processOptions(additionalProperties, openAPI);

        assertEquals("2.5.3", config.getPubVersion());
    }

    @Test
    @DisplayName("processOptions: should use default pubVersion if not provided")
    void testProcessOptions_DefaultPubVersion() {
        config.processOptions(additionalProperties, null);

        assertEquals("1.0.0", config.getPubVersion());
    }

    @Test
    @DisplayName("processOptions: should derive pubDescription from OpenAPI")
    void testProcessOptions_DerivePubDescription() {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.setDescription("A test API");
        openAPI.setInfo(info);

        config.processOptions(additionalProperties, openAPI);

        assertEquals("A test API", config.getPubDescription());
    }

    // ========================================
    // Feature Toggle Processing Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should enable authentication by default")
    void testProcessOptions_DefaultEnableAuthentication() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEnableAuthentication());
    }

    @Test
    @DisplayName("processOptions: should disable authentication when set to false")
    void testProcessOptions_DisableAuthentication() {
        additionalProperties.put("enableAuthentication", "false");
        config.processOptions(additionalProperties, null);

        assertFalse(config.isEnableAuthentication());
    }

    @Test
    @DisplayName("processOptions: should enable caching by default")
    void testProcessOptions_DefaultEnableCaching() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEnableCaching());
    }

    @Test
    @DisplayName("processOptions: should disable certificate pinning by default")
    void testProcessOptions_DefaultCertificatePinning() {
        config.processOptions(additionalProperties, null);

        assertFalse(config.isEnableCertificatePinning());
    }

    @Test
    @DisplayName("processOptions: should enable certificate pinning when set")
    void testProcessOptions_EnableCertificatePinning() {
        additionalProperties.put("enableCertificatePinning", true);
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEnableCertificatePinning());
    }

    // ========================================
    // Authentication Defaults Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should use default refreshThresholdMinutes")
    void testProcessOptions_DefaultRefreshThreshold() {
        config.processOptions(additionalProperties, null);

        assertEquals(5, config.getRefreshThresholdMinutes());
    }

    @Test
    @DisplayName("processOptions: should process custom refreshThresholdMinutes")
    void testProcessOptions_CustomRefreshThreshold() {
        additionalProperties.put("refreshThresholdMinutes", "15");
        config.processOptions(additionalProperties, null);

        assertEquals(15, config.getRefreshThresholdMinutes());
    }

    @Test
    @DisplayName("processOptions: should validate refreshThresholdMinutes bounds")
    void testProcessOptions_RefreshThresholdBounds() {
        additionalProperties.put("refreshThresholdMinutes", "100");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should enable secure token storage by default")
    void testProcessOptions_DefaultSecureTokenStorage() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isUseSecureTokenStorage());
    }

    // ========================================
    // Cache Defaults Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should use default cache TTL")
    void testProcessOptions_DefaultCacheTtl() {
        config.processOptions(additionalProperties, null);

        assertEquals(1, config.getDefaultCacheTtlHours());
    }

    @Test
    @DisplayName("processOptions: should process custom cache TTL")
    void testProcessOptions_CustomCacheTtl() {
        additionalProperties.put("defaultCacheTtlHours", "24");
        config.processOptions(additionalProperties, null);

        assertEquals(24, config.getDefaultCacheTtlHours());
    }

    @Test
    @DisplayName("processOptions: should validate cache TTL bounds")
    void testProcessOptions_CacheTtlBounds() {
        additionalProperties.put("defaultCacheTtlHours", "1000");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should use default cache disk size")
    void testProcessOptions_DefaultCacheDiskSize() {
        config.processOptions(additionalProperties, null);

        assertEquals(20, config.getCacheDiskSizeMb());
    }

    @Test
    @DisplayName("processOptions: should process custom cache disk size")
    void testProcessOptions_CustomCacheDiskSize() {
        additionalProperties.put("cacheDiskSizeMb", "100");
        config.processOptions(additionalProperties, null);

        assertEquals(100, config.getCacheDiskSizeMb());
    }

    @Test
    @DisplayName("processOptions: should enable cache encryption by default")
    void testProcessOptions_DefaultEncryptCache() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEncryptCache());
    }

    @Test
    @DisplayName("processOptions: should enable user cache isolation by default")
    void testProcessOptions_DefaultUserCacheIsolation() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEnableUserCacheIsolation());
    }

    // ========================================
    // Logging Defaults Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should use default log level")
    void testProcessOptions_DefaultLogLevel() {
        config.processOptions(additionalProperties, null);

        assertEquals("LogLevel.info", config.getDefaultLogLevel());
    }

    @Test
    @DisplayName("processOptions: should process custom log level")
    void testProcessOptions_CustomLogLevel() {
        additionalProperties.put("defaultLogLevel", "debug");
        config.processOptions(additionalProperties, null);

        assertEquals("LogLevel.debug", config.getDefaultLogLevel());
    }

    @Test
    @DisplayName("processOptions: should validate log level enum values")
    void testProcessOptions_InvalidLogLevel() {
        additionalProperties.put("defaultLogLevel", "invalid");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should handle case-insensitive log levels")
    void testProcessOptions_CaseInsensitiveLogLevel() {
        additionalProperties.put("defaultLogLevel", "ERROR");
        config.processOptions(additionalProperties, null);

        assertEquals("LogLevel.error", config.getDefaultLogLevel());
    }

    @Test
    @DisplayName("processOptions: should enable sensitive data redaction by default")
    void testProcessOptions_DefaultRedactSensitiveData() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isRedactSensitiveData());
    }

    // ========================================
    // Apply to Additional Properties Tests
    // ========================================

    @Test
    @DisplayName("applyToAdditionalProperties: should apply all config to properties")
    void testApplyToAdditionalProperties() {
        // Process some custom options
        additionalProperties.put("pubName", "test_api");
        additionalProperties.put("enableAuthentication", false);
        additionalProperties.put("defaultLogLevel", "warning");
        config.processOptions(additionalProperties, null);

        // Clear and reapply
        Map<String, Object> newProps = new HashMap<>();
        config.applyToAdditionalProperties(newProps);

        // Verify all properties are applied
        assertEquals("test_api", newProps.get("pubName"));
        assertEquals(Boolean.FALSE, newProps.get("enableAuthentication"));
        assertEquals("LogLevel.warning", newProps.get("defaultLogLevel"));
        assertEquals(Integer.valueOf(5), newProps.get("refreshThresholdMinutes"));
        assertEquals(Integer.valueOf(1), newProps.get("defaultCacheTtlHours"));
    }

    @Test
    @DisplayName("applyToAdditionalProperties: should apply boolean objects for Mustache")
    void testApplyToAdditionalProperties_BooleanObjects() {
        config.processOptions(additionalProperties, null);

        Map<String, Object> newProps = new HashMap<>();
        config.applyToAdditionalProperties(newProps);

        // Verify Boolean objects (not primitives) for Mustache template conditionals
        assertTrue(newProps.get("enableAuthentication") instanceof Boolean);
        assertTrue(newProps.get("enableCaching") instanceof Boolean);
        assertTrue(newProps.get("enableLogging") instanceof Boolean);
    }

    // ========================================
    // Boolean Conversion Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should convert string 'true' to Boolean")
    void testConvertToBoolean_StringTrue() {
        additionalProperties.put("enableCaching", "true");
        config.processOptions(additionalProperties, null);

        assertTrue(config.isEnableCaching());
    }

    @Test
    @DisplayName("processOptions: should convert string 'false' to Boolean")
    void testConvertToBoolean_StringFalse() {
        additionalProperties.put("enableCaching", "false");
        config.processOptions(additionalProperties, null);

        assertFalse(config.isEnableCaching());
    }

    @Test
    @DisplayName("processOptions: should handle Boolean object")
    void testConvertToBoolean_BooleanObject() {
        additionalProperties.put("enableCaching", Boolean.FALSE);
        config.processOptions(additionalProperties, null);

        assertFalse(config.isEnableCaching());
    }

    @Test
    @DisplayName("processOptions: should use default for invalid boolean")
    void testConvertToBoolean_Invalid() {
        additionalProperties.put("enableCaching", "invalid");
        config.processOptions(additionalProperties, null);

        // Should use default (true)
        assertTrue(config.isEnableCaching());
    }

    // ========================================
    // Integer Validation Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should parse integer from string")
    void testProcessIntegerOption_String() {
        additionalProperties.put("cacheDiskSizeMb", "50");
        config.processOptions(additionalProperties, null);

        assertEquals(50, config.getCacheDiskSizeMb());
    }

    @Test
    @DisplayName("processOptions: should handle Integer object")
    void testProcessIntegerOption_Integer() {
        additionalProperties.put("cacheDiskSizeMb", 75);
        config.processOptions(additionalProperties, null);

        assertEquals(75, config.getCacheDiskSizeMb());
    }

    @Test
    @DisplayName("processOptions: should throw on invalid integer string")
    void testProcessIntegerOption_InvalidString() {
        additionalProperties.put("cacheDiskSizeMb", "not-a-number");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should throw on integer below minimum")
    void testProcessIntegerOption_BelowMin() {
        additionalProperties.put("refreshThresholdMinutes", "0");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should throw on integer above maximum")
    void testProcessIntegerOption_AboveMax() {
        additionalProperties.put("refreshThresholdMinutes", "100");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    // ========================================
    // Code Generation Style Option Tests
    // ========================================

    @Test
    @DisplayName("processOptions: should use default serializationLibrary")
    void testProcessOptions_DefaultSerializationLibrary() {
        config.processOptions(additionalProperties, null);

        assertEquals("json_serializable", config.getSerializationLibrary());
    }

    @Test
    @DisplayName("processOptions: should accept json_serializable")
    void testProcessOptions_JsonSerializable() {
        additionalProperties.put("serializationLibrary", "json_serializable");
        config.processOptions(additionalProperties, null);

        assertEquals("json_serializable", config.getSerializationLibrary());
    }

    @Test
    @DisplayName("processOptions: should accept freezed")
    void testProcessOptions_Freezed() {
        additionalProperties.put("serializationLibrary", "freezed");
        config.processOptions(additionalProperties, null);

        assertEquals("freezed", config.getSerializationLibrary());
    }

    @Test
    @DisplayName("processOptions: should reject invalid serializationLibrary")
    void testProcessOptions_InvalidSerializationLibrary() {
        additionalProperties.put("serializationLibrary", "protobuf");

        assertThrows(IllegalArgumentException.class,
                () -> config.processOptions(additionalProperties, null));
    }

    @Test
    @DisplayName("processOptions: should handle case-insensitive serializationLibrary")
    void testProcessOptions_CaseInsensitiveSerializationLibrary() {
        additionalProperties.put("serializationLibrary", "JSON_SERIALIZABLE");
        config.processOptions(additionalProperties, null);

        assertEquals("json_serializable", config.getSerializationLibrary());
    }

    @Test
    @DisplayName("processOptions: should use default generateInterfaces")
    void testProcessOptions_DefaultGenerateInterfaces() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isGenerateInterfaces());
    }

    @Test
    @DisplayName("processOptions: should disable generateInterfaces")
    void testProcessOptions_DisableGenerateInterfaces() {
        additionalProperties.put("generateInterfaces", "false");
        config.processOptions(additionalProperties, null);

        assertFalse(config.isGenerateInterfaces());
    }

    @Test
    @DisplayName("processOptions: should use default dataSourceSuffix")
    void testProcessOptions_DefaultDataSourceSuffix() {
        config.processOptions(additionalProperties, null);

        assertEquals("RemoteDataSource", config.getDataSourceSuffix());
    }

    @Test
    @DisplayName("processOptions: should accept custom dataSourceSuffix")
    void testProcessOptions_CustomDataSourceSuffix() {
        additionalProperties.put("dataSourceSuffix", "Api");
        config.processOptions(additionalProperties, null);

        assertEquals("Api", config.getDataSourceSuffix());
    }

    @Test
    @DisplayName("processOptions: should use default generateBarrelExports")
    void testProcessOptions_DefaultGenerateBarrelExports() {
        config.processOptions(additionalProperties, null);

        assertTrue(config.isGenerateBarrelExports());
    }

    @Test
    @DisplayName("processOptions: should disable generateBarrelExports")
    void testProcessOptions_DisableGenerateBarrelExports() {
        additionalProperties.put("generateBarrelExports", false);
        config.processOptions(additionalProperties, null);

        assertFalse(config.isGenerateBarrelExports());
    }

    @Test
    @DisplayName("applyToAdditionalProperties: should apply code style options")
    void testApplyToAdditionalProperties_CodeStyleOptions() {
        additionalProperties.put("serializationLibrary", "freezed");
        additionalProperties.put("generateInterfaces", false);
        additionalProperties.put("dataSourceSuffix", "Api");
        additionalProperties.put("generateBarrelExports", false);
        config.processOptions(additionalProperties, null);

        Map<String, Object> newProps = new HashMap<>();
        config.applyToAdditionalProperties(newProps);

        assertEquals("freezed", newProps.get("serializationLibrary"));
        assertEquals(Boolean.FALSE, newProps.get("generateInterfaces"));
        assertEquals("Api", newProps.get("dataSourceSuffix"));
        assertEquals(Boolean.FALSE, newProps.get("generateBarrelExports"));
    }
}
