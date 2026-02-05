package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.FileSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartTypeResolver.
 * Tests binary type resolution for context-aware type declarations.
 */
class DartTypeResolverTest {

    private DartTypeMapper typeMapper;
    private DartTypeResolver typeResolver;

    @BeforeEach
    void setUp() {
        typeMapper = new DartTypeMapper();
        typeResolver = new DartTypeResolver(typeMapper);
    }

    // ========================================
    // Null and Non-Binary Type Tests
    // ========================================

    @Test
    @DisplayName("resolveBinaryType: should return null for null schema")
    void testResolveBinaryType_NullSchema() {
        String result = typeResolver.resolveBinaryType(null);
        assertNull(result);
    }

    @Test
    @DisplayName("resolveBinaryType: should return null for String schema")
    void testResolveBinaryType_StringSchema() {
        Schema<?> stringSchema = new StringSchema();
        String result = typeResolver.resolveBinaryType(stringSchema);

        assertNull(result);
    }

    @Test
    @DisplayName("resolveBinaryType: should return null for Integer schema")
    void testResolveBinaryType_IntegerSchema() {
        Schema<?> integerSchema = new IntegerSchema();
        String result = typeResolver.resolveBinaryType(integerSchema);

        assertNull(result);
    }

    // ========================================
    // Binary/File Type Tests
    // ========================================

    @Test
    @DisplayName("resolveBinaryType: should return List<int> for binary schema")
    void testResolveBinaryType_BinarySchema() {
        Schema<?> binarySchema = new BinarySchema();
        String result = typeResolver.resolveBinaryType(binarySchema);

        assertEquals("List<int>", result);
    }

    @Test
    @DisplayName("resolveBinaryType: should return List<int> for file schema")
    void testResolveBinaryType_FileSchema() {
        Schema<?> fileSchema = new FileSchema();
        String result = typeResolver.resolveBinaryType(fileSchema);

        assertEquals("List<int>", result);
    }

    @Test
    @DisplayName("resolveBinaryType: should return List<int> for string schema with binary format")
    void testResolveBinaryType_StringSchemaWithBinaryFormat() {
        Schema<String> stringSchema = new StringSchema();
        stringSchema.setFormat("binary");
        String result = typeResolver.resolveBinaryType(stringSchema);

        assertEquals("List<int>", result);
    }

    @Test
    @DisplayName("resolveBinaryType: should return null for byte format (base64-encoded, not binary)")
    void testResolveBinaryType_ByteFormat() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("byte");
        String result = typeResolver.resolveBinaryType(schema);

        // byte format is base64-encoded data, not binary
        assertNull(result);
    }

    // ========================================
    // Multipart Context Independence Tests
    // ========================================

    @Test
    @DisplayName("resolveBinaryType: should always return List<int> for binary even in multipart context")
    void testResolveBinaryType_BinaryInMultipartContext() {
        // Enter multipart context
        typeMapper.enterMultipartContext();

        Schema<?> binarySchema = new BinarySchema();
        String result = typeResolver.resolveBinaryType(binarySchema);

        // Should still return List<int>, not MultipartFile
        // MultipartFile mapping is handled separately in property creation
        assertEquals("List<int>", result);

        // Clean up
        typeMapper.exitMultipartContext();
    }
}
