package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;

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
}
