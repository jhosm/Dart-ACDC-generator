package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartAllOfFlattener.
 *
 * Tests allOf composition flattening including property merging,
 * nested composition handling, conflict resolution, and attribute copying.
 */
@DisplayName("DartAllOfFlattener Tests")
class DartAllOfFlattenerTest {

    private DartAllOfFlattener flattener;

    @BeforeEach
    void setUp() {
        flattener = new DartAllOfFlattener();
    }

    // ── flattenAllOf entry point ──────────────────────────────────────

    @Nested
    @DisplayName("flattenAllOf")
    class FlattenAllOfTests {

        @Test
        @DisplayName("should handle empty schema map")
        void testEmptySchemaMap() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertTrue(schemas.isEmpty());
        }

        @Test
        @DisplayName("should skip schemas without allOf")
        @SuppressWarnings("rawtypes")
        void testSchemasWithoutAllOf() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();
            Schema simpleSchema = new Schema();
            simpleSchema.setType("object");
            simpleSchema.setProperties(Map.of("name", new StringSchema()));
            schemas.put("Simple", simpleSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — schema should be untouched
            assertSame(simpleSchema, schemas.get("Simple"));
        }

        @Test
        @DisplayName("should skip schemas with null allOf")
        @SuppressWarnings("rawtypes")
        void testSchemasWithNullAllOf() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();
            Schema schema = new Schema();
            // allOf is null by default
            schemas.put("NullAllOf", schema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — schema unchanged
            assertSame(schema, schemas.get("NullAllOf"));
        }

        @Test
        @DisplayName("should skip schemas with empty allOf list")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testSchemasWithEmptyAllOf() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();
            Schema schema = new Schema();
            schema.setAllOf(new ArrayList<>());
            schemas.put("EmptyAllOf", schema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — schema unchanged because allOf is empty
            assertSame(schema, schemas.get("EmptyAllOf"));
        }

