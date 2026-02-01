package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartTypeMapper.
 * Tests type mapping, binary type detection, and multipart context handling.
 */
class DartTypeMapperTest {

    private DartTypeMapper typeMapper;

    @BeforeEach
    void setUp() {
        typeMapper = new DartTypeMapper();
    }

    @AfterEach
    void tearDown() {
        // Ensure multipart context is cleared after each test
        typeMapper.exitMultipartContext();
    }

    // ========================================
    // Binary Type Detection Tests
    // ========================================

    @Test
    @DisplayName("isBinaryType: should detect binary schema (string/binary)")
    void testIsBinaryType_Binary() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("binary");

        assertTrue(typeMapper.isBinaryType(schema));
    }

    @Test
    @DisplayName("isBinaryType: should return false for non-binary string")
    void testIsBinaryType_NonBinaryString() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");

        assertFalse(typeMapper.isBinaryType(schema));
    }

    @Test
    @DisplayName("isBinaryType: should return false for non-string type")
    void testIsBinaryType_NonString() {
        Schema<?> schema = new Schema<>();
        schema.setType("integer");
        schema.setFormat("binary");

        assertFalse(typeMapper.isBinaryType(schema));
    }

    @Test
    @DisplayName("isBinaryType: should return false for null schema")
    void testIsBinaryType_Null() {
        assertFalse(typeMapper.isBinaryType(null));
    }

    @Test
    @DisplayName("isBinaryType: should return false for byte format")
    void testIsBinaryType_ByteFormat() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("byte");

        assertFalse(typeMapper.isBinaryType(schema));
    }

    // ========================================
    // Binary Type Declaration Tests
    // ========================================

    @Test
    @DisplayName("getBinaryTypeDeclaration: should return List<int> outside multipart context")
    void testGetBinaryTypeDeclaration_NonMultipart() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("binary");

        String result = typeMapper.getBinaryTypeDeclaration(schema);
        assertEquals("List<int>", result);
    }

    @Test
    @DisplayName("getBinaryTypeDeclaration: should return MultipartFile in multipart context")
    void testGetBinaryTypeDeclaration_Multipart() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("binary");

        typeMapper.enterMultipartContext();
        String result = typeMapper.getBinaryTypeDeclaration(schema);
        assertEquals("MultipartFile", result);
    }

    @Test
    @DisplayName("getBinaryTypeDeclaration: should return null for non-binary schema")
    void testGetBinaryTypeDeclaration_NonBinary() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");

        String result = typeMapper.getBinaryTypeDeclaration(schema);
        assertNull(result);
    }

    // ========================================
    // Multipart Content Detection Tests
    // ========================================

    @Test
    @DisplayName("isMultipartContent: should detect multipart/form-data")
    void testIsMultipartContent_Multipart() {
        Content content = new Content();
        content.addMediaType("multipart/form-data", new MediaType());

        assertTrue(typeMapper.isMultipartContent(content));
    }

    @Test
    @DisplayName("isMultipartContent: should return false for non-multipart content")
    void testIsMultipartContent_NonMultipart() {
        Content content = new Content();
        content.addMediaType("application/json", new MediaType());

        assertFalse(typeMapper.isMultipartContent(content));
    }

    @Test
    @DisplayName("isMultipartContent: should return false for null content")
    void testIsMultipartContent_Null() {
        assertFalse(typeMapper.isMultipartContent(null));
    }

    @Test
    @DisplayName("isMultipartContent: should return false for empty content")
    void testIsMultipartContent_Empty() {
        Content content = new Content();
        assertFalse(typeMapper.isMultipartContent(content));
    }

    // ========================================
    // Multipart Context Management Tests
    // ========================================

    @Test
    @DisplayName("isInMultipartContext: should return false initially")
    void testIsInMultipartContext_Initial() {
        assertFalse(typeMapper.isInMultipartContext());
    }

    @Test
    @DisplayName("enterMultipartContext: should set context to true")
    void testEnterMultipartContext() {
        typeMapper.enterMultipartContext();
        assertTrue(typeMapper.isInMultipartContext());
    }

    @Test
    @DisplayName("exitMultipartContext: should clear context")
    void testExitMultipartContext() {
        typeMapper.enterMultipartContext();
        assertTrue(typeMapper.isInMultipartContext());

        typeMapper.exitMultipartContext();
        assertFalse(typeMapper.isInMultipartContext());
    }

    @Test
    @DisplayName("exitMultipartContext: should be safe to call multiple times")
    void testExitMultipartContext_MultipleCalls() {
        typeMapper.enterMultipartContext();
        typeMapper.exitMultipartContext();
        typeMapper.exitMultipartContext(); // Should not throw

        assertFalse(typeMapper.isInMultipartContext());
    }

    @Test
    @DisplayName("Context should affect binary type declaration")
    void testContext_AffectsBinaryTypeDeclaration() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setFormat("binary");

        // Before entering context
        String beforeType = typeMapper.getBinaryTypeDeclaration(schema);
        assertEquals("List<int>", beforeType);

        // After entering context
        typeMapper.enterMultipartContext();
        String duringType = typeMapper.getBinaryTypeDeclaration(schema);
        assertEquals("MultipartFile", duringType);

        // After exiting context
        typeMapper.exitMultipartContext();
        String afterType = typeMapper.getBinaryTypeDeclaration(schema);
        assertEquals("List<int>", afterType);
    }

    // ========================================
    // Type Constant Tests
    // ========================================

    @Test
    @DisplayName("getMultipartFileType: should return correct type name")
    void testGetMultipartFileType() {
        assertEquals("MultipartFile", typeMapper.getMultipartFileType());
    }

    @Test
    @DisplayName("getListIntType: should return correct type name")
    void testGetListIntType() {
        assertEquals("List<int>", typeMapper.getListIntType());
    }
}
