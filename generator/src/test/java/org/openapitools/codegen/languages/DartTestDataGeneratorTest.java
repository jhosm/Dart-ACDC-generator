package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartTestDataGenerator.
 * Tests test value generation for various Dart types.
 */
class DartTestDataGeneratorTest {

    private DartTestDataGenerator generator;
    private Map<String, Schema> modelSchemas;
    private Set<String> primitives;

    @BeforeEach
    void setUp() {
        modelSchemas = new HashMap<>();
        primitives = new HashSet<>(Arrays.asList("int", "double", "num", "bool", "String", "DateTime"));
        generator = new DartTestDataGenerator(modelSchemas, primitives);
    }

    // ========================================
    // Primitive Type Test Values
    // ========================================

    @Test
    @DisplayName("getTestValueForType: should generate int test value")
    void testGetTestValueForType_Int() {
        assertEquals("42", generator.getTestValueForType("int"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate double test value")
    void testGetTestValueForType_Double() {
        assertEquals("3.14", generator.getTestValueForType("double"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate num test value")
    void testGetTestValueForType_Num() {
        assertEquals("123.45", generator.getTestValueForType("num"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate bool test value")
    void testGetTestValueForType_Bool() {
        assertEquals("true", generator.getTestValueForType("bool"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate String test value")
    void testGetTestValueForType_String() {
        assertEquals("'test_value'", generator.getTestValueForType("String"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate DateTime test value")
    void testGetTestValueForType_DateTime() {
        assertEquals("DateTime.parse('2024-01-01T00:00:00.000Z')", generator.getTestValueForType("DateTime"));
    }

    @Test
    @DisplayName("getTestValueForType: should return null for null input")
    void testGetTestValueForType_Null() {
        assertEquals("null", generator.getTestValueForType(null));
    }

    @Test
    @DisplayName("getTestValueForType: should return null for empty input")
    void testGetTestValueForType_Empty() {
        assertEquals("null", generator.getTestValueForType(""));
    }

    // ========================================
    // Collection Type Test Values
    // ========================================

    @Test
    @DisplayName("getTestValueForType: should generate bare List test value")
    void testGetTestValueForType_BareList() {
        assertEquals("const []", generator.getTestValueForType("List"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate List<int> test value")
    void testGetTestValueForType_ListInt() {
        assertEquals("[]", generator.getTestValueForType("List<int>"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate List<String> test value")
    void testGetTestValueForType_ListString() {
        assertEquals("[]", generator.getTestValueForType("List<String>"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate List<Pet> test value")
    void testGetTestValueForType_ListModel() {
        assertEquals("const []", generator.getTestValueForType("List<Pet>"));
    }

    @Test
    @DisplayName("getTestValueForType: should generate Map test value")
    void testGetTestValueForType_Map() {
        assertEquals("const <String, dynamic>{}", generator.getTestValueForType("Map<String, dynamic>"));
    }

    // ========================================
    // Special Type Test Values
    // ========================================

    @Test
    @DisplayName("getTestValueForType: should generate MultipartFile test value")
    void testGetTestValueForType_MultipartFile() {
        String result = generator.getTestValueForType("MultipartFile");
        assertEquals("MultipartFile.fromString('test', filename: 'test.txt')", result);
    }

    // ========================================
    // Model Type Test Values
    // ========================================

    @Test
    @DisplayName("getTestValueForType: should generate test value for model without schema")
    void testGetTestValueForType_ModelNoSchema() {
        String result = generator.getTestValueForType("Pet");
        assertEquals("Pet.fromJson(const <String, dynamic>{})", result);
    }

    @Test
    @DisplayName("getTestValueForType: should generate test value for model with required fields")
    void testGetTestValueForType_ModelWithRequired() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        Schema<?> nameSchema = new Schema<>();
        nameSchema.setType("string");
        properties.put("name", nameSchema);
        schema.setProperties(properties);
        schema.setRequired(List.of("name"));
        modelSchemas.put("Pet", schema);

        String result = generator.getTestValueForType("Pet");
        assertEquals("Pet.fromJson(const <String, dynamic>{'name': 'test_name'})", result);
    }

    // ========================================
    // Raw Test Values
    // ========================================

    @Test
    @DisplayName("getTestValueRawForType: should generate raw int value")
    void testGetTestValueRawForType_Int() {
        assertEquals("42", generator.getTestValueRawForType("int"));
    }

    @Test
    @DisplayName("getTestValueRawForType: should generate raw String value")
    void testGetTestValueRawForType_String() {
        assertEquals("test_value", generator.getTestValueRawForType("String"));
    }

    @Test
    @DisplayName("getTestValueRawForType: should generate raw DateTime value")
    void testGetTestValueRawForType_DateTime() {
        assertEquals("2024-01-01T00:00:00.000Z", generator.getTestValueRawForType("DateTime"));
    }

    @Test
    @DisplayName("getTestValueRawForType: should return test_value for unknown types")
    void testGetTestValueRawForType_UnknownType() {
        assertEquals("test_value", generator.getTestValueRawForType("CustomType"));
    }

    @Test
    @DisplayName("getTestValueRawForType: should return null for null input")
    void testGetTestValueRawForType_Null() {
        assertEquals("null", generator.getTestValueRawForType(null));
    }

    // ========================================
    // Test JSON Generation
    // ========================================

    @Test
    @DisplayName("generateTestJsonForModel: should return empty JSON for unknown model")
    void testGenerateTestJsonForModel_UnknownModel() {
        String result = generator.generateTestJsonForModel("UnknownModel");
        assertEquals("<String, dynamic>{}", result);
    }

    @Test
    @DisplayName("generateTestJsonForModel: should return empty JSON for model without required fields")
    void testGenerateTestJsonForModel_NoRequired() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setProperties(new HashMap<>());
        modelSchemas.put("Pet", schema);

        String result = generator.generateTestJsonForModel("Pet");
        assertEquals("<String, dynamic>{}", result);
    }

    @Test
    @DisplayName("generateTestJsonForModel: should generate JSON with string field")
    void testGenerateTestJsonForModel_StringField() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        Schema<?> nameSchema = new Schema<>();
        nameSchema.setType("string");
        properties.put("name", nameSchema);
        schema.setProperties(properties);
        schema.setRequired(List.of("name"));
        modelSchemas.put("Pet", schema);

        String result = generator.generateTestJsonForModel("Pet");
        assertEquals("<String, dynamic>{'name': 'test_name'}", result);
    }

    @Test
    @DisplayName("generateTestJsonForModel: should generate JSON with multiple fields")
    void testGenerateTestJsonForModel_MultipleFields() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        
        Schema<?> nameSchema = new Schema<>();
        nameSchema.setType("string");
        properties.put("name", nameSchema);
        
        Schema<?> ageSchema = new Schema<>();
        ageSchema.setType("integer");
        properties.put("age", ageSchema);
        
        schema.setProperties(properties);
        schema.setRequired(List.of("name", "age"));
        modelSchemas.put("Pet", schema);

        String result = generator.generateTestJsonForModel("Pet");
        assertTrue(result.contains("'name': 'test_name'"));
        assertTrue(result.contains("'age': 42"));
    }

    @Test
    @DisplayName("generateTestJsonForModel: should handle all field types")
    void testGenerateTestJsonForModel_AllFieldTypes() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        Map<String, Schema> properties = new HashMap<>();
        
        Schema<?> stringSchema = new Schema<>();
        stringSchema.setType("string");
        properties.put("stringField", stringSchema);
        
        Schema<?> intSchema = new Schema<>();
        intSchema.setType("integer");
        properties.put("intField", intSchema);
        
        Schema<?> numberSchema = new Schema<>();
        numberSchema.setType("number");
        properties.put("numberField", numberSchema);
        
        Schema<?> boolSchema = new Schema<>();
        boolSchema.setType("boolean");
        properties.put("boolField", boolSchema);
        
        Schema<?> arraySchema = new Schema<>();
        arraySchema.setType("array");
        properties.put("arrayField", arraySchema);
        
        Schema<?> objectSchema = new Schema<>();
        objectSchema.setType("object");
        properties.put("objectField", objectSchema);
        
        schema.setProperties(properties);
        schema.setRequired(List.of("stringField", "intField", "numberField", "boolField", "arrayField", "objectField"));
        modelSchemas.put("Model", schema);

        String result = generator.generateTestJsonForModel("Model");
        assertTrue(result.contains("'stringField': 'test_stringField'"));
        assertTrue(result.contains("'intField': 42"));
        assertTrue(result.contains("'numberField': 42"));
        assertTrue(result.contains("'boolField': true"));
        assertTrue(result.contains("'arrayField': []"));
        assertTrue(result.contains("'objectField': {}"));
    }

    @Test
    @DisplayName("generateTestJsonForModel: should skip fields not in schema")
    void testGenerateTestJsonForModel_SkipMissingFields() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setProperties(new HashMap<>());
        schema.setRequired(List.of("missingField"));
        modelSchemas.put("Pet", schema);

        String result = generator.generateTestJsonForModel("Pet");
        assertEquals("<String, dynamic>{}", result);
    }
}