        @Test
        @DisplayName("should flatten simple allOf with inline schemas")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testFlattenSimpleInlineAllOf() {
            // Arrange: Dog allOf: [{ name: string }, { breed: string }]
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema schema1 = new Schema();
            schema1.setProperties(new LinkedHashMap<>(Map.of("name", new StringSchema())));

            Schema schema2 = new Schema();
            schema2.setProperties(new LinkedHashMap<>(Map.of("breed", new StringSchema())));

            Schema dogSchema = new Schema();
            dogSchema.setAllOf(List.of(schema1, schema2));
            schemas.put("Dog", dogSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Dog");
            assertNotSame(dogSchema, flattened, "Should replace with new flattened schema");
            assertNotNull(flattened.getProperties());
            assertEquals(2, flattened.getProperties().size());
            assertTrue(flattened.getProperties().containsKey("name"));
            assertTrue(flattened.getProperties().containsKey("breed"));
        }

        @Test
        @DisplayName("should flatten allOf with $ref to another schema")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testFlattenAllOfWithRef() {
            // Arrange: Animal has name; Dog allOf: [$ref Animal, { breed }]
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema animalSchema = new Schema();
            animalSchema.setProperties(new LinkedHashMap<>(Map.of("name", new StringSchema())));
            animalSchema.setRequired(List.of("name"));
            schemas.put("Animal", animalSchema);

            Schema refSchema = new Schema();
            refSchema.set$ref("#/components/schemas/Animal");

            Schema inlineSchema = new Schema();
            inlineSchema.setProperties(new LinkedHashMap<>(Map.of("breed", new StringSchema())));
            inlineSchema.setRequired(List.of("breed"));

            Schema dogSchema = new Schema();
            dogSchema.setAllOf(List.of(refSchema, inlineSchema));
            schemas.put("Dog", dogSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Dog");
            assertNotNull(flattened.getProperties());
            assertEquals(2, flattened.getProperties().size());
            assertTrue(flattened.getProperties().containsKey("name"));
            assertTrue(flattened.getProperties().containsKey("breed"));

            // Required fields from both schemas should be merged
            assertNotNull(flattened.getRequired());
            assertTrue(flattened.getRequired().contains("name"));
            assertTrue(flattened.getRequired().contains("breed"));
        }

        @Test
        @DisplayName("should only flatten schemas that have allOf, leaving others untouched")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testMixedSchemasOnlyAllOfFlattened() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            // Simple schema without allOf
            Schema simpleSchema = new Schema();
            simpleSchema.setType("object");
            schemas.put("Simple", simpleSchema);

            // Schema with allOf
            Schema propSchema = new Schema();
            propSchema.setProperties(new LinkedHashMap<>(Map.of("id", new StringSchema())));

            Schema allOfSchema = new Schema();
            allOfSchema.setAllOf(List.of(propSchema));
            schemas.put("Composed", allOfSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertSame(simpleSchema, schemas.get("Simple"), "Non-allOf schema should be untouched");
            assertNotSame(allOfSchema, schemas.get("Composed"), "allOf schema should be replaced");
        }

        @Test
        @DisplayName("should handle multiple schemas with allOf in same map")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testMultipleAllOfSchemas() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema base = new Schema();
            base.setProperties(new LinkedHashMap<>(Map.of("id", new StringSchema())));
            schemas.put("Base", base);

            // First allOf schema
            Schema ref1 = new Schema();
            ref1.set$ref("#/components/schemas/Base");
            Schema inline1 = new Schema();
            inline1.setProperties(new LinkedHashMap<>(Map.of("color", new StringSchema())));
            Schema cat = new Schema();
            cat.setAllOf(List.of(ref1, inline1));
            schemas.put("Cat", cat);

            // Second allOf schema
            Schema ref2 = new Schema();
            ref2.set$ref("#/components/schemas/Base");
            Schema inline2 = new Schema();
            inline2.setProperties(new LinkedHashMap<>(Map.of("breed", new StringSchema())));
            Schema dog = new Schema();
            dog.setAllOf(List.of(ref2, inline2));
            schemas.put("Dog", dog);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flatCat = schemas.get("Cat");
            assertNotNull(flatCat.getProperties());
            assertTrue(flatCat.getProperties().containsKey("id"));
            assertTrue(flatCat.getProperties().containsKey("color"));

            Schema flatDog = schemas.get("Dog");
            assertNotNull(flatDog.getProperties());
            assertTrue(flatDog.getProperties().containsKey("id"));
            assertTrue(flatDog.getProperties().containsKey("breed"));
        }
    }

    // ── $ref resolution ──────────────────────────────────────────────

    @Nested
    @DisplayName("$ref resolution")
    class RefResolutionTests {

        @Test
        @DisplayName("should handle $ref with empty string")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testEmptyStringRef() {
            // Arrange: $ref is "" — extractSchemaNameFromRef returns "UnknownSchema"
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema refSchema = new Schema();
            refSchema.set$ref("");

            Schema composed = new Schema();
            composed.setAllOf(List.of(refSchema));
            schemas.put("EmptyRef", composed);

            // Act — should not throw; empty ref resolves to "UnknownSchema" which doesn't exist
            flattener.flattenAllOf(schemas);

            // Assert — unresolvable ref skipped, no properties
            Schema flattened = schemas.get("EmptyRef");
            assertNotNull(flattened);
            assertNull(flattened.getProperties());
        }

        @Test
        @DisplayName("should handle $ref without slash separator")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testRefWithoutSlash() {
            // Arrange: $ref is "JustAName" — extractSchemaNameFromRef returns "JustAName" as fallback
            Map<String, Schema> schemas = new LinkedHashMap<>();

            // Add a schema with the exact key "JustAName" so it can be resolved
            Schema target = new Schema();
            target.setProperties(new LinkedHashMap<>(Map.of("x", new StringSchema())));
            schemas.put("JustAName", target);

            Schema refSchema = new Schema();
            refSchema.set$ref("JustAName");

            Schema composed = new Schema();
            composed.setAllOf(List.of(refSchema));
            schemas.put("WeirdRef", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — should resolve successfully using the fallback name
            Schema flattened = schemas.get("WeirdRef");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("x"));
        }

