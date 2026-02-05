package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.model.OperationsMap;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartOperationImportResolver.
 *
 * Tests import resolution and filtering for operation-level imports,
 * ensuring only models actually used in operation signatures are imported.
 */
@DisplayName("DartOperationImportResolver")
class DartOperationImportResolverTest {

    private DartAcdcGenerator generator;
    private DartOperationImportResolver resolver;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        // Use the generator's own languageSpecificPrimitives via the public accessor method
        resolver = new DartOperationImportResolver(generator, generator.languageSpecificPrimitives());
    }

    // ========================================
    // getModelImportFromType Tests
    // ========================================

    @Nested
    @DisplayName("getModelImportFromType")
    class GetModelImportFromType {

        @Test
        @DisplayName("should return null for null type name")
        void testNullTypeName() {
            assertNull(resolver.getModelImportFromType(null));
        }

        @Test
        @DisplayName("should return null for empty type name")
        void testEmptyTypeName() {
            assertNull(resolver.getModelImportFromType(""));
        }

        @Test
        @DisplayName("should return null for language-specific primitive 'int'")
        void testPrimitiveInt() {
            assertNull(resolver.getModelImportFromType("int"));
        }

        @Test
        @DisplayName("should return null for language-specific primitive 'String'")
        void testPrimitiveString() {
            assertNull(resolver.getModelImportFromType("String"));
        }

        @Test
        @DisplayName("should return null for language-specific primitive 'bool'")
        void testPrimitiveBool() {
            assertNull(resolver.getModelImportFromType("bool"));
        }

        @Test
        @DisplayName("should return null for 'MultipartFile' special type")
        void testMultipartFile() {
            assertNull(resolver.getModelImportFromType("MultipartFile"));
        }

        @Test
        @DisplayName("should return null for 'List' special type")
        void testListType() {
            assertNull(resolver.getModelImportFromType("List"));
        }

        @Test
        @DisplayName("should return null for 'Map' special type")
        void testMapType() {
            assertNull(resolver.getModelImportFromType("Map"));
        }

        @Test
        @DisplayName("should return null for 'void' type")
        void testVoidType() {
            assertNull(resolver.getModelImportFromType("void"));
        }

        @Test
        @DisplayName("should return null for 'file' type")
        void testFileType() {
            assertNull(resolver.getModelImportFromType("file"));
        }

        @Test
        @DisplayName("should return null for generic type 'List<int>'")
        void testGenericListInt() {
            assertNull(resolver.getModelImportFromType("List<int>"));
        }

        @Test
        @DisplayName("should return null for generic type 'Map<String, dynamic>'")
        void testGenericMap() {
            assertNull(resolver.getModelImportFromType("Map<String, dynamic>"));
        }

        @Test
        @DisplayName("should return import path for model type 'Pet'")
        void testModelType() {
            String result = resolver.getModelImportFromType("Pet");
            assertNotNull(result);
            assertTrue(result.contains("pet"), "Import path should contain model name: " + result);
        }

        @Test
        @DisplayName("should return import path for model type 'ApiResponse'")
        void testModelTypeApiResponse() {
            String result = resolver.getModelImportFromType("ApiResponse");
            assertNotNull(result);
            assertTrue(result.contains("api_response"), "Import path should contain model name: " + result);
        }
    }

    // ========================================
    // isPrimitiveTypeImport Tests
    // ========================================

    @Nested
    @DisplayName("isPrimitiveTypeImport")
    class IsPrimitiveTypeImport {

        @Test
        @DisplayName("should return false for null import path")
        void testNull() {
            assertFalse(resolver.isPrimitiveTypeImport(null));
        }

        @Test
        @DisplayName("should return false for non-model path")
        void testNonModelPath() {
            assertFalse(resolver.isPrimitiveTypeImport("package:mypackage/lib/api/pet_api.dart"));
        }

        @Test
        @DisplayName("should return true for primitive model import 'string.dart'")
        void testStringPrimitive() {
            assertTrue(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/string.dart"));
        }

        @Test
        @DisplayName("should return true for primitive model import 'integer.dart'")
        void testIntegerPrimitive() {
            assertTrue(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/integer.dart"));
        }

        @Test
        @DisplayName("should return true for primitive model import 'boolean.dart'")
        void testBooleanPrimitive() {
            assertTrue(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/boolean.dart"));
        }

        @Test
        @DisplayName("should return true for primitive model import 'object.dart'")
        void testObjectPrimitive() {
            assertTrue(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/object.dart"));
        }

        @Test
        @DisplayName("should return true for primitive model import 'datetime.dart'")
        void testDateTimePrimitive() {
            assertTrue(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/datetime.dart"));
        }

        @Test
        @DisplayName("should return false for actual model import 'pet.dart'")
        void testActualModel() {
            assertFalse(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/pet.dart"));
        }

        @Test
        @DisplayName("should return false for actual model import 'api_response.dart'")
        void testActualModelApiResponse() {
            assertFalse(resolver.isPrimitiveTypeImport("package:mypackage/lib/models/api_response.dart"));
        }
    }

    // ========================================
    // extractImportPath Tests
    // ========================================

    @Nested
    @DisplayName("extractImportPath")
    class ExtractImportPath {

        @Test
        @DisplayName("should return string directly when import is a String")
        void testStringImport() {
            assertEquals("package:test/models/pet.dart", resolver.extractImportPath("package:test/models/pet.dart"));
        }

        @Test
        @DisplayName("should extract path from Map with 'import' key")
        void testMapImport() {
            Map<String, String> importMap = new HashMap<>();
            importMap.put("import", "package:test/models/pet.dart");
            assertEquals("package:test/models/pet.dart", resolver.extractImportPath(importMap));
        }

        @Test
        @DisplayName("should return null for Map without 'import' key")
        void testMapWithoutImportKey() {
            Map<String, String> importMap = new HashMap<>();
            importMap.put("other", "value");
            assertNull(resolver.extractImportPath(importMap));
        }

        @Test
        @DisplayName("should return null for unsupported type")
        void testUnsupportedType() {
            assertNull(resolver.extractImportPath(42));
        }

        @Test
        @DisplayName("should return null for null import object")
        void testNullImport() {
            assertNull(resolver.extractImportPath(null));
        }
    }

    // ========================================
    // collectUsedModelImports Tests
    // ========================================

    @Nested
    @DisplayName("collectUsedModelImports")
    class CollectUsedModelImports {

        @Test
        @DisplayName("should collect import for return type")
        void testReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertFalse(imports.isEmpty(), "Should have collected return type import");
        }

        @Test
        @DisplayName("should prefer returnBaseType over returnType for arrays")
        void testArrayReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "List<Pet>";
            op.returnBaseType = "Pet";

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertFalse(imports.isEmpty(), "Should have collected base type import for array");
        }

        @Test
        @DisplayName("should skip void return type")
        void testVoidReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertTrue(imports.isEmpty(), "Should not collect import for void return type");
        }

        @Test
        @DisplayName("should skip null return type")
        void testNullReturnType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = null;

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertTrue(imports.isEmpty(), "Should not collect import for null return type");
        }

        @Test
        @DisplayName("should collect imports for parameter types")
        void testParameterTypes() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";

            CodegenParameter param = new CodegenParameter();
            param.dataType = "Pet";
            param.baseType = "Pet";
            op.allParams = List.of(param);

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertFalse(imports.isEmpty(), "Should have collected parameter type import");
        }

        @Test
        @DisplayName("should skip primitive parameter types")
        void testPrimitiveParameterType() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";

            CodegenParameter param = new CodegenParameter();
            param.dataType = "int";
            param.baseType = "int";
            op.allParams = List.of(param);

            Set<String> imports = resolver.collectUsedModelImports(List.of(op));

            assertTrue(imports.isEmpty(), "Should not collect import for primitive parameter type");
        }

        @Test
        @DisplayName("should collect imports from multiple operations")
        void testMultipleOperations() {
            CodegenOperation op1 = new CodegenOperation();
            op1.returnType = "Pet";
            op1.returnBaseType = "Pet";

            CodegenOperation op2 = new CodegenOperation();
            op2.returnType = "Order";
            op2.returnBaseType = "Order";

            Set<String> imports = resolver.collectUsedModelImports(List.of(op1, op2));

            assertTrue(imports.size() >= 2, "Should have collected imports for both operations");
        }

        @Test
        @DisplayName("should handle operations with null allParams")
        void testNullAllParams() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "void";
            op.allParams = null;

            assertDoesNotThrow(() -> resolver.collectUsedModelImports(List.of(op)));
        }

        @Test
        @DisplayName("should handle empty operations list")
        void testEmptyOperationsList() {
            Set<String> imports = resolver.collectUsedModelImports(Collections.emptyList());
            assertTrue(imports.isEmpty());
        }
    }

    // ========================================
    // resolveImports Integration Tests
    // ========================================

    @Nested
    @DisplayName("resolveImports (integration)")
    class ResolveImports {

        @Test
        @DisplayName("should filter imports to only used models")
        void testFiltersToUsedModels() {
            // Setup operation that uses Pet
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            // Setup operations map with imports
            OperationsMap result = new OperationsMap();
            String petImport = generator.toModelImport("Pet");
            String unusedImport = generator.toModelImport("UnusedModel");

            List<Map<String, String>> imports = new ArrayList<>();
            imports.add(Map.of("import", petImport));
            imports.add(Map.of("import", unusedImport));
            result.put("imports", imports);

            // Run resolver
            resolver.resolveImports(result, List.of(op));

            // Verify
            @SuppressWarnings("unchecked")
            List<String> resolvedImports = (List<String>) result.get("imports");
            assertTrue(resolvedImports.contains(petImport), "Should keep used import: " + petImport);
            assertFalse(resolvedImports.contains(unusedImport), "Should remove unused import: " + unusedImport);
        }

        @Test
        @DisplayName("should filter out primitive type imports")
        void testFiltersPrimitiveImports() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            OperationsMap result = new OperationsMap();
            String petImport = generator.toModelImport("Pet");

            List<Object> imports = new ArrayList<>();
            imports.add(Map.of("import", petImport));
            // Simulate a primitive type import that slipped through
            imports.add(Map.of("import", "package:openapi_client/lib/models/string.dart"));
            result.put("imports", imports);

            resolver.resolveImports(result, List.of(op));

            @SuppressWarnings("unchecked")
            List<String> resolvedImports = (List<String>) result.get("imports");
            assertEquals(1, resolvedImports.size(), "Should only have non-primitive import");
            assertTrue(resolvedImports.contains(petImport));
        }

        @Test
        @DisplayName("should handle empty imports list")
        void testEmptyImports() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            OperationsMap result = new OperationsMap();
            result.put("imports", new ArrayList<>());

            assertDoesNotThrow(() -> resolver.resolveImports(result, List.of(op)));
        }

        @Test
        @DisplayName("should handle null imports list")
        void testNullImports() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            OperationsMap result = new OperationsMap();
            // No imports key set

            assertDoesNotThrow(() -> resolver.resolveImports(result, List.of(op)));
        }

        @Test
        @DisplayName("should handle String import objects alongside Map import objects")
        void testMixedImportTypes() {
            CodegenOperation op = new CodegenOperation();
            op.returnType = "Pet";
            op.returnBaseType = "Pet";

            OperationsMap result = new OperationsMap();
            String petImport = generator.toModelImport("Pet");

            List<Object> imports = new ArrayList<>();
            imports.add(petImport); // String import
            imports.add(Map.of("import", generator.toModelImport("UnusedModel"))); // Map import
            result.put("imports", imports);

            resolver.resolveImports(result, List.of(op));

            @SuppressWarnings("unchecked")
            List<String> resolvedImports = (List<String>) result.get("imports");
            assertTrue(resolvedImports.contains(petImport));
            assertEquals(1, resolvedImports.size());
        }
    }
}
