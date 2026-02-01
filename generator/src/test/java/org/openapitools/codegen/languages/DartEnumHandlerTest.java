package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartEnumHandler.
 * Tests enum variable name generation and collision resolution.
 */
class DartEnumHandlerTest {

    private DartEnumHandler enumHandler;
    private Set<String> reservedWords;

    @BeforeEach
    void setUp() {
        reservedWords = new HashSet<>(Arrays.asList(
                "class", "if", "else", "for", "while", "return", "static", "final"
        ));
        enumHandler = new DartEnumHandler(reservedWords);
    }

    // ========================================
    // Enum Variable Name Tests
    // ========================================

    @Test
    @DisplayName("toEnumVarName: should convert simple string to camelCase")
    void testToEnumVarName_Simple() {
        String result = enumHandler.toEnumVarName("active", "string");
        assertEquals("active", result);
    }

    @Test
    @DisplayName("toEnumVarName: should convert kebab-case to camelCase")
    void testToEnumVarName_KebabCase() {
        String result = enumHandler.toEnumVarName("user-active", "string");
        assertEquals("userActive", result);
    }

    @Test
    @DisplayName("toEnumVarName: should convert snake_case to camelCase")
    void testToEnumVarName_SnakeCase() {
        String result = enumHandler.toEnumVarName("user_active", "string");
        assertEquals("userActive", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle numeric values")
    void testToEnumVarName_Numeric() {
        String result = enumHandler.toEnumVarName("123", "string");
        assertEquals("value123", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle negative numeric values")
    void testToEnumVarName_NegativeNumeric() {
        String result = enumHandler.toEnumVarName("-123", "string");
        assertEquals("value123", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle decimal numeric values")
    void testToEnumVarName_DecimalNumeric() {
        String result = enumHandler.toEnumVarName("123.45", "string");
        assertEquals("value12345", result);
    }

    @Test
    @DisplayName("toEnumVarName: should return 'empty' for null input")
    void testToEnumVarName_Null() {
        String result = enumHandler.toEnumVarName(null, "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toEnumVarName: should return 'empty' for empty string")
    void testToEnumVarName_Empty() {
        String result = enumHandler.toEnumVarName("", "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toEnumVarName: should prefix with 'value' if starts with digit after conversion")
    void testToEnumVarName_StartsWithDigit() {
        String result = enumHandler.toEnumVarName("1st-place", "string");
        assertEquals("value1stPlace", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle reserved words")
    void testToEnumVarName_ReservedWord() {
        String result = enumHandler.toEnumVarName("class", "string");
        assertEquals("class_", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle uppercase string")
    void testToEnumVarName_Uppercase() {
        String result = enumHandler.toEnumVarName("ACTIVE", "string");
        assertEquals("active", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle mixed case")
    void testToEnumVarName_MixedCase() {
        String result = enumHandler.toEnumVarName("UserStatus", "string");
        assertEquals("userstatus", result);
    }

    @Test
    @DisplayName("toEnumVarName: should remove special characters")
    void testToEnumVarName_SpecialChars() {
        String result = enumHandler.toEnumVarName("user@active!", "string");
        assertEquals("useractive", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle only special characters")
    void testToEnumVarName_OnlySpecialChars() {
        String result = enumHandler.toEnumVarName("@#$%", "string");
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle zero")
    void testToEnumVarName_Zero() {
        String result = enumHandler.toEnumVarName("0", "string");
        assertEquals("value0", result);
    }

    @Test
    @DisplayName("toEnumVarName: should handle negative zero")
    void testToEnumVarName_NegativeZero() {
        String result = enumHandler.toEnumVarName("-0", "string");
        assertEquals("value0", result);
    }

    // ========================================
    // CreateEnumVars Tests
    // ========================================

    @Test
    @DisplayName("createEnumVars: should create enum vars from string values")
    void testCreateEnumVars_Strings() {
        List<Object> values = Arrays.asList("active", "inactive", "pending");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(3, result.size());
        assertEquals("active", result.get(0).get("name"));
        assertEquals("active", result.get(0).get("value"));
        assertEquals(true, result.get(0).get("isString"));
    }

    @Test
    @DisplayName("createEnumVars: should create enum vars from integer values")
    void testCreateEnumVars_Integers() {
        List<Object> values = Arrays.asList(1, 2, 3);
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "integer");

        assertEquals(3, result.size());
        assertEquals("value1", result.get(0).get("name"));
        assertEquals("1", result.get(0).get("value"));
        assertEquals(false, result.get(0).get("isString"));
    }

    @Test
    @DisplayName("createEnumVars: should handle name collisions with numeric suffix")
    void testCreateEnumVars_Collisions() {
        // Both "active" and "ACTIVE" map to "active"
        List<Object> values = Arrays.asList("active", "ACTIVE", "active-user");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(3, result.size());
        assertEquals("active", result.get(0).get("name"));
        assertEquals("active2", result.get(1).get("name"));
        assertEquals("activeUser", result.get(2).get("name"));
    }

    @Test
    @DisplayName("createEnumVars: should handle empty list")
    void testCreateEnumVars_Empty() {
        List<Object> values = Collections.emptyList();
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("createEnumVars: should handle single value")
    void testCreateEnumVars_SingleValue() {
        List<Object> values = Collections.singletonList("active");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(1, result.size());
        assertEquals("active", result.get(0).get("name"));
    }

    @Test
    @DisplayName("createEnumVars: should handle mixed valid and invalid values")
    void testCreateEnumVars_MixedValues() {
        List<Object> values = Arrays.asList("valid", "123", "@#$", "another");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(4, result.size());
        assertEquals("valid", result.get(0).get("name"));
        assertEquals("value123", result.get(1).get("name"));
        assertEquals("empty", result.get(2).get("name"));
        assertEquals("another", result.get(3).get("name"));
    }

    @Test
    @DisplayName("createEnumVars: should handle multiple collisions")
    void testCreateEnumVars_MultipleCollisions() {
        // All map to "empty"
        List<Object> values = Arrays.asList("", "@#$", "!@#");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(3, result.size());
        assertEquals("empty", result.get(0).get("name"));
        assertEquals("empty2", result.get(1).get("name"));
        assertEquals("empty3", result.get(2).get("name"));
    }

    @Test
    @DisplayName("createEnumVars: should preserve original values")
    void testCreateEnumVars_PreserveValues() {
        List<Object> values = Arrays.asList("ACTIVE", "user-status", "123");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals("ACTIVE", result.get(0).get("value"));
        assertEquals("user-status", result.get(1).get("value"));
        assertEquals("123", result.get(2).get("value"));
    }

    @Test
    @DisplayName("createEnumVars: should handle reserved words")
    void testCreateEnumVars_ReservedWords() {
        List<Object> values = Arrays.asList("class", "if", "return");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(3, result.size());
        assertEquals("class_", result.get(0).get("name"));
        assertEquals("if_", result.get(1).get("name"));
        assertEquals("return_", result.get(2).get("name"));
    }

    @Test
    @DisplayName("createEnumVars: should handle numeric collision with reserved word collision")
    void testCreateEnumVars_ComplexCollisions() {
        // "class" and "CLASS" both map to "class_"
        List<Object> values = Arrays.asList("class", "CLASS");
        List<Map<String, Object>> result = enumHandler.createEnumVars(values, "string");

        assertEquals(2, result.size());
        assertEquals("class_", result.get(0).get("name"));
        assertEquals("class_2", result.get(1).get("name"));
    }
}