        @Test
        @DisplayName("should handle $ref without slash that cannot be resolved")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testRefWithoutSlashUnresolvable() {
            // Arrange: $ref is "MissingSchema" — returned as-is but not in schemas map
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema refSchema = new Schema();
            refSchema.set$ref("MissingSchema");

            Schema composed = new Schema();
            composed.setAllOf(List.of(refSchema));
            schemas.put("BadRef", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — unresolvable, no properties
            Schema flattened = schemas.get("BadRef");
            assertNotNull(flattened);
            assertNull(flattened.getProperties());
        }

        @Test
        @DisplayName("should handle unresolvable $ref gracefully")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testUnresolvableRef() {
            // Arrange: $ref points to non-existent schema
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema refSchema = new Schema();
            refSchema.set$ref("#/components/schemas/NonExistent");

            Schema composed = new Schema();
            composed.setAllOf(List.of(refSchema));
            schemas.put("Broken", composed);

            // Act — should not throw
            flattener.flattenAllOf(schemas);

            // Assert — flattened with no properties (the ref was skipped)
            Schema flattened = schemas.get("Broken");
            assertNotNull(flattened);
            assertNull(flattened.getProperties());
        }

        @Test
        @DisplayName("should resolve $ref with standard components path")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testResolveStandardRef() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema target = new Schema();
            target.setProperties(new LinkedHashMap<>(Map.of("field", new StringSchema())));
            schemas.put("Target", target);

            Schema ref = new Schema();
            ref.set$ref("#/components/schemas/Target");

            Schema composed = new Schema();
            composed.setAllOf(List.of(ref));
            schemas.put("Source", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Source");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("field"));
        }

        @Test
        @DisplayName("should handle mix of resolvable and unresolvable refs")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testMixedResolvableAndUnresolvable() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema existing = new Schema();
            existing.setProperties(new LinkedHashMap<>(Map.of("real", new StringSchema())));
            schemas.put("Existing", existing);

            Schema goodRef = new Schema();
            goodRef.set$ref("#/components/schemas/Existing");

            Schema badRef = new Schema();
            badRef.set$ref("#/components/schemas/Ghost");

            Schema inline = new Schema();
            inline.setProperties(new LinkedHashMap<>(Map.of("local", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(goodRef, badRef, inline));
            schemas.put("Mixed", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — should have properties from resolved ref and inline, but not from ghost
            Schema flattened = schemas.get("Mixed");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("real"));
            assertTrue(flattened.getProperties().containsKey("local"));
            assertEquals(2, flattened.getProperties().size());
        }
    }

    // ── Nested composition (allOf + oneOf/anyOf) ─────────────────────

    @Nested
    @DisplayName("Nested composition handling")
    class NestedCompositionTests {

        @Test
        @DisplayName("should handle allOf referencing a oneOf schema")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testAllOfWithOneOfRef() {
            // Arrange: PaymentMethod has oneOf; Order allOf refs PaymentMethod
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema oneOfSchema = new Schema();
            oneOfSchema.setOneOf(List.of(new StringSchema(), new StringSchema()));
            schemas.put("PaymentMethod", oneOfSchema);

            Schema ref = new Schema();
            ref.set$ref("#/components/schemas/PaymentMethod");

            Schema inlineProps = new Schema();
            inlineProps.setProperties(new LinkedHashMap<>(Map.of("orderId", new StringSchema())));

            Schema orderSchema = new Schema();
            orderSchema.setAllOf(List.of(ref, inlineProps));
            schemas.put("Order", orderSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — should create a camelCase property typed as the composition schema
            Schema flattened = schemas.get("Order");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("paymentMethod"),
                    "Should create camelCase property for oneOf reference");
            assertTrue(flattened.getProperties().containsKey("orderId"));

            // The paymentMethod property should be a $ref
            Schema paymentProp = (Schema) flattened.getProperties().get("paymentMethod");
            assertEquals("#/components/schemas/PaymentMethod", paymentProp.get$ref());

            // paymentMethod should be in required list
            assertNotNull(flattened.getRequired());
            assertTrue(flattened.getRequired().contains("paymentMethod"));
        }

        @Test
        @DisplayName("should handle allOf referencing an anyOf schema")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testAllOfWithAnyOfRef() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema anyOfSchema = new Schema();
            anyOfSchema.setAnyOf(List.of(new StringSchema(), new StringSchema()));
            schemas.put("Shape", anyOfSchema);

            Schema ref = new Schema();
            ref.set$ref("#/components/schemas/Shape");

            Schema composed = new Schema();
            composed.setAllOf(List.of(ref));
            schemas.put("Drawing", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Drawing");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("shape"));

            Schema shapeProp = (Schema) flattened.getProperties().get("shape");
            assertEquals("#/components/schemas/Shape", shapeProp.get$ref());

            assertNotNull(flattened.getRequired());
            assertTrue(flattened.getRequired().contains("shape"));
        }

        @Test
        @DisplayName("should NOT treat $ref to schema with empty oneOf as nested composition")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testRefToSchemaWithEmptyOneOfNotNestedComposition() {
            // Arrange: Schema has oneOf set but empty → isCompositionSchema returns false
            // This exercises the oneOf != null && !oneOf.isEmpty() branch (empty list path)
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema emptyOneOf = new Schema();
            emptyOneOf.setOneOf(new ArrayList<>());
            emptyOneOf.setProperties(new LinkedHashMap<>(Map.of("val", new StringSchema())));
            schemas.put("EmptyOneOf", emptyOneOf);

            Schema ref = new Schema();
            ref.set$ref("#/components/schemas/EmptyOneOf");

            Schema composed = new Schema();
            composed.setAllOf(List.of(ref));
            schemas.put("Test", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — should merge properties normally, not as nested composition
            Schema flattened = schemas.get("Test");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("val"));
            assertEquals(1, flattened.getProperties().size());
        }

        @Test
        @DisplayName("should NOT treat $ref to schema with empty anyOf as nested composition")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testRefToSchemaWithEmptyAnyOfNotNestedComposition() {
            // Arrange: Schema has anyOf set but empty
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema emptyAnyOf = new Schema();
            emptyAnyOf.setAnyOf(new ArrayList<>());
            emptyAnyOf.setProperties(new LinkedHashMap<>(Map.of("val", new StringSchema())));
            schemas.put("EmptyAnyOf", emptyAnyOf);

            Schema ref = new Schema();
            ref.set$ref("#/components/schemas/EmptyAnyOf");

            Schema composed = new Schema();
            composed.setAllOf(List.of(ref));
            schemas.put("Test", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Test");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("val"));
            assertEquals(1, flattened.getProperties().size());
        }

        @Test
        @DisplayName("should NOT treat inline oneOf as nested composition (no referencedSchemaName)")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testInlineOneOfNotTreatedAsNestedComposition() {
            // Arrange: An inline schema that has oneOf but NO $ref
            // The code checks: isCompositionSchema && referencedSchemaName != null
            // Inline schemas have referencedSchemaName == null, so they go through normal merge
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema inlineOneOf = new Schema();
            inlineOneOf.setOneOf(List.of(new StringSchema()));
            inlineOneOf.setProperties(new LinkedHashMap<>(Map.of("type", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(inlineOneOf));
            schemas.put("WithInlineOneOf", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — should merge properties normally, not create nested composition property
            Schema flattened = schemas.get("WithInlineOneOf");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("type"));
            // Should NOT have a camelCase composition property
            assertEquals(1, flattened.getProperties().size());
        }
    }

    // ── Property merging and conflict handling ───────────────────────

    @Nested
    @DisplayName("Property merging and conflicts")
    class PropertyMergingTests {

        @Test
        @DisplayName("should merge properties from multiple inline schemas")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testMergeMultipleInlineSchemas() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema s2 = new Schema();
            s2.setProperties(new LinkedHashMap<>(Map.of("b", new StringSchema())));

            Schema s3 = new Schema();
            s3.setProperties(new LinkedHashMap<>(Map.of("c", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1, s2, s3));
            schemas.put("ThreeWay", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("ThreeWay");
            assertEquals(3, flattened.getProperties().size());
            assertTrue(flattened.getProperties().containsKey("a"));
            assertTrue(flattened.getProperties().containsKey("b"));
            assertTrue(flattened.getProperties().containsKey("c"));
        }

        @Test
        @DisplayName("should use last definition when property conflict with same type")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testPropertyConflictSameType() {
            // Arrange: Both schemas define "status" as string
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema statusFirst = new StringSchema();
            statusFirst.setDescription("first");

            Schema statusSecond = new StringSchema();
            statusSecond.setDescription("second");

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("status", statusFirst)));

            Schema s2 = new Schema();
            s2.setProperties(new LinkedHashMap<>(Map.of("status", statusSecond)));

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1, s2));
            schemas.put("Conflict", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — last definition wins
            Schema flattened = schemas.get("Conflict");
            Schema statusProp = (Schema) flattened.getProperties().get("status");
            assertEquals("second", statusProp.getDescription());
        }

        @Test
        @DisplayName("should use last definition when property conflict with different types")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testPropertyConflictDifferentTypes() {
            // Arrange: "value" is string in one, integer in another
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema stringValue = new Schema();
            stringValue.setType("string");

            Schema intValue = new Schema();
            intValue.setType("integer");

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("value", stringValue)));

            Schema s2 = new Schema();
            s2.setProperties(new LinkedHashMap<>(Map.of("value", intValue)));

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1, s2));
            schemas.put("TypeConflict", composed);

