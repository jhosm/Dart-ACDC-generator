package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DartSchemaRegistry Tests")
class DartSchemaRegistryTest {

    private DartSchemaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DartSchemaRegistry();
    }

    // ========================================
    // Registration Tests
    // ========================================

    @Test
    @DisplayName("registerSchemas: should register schemas from OpenAPI")
    void testRegisterSchemas_FromOpenAPI() {
        OpenAPI openAPI = createOpenAPIWithSchemas();

        int count = registry.registerSchemas(openAPI);

        assertEquals(2, count);
        assertTrue(registry.hasSchema("User"));
        assertTrue(registry.hasSchema("Pet"));
    }

    @Test
    @DisplayName("registerSchemas: should return 0 when no schemas exist")
    void testRegisterSchemas_NoSchemas() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setComponents(new Components());

        int count = registry.registerSchemas(openAPI);

        assertEquals(0, count);
        assertEquals(0, registry.getSchemaCount());
    }

    @Test
    @DisplayName("registerSchemas: should return 0 when OpenAPI has no components")
    void testRegisterSchemas_NoComponents() {
        OpenAPI openAPI = new OpenAPI();

        int count = registry.registerSchemas(openAPI);

        assertEquals(0, count);
        assertEquals(0, registry.getSchemaCount());
    }

    @Test
    @DisplayName("registerSchemas: should return 0 when OpenAPI is null")
    void testRegisterSchemas_NullOpenAPI() {
        int count = registry.registerSchemas((OpenAPI) null);

        assertEquals(0, count);
        assertEquals(0, registry.getSchemaCount());
    }

    @Test
    @DisplayName("registerSchemas: should clear previous schemas when re-registering")
    void testRegisterSchemas_ClearsPrevious() {
        // Register first set
        Map<String, Schema> firstSchemas = new HashMap<>();
        firstSchemas.put("User", new StringSchema());
        registry.registerSchemas(firstSchemas);
        assertEquals(1, registry.getSchemaCount());

        // Register second set
        Map<String, Schema> secondSchemas = new HashMap<>();
        secondSchemas.put("Pet", new IntegerSchema());
        registry.registerSchemas(secondSchemas);

        // Should only have second set
        assertEquals(1, registry.getSchemaCount());
        assertFalse(registry.hasSchema("User"));
        assertTrue(registry.hasSchema("Pet"));
    }

    @Test
    @DisplayName("registerSchemas: should handle null schema map")
    void testRegisterSchemas_NullMap() {
        registry.registerSchemas((Map<String, Schema>) null);

        assertEquals(0, registry.getSchemaCount());
    }

    // ========================================
    // Update Tests
    // ========================================

    @Test
    @DisplayName("updateSchemas: should add schemas without clearing existing ones")
    void testUpdateSchemas() {
        // Register first set
        Map<String, Schema> firstSchemas = new HashMap<>();
        firstSchemas.put("User", new StringSchema());
        registry.registerSchemas(firstSchemas);

        // Update with second set
        Map<String, Schema> secondSchemas = new HashMap<>();
        secondSchemas.put("Pet", new IntegerSchema());
        registry.updateSchemas(secondSchemas);

        // Should have both
        assertEquals(2, registry.getSchemaCount());
        assertTrue(registry.hasSchema("User"));
        assertTrue(registry.hasSchema("Pet"));
    }

    @Test
    @DisplayName("updateSchemas: should overwrite existing schema with same name")
    void testUpdateSchemas_Overwrite() {
        // Register first schema
        StringSchema firstSchema = new StringSchema();
        Map<String, Schema> firstSchemas = new HashMap<>();
        firstSchemas.put("User", firstSchema);
        registry.registerSchemas(firstSchemas);

        // Update with same name, different schema
        IntegerSchema secondSchema = new IntegerSchema();
        Map<String, Schema> secondSchemas = new HashMap<>();
        secondSchemas.put("User", secondSchema);
        registry.updateSchemas(secondSchemas);

        // Should have second schema
        assertEquals(1, registry.getSchemaCount());
        assertSame(secondSchema, registry.getSchema("User"));
    }

    @Test
    @DisplayName("updateSchemas: should handle null schema map")
    void testUpdateSchemas_NullMap() {
        Map<String, Schema> firstSchemas = new HashMap<>();
        firstSchemas.put("User", new StringSchema());
        registry.registerSchemas(firstSchemas);

        registry.updateSchemas(null);

        // Should still have original schema
        assertEquals(1, registry.getSchemaCount());
        assertTrue(registry.hasSchema("User"));
    }

    // ========================================
    // Lookup Tests
    // ========================================

    @Test
    @DisplayName("getSchema: should return schema by name")
    void testGetSchema() {
        StringSchema schema = new StringSchema();
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", schema);
        registry.registerSchemas(schemas);

        Schema result = registry.getSchema("User");

        assertSame(schema, result);
    }

    @Test
    @DisplayName("getSchema: should return null for non-existent schema")
    void testGetSchema_NotFound() {
        Schema result = registry.getSchema("NonExistent");

        assertNull(result);
    }

    @Test
    @DisplayName("hasSchema: should return true for existing schema")
    void testHasSchema_Exists() {
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        registry.registerSchemas(schemas);

        assertTrue(registry.hasSchema("User"));
    }

    @Test
    @DisplayName("hasSchema: should return false for non-existent schema")
    void testHasSchema_NotExists() {
        assertFalse(registry.hasSchema("NonExistent"));
    }

    @Test
    @DisplayName("getAllSchemas: should return all registered schemas")
    void testGetAllSchemas() {
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        schemas.put("Pet", new IntegerSchema());
        registry.registerSchemas(schemas);

        Map<String, Schema> result = registry.getAllSchemas();

        assertEquals(2, result.size());
        assertTrue(result.containsKey("User"));
        assertTrue(result.containsKey("Pet"));
    }

    @Test
    @DisplayName("getAllSchemas: should return copy to prevent external modification")
    void testGetAllSchemas_ReturnsCopy() {
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        registry.registerSchemas(schemas);

        Map<String, Schema> result = registry.getAllSchemas();
        result.put("Pet", new IntegerSchema());

        // Registry should not be affected
        assertEquals(1, registry.getSchemaCount());
        assertFalse(registry.hasSchema("Pet"));
    }

    @Test
    @DisplayName("getAllSchemas: should return empty map when no schemas registered")
    void testGetAllSchemas_Empty() {
        Map<String, Schema> result = registry.getAllSchemas();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // Count Tests
    // ========================================

    @Test
    @DisplayName("getSchemaCount: should return number of registered schemas")
    void testGetSchemaCount() {
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        schemas.put("Pet", new IntegerSchema());
        registry.registerSchemas(schemas);

        assertEquals(2, registry.getSchemaCount());
    }

    @Test
    @DisplayName("getSchemaCount: should return 0 when no schemas registered")
    void testGetSchemaCount_Empty() {
        assertEquals(0, registry.getSchemaCount());
    }

    // ========================================
    // Clear Tests
    // ========================================

    @Test
    @DisplayName("clear: should remove all schemas")
    void testClear() {
        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        schemas.put("Pet", new IntegerSchema());
        registry.registerSchemas(schemas);

        registry.clear();

        assertEquals(0, registry.getSchemaCount());
        assertFalse(registry.hasSchema("User"));
        assertFalse(registry.hasSchema("Pet"));
    }

    @Test
    @DisplayName("clear: should be idempotent")
    void testClear_Idempotent() {
        registry.clear();
        registry.clear();

        assertEquals(0, registry.getSchemaCount());
    }

    // ========================================
    // Helper Methods
    // ========================================

    private OpenAPI createOpenAPIWithSchemas() {
        OpenAPI openAPI = new OpenAPI();
        Components components = new Components();

        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("User", new StringSchema());
        schemas.put("Pet", new IntegerSchema());

        components.setSchemas(schemas);
        openAPI.setComponents(components);

        return openAPI;
    }
}
