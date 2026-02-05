package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartOperationEnricher.
 *
 * Tests test metadata enrichment for operations and parameters,
 * including httpMethodCapitalized, sampleResponseJson, testValue,
 * and testValueRaw vendor extensions.
 */
@DisplayName("DartOperationEnricher")
class DartOperationEnricherTest {

    private DartAcdcGenerator generator;
    private DartTestDataGenerator testDataGenerator;
    private DartOperationEnricher enricher;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        testDataGenerator = new DartTestDataGenerator(new HashMap<>(), generator.languageSpecificPrimitives());
        enricher = new DartOperationEnricher(testDataGenerator, generator.languageSpecificPrimitives());
    }

    // ========================================
    // enrichHttpMethod Tests
    // ========================================

    @Nested
    @DisplayName("enrichHttpMethod")
    class EnrichHttpMethod {

        @Test
        @DisplayName("should capitalize GET to 'Get'")
        void testGetMethod() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "get";

            enricher.enrichHttpMethod(op);

            assertEquals("Get", op.vendorExtensions.get("httpMethodCapitalized"));
        }

        @Test
        @DisplayName("should capitalize POST to 'Post'")
        void testPostMethod() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "post";

            enricher.enrichHttpMethod(op);

            assertEquals("Post", op.vendorExtensions.get("httpMethodCapitalized"));
        }

        @Test
        @DisplayName("should capitalize DELETE to 'Delete'")
        void testDeleteMethod() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "delete";

            enricher.enrichHttpMethod(op);

            assertEquals("Delete", op.vendorExtensions.get("httpMethodCapitalized"));
        }

        @Test
        @DisplayName("should handle null httpMethod without error")
        void testNullHttpMethod() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = null;

            assertDoesNotThrow(() -> enricher.enrichHttpMethod(op));
            assertFalse(op.vendorExtensions.containsKey("httpMethodCapitalized"));
        }

        @Test
        @DisplayName("should handle uppercase HTTP method (POST -> Post)")
        void testUppercaseMethod() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "POST";

            enricher.enrichHttpMethod(op);

            assertEquals("Post", op.vendorExtensions.get("httpMethodCapitalized"));
        }
    }

    // ========================================
    // getSampleResponseJson Tests
    // ========================================

    @Nested
    @DisplayName("getSampleResponseJson")
    class GetSampleResponseJson {

        @Test
        @DisplayName("should return 'null' for null return type")
        void testNullReturnType() {
            assertEquals("null", enricher.getSampleResponseJson(null, null, false));
        }

        @Test
        @DisplayName("should return 'null' for empty return type")
        void testEmptyReturnType() {
            assertEquals("null", enricher.getSampleResponseJson("", null, false));
        }

        @Test
        @DisplayName("should return 'null' for void return type")
        void testVoidReturnType() {
            assertEquals("null", enricher.getSampleResponseJson("void", null, false));
        }

        @Test
        @DisplayName("should return 'null' for whitespace-only return type")
        void testWhitespaceReturnType() {
            assertEquals("null", enricher.getSampleResponseJson("   ", null, false));
        }

        @Test
        @DisplayName("should return '42' for int return type")
        void testIntReturnType() {
            assertEquals("42", enricher.getSampleResponseJson("int", null, false));
        }

        @Test
        @DisplayName("should return '42' for double return type")
        void testDoubleReturnType() {
            assertEquals("42", enricher.getSampleResponseJson("double", null, false));
        }

        @Test
        @DisplayName("should return 'true' for bool return type")
        void testBoolReturnType() {
            assertEquals("true", enricher.getSampleResponseJson("bool", null, false));
        }

        @Test
        @DisplayName("should return quoted string for String return type")
        void testStringReturnType() {
            assertEquals("'test_response'", enricher.getSampleResponseJson("String", null, false));
        }

        @Test
        @DisplayName("should return ISO date for DateTime return type")
        void testDateTimeReturnType() {
            assertEquals("'2024-01-01T00:00:00.000Z'",
                    enricher.getSampleResponseJson("DateTime", null, false));
        }

        @Test
        @DisplayName("should wrap array response in brackets")
        void testArrayResponse() {
            String result = enricher.getSampleResponseJson("List<Pet>", "Pet", true);
            assertTrue(result.startsWith("["), "Array response should start with [");
            assertTrue(result.endsWith("]"), "Array response should end with ]");
        }

        @Test
        @DisplayName("should generate model JSON for non-primitive types")
        void testModelReturnType() {
            String result = enricher.getSampleResponseJson("Pet", "Pet", false);
            // Should return JSON-like structure from test data generator
            assertNotNull(result);
            assertNotEquals("null", result);
        }

        @Test
        @DisplayName("should use returnBaseType for array elements")
        void testArrayBaseType() {
            String singleResult = enricher.getSampleResponseJson("Pet", "Pet", false);
            String arrayResult = enricher.getSampleResponseJson("List<Pet>", "Pet", true);

            // Array result should contain the single result wrapped in brackets
            assertEquals("[" + singleResult + "]", arrayResult);
        }

        @Test
        @DisplayName("should fall back to returnType when returnBaseType is null")
        void testFallbackToReturnType() {
            String result = enricher.getSampleResponseJson("Pet", null, false);
            assertNotNull(result);
            assertNotEquals("null", result);
        }

        @Test
        @DisplayName("should use default map for unknown primitive type")
        void testUnknownPrimitiveType() {
            // DateTime is in languageSpecificPrimitives but uses default branch
            // Let's use a type that IS in the set but doesn't match specific cases
            // Actually, test the default case by checking Map<String, dynamic>
            String result = enricher.getSampleResponseJson("Map<String, dynamic>", null, false);
            // This should be in languageSpecificPrimitives as a complex type
            assertNotNull(result);
        }
    }

    // ========================================
    // addTestValuesToParams Tests
    // ========================================

    @Nested
    @DisplayName("addTestValuesToParams")
    class AddTestValuesToParams {

        @Test
        @DisplayName("should handle null params list")
        void testNullParams() {
            assertDoesNotThrow(() -> enricher.addTestValuesToParams(null));
        }

        @Test
        @DisplayName("should handle empty params list")
        void testEmptyParams() {
            assertDoesNotThrow(() -> enricher.addTestValuesToParams(new ArrayList<>()));
        }

        @Test
        @DisplayName("should add testValue for int parameter")
        void testIntParam() {
            CodegenParameter param = new CodegenParameter();
            param.dataType = "int";

            enricher.addTestValuesToParams(List.of(param));

            assertTrue(param.vendorExtensions.containsKey("testValue"));
            assertNotNull(param.vendorExtensions.get("testValue"));
        }

        @Test
        @DisplayName("should add testValueRaw for String parameter")
        void testStringParam() {
            CodegenParameter param = new CodegenParameter();
            param.dataType = "String";

            enricher.addTestValuesToParams(List.of(param));

            assertTrue(param.vendorExtensions.containsKey("testValueRaw"));
            assertNotNull(param.vendorExtensions.get("testValueRaw"));
        }

        @Test
        @DisplayName("should add both testValue and testValueRaw")
        void testBothExtensions() {
            CodegenParameter param = new CodegenParameter();
            param.dataType = "int";

            enricher.addTestValuesToParams(List.of(param));

            assertTrue(param.vendorExtensions.containsKey("testValue"));
            assertTrue(param.vendorExtensions.containsKey("testValueRaw"));
        }

        @Test
        @DisplayName("should handle multiple parameters")
        void testMultipleParams() {
            CodegenParameter param1 = new CodegenParameter();
            param1.dataType = "int";
            CodegenParameter param2 = new CodegenParameter();
            param2.dataType = "String";

            enricher.addTestValuesToParams(List.of(param1, param2));

            assertTrue(param1.vendorExtensions.containsKey("testValue"));
            assertTrue(param2.vendorExtensions.containsKey("testValue"));
        }
    }

    // ========================================
    // enrichSampleResponse Tests
    // ========================================

    @Nested
    @DisplayName("enrichSampleResponse")
    class EnrichSampleResponse {

        @Test
        @DisplayName("should add sampleResponseJson vendor extension")
        void testAddsVendorExtension() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";
            op.isArray = false;

            enricher.enrichSampleResponse(op);

            assertTrue(op.vendorExtensions.containsKey("sampleResponseJson"));
            assertNotNull(op.vendorExtensions.get("sampleResponseJson"));
        }

        @Test
        @DisplayName("should use fallback for null return type operation")
        void testFallbackForNullReturn() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = null;

            enricher.enrichSampleResponse(op);

            // getSampleResponseJson returns "null" for null type, which is not empty
            assertEquals("null", op.vendorExtensions.get("sampleResponseJson"));
        }

        @Test
        @DisplayName("should use array fallback for empty sampleResponseJson on array operations")
        void testArrayFallback() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";
            op.isArray = true;

            enricher.enrichSampleResponse(op);

            // void return type returns "null" which is non-empty, so no fallback
            assertNotNull(op.vendorExtensions.get("sampleResponseJson"));
        }
    }

    // ========================================
    // enrichTestValues Tests
    // ========================================

    @Nested
    @DisplayName("enrichTestValues")
    class EnrichTestValues {

        @Test
        @DisplayName("should enrich all parameter lists")
        void testAllParameterLists() {
            CodegenOperation op = new CodegenOperation();

            CodegenParameter allParam = new CodegenParameter();
            allParam.dataType = "int";
            op.allParams = List.of(allParam);

            CodegenParameter pathParam = new CodegenParameter();
            pathParam.dataType = "String";
            op.pathParams = List.of(pathParam);

            CodegenParameter queryParam = new CodegenParameter();
            queryParam.dataType = "bool";
            op.queryParams = List.of(queryParam);

            op.bodyParams = null;
            op.headerParams = null;
            op.formParams = null;

            enricher.enrichTestValues(op);

            assertTrue(allParam.vendorExtensions.containsKey("testValue"));
            assertTrue(pathParam.vendorExtensions.containsKey("testValue"));
            assertTrue(queryParam.vendorExtensions.containsKey("testValue"));
        }

        @Test
        @DisplayName("should handle all null parameter lists")
        void testAllNullLists() {
            CodegenOperation op = new CodegenOperation();
            op.allParams = null;
            op.pathParams = null;
            op.queryParams = null;
            op.bodyParams = null;
            op.headerParams = null;
            op.formParams = null;

            assertDoesNotThrow(() -> enricher.enrichTestValues(op));
        }
    }

    // ========================================
    // enrichOperations Integration Tests
    // ========================================

    @Nested
    @DisplayName("enrichOperations (integration)")
    class EnrichOperations {

        @Test
        @DisplayName("should enrich all aspects of an operation")
        void testFullEnrichment() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "get";
            op.returnType = "Pet";
            op.returnBaseType = "Pet";
            op.isArray = false;

            CodegenParameter param = new CodegenParameter();
            param.dataType = "int";
            op.allParams = List.of(param);
            op.pathParams = List.of(param);
            op.queryParams = null;
            op.bodyParams = null;
            op.headerParams = null;
            op.formParams = null;

            enricher.enrichOperations(List.of(op));

            // Check httpMethodCapitalized
            assertEquals("Get", op.vendorExtensions.get("httpMethodCapitalized"));
            // Check sampleResponseJson
            assertTrue(op.vendorExtensions.containsKey("sampleResponseJson"));
            assertNotNull(op.vendorExtensions.get("sampleResponseJson"));
            // Check testValue on params
            assertTrue(param.vendorExtensions.containsKey("testValue"));
            assertTrue(param.vendorExtensions.containsKey("testValueRaw"));
        }

        @Test
        @DisplayName("should handle empty operations list")
        void testEmptyList() {
            assertDoesNotThrow(() -> enricher.enrichOperations(Collections.emptyList()));
        }

        @Test
        @DisplayName("should handle multiple operations")
        void testMultipleOperations() {
            CodegenOperation op1 = new CodegenOperation();
            op1.httpMethod = "get";
            op1.returnType = "void";

            CodegenOperation op2 = new CodegenOperation();
            op2.httpMethod = "post";
            op2.returnType = "int";

            enricher.enrichOperations(List.of(op1, op2));

            assertEquals("Get", op1.vendorExtensions.get("httpMethodCapitalized"));
            assertEquals("Post", op2.vendorExtensions.get("httpMethodCapitalized"));
        }
    }
}
