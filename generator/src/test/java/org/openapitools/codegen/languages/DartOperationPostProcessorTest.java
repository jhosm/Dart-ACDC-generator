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
 * Unit tests for DartOperationPostProcessor.
 *
 * Tests operation post-processing including HTTP method normalization,
 * return type fixing, multipart handling, and array type fixing.
 */
@DisplayName("DartOperationPostProcessor")
class DartOperationPostProcessorTest {

    private DartAcdcGenerator generator;
    private DartOperationPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        postProcessor = new DartOperationPostProcessor(generator, generator.languageSpecificPrimitives());
    }

    // ========================================
    // normalizeHttpMethod Tests
    // ========================================

    @Nested
    @DisplayName("normalizeHttpMethod")
    class NormalizeHttpMethod {

        @Test
        @DisplayName("should lowercase GET to get")
        void testGet() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "GET";

            postProcessor.normalizeHttpMethod(op);

            assertEquals("get", op.httpMethod);
        }

        @Test
        @DisplayName("should lowercase POST to post")
        void testPost() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "POST";

            postProcessor.normalizeHttpMethod(op);

            assertEquals("post", op.httpMethod);
        }

        @Test
        @DisplayName("should lowercase DELETE to delete")
        void testDelete() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "DELETE";

            postProcessor.normalizeHttpMethod(op);

            assertEquals("delete", op.httpMethod);
        }

        @Test
        @DisplayName("should handle null httpMethod")
        void testNull() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = null;

            assertDoesNotThrow(() -> postProcessor.normalizeHttpMethod(op));
            assertNull(op.httpMethod);
        }

        @Test
        @DisplayName("should not change already lowercase method")
        void testAlreadyLowercase() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "get";

            postProcessor.normalizeHttpMethod(op);

            assertEquals("get", op.httpMethod);
        }
    }

    // ========================================
    // fixReturnTypes Tests
    // ========================================

    @Nested
    @DisplayName("fixReturnTypes")
    class FixReturnTypes {

        @Test
        @DisplayName("should fix snake_case returnType to PascalCase")
        void testSnakeCaseReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "ping_200_response";
            op.operationId = "ping";

            postProcessor.fixReturnTypes(op);

            assertEquals("Ping200Response", op.returnType);
        }

        @Test
        @DisplayName("should fix snake_case returnBaseType to PascalCase")
        void testSnakeCaseReturnBaseType() {
            CodegenOperation op = new CodegenOperation();
            op.returnBaseType = "api_response";
            op.operationId = "getApiResponse";

            postProcessor.fixReturnTypes(op);

            assertEquals("ApiResponse", op.returnBaseType);
        }

        @Test
        @DisplayName("should not modify primitive returnType")
        void testPrimitiveReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "int";

            postProcessor.fixReturnTypes(op);

            assertEquals("int", op.returnType);
        }

        @Test
        @DisplayName("should not modify void returnType")
        void testVoidReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";

            postProcessor.fixReturnTypes(op);

            assertEquals("void", op.returnType);
        }

        @Test
        @DisplayName("should not modify generic returnType")
        void testGenericReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List<Pet>";

            postProcessor.fixReturnTypes(op);

            assertEquals("List<Pet>", op.returnType);
        }

        @Test
        @DisplayName("should handle null returnType")
        void testNullReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = null;

            assertDoesNotThrow(() -> postProcessor.fixReturnTypes(op));
            assertNull(op.returnType);
        }

        @Test
        @DisplayName("should handle empty returnType")
        void testEmptyReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "";

            assertDoesNotThrow(() -> postProcessor.fixReturnTypes(op));
            assertEquals("", op.returnType);
        }

        @Test
        @DisplayName("should not modify already PascalCase returnType")
        void testAlreadyPascalCase() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";

            postProcessor.fixReturnTypes(op);

            assertEquals("Pet", op.returnType);
        }
    }

    // ========================================
    // handleMultipartOperation Tests
    // ========================================

    @Nested
    @DisplayName("handleMultipartOperation")
    class HandleMultipartOperation {

        @Test
        @DisplayName("should mark multipart operation with vendor extension")
        void testMarksMultipart() {
            CodegenOperation op = createMultipartOperation();

            postProcessor.handleMultipartOperation(op);

            assertTrue((Boolean) op.vendorExtensions.get("x-is-multipart"));
        }

        @Test
        @DisplayName("should add Dio import for multipart operation")
        void testAddsDioImport() {
            CodegenOperation op = createMultipartOperation();

            postProcessor.handleMultipartOperation(op);

            assertTrue(op.imports.contains("package:dio/dio.dart"));
        }

        @Test
        @DisplayName("should convert binary parameter to MultipartFile")
        void testConvertsBinaryToMultipartFile() {
            CodegenOperation op = createMultipartOperation();
            CodegenParameter param = new CodegenParameter();
            param.paramName = "file";
            param.dataType = "List<int>";
            param.isBinary = true;
            op.allParams = new ArrayList<>(List.of(param));

            postProcessor.handleMultipartOperation(op);

            assertEquals("MultipartFile", param.dataType);
            assertEquals("MultipartFile", param.datatypeWithEnum);
            assertEquals("MultipartFile", param.baseType);
            assertTrue((Boolean) param.vendorExtensions.get("x-is-multipart-file"));
        }

        @Test
        @DisplayName("should convert List<int> parameter to MultipartFile in multipart context")
        void testConvertsListIntToMultipartFile() {
            CodegenOperation op = createMultipartOperation();
            CodegenParameter param = new CodegenParameter();
            param.paramName = "data";
            param.dataType = "List<int>";
            param.isBinary = false;
            op.allParams = new ArrayList<>(List.of(param));

            postProcessor.handleMultipartOperation(op);

            assertEquals("MultipartFile", param.dataType);
        }

        @Test
        @DisplayName("should not convert non-binary parameters")
        void testDoesNotConvertNonBinary() {
            CodegenOperation op = createMultipartOperation();
            CodegenParameter param = new CodegenParameter();
            param.paramName = "name";
            param.dataType = "String";
            param.isBinary = false;
            op.allParams = new ArrayList<>(List.of(param));

            postProcessor.handleMultipartOperation(op);

            assertEquals("String", param.dataType);
        }

        @Test
        @DisplayName("should not process non-multipart operations")
        void testIgnoresNonMultipart() {
            CodegenOperation op = new CodegenOperation();
            op.hasConsumes = true;
            op.consumes = List.of(Map.of("mediaType", "application/json"));

            postProcessor.handleMultipartOperation(op);

            assertFalse(op.vendorExtensions.containsKey("x-is-multipart"));
        }

        @Test
        @DisplayName("should handle operation with no consumes")
        void testNoConsumes() {
            CodegenOperation op = new CodegenOperation();
            op.hasConsumes = false;

            assertDoesNotThrow(() -> postProcessor.handleMultipartOperation(op));
        }

        @Test
        @DisplayName("should handle null allParams in multipart operation")
        void testNullAllParams() {
            CodegenOperation op = createMultipartOperation();
            op.allParams = null;

            assertDoesNotThrow(() -> postProcessor.handleMultipartOperation(op));
            assertTrue((Boolean) op.vendorExtensions.get("x-is-multipart"));
        }

        private CodegenOperation createMultipartOperation() {
            CodegenOperation op = new CodegenOperation();
            op.hasConsumes = true;
            op.consumes = List.of(Map.of("mediaType", "multipart/form-data"));
            return op;
        }
    }

    // ========================================
    // updateParameterInLists Tests
    // ========================================

    @Nested
    @DisplayName("updateParameterInLists")
    class UpdateParameterInLists {

        @Test
        @DisplayName("should update matching parameter in bodyParams")
        void testUpdatesBodyParams() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter bodyParam = new CodegenParameter();
            bodyParam.paramName = "file";
            bodyParam.dataType = "List<int>";
            op.bodyParams = new ArrayList<>(List.of(bodyParam));

            CodegenParameter updatedParam = new CodegenParameter();
            updatedParam.paramName = "file";
            updatedParam.dataType = "MultipartFile";
            updatedParam.datatypeWithEnum = "MultipartFile";
            updatedParam.baseType = "MultipartFile";

            postProcessor.updateParameterInLists(op, updatedParam);

            assertEquals("MultipartFile", bodyParam.dataType);
            assertEquals("MultipartFile", bodyParam.datatypeWithEnum);
            assertEquals("MultipartFile", bodyParam.baseType);
        }

        @Test
        @DisplayName("should update matching parameter in formParams")
        void testUpdatesFormParams() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter formParam = new CodegenParameter();
            formParam.paramName = "file";
            formParam.dataType = "List<int>";
            op.formParams = new ArrayList<>(List.of(formParam));

            CodegenParameter updatedParam = new CodegenParameter();
            updatedParam.paramName = "file";
            updatedParam.dataType = "MultipartFile";
            updatedParam.datatypeWithEnum = "MultipartFile";
            updatedParam.baseType = "MultipartFile";

            postProcessor.updateParameterInLists(op, updatedParam);

            assertEquals("MultipartFile", formParam.dataType);
        }

        @Test
        @DisplayName("should not update non-matching parameters")
        void testDoesNotUpdateNonMatching() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter bodyParam = new CodegenParameter();
            bodyParam.paramName = "name";
            bodyParam.dataType = "String";
            op.bodyParams = new ArrayList<>(List.of(bodyParam));

            CodegenParameter updatedParam = new CodegenParameter();
            updatedParam.paramName = "file";
            updatedParam.dataType = "MultipartFile";
            updatedParam.datatypeWithEnum = "MultipartFile";
            updatedParam.baseType = "MultipartFile";

            postProcessor.updateParameterInLists(op, updatedParam);

            assertEquals("String", bodyParam.dataType);
        }

        @Test
        @DisplayName("should handle null bodyParams and formParams")
        void testNullLists() {
            CodegenOperation op = new CodegenOperation();
            op.bodyParams = null;
            op.formParams = null;

            CodegenParameter updatedParam = new CodegenParameter();
            updatedParam.paramName = "file";
            updatedParam.dataType = "MultipartFile";

            assertDoesNotThrow(() -> postProcessor.updateParameterInLists(op, updatedParam));
        }
    }

    // ========================================
    // collectParameterImports Tests
    // ========================================

    @Nested
    @DisplayName("collectParameterImports")
    class CollectParameterImports {

        @Test
        @DisplayName("should collect Dio import from parameter vendor extension")
        void testCollectsDioImport() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter param = new CodegenParameter();
            param.vendorExtensions.put("x-dart-import", "package:dio/dio.dart");
            op.allParams = List.of(param);

            postProcessor.collectParameterImports(op);

            assertTrue(op.imports.contains("package:dio/dio.dart"));
        }

        @Test
        @DisplayName("should not add import for parameter without vendor extension")
        void testSkipsParamsWithoutExtension() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter param = new CodegenParameter();
            op.allParams = List.of(param);

            postProcessor.collectParameterImports(op);

            assertTrue(op.imports.isEmpty());
        }

        @Test
        @DisplayName("should handle null allParams")
        void testNullAllParams() {
            CodegenOperation op = new CodegenOperation();
            op.allParams = null;

            assertDoesNotThrow(() -> postProcessor.collectParameterImports(op));
        }

        @Test
        @DisplayName("should skip empty dart import")
        void testSkipsEmptyImport() {
            CodegenOperation op = new CodegenOperation();
            CodegenParameter param = new CodegenParameter();
            param.vendorExtensions.put("x-dart-import", "");
            op.allParams = List.of(param);

            postProcessor.collectParameterImports(op);

            assertTrue(op.imports.isEmpty());
        }
    }

    // ========================================
    // fixArrayReturnType Tests
    // ========================================

    @Nested
    @DisplayName("fixArrayReturnType")
    class FixArrayReturnType {

        @Test
        @DisplayName("should fix bare List to List<T>")
        void testFixesBareList() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List";
            op.returnBaseType = "Pet";
            op.isArray = true;

            postProcessor.fixArrayReturnType(op);

            assertEquals("List<Pet>", op.returnType);
        }

        @Test
        @DisplayName("should add isListContainer vendor extension for arrays")
        void testAddsIsListContainer() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List";
            op.returnBaseType = "Pet";
            op.isArray = true;

            postProcessor.fixArrayReturnType(op);

            assertTrue((Boolean) op.vendorExtensions.get("isListContainer"));
        }

        @Test
        @DisplayName("should not modify non-array return types")
        void testSkipsNonArray() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.isArray = false;

            postProcessor.fixArrayReturnType(op);

            assertEquals("Pet", op.returnType);
            assertFalse(op.vendorExtensions.containsKey("isListContainer"));
        }

        @Test
        @DisplayName("should not modify already-parameterized List type")
        void testSkipsParameterizedList() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List<Pet>";
            op.returnBaseType = "Pet";
            op.isArray = true;

            postProcessor.fixArrayReturnType(op);

            assertEquals("List<Pet>", op.returnType);
            assertTrue((Boolean) op.vendorExtensions.get("isListContainer"));
        }

        @Test
        @DisplayName("should handle null returnType")
        void testNullReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = null;
            op.isArray = true;

            assertDoesNotThrow(() -> postProcessor.fixArrayReturnType(op));
        }

        @Test
        @DisplayName("should handle null returnBaseType for array")
        void testNullReturnBaseType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List";
            op.returnBaseType = null;
            op.isArray = true;

            assertDoesNotThrow(() -> postProcessor.fixArrayReturnType(op));
            assertEquals("List", op.returnType);
        }
    }

    // ========================================
    // postProcessOperations Integration Tests
    // ========================================

    @Nested
    @DisplayName("postProcessOperations (integration)")
    class PostProcessOperations {

        @Test
        @DisplayName("should apply all processing steps to an operation")
        void testFullProcessing() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "GET";
            op.returnType = "pet";
            op.returnBaseType = "pet";
            op.operationId = "getPet";
            op.isArray = false;

            postProcessor.postProcessOperations(List.of(op));

            assertEquals("get", op.httpMethod);
            assertEquals("Pet", op.returnType);
        }

        @Test
        @DisplayName("should fix array return type with snake_case base type")
        void testArrayWithSnakeCaseBase() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "GET";
            op.returnType = "List";
            op.returnBaseType = "api_response";
            op.operationId = "getResponses";
            op.isArray = true;

            postProcessor.postProcessOperations(List.of(op));

            assertEquals("ApiResponse", op.returnBaseType);
            assertEquals("List<ApiResponse>", op.returnType);
            assertTrue((Boolean) op.vendorExtensions.get("isListContainer"));
        }

        @Test
        @DisplayName("should handle multipart operation with binary parameter")
        void testMultipartWithBinary() {
            CodegenOperation op = new CodegenOperation();
            op.httpMethod = "POST";
            op.hasConsumes = true;
            op.consumes = List.of(Map.of("mediaType", "multipart/form-data"));

            CodegenParameter param = new CodegenParameter();
            param.paramName = "file";
            param.dataType = "List<int>";
            param.isBinary = true;
            op.allParams = new ArrayList<>(List.of(param));

            postProcessor.postProcessOperations(List.of(op));

            assertEquals("post", op.httpMethod);
            assertEquals("MultipartFile", param.dataType);
            assertTrue((Boolean) op.vendorExtensions.get("x-is-multipart"));
            assertTrue(op.imports.contains("package:dio/dio.dart"));
        }

        @Test
        @DisplayName("should handle empty operations list")
        void testEmptyList() {
            assertDoesNotThrow(() -> postProcessor.postProcessOperations(Collections.emptyList()));
        }
    }
}
