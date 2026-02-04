package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.openapitools.codegen.CodegenModel;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartModelFactory.
 *
 * Tests the factory's coordination of model creation, enum detection,
 * composition processing, and sealed class extension relationships.
 */
@DisplayName("DartModelFactory Tests")
class DartModelFactoryTest {

    private DartAcdcGenerator generator;
    private Map<String, String> sealedClassExtensions;
    private DartModelFactory factory;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        sealedClassExtensions = new HashMap<>();
        factory = new DartModelFactory(generator, sealedClassExtensions);
    }

    @Test
    @DisplayName("createModel should create basic model from schema")
    void testCreateModelBasic() {
        // Arrange
        String modelName = "Pet";
        Schema<?> schema = new StringSchema();
        schema.setType("object");

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertEquals("Pet", result.classname);
        assertFalse(result.isEnum);
    }

    @Test
    @DisplayName("createModel should detect standalone enum schemas")
    @SuppressWarnings("unchecked")
    void testCreateModelDetectsStandaloneEnum() {
        // Arrange
        String modelName = "Status";
        @SuppressWarnings("rawtypes")
        Schema schema = new StringSchema();
        schema.setEnum(Arrays.asList("active", "inactive", "pending"));
        // No properties - standalone enum

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertTrue(result.isEnum, "Model should be marked as enum");
    }

    @Test
    @DisplayName("createModel should not mark enum-valued model with properties as enum")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testCreateModelDoesNotMarkEnumWithProperties() {
        // Arrange
        String modelName = "Model";
        Schema schema = new StringSchema();
        schema.setEnum(Arrays.asList("value1", "value2"));
        Map<String, Schema> properties = new HashMap<>();
        properties.put("prop1", new StringSchema());
        schema.setProperties(properties); // Has properties

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertFalse(result.isEnum, "Model with properties should not be marked as enum");
    }

    @Test
    @DisplayName("createModel should process oneOf composition")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCreateModelProcessesOneOf() {
        // Arrange
        String modelName = "Animal";
        Schema schema = new Schema();
        List<Schema> oneOfSchemas = new ArrayList<>();

        // Create reference schemas
        Schema dogRef = new Schema();
        dogRef.set$ref("#/components/schemas/Dog");
        Schema catRef = new Schema();
        catRef.set$ref("#/components/schemas/Cat");

        oneOfSchemas.add(dogRef);
        oneOfSchemas.add(catRef);
        schema.setOneOf(oneOfSchemas);

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertEquals("Animal", result.classname);
        // Verify oneOf vendor extension is set
        assertTrue(result.vendorExtensions.containsKey("x-is-one-of"));
    }

    @Test
    @DisplayName("createModel should process anyOf composition")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCreateModelProcessesAnyOf() {
        // Arrange
        String modelName = "FlexibleModel";
        Schema schema = new Schema();
        List<Schema> anyOfSchemas = new ArrayList<>();

        // Create reference schemas
        Schema option1 = new Schema();
        option1.set$ref("#/components/schemas/Option1");
        Schema option2 = new Schema();
        option2.set$ref("#/components/schemas/Option2");

        anyOfSchemas.add(option1);
        anyOfSchemas.add(option2);
        schema.setAnyOf(anyOfSchemas);

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertEquals("FlexibleModel", result.classname);
        // Verify anyOf vendor extension is set
        assertTrue(result.vendorExtensions.containsKey("x-is-any-of"));
    }

    @Test
    @DisplayName("createModel should apply sealed class extension relationships")
    void testCreateModelAppliesSealedClassExtension() {
        // Arrange
        String modelName = "Dog";
        String parentClass = "Animal";
        Schema<?> schema = new StringSchema();
        schema.setType("object");

        // Register sealed class relationship
        sealedClassExtensions.put("Dog", parentClass);

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertEquals(parentClass, result.parent, "Parent should be set to sealed class");
        assertTrue((Boolean) result.vendorExtensions.get("x-extends-sealed-class"));
        assertEquals(parentClass, result.vendorExtensions.get("x-sealed-parent"));
        assertEquals("animal", result.vendorExtensions.get("x-sealed-parent-filename"));
    }

    @Test
    @DisplayName("createModel should not apply sealed class extension when not registered")
    void testCreateModelWithoutSealedClassExtension() {
        // Arrange
        String modelName = "Cat";
        Schema<?> schema = new StringSchema();
        schema.setType("object");

        // Don't register any sealed class relationship

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNull(result.parent, "Parent should not be set");
        assertFalse(result.vendorExtensions.containsKey("x-extends-sealed-class"));
    }

    @Test
    @DisplayName("createModel should handle schema with both oneOf and sealed class extension")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCreateModelWithOneOfAndSealedClass() {
        // Arrange
        String modelName = "Shape";
        Schema schema = new Schema();
        List<Schema> oneOfSchemas = new ArrayList<>();

        Schema circleRef = new Schema();
        circleRef.set$ref("#/components/schemas/Circle");
        oneOfSchemas.add(circleRef);

        schema.setOneOf(oneOfSchemas);

        // Register sealed class relationship
        sealedClassExtensions.put("Shape", "Geometry");

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertEquals("Geometry", result.parent);
        assertTrue(result.vendorExtensions.containsKey("x-is-one-of"));
    }

    @Test
    @DisplayName("createModel should handle schema without composition")
    void testCreateModelWithoutComposition() {
        // Arrange
        String modelName = "SimpleModel";
        Schema<?> schema = new StringSchema();
        schema.setType("object");
        // No oneOf, anyOf, or enum

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertEquals("SimpleModel", result.classname);
        assertFalse(result.isEnum);
        assertFalse(result.vendorExtensions.containsKey("x-is-one-of"));
        assertFalse(result.vendorExtensions.containsKey("x-is-any-of"));
    }

    @Test
    @DisplayName("createModel should process both oneOf and anyOf if present")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCreateModelWithBothOneOfAndAnyOf() {
        // Arrange
        String modelName = "ComplexModel";
        Schema schema = new Schema();

        List<Schema> oneOfSchemas = new ArrayList<>();
        Schema oneOfRef = new Schema();
        oneOfRef.set$ref("#/components/schemas/Option1");
        oneOfSchemas.add(oneOfRef);

        List<Schema> anyOfSchemas = new ArrayList<>();
        Schema anyOfRef = new Schema();
        anyOfRef.set$ref("#/components/schemas/Option2");
        anyOfSchemas.add(anyOfRef);

        schema.setOneOf(oneOfSchemas);
        schema.setAnyOf(anyOfSchemas);

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        assertTrue(result.vendorExtensions.containsKey("x-is-one-of"));
        assertTrue(result.vendorExtensions.containsKey("x-is-any-of"));
    }

    @Test
    @DisplayName("createModel should preserve classname from base model")
    void testCreateModelPreservesClassname() {
        // Arrange
        String modelName = "user_profile"; // snake_case input
        Schema<?> schema = new StringSchema();
        schema.setType("object");

        // Act
        CodegenModel result = factory.createModel(modelName, schema);

        // Assert
        assertNotNull(result);
        // Generator converts to PascalCase
        assertEquals("UserProfile", result.classname);
    }
}
