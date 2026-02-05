package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenParameter;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartRequestBodyFactory.
 *
 * Tests request body parameter creation with special focus on:
 * - Multipart/form-data detection
 * - Context management
 * - Vendor extension marking
 */
@DisplayName("DartRequestBodyFactory")
class DartRequestBodyFactoryTest {

    private DartAcdcGenerator generator;
    private DartTypeMapper typeMapper;
    private DartRequestBodyFactory factory;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        typeMapper = new DartTypeMapper();
        factory = new DartRequestBodyFactory(generator, typeMapper);
    }

    @Test
    @DisplayName("should create parameter for non-multipart request body")
    void testNonMultipartRequestBody() {
        // Given
        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType jsonMedia = new MediaType();
        jsonMedia.setSchema(new ObjectSchema());
        content.addMediaType("application/json", jsonMedia);
        requestBody.setContent(content);

        Set<String> imports = new HashSet<>();

        // When
        CodegenParameter result = factory.createFromRequestBody(requestBody, imports, "body");

        // Then
        assertNotNull(result);
        assertFalse(result.vendorExtensions.containsKey("x-is-multipart-context"));
    }

    @Test
    @DisplayName("should detect multipart content and set context")
    void testMultipartRequestBody() {
        // Given
        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType multipartMedia = new MediaType();
        multipartMedia.setSchema(new ObjectSchema());
        content.addMediaType("multipart/form-data", multipartMedia);
        requestBody.setContent(content);

        Set<String> imports = new HashSet<>();

        // When
        CodegenParameter result = factory.createFromRequestBody(requestBody, imports, "body");

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.vendorExtensions.getOrDefault("x-is-multipart-context", false));
    }

    @Test
    @DisplayName("should mark binary parameter in multipart context")
    void testMultipartBinaryParameter() {
        // Given
        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType multipartMedia = new MediaType();

        Schema<?> binarySchema = new StringSchema();
        binarySchema.setFormat("binary");
        multipartMedia.setSchema(binarySchema);

        content.addMediaType("multipart/form-data", multipartMedia);
        requestBody.setContent(content);

        Set<String> imports = new HashSet<>();

        // When
        CodegenParameter result = factory.createFromRequestBody(requestBody, imports, "file");

        // Then
        assertNotNull(result);
        // The parameter should be marked as binary
        assertTrue(result.isBinary || "file".equals(result.baseType));
        // Should have multipart context marker
        assertTrue((Boolean) result.vendorExtensions.getOrDefault("x-is-multipart-context", false));
    }

    @Test
    @DisplayName("should handle request body with no content")
    void testRequestBodyWithoutContent() {
        // Given
        RequestBody requestBody = new RequestBody();
        // No content set

        Set<String> imports = new HashSet<>();

        // When/Then - Should not throw exception
        CodegenParameter result = factory.createFromRequestBody(requestBody, imports, "body");

        // Result may be null or a parameter without multipart markers
        if (result != null) {
            assertFalse(result.vendorExtensions.containsKey("x-is-multipart-context"));
        }
    }

    @Test
    @DisplayName("should handle multipart request with complex schema")
    void testMultipartComplexSchema() {
        // Given
        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        MediaType multipartMedia = new MediaType();

        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("name", new StringSchema());
        schema.addProperty("file", new StringSchema().format("binary"));
        multipartMedia.setSchema(schema);

        content.addMediaType("multipart/form-data", multipartMedia);
        requestBody.setContent(content);

        Set<String> imports = new HashSet<>();

        // When
        CodegenParameter result = factory.createFromRequestBody(requestBody, imports, "formData");

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.vendorExtensions.getOrDefault("x-is-multipart-context", false));
    }
}