            // Act — should not throw, just log warning
            flattener.flattenAllOf(schemas);

            // Assert — last definition (integer) wins
            Schema flattened = schemas.get("TypeConflict");
            Schema valueProp = (Schema) flattened.getProperties().get("value");
            assertEquals("integer", valueProp.getType());
        }

        @Test
        @DisplayName("should handle allOf element with null properties")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testAllOfElementWithNullProperties() {
            // Arrange: One schema has properties, one doesn't
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema noProps = new Schema();
            // properties is null by default

            Schema withProps = new Schema();
            withProps.setProperties(new LinkedHashMap<>(Map.of("field", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(noProps, withProps));
            schemas.put("PartialProps", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("PartialProps");
            assertNotNull(flattened.getProperties());
            assertEquals(1, flattened.getProperties().size());
            assertTrue(flattened.getProperties().containsKey("field"));
        }

        @Test
        @DisplayName("should handle allOf where all elements have null properties")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testAllOfAllElementsNullProperties() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema noProps1 = new Schema();
            Schema noProps2 = new Schema();

            Schema composed = new Schema();
            composed.setAllOf(List.of(noProps1, noProps2));
            schemas.put("NoProps", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — no properties on composed schema
            Schema flattened = schemas.get("NoProps");
            assertNull(flattened.getProperties());
        }
    }

    // ── Required fields merging ──────────────────────────────────────

    @Nested
    @DisplayName("Required fields merging")
    class RequiredFieldsTests {

        @Test
        @DisplayName("should merge required fields from multiple schemas")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testMergeRequiredFromMultiple() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));
            s1.setRequired(List.of("a"));

            Schema s2 = new Schema();
            s2.setProperties(new LinkedHashMap<>(Map.of("b", new StringSchema())));
            s2.setRequired(List.of("b"));

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1, s2));
            schemas.put("WithRequired", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("WithRequired");
            assertNotNull(flattened.getRequired());
            assertEquals(2, flattened.getRequired().size());
            assertTrue(flattened.getRequired().contains("a"));
            assertTrue(flattened.getRequired().contains("b"));
        }

        @Test
        @DisplayName("should handle schemas with null required arrays")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testNullRequiredArrays() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));
            // required is null by default

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1));
            schemas.put("NoRequired", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — no required list on result
            Schema flattened = schemas.get("NoRequired");
            assertNull(flattened.getRequired());
        }

        @Test
        @DisplayName("should deduplicate required fields")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testDeduplicateRequired() {
            // Arrange: Both schemas require "id"
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema s1 = new Schema();
            s1.setProperties(new LinkedHashMap<>(Map.of("id", new StringSchema())));
            s1.setRequired(List.of("id"));

            Schema s2 = new Schema();
            s2.setProperties(new LinkedHashMap<>(Map.of("id", new StringSchema())));
            s2.setRequired(List.of("id"));

            Schema composed = new Schema();
            composed.setAllOf(List.of(s1, s2));
            schemas.put("DupRequired", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert — uses LinkedHashSet internally, so "id" appears only once
            Schema flattened = schemas.get("DupRequired");
            assertNotNull(flattened.getRequired());
            assertEquals(1, flattened.getRequired().size());
            assertEquals("id", flattened.getRequired().get(0));
        }
    }

    // ── Composed schema attribute copying ────────────────────────────

    @Nested
    @DisplayName("Composed schema attribute copying")
    class ComposedSchemaAttributeTests {

        @Test
        @DisplayName("should default type to 'object' when original type is null")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testDefaultTypeToObject() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            // type is null by default
            composed.setAllOf(List.of(props));
            schemas.put("NoType", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("NoType");
            assertEquals("object", flattened.getType());
        }

        @Test
        @DisplayName("should preserve original type when set")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testPreserveOriginalType() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setType("object");
            composed.setAllOf(List.of(props));
            schemas.put("WithType", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertEquals("object", schemas.get("WithType").getType());
        }

        @Test
        @DisplayName("should copy description from original schema")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testCopyDescription() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setDescription("A composed schema");
            composed.setAllOf(List.of(props));
            schemas.put("WithDesc", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertEquals("A composed schema", schemas.get("WithDesc").getDescription());
        }

        @Test
        @DisplayName("should not set description when original has none")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testNoDescriptionWhenOriginalHasNone() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(props));
            schemas.put("NoDesc", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertNull(schemas.get("NoDesc").getDescription());
        }

        @Test
        @DisplayName("should copy title from original schema")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testCopyTitle() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setTitle("MyTitle");
            composed.setAllOf(List.of(props));
            schemas.put("WithTitle", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertEquals("MyTitle", schemas.get("WithTitle").getTitle());
        }

        @Test
        @DisplayName("should not set title when original has none")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testNoTitleWhenOriginalHasNone() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setAllOf(List.of(props));
            schemas.put("NoTitle", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            assertNull(schemas.get("NoTitle").getTitle());
        }

        @Test
        @DisplayName("should copy all attributes together")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testCopyAllAttributes() {
            // Arrange
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema props = new Schema();
            props.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));

            Schema composed = new Schema();
            composed.setType("object");
            composed.setDescription("Full description");
            composed.setTitle("Full title");
            composed.setAllOf(List.of(props));
            schemas.put("FullAttrs", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("FullAttrs");
            assertEquals("object", flattened.getType());
            assertEquals("Full description", flattened.getDescription());
            assertEquals("Full title", flattened.getTitle());
        }
    }

    // ── Integration-style scenarios ──────────────────────────────────

    @Nested
    @DisplayName("Integration scenarios")
    class IntegrationTests {

        @Test
        @DisplayName("should handle the Javadoc example: Dog allOf Animal + inline")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testJavadocExample() {
            // Arrange — exactly the example from the class Javadoc
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema animalSchema = new Schema();
            animalSchema.setType("object");
            animalSchema.setProperties(new LinkedHashMap<>(Map.of("name", new StringSchema())));
            schemas.put("Animal", animalSchema);

            Schema refToAnimal = new Schema();
            refToAnimal.set$ref("#/components/schemas/Animal");

            Schema dogInline = new Schema();
            dogInline.setType("object");
            dogInline.setProperties(new LinkedHashMap<>(Map.of("breed", new StringSchema())));

            Schema dogSchema = new Schema();
            dogSchema.setAllOf(List.of(refToAnimal, dogInline));
            schemas.put("Dog", dogSchema);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema dog = schemas.get("Dog");
            assertNotNull(dog.getProperties());
            assertEquals(2, dog.getProperties().size());
            assertTrue(dog.getProperties().containsKey("name"));
            assertTrue(dog.getProperties().containsKey("breed"));
        }

        @Test
        @DisplayName("should handle deep allOf chain: C extends B extends A")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testDeepAllOfChain() {
            // Arrange: A has "a"; B allOf [A, {b}]; C allOf [B, {c}]
            // Note: flattener works on the map, so B must be flattened before C can ref it
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema schemaA = new Schema();
            schemaA.setProperties(new LinkedHashMap<>(Map.of("a", new StringSchema())));
            schemas.put("A", schemaA);

            // B = allOf [A, inline{b}]
            Schema refA = new Schema();
            refA.set$ref("#/components/schemas/A");
            Schema inlineB = new Schema();
            inlineB.setProperties(new LinkedHashMap<>(Map.of("b", new StringSchema())));
            Schema schemaB = new Schema();
            schemaB.setAllOf(List.of(refA, inlineB));
            schemas.put("B", schemaB);

            // C = allOf [B, inline{c}]
            Schema refB = new Schema();
            refB.set$ref("#/components/schemas/B");
            Schema inlineC = new Schema();
            inlineC.setProperties(new LinkedHashMap<>(Map.of("c", new StringSchema())));
            Schema schemaC = new Schema();
            schemaC.setAllOf(List.of(refB, inlineC));
            schemas.put("C", schemaC);

            // Act — iteration order matters: B gets flattened before C
            flattener.flattenAllOf(schemas);

            // Assert: B should have a + b
            Schema flatB = schemas.get("B");
            assertNotNull(flatB.getProperties());
            assertTrue(flatB.getProperties().containsKey("a"));
            assertTrue(flatB.getProperties().containsKey("b"));

            // C should have a + b + c (because B is already flattened when C resolves the ref)
            Schema flatC = schemas.get("C");
            assertNotNull(flatC.getProperties());
            assertTrue(flatC.getProperties().containsKey("a"));
            assertTrue(flatC.getProperties().containsKey("b"));
            assertTrue(flatC.getProperties().containsKey("c"));
        }

        @Test
        @DisplayName("should handle allOf combining regular props and oneOf ref")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testCombinedRegularAndComposition() {
            // Arrange: Result allOf [BaseObj, OneOfType]
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema baseObj = new Schema();
            baseObj.setProperties(new LinkedHashMap<>(Map.of("id", new StringSchema())));
            baseObj.setRequired(List.of("id"));
            schemas.put("BaseObj", baseObj);

            Schema oneOfType = new Schema();
            oneOfType.setOneOf(List.of(new StringSchema()));
            schemas.put("PaymentType", oneOfType);

            Schema refBase = new Schema();
            refBase.set$ref("#/components/schemas/BaseObj");
            Schema refOneOf = new Schema();
            refOneOf.set$ref("#/components/schemas/PaymentType");

            Schema result = new Schema();
            result.setDescription("Combined result");
            result.setAllOf(List.of(refBase, refOneOf));
            schemas.put("Result", result);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Result");
            assertNotNull(flattened.getProperties());
            assertTrue(flattened.getProperties().containsKey("id"));
            assertTrue(flattened.getProperties().containsKey("paymentType"));
            assertEquals("Combined result", flattened.getDescription());

            assertNotNull(flattened.getRequired());
            assertTrue(flattened.getRequired().contains("id"));
            assertTrue(flattened.getRequired().contains("paymentType"));
        }

        @Test
        @DisplayName("should handle single-element allOf")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testSingleElementAllOf() {
            // Arrange: allOf with just one schema
            Map<String, Schema> schemas = new LinkedHashMap<>();

            Schema single = new Schema();
            single.setProperties(new LinkedHashMap<>(Map.of("only", new StringSchema())));
            single.setRequired(List.of("only"));

            Schema composed = new Schema();
            composed.setAllOf(List.of(single));
            schemas.put("Single", composed);

            // Act
            flattener.flattenAllOf(schemas);

            // Assert
            Schema flattened = schemas.get("Single");
            assertNotNull(flattened.getProperties());
            assertEquals(1, flattened.getProperties().size());
            assertTrue(flattened.getProperties().containsKey("only"));
            assertEquals(1, flattened.getRequired().size());
        }
    }

    // ── Private method edge cases (via reflection) ───────────────────

    @Nested
    @DisplayName("Private method edge cases")
    class PrivateMethodEdgeCases {

        // ── extractSchemaNameFromRef ──

        @Test
        @DisplayName("extractSchemaNameFromRef should return 'UnknownSchema' for null ref")
        void testExtractSchemaNameFromRef_null() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("extractSchemaNameFromRef", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, (Object) null);
            assertEquals("UnknownSchema", result);
        }

        @Test
        @DisplayName("extractSchemaNameFromRef should return 'UnknownSchema' for empty ref")
        void testExtractSchemaNameFromRef_empty() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("extractSchemaNameFromRef", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, "");
            assertEquals("UnknownSchema", result);
        }

        @Test
        @DisplayName("extractSchemaNameFromRef should return whole string for ref without slash")
        void testExtractSchemaNameFromRef_noSlash() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("extractSchemaNameFromRef", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, "JustAName");
            assertEquals("JustAName", result);
        }

        @Test
        @DisplayName("extractSchemaNameFromRef should extract name after last slash")
        void testExtractSchemaNameFromRef_standard() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("extractSchemaNameFromRef", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, "#/components/schemas/Dog");
            assertEquals("Dog", result);
        }

        // ── toCamelCase ──

        @Test
        @DisplayName("toCamelCase should return null for null input")
        void testToCamelCase_null() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("toCamelCase", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, (Object) null);
            assertNull(result);
        }

        @Test
        @DisplayName("toCamelCase should return empty string for empty input")
        void testToCamelCase_empty() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("toCamelCase", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(flattener, "");
            assertEquals("", result);
        }

        @Test
        @DisplayName("toCamelCase should lowercase first character")
        void testToCamelCase_standard() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod("toCamelCase", String.class);
            method.setAccessible(true);

            assertEquals("animal", method.invoke(flattener, "Animal"));
            assertEquals("dog", method.invoke(flattener, "dog"));
            assertEquals("a", method.invoke(flattener, "A"));
        }

        // ── composeAllOfSchema defensive guard ──

        @Test
        @DisplayName("composeAllOfSchema should return schema unchanged if allOf is null")
        @SuppressWarnings("rawtypes")
        void testComposeAllOfSchema_nullAllOf() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod(
                    "composeAllOfSchema", String.class, Schema.class, Map.class);
            method.setAccessible(true);

            Schema schema = new Schema();
            // allOf is null by default
            Map<String, Schema> allSchemas = new LinkedHashMap<>();

            Schema result = (Schema) method.invoke(flattener, "Test", schema, allSchemas);
            assertSame(schema, result, "Should return the same schema when allOf is null");
        }

        @Test
        @DisplayName("composeAllOfSchema should return schema unchanged if allOf is empty")
        @SuppressWarnings({ "rawtypes", "unchecked" })
        void testComposeAllOfSchema_emptyAllOf() throws Exception {
            Method method = DartAllOfFlattener.class.getDeclaredMethod(
                    "composeAllOfSchema", String.class, Schema.class, Map.class);
            method.setAccessible(true);

            Schema schema = new Schema();
            schema.setAllOf(new ArrayList<>());
            Map<String, Schema> allSchemas = new LinkedHashMap<>();

            Schema result = (Schema) method.invoke(flattener, "Test", schema, allSchemas);
            assertSame(schema, result, "Should return the same schema when allOf is empty");
        }
    }
}
