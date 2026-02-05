package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartModelEnricher.
 * Tests model enrichment with composition alternatives (oneOf/anyOf).
 */
class DartModelEnricherTest {

    private DartAcdcGenerator generator;
    private DartTestDataGenerator testDataGenerator;
    private DartDiscriminatorProcessor discriminatorProcessor;
    private DartModelEnricher enricher;

    @BeforeEach
    void setUp() {
        // Create real instances for integration-style testing
        generator = new DartAcdcGenerator();
        generator.processOpts(); // Initialize generator

        // Create test data generator with empty model schemas and generator's primitives
        testDataGenerator = new DartTestDataGenerator(new HashMap<>(), generator.languageSpecificPrimitives());
        discriminatorProcessor = new DartDiscriminatorProcessor(generator, testDataGenerator);
        enricher = new DartModelEnricher(generator, testDataGenerator, discriminatorProcessor);
    }

    @Test
    @DisplayName("Should return empty list for null schemas")
    void testEnrichWithCompositionAlternatives_NullSchemas() {
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives("Animal", null, "oneOf");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list for null schemas");
    }

    @Test
    @DisplayName("Should return empty list for empty schema list")
    void testEnrichWithCompositionAlternatives_EmptySchemas() {
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", Collections.emptyList(), "oneOf");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list for empty schemas");
    }

    @Test
    @DisplayName("Should create metadata for reference schema")
    void testEnrichWithCompositionAlternatives_ReferenceSchema() {
        // Setup
        Schema<Object> dogSchema = new Schema<>();
        dogSchema.set$ref("#/components/schemas/Dog");
        List<Schema> schemas = Collections.singletonList(dogSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertEquals(1, result.size());
        Map<String, Object> alternative = result.get(0);
        assertEquals("Animal", alternative.get("parentClassName"));
        assertTrue((Boolean) alternative.get("isRef"));
        assertEquals("Dog", alternative.get("schemaName"));
        assertEquals("Dog", alternative.get("subclassName"));
        assertNotNull(alternative.get("importPath"));
        assertNotNull(alternative.get("testJson"));
        assertFalse((Boolean) alternative.get("hasNext"));
    }

    @Test
    @DisplayName("Should create metadata for multiple reference schemas with hasNext flag")
    void testEnrichWithCompositionAlternatives_MultipleReferences() {
        // Setup
        Schema<Object> dogSchema = new Schema<>();
        dogSchema.set$ref("#/components/schemas/Dog");
        Schema<Object> catSchema = new Schema<>();
        catSchema.set$ref("#/components/schemas/Cat");
        List<Schema> schemas = Arrays.asList(dogSchema, catSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertEquals(2, result.size());

        // First alternative (Dog) should have hasNext = true
        Map<String, Object> dogAlternative = result.get(0);
        assertEquals("Dog", dogAlternative.get("subclassName"));
        assertTrue((Boolean) dogAlternative.get("hasNext"), "First alternative should have hasNext=true");

        // Second alternative (Cat) should have hasNext = false
        Map<String, Object> catAlternative = result.get(1);
        assertEquals("Cat", catAlternative.get("subclassName"));
        assertFalse((Boolean) catAlternative.get("hasNext"), "Last alternative should have hasNext=false");
    }

    @Test
    @DisplayName("Should create wrapper metadata for primitive string schema")
    void testEnrichWithCompositionAlternatives_PrimitiveString() {
        // Setup
        StringSchema stringSchema = new StringSchema();
        List<Schema> schemas = Collections.singletonList(stringSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertEquals(1, result.size());
        Map<String, Object> alternative = result.get(0);
        assertEquals("Animal", alternative.get("parentClassName"));
        assertTrue((Boolean) alternative.get("isPrimitive"));
        assertEquals("String", alternative.get("dartType"));
        assertEquals("AnimalString", alternative.get("subclassName"));
        assertNotNull(alternative.get("testValue"));
        assertFalse((Boolean) alternative.get("hasNext"));
    }

    @Test
    @DisplayName("Should create wrapper metadata for primitive integer schema")
    void testEnrichWithCompositionAlternatives_PrimitiveInteger() {
        // Setup
        IntegerSchema integerSchema = new IntegerSchema();
        List<Schema> schemas = Collections.singletonList(integerSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertEquals(1, result.size());
        Map<String, Object> alternative = result.get(0);
        assertEquals("Animal", alternative.get("parentClassName"));
        assertTrue((Boolean) alternative.get("isPrimitive"));
        assertEquals("int", alternative.get("dartType"));
        assertEquals("AnimalInt", alternative.get("subclassName"));
        assertNotNull(alternative.get("testValue"));
        assertFalse((Boolean) alternative.get("hasNext"));
    }

    @Test
    @DisplayName("Should create Option naming for inline object schema")
    void testEnrichWithCompositionAlternatives_InlineObjectSchema() {
        // Setup
        ObjectSchema objectSchema = new ObjectSchema();
        List<Schema> schemas = Collections.singletonList(objectSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertEquals(1, result.size());
        Map<String, Object> alternative = result.get(0);
        assertEquals("Animal", alternative.get("parentClassName"));
        assertTrue((Boolean) alternative.get("isInline"));
        assertEquals("AnimalOption1", alternative.get("subclassName"));
        assertEquals(1, alternative.get("index"));
        assertEquals("<String, dynamic>{}", alternative.get("testJson"));
        assertFalse((Boolean) alternative.get("hasNext"));
    }

    @Test
    @DisplayName("Should handle multiple inline objects with incrementing Option numbers")
    void testEnrichWithCompositionAlternatives_MultipleInlineObjects() {
        // Setup
        ObjectSchema object1 = new ObjectSchema();
        ObjectSchema object2 = new ObjectSchema();
        ObjectSchema object3 = new ObjectSchema();
        List<Schema> schemas = Arrays.asList(object1, object2, object3);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "anyOf");

        // Verify
        assertEquals(3, result.size());
        assertEquals("AnimalOption1", result.get(0).get("subclassName"));
        assertEquals(1, result.get(0).get("index"));
        assertEquals("AnimalOption2", result.get(1).get("subclassName"));
        assertEquals(2, result.get(1).get("index"));
        assertEquals("AnimalOption3", result.get(2).get("subclassName"));
        assertEquals(3, result.get(2).get("index"));
    }

    @Test
    @DisplayName("Should handle mixed types: reference, primitive, and inline")
    void testEnrichWithCompositionAlternatives_MixedTypes() {
        // Setup: ref, primitive, inline object
        Schema<Object> dogSchema = new Schema<>();
        dogSchema.set$ref("#/components/schemas/Dog");

        StringSchema stringSchema = new StringSchema();

        ObjectSchema objectSchema = new ObjectSchema();

        List<Schema> schemas = Arrays.asList(dogSchema, stringSchema, objectSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "anyOf");

        // Verify
        assertEquals(3, result.size());

        // Reference alternative
        Map<String, Object> refAlt = result.get(0);
        assertTrue((Boolean) refAlt.get("isRef"));
        assertEquals("Dog", refAlt.get("subclassName"));
        assertTrue((Boolean) refAlt.get("hasNext"));

        // Primitive alternative
        Map<String, Object> primitiveAlt = result.get(1);
        assertTrue((Boolean) primitiveAlt.get("isPrimitive"));
        assertEquals("AnimalString", primitiveAlt.get("subclassName"));
        assertTrue((Boolean) primitiveAlt.get("hasNext"));

        // Inline alternative
        Map<String, Object> inlineAlt = result.get(2);
        assertTrue((Boolean) inlineAlt.get("isInline"));
        assertEquals("AnimalOption3", inlineAlt.get("subclassName"));
        assertFalse((Boolean) inlineAlt.get("hasNext")); // Last one
    }

    @Test
    @DisplayName("Should skip schema with neither $ref nor type")
    void testEnrichWithCompositionAlternatives_InvalidSchema() {
        // Setup: Schema with neither $ref nor type
        Schema<Object> invalidSchema = new Schema<>();
        // Don't set $ref or type
        List<Schema> schemas = Collections.singletonList(invalidSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify
        assertTrue(result.isEmpty(), "Should skip invalid schemas without $ref or type");
    }

    @Test
    @DisplayName("Should skip invalid schema but process valid ones")
    void testEnrichWithCompositionAlternatives_MixedValidAndInvalid() {
        // Setup
        Schema<Object> invalidSchema = new Schema<>(); // No $ref or type

        Schema<Object> dogSchema = new Schema<>();
        dogSchema.set$ref("#/components/schemas/Dog");

        List<Schema> schemas = Arrays.asList(invalidSchema, dogSchema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Animal", schemas, "oneOf");

        // Verify - only the valid schema should be processed
        assertEquals(1, result.size());
        assertEquals("Dog", result.get(0).get("subclassName"));
    }

    @Test
    @DisplayName("Should create metadata with null $ref gracefully")
    void testCreateAlternativeMetadata_NullSchema() {
        // Schema with null type and null $ref should return null
        Schema<Object> nullSchema = new Schema<>();

        Map<String, Object> result = enricher.createAlternativeMetadata(
                "Animal", nullSchema, 0, false, "oneOf");

        assertNull(result, "Should return null for invalid schema");
    }

    @Test
    @DisplayName("Should extract correct schema name from $ref")
    void testEnrichWithCompositionAlternatives_ComplexRef() {
        // Setup
        Schema<Object> schema = new Schema<>();
        schema.set$ref("#/components/schemas/MyComplexModel");
        List<Schema> schemas = Collections.singletonList(schema);

        // Execute
        List<Map<String, Object>> result = enricher.enrichWithCompositionAlternatives(
                "Parent", schemas, "oneOf");

        // Verify
        assertEquals(1, result.size());
        assertEquals("MyComplexModel", result.get(0).get("schemaName"));
        assertEquals("MyComplexModel", result.get(0).get("subclassName"));
    }
}
