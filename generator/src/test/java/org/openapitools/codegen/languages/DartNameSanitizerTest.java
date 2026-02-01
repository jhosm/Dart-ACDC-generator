package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartNameSanitizer.
 * Tests name sanitization, reserved word escaping, and case conversion.
 */
class DartNameSanitizerTest {

    private DartNameSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new DartNameSanitizer();
    }

    // ========================================
    // Package Name Sanitization Tests
    // ========================================

    @Test
    @DisplayName("sanitizePubName: should convert to lowercase and replace spaces")
    void testSanitizePubName_BasicConversion() {
        String result = sanitizer.sanitizePubName("My API Client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should handle hyphens")
    void testSanitizePubName_Hyphens() {
        String result = sanitizer.sanitizePubName("my-api-client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should remove invalid characters")
    void testSanitizePubName_InvalidChars() {
        String result = sanitizer.sanitizePubName("my@api#client!");
        assertEquals("myapiclient", result);
    }

    @Test
    @DisplayName("sanitizePubName: should collapse consecutive underscores")
    void testSanitizePubName_ConsecutiveUnderscores() {
        String result = sanitizer.sanitizePubName("my___api___client");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should remove leading/trailing underscores")
    void testSanitizePubName_LeadingTrailing() {
        String result = sanitizer.sanitizePubName("_my_api_client_");
        assertEquals("my_api_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should prefix with 'api_' if starts with digit")
    void testSanitizePubName_StartsWithDigit() {
        String result = sanitizer.sanitizePubName("123api");
        assertEquals("api_123api", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for null input")
    void testSanitizePubName_Null() {
        String result = sanitizer.sanitizePubName(null);
        assertEquals("openapi_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for empty string")
    void testSanitizePubName_Empty() {
        String result = sanitizer.sanitizePubName("");
        assertEquals("openapi_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should return default for string that becomes empty after sanitization")
    void testSanitizePubName_BecomesEmpty() {
        String result = sanitizer.sanitizePubName("@#$!");
        assertEquals("openapi_client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should handle mixed case and special characters")
    void testSanitizePubName_Complex() {
        String result = sanitizer.sanitizePubName("My-API@2024#Client!");
        assertEquals("my_api2024client", result);
    }

    @Test
    @DisplayName("sanitizePubName: should handle already valid names")
    void testSanitizePubName_AlreadyValid() {
        String result = sanitizer.sanitizePubName("my_api_client");
        assertEquals("my_api_client", result);
    }

    // ========================================
    // Reserved Word Escaping Tests
    // ========================================

    @Test
    @DisplayName("escapeReservedWord: should append underscore")
    void testEscapeReservedWord_BasicEscape() {
        String result = sanitizer.escapeReservedWord("class");
        assertEquals("class_", result);
    }

    @Test
    @DisplayName("escapeReservedWord: should handle any word")
    void testEscapeReservedWord_AnyWord() {
        String result = sanitizer.escapeReservedWord("myVariable");
        assertEquals("myVariable_", result);
    }

    @Test
    @DisplayName("escapeReservedWord: should handle empty string")
    void testEscapeReservedWord_Empty() {
        String result = sanitizer.escapeReservedWord("");
        assertEquals("_", result);
    }

    // ========================================
    // Snake Case Conversion Tests
    // ========================================

    @Test
    @DisplayName("toSnakeCase: should convert PascalCase to snake_case")
    void testToSnakeCase_PascalCase() {
        String result = sanitizer.toSnakeCase("UserProfile");
        assertEquals("user_profile", result);
    }

    @Test
    @DisplayName("toSnakeCase: should convert camelCase to snake_case")
    void testToSnakeCase_CamelCase() {
        String result = sanitizer.toSnakeCase("simpleValue");
        assertEquals("simple_value", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle acronyms correctly")
    void testToSnakeCase_Acronyms() {
        String result = sanitizer.toSnakeCase("HTTPResponse");
        assertEquals("http_response", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle multiple consecutive uppercase letters")
    void testToSnakeCase_ConsecutiveUppercase() {
        String result = sanitizer.toSnakeCase("XMLParser");
        assertEquals("xml_parser", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle single word")
    void testToSnakeCase_SingleWord() {
        String result = sanitizer.toSnakeCase("user");
        assertEquals("user", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle already snake_case")
    void testToSnakeCase_AlreadySnakeCase() {
        String result = sanitizer.toSnakeCase("user_profile");
        assertEquals("user_profile", result);
    }

    @Test
    @DisplayName("toSnakeCase: should return null for null input")
    void testToSnakeCase_Null() {
        String result = sanitizer.toSnakeCase(null);
        assertNull(result);
    }

    @Test
    @DisplayName("toSnakeCase: should return empty string for empty input")
    void testToSnakeCase_Empty() {
        String result = sanitizer.toSnakeCase("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle complex names")
    void testToSnakeCase_Complex() {
        String result = sanitizer.toSnakeCase("HTTPSConnectionURL");
        assertEquals("https_connection_url", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle numbers")
    void testToSnakeCase_WithNumbers() {
        String result = sanitizer.toSnakeCase("User2Profile");
        assertEquals("user2_profile", result);
    }

    @Test
    @DisplayName("toSnakeCase: should handle single uppercase letter")
    void testToSnakeCase_SingleUppercase() {
        String result = sanitizer.toSnakeCase("A");
        assertEquals("a", result);
    }
}
