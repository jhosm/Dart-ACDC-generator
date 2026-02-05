package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartDiscriminatorProcessor.
 *
 * Tests discriminator processing, sealed class registration,
 * and utility methods for schema reference parsing and primitive type detection.
 */
@DisplayName("DartDiscriminatorProcessor Tests")
class DartDiscriminatorProcessorTest {

    private DartAcdcGenerator generator;
    private DartTestDataGenerator testDataGenerator;
    private DartDiscriminatorProcessor processor;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        testDataGenerator = new DartTestDataGenerator(new HashMap<>(), Set.of("String", "int", "double", "bool"));
        processor = new DartDiscriminatorProcessor(generator, testDataGenerator);
    }

    @Test
    @DisplayName("processDiscriminator should handle schema without discriminator")
    void testProcessDiscriminatorWithoutDiscriminator() {
        // Arrange
        String schemaName = "Animal";
        Schema<?> schema = new StringSchema();
        CodegenModel model = new CodegenModel();

        // Act
        processor.processDiscriminator(schemaName, schema, model);

        // Assert
        assertFalse((Boolean) model.vendorExtensions.get("x-has-discriminator"));
    }

    @Test
    @DisplayName("processDiscriminator should handle null discriminator property name")
    @SuppressWarnings("unchecked")
    void testProcessDiscriminatorWithNullPropertyName() {
        // Arrange
        String schemaName = "Animal";
        @SuppressWarnings("rawtypes")
        Schema schema = new StringSchema();
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName(null);
        schema.setDiscriminator(discriminator);
        CodegenModel model = new CodegenModel();

        // Act
        processor.processDiscriminator(schemaName, schema, model);

        // Assert
        assertFalse((Boolean) model.vendorExtensions.get("x-has-discriminator"));
    }

    @Test
    @DisplayName("processDiscriminator should handle empty discriminator property name")
    @SuppressWarnings("unchecked")
    void testProcessDiscriminatorWithEmptyPropertyName() {
        // Arrange
        String schemaName = "Animal";
        @SuppressWarnings("rawtypes")
        Schema schema = new StringSchema();
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("");
        schema.setDiscriminator(discriminator);
        CodegenModel model = new CodegenModel();

        // Act
        processor.processDiscriminator(schemaName, schema, model);

        // Assert
        assertFalse((Boolean) model.vendorExtensions.get("x-has-discriminator"));
    }

    @Test
    @DisplayName("processDiscriminator should process discriminator with property name")
    @SuppressWarnings("unchecked")
    void testProcessDiscriminatorWithPropertyName() {
        // Arrange
        String schemaName = "Animal";
        @SuppressWarnings("rawtypes")
        Schema schema = new StringSchema();
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("petType");
        schema.setDiscriminator(discriminator);
        CodegenModel model = new CodegenModel();

        // Act
        processor.processDiscriminator(schemaName, schema, model);

        // Assert
        assertTrue((Boolean) model.vendorExtensions.get("x-has-discriminator"));
        assertEquals("petType", model.vendorExtensions.get("x-discriminator-name"));
    }

    @Test
    @DisplayName("processDiscriminator should process discriminator with mapping")
    @SuppressWarnings("unchecked")
    void testProcessDiscriminatorWithMapping() {
        // Arrange
        String schemaName = "Animal";
        @SuppressWarnings("rawtypes")
        Schema schema = new StringSchema();
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("petType");

        Map<String, String> mapping = new HashMap<>();
        mapping.put("dog", "#/components/schemas/Dog");
        mapping.put("cat", "#/components/schemas/Cat");
        discriminator.setMapping(mapping);

        schema.setDiscriminator(discriminator);
        CodegenModel model = new CodegenModel();

        // Act
        processor.processDiscriminator(schemaName, schema, model);

        // Assert
        assertTrue((Boolean) model.vendorExtensions.get("x-has-discriminator"));
        assertEquals("petType", model.vendorExtensions.get("x-discriminator-name"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> discriminatorMapping =
            (List<Map<String, Object>>) model.vendorExtensions.get("x-discriminator-mapping");

        assertNotNull(discriminatorMapping);
        assertEquals(2, discriminatorMapping.size());

        // Verify mapping entries
        Map<String, Object> dogEntry = discriminatorMapping.stream()
            .filter(entry -> entry.get("mappingKey").equals("dog"))
            .findFirst()
            .orElse(null);
        assertNotNull(dogEntry);
        assertEquals("Dog", dogEntry.get("schemaName"));
        assertEquals("Dog", dogEntry.get("subclassName"));
    }

    @Test
    @DisplayName("registerSealedClassExtensions should register reference schemas")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testRegisterSealedClassExtensionsWithReferences() {
        // Arrange
        String parentName = "Animal";
        List<Schema> schemas = new ArrayList<>();

        Schema dogRef = new Schema();
        dogRef.set$ref("#/components/schemas/Dog");

        Schema catRef = new Schema();
        catRef.set$ref("#/components/schemas/Cat");

        schemas.add(dogRef);
        schemas.add(catRef);

        // Act
        processor.registerSealedClassExtensions(parentName, schemas);

        // Assert
        Map<String, String> extensions = processor.getSealedClassExtensions();
        assertEquals(2, extensions.size());
        assertEquals("Animal", extensions.get("Dog"));
        assertEquals("Animal", extensions.get("Cat"));
    }

    @Test
    @DisplayName("registerSealedClassExtensions should ignore inline schemas")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testRegisterSealedClassExtensionsIgnoresInlineSchemas() {
        // Arrange
        String parentName = "Animal";
        List<Schema> schemas = new ArrayList<>();

        Schema inlineSchema = new Schema();
        inlineSchema.setType("object");

        schemas.add(inlineSchema);

        // Act
        processor.registerSealedClassExtensions(parentName, schemas);

        // Assert
        Map<String, String> extensions = processor.getSealedClassExtensions();
        assertEquals(0, extensions.size());
    }

    @Test
    @DisplayName("getSealedClassExtensions should return unmodifiable map")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testGetSealedClassExtensionsReturnsUnmodifiable() {
        // Arrange
        String parentName = "Animal";
        List<Schema> schemas = new ArrayList<>();
        Schema dogRef = new Schema();
        dogRef.set$ref("#/components/schemas/Dog");
        schemas.add(dogRef);
        processor.registerSealedClassExtensions(parentName, schemas);

        // Act
        Map<String, String> extensions = processor.getSealedClassExtensions();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            extensions.put("Bird", "Animal");
        });
    }

    @Test
    @DisplayName("isPrimitiveType should return true for OpenAPI primitive types")
    void testIsPrimitiveTypeForPrimitives() {
        assertTrue(processor.isPrimitiveType("string"));
        assertTrue(processor.isPrimitiveType("integer"));
        assertTrue(processor.isPrimitiveType("number"));
        assertTrue(processor.isPrimitiveType("boolean"));
    }

    @Test
    @DisplayName("isPrimitiveType should return false for non-primitive types")
    void testIsPrimitiveTypeForNonPrimitives() {
        assertFalse(processor.isPrimitiveType("array"));
        assertFalse(processor.isPrimitiveType("object"));
        assertFalse(processor.isPrimitiveType("custom"));
        assertFalse(processor.isPrimitiveType(null));
    }

    @Test
    @DisplayName("extractSchemaNameFromRef should extract schema name from valid $ref")
    void testExtractSchemaNameFromValidRef() {
        assertEquals("Pet", processor.extractSchemaNameFromRef("#/components/schemas/Pet"));
        assertEquals("Dog", processor.extractSchemaNameFromRef("#/components/schemas/Dog"));
        assertEquals("Category", processor.extractSchemaNameFromRef("#/components/schemas/Category"));
    }

    @Test
    @DisplayName("extractSchemaNameFromRef should handle malformed $ref without slash")
    void testExtractSchemaNameFromRefWithoutSlash() {
        String ref = "InvalidRef";
        String result = processor.extractSchemaNameFromRef(ref);
        assertEquals(ref, result);
    }

    @Test
    @DisplayName("extractSchemaNameFromRef should handle null $ref")
    void testExtractSchemaNameFromNullRef() {
        String result = processor.extractSchemaNameFromRef(null);
        assertEquals("UnknownSchema", result);
    }

    @Test
    @DisplayName("extractSchemaNameFromRef should handle empty $ref")
    void testExtractSchemaNameFromEmptyRef() {
        String result = processor.extractSchemaNameFromRef("");
        assertEquals("UnknownSchema", result);
    }

    @Test
    @DisplayName("processDiscriminator and registerSealedClassExtensions should work together")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testIntegrationDiscriminatorAndSealed() {
        // Arrange
        String schemaName = "Animal";
        Schema schema = new StringSchema();

        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("petType");
        schema.setDiscriminator(discriminator);

        CodegenModel model = new CodegenModel();

        List<Schema> oneOfSchemas = new ArrayList<>();
        Schema dogRef = new Schema();
        dogRef.set$ref("#/components/schemas/Dog");
        oneOfSchemas.add(dogRef);

        // Act
        processor.processDiscriminator(schemaName, schema, model);
        processor.registerSealedClassExtensions(schemaName, oneOfSchemas);

        // Assert
        assertTrue((Boolean) model.vendorExtensions.get("x-has-discriminator"));
        Map<String, String> extensions = processor.getSealedClassExtensions();
        assertEquals("Animal", extensions.get("Dog"));
    }
}
