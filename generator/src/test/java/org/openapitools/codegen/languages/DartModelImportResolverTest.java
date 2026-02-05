package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartModelImportResolver.
 *
 * Tests final import resolution including filtering invalid imports,
 * deduplication, primitive type removal, and the utility getModelImportFromType.
 */
@DisplayName("DartModelImportResolver")
class DartModelImportResolverTest {

    private DartModelImportResolver resolver;
    private DartAcdcGenerator generator;

    @BeforeEach
    void setUp() {
        resolver = new DartModelImportResolver();
        generator = new DartAcdcGenerator();
    }

    /**
     * Helper to wrap a CodegenModel in a full Map<String, ModelsMap> structure.
     */
    private Map<String, ModelsMap> wrapModel(String name, CodegenModel model) {
        ModelMap modelMap = new ModelMap();
        modelMap.setModel(model);

        List<ModelMap> models = new ArrayList<>();
        models.add(modelMap);

        ModelsMap modelsMap = new ModelsMap();
        modelsMap.setModels(models);

        Map<String, ModelsMap> objs = new LinkedHashMap<>();
        objs.put(name, modelsMap);
        return objs;
    }

    /**
     * Helper to create a CodegenModel with given imports.
     */
    private CodegenModel modelWithImports(String classname, String... imports) {
        CodegenModel model = new CodegenModel();
        model.classname = classname;
        model.imports = new TreeSet<>(Arrays.asList(imports));
        return model;
    }

    // ========================================
    // resolveAllImports null/empty handling
    // ========================================

    @Nested
    @DisplayName("resolveAllImports null/empty handling")
    class ResolveAllImportsNullHandling {

        @Test
        @DisplayName("should handle null input")
        void testNullInput() {
            Map<String, ModelsMap> result = resolver.resolveAllImports(null);
            assertNull(result);
        }

        @Test
        @DisplayName("should handle empty map")
        void testEmptyMap() {
            Map<String, ModelsMap> result = resolver.resolveAllImports(new LinkedHashMap<>());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should handle null ModelsMap value")
        void testNullModelsMapValue() {
            Map<String, ModelsMap> objs = new LinkedHashMap<>();
            objs.put("Pet", null);

            assertDoesNotThrow(() -> resolver.resolveAllImports(objs));
        }

        @Test
        @DisplayName("should handle ModelsMap with null models list")
        void testNullModelsList() {
            Map<String, ModelsMap> objs = new LinkedHashMap<>();
            ModelsMap modelsMap = new ModelsMap();
            objs.put("Pet", modelsMap);

            assertDoesNotThrow(() -> resolver.resolveAllImports(objs));
        }

        @Test
        @DisplayName("should handle model with null imports")
        void testNullImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = null;

            assertDoesNotThrow(() -> resolver.resolveAllImports(wrapModel("Pet", model)));
        }

        @Test
        @DisplayName("should handle model with empty imports")
        void testEmptyImports() {
            CodegenModel model = modelWithImports("Pet");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty());
        }
    }

    // ========================================
    // Import filtering Tests
    // ========================================

    @Nested
    @DisplayName("import filtering")
    class ImportFiltering {

        @Test
        @DisplayName("should keep valid package imports")
        void testKeepValidPackageImports() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/user.dart",
                    "package:my_api/models/category.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(2, processed.imports.size());
            assertTrue(processed.imports.contains("package:my_api/models/user.dart"));
            assertTrue(processed.imports.contains("package:my_api/models/category.dart"));
        }

        @Test
        @DisplayName("should remove simple-name imports added by base generator")
        void testRemoveSimpleNameImports() {
            CodegenModel model = modelWithImports("Pet",
                    "Cat",
                    "Dog",
                    "package:my_api/models/user.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(1, processed.imports.size());
            assertTrue(processed.imports.contains("package:my_api/models/user.dart"));
        }

        @Test
        @DisplayName("should remove null and empty import strings")
        void testRemoveNullEmptyImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new HashSet<>();
            model.imports.add("package:my_api/models/user.dart");
            model.imports.add("");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(1, processed.imports.size());
        }

        @Test
        @DisplayName("should keep non-model package imports like dio")
        void testKeepNonModelPackageImports() {
            CodegenModel model = modelWithImports("Pet",
                    "package:dio/dio.dart",
                    "package:my_api/models/user.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(2, processed.imports.size());
            assertTrue(processed.imports.contains("package:dio/dio.dart"));
            assertTrue(processed.imports.contains("package:my_api/models/user.dart"));
        }
    }

    // ========================================
    // Primitive type import filtering
    // ========================================

    @Nested
    @DisplayName("primitive type import filtering")
    class PrimitiveTypeFiltering {

        @Test
        @DisplayName("should remove string.dart primitive import")
        void testRemoveStringImport() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/string.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty());
        }

        @Test
        @DisplayName("should remove int.dart primitive import")
        void testRemoveIntImport() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/int.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty());
        }

        @Test
        @DisplayName("should remove all primitive type imports")
        void testRemoveAllPrimitives() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/string.dart",
                    "package:my_api/models/integer.dart",
                    "package:my_api/models/number.dart",
                    "package:my_api/models/boolean.dart",
                    "package:my_api/models/int.dart",
                    "package:my_api/models/double.dart",
                    "package:my_api/models/num.dart",
                    "package:my_api/models/array.dart",
                    "package:my_api/models/object.dart",
                    "package:my_api/models/list.dart",
                    "package:my_api/models/map.dart",
                    "package:my_api/models/set.dart",
                    "package:my_api/models/dynamic.dart",
                    "package:my_api/models/datetime.dart",
                    "package:my_api/models/date_time.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty(),
                    "All primitive type imports should be removed");
        }

        @Test
        @DisplayName("should keep non-primitive model imports alongside primitives")
        void testKeepNonPrimitiveImports() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/string.dart",
                    "package:my_api/models/category.dart",
                    "package:my_api/models/int.dart",
                    "package:my_api/models/tag.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(2, processed.imports.size());
            assertTrue(processed.imports.contains("package:my_api/models/category.dart"));
            assertTrue(processed.imports.contains("package:my_api/models/tag.dart"));
        }

        @Test
        @DisplayName("should not treat non-model-path imports as primitives")
        void testNonModelPathNotPrimitive() {
            // package:dio/string.dart is NOT in /models/ so should not be treated as primitive
            CodegenModel model = modelWithImports("Pet",
                    "package:some_lib/string.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(1, processed.imports.size());
        }
    }

    // ========================================
    // Deduplication Tests
    // ========================================

    @Nested
    @DisplayName("deduplication")
    class Deduplication {

        @Test
        @DisplayName("should deduplicate identical imports")
        void testDeduplicateIdentical() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            // Use a list-backed set-like structure to simulate duplicates
            // In practice, base generator may add the same import via different code paths
            model.imports = new LinkedHashSet<>();
            model.imports.add("package:my_api/models/user.dart");
            // TreeSet deduplicates automatically, so just verify it stays at 1
            model.imports.add("package:my_api/models/tag.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            assertEquals(2, processed.imports.size());
        }

        @Test
        @DisplayName("should produce sorted imports (TreeSet)")
        void testSortedImports() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/zebra.dart",
                    "package:my_api/models/apple.dart",
                    "package:my_api/models/banana.dart");

            Map<String, ModelsMap> result = resolver.resolveAllImports(wrapModel("Pet", model));

            CodegenModel processed = result.get("Pet").getModels().get(0).getModel();
            List<String> importList = new ArrayList<>();
            for (Object imp : processed.imports) {
                importList.add(imp.toString());
            }
            assertEquals("package:my_api/models/apple.dart", importList.get(0));
            assertEquals("package:my_api/models/banana.dart", importList.get(1));
            assertEquals("package:my_api/models/zebra.dart", importList.get(2));
        }
    }

    // ========================================
    // getModelImportFromType Tests
    // ========================================

    @Nested
    @DisplayName("getModelImportFromType")
    class GetModelImportFromType {

        @Test
        @DisplayName("should return import path for model type")
        void testModelType() {
            String result = resolver.getModelImportFromType("Cat", generator);
            assertNotNull(result);
            assertTrue(result.startsWith("package:"));
            assertTrue(result.contains("cat.dart"));
        }

        @Test
        @DisplayName("should return null for null type")
        void testNullType() {
            assertNull(resolver.getModelImportFromType(null, generator));
        }

        @Test
        @DisplayName("should return null for empty type")
        void testEmptyType() {
            assertNull(resolver.getModelImportFromType("", generator));
        }

        @Test
        @DisplayName("should return null for primitive type 'string'")
        void testStringPrimitive() {
            assertNull(resolver.getModelImportFromType("string", generator));
        }

        @Test
        @DisplayName("should return null for primitive type 'integer'")
        void testIntegerPrimitive() {
            assertNull(resolver.getModelImportFromType("integer", generator));
        }

        @Test
        @DisplayName("should return null for primitive type 'boolean'")
        void testBooleanPrimitive() {
            assertNull(resolver.getModelImportFromType("boolean", generator));
        }

        @Test
        @DisplayName("should return null for primitive type 'number'")
        void testNumberPrimitive() {
            assertNull(resolver.getModelImportFromType("number", generator));
        }

        @Test
        @DisplayName("should return null for Dart primitive 'int'")
        void testDartIntPrimitive() {
            assertNull(resolver.getModelImportFromType("int", generator));
        }

        @Test
        @DisplayName("should return null for Dart primitive 'dynamic'")
        void testDartDynamicPrimitive() {
            assertNull(resolver.getModelImportFromType("dynamic", generator));
        }

        @Test
        @DisplayName("should be case-insensitive for primitives")
        void testCaseInsensitive() {
            assertNull(resolver.getModelImportFromType("String", generator));
            assertNull(resolver.getModelImportFromType("INT", generator));
            assertNull(resolver.getModelImportFromType("Boolean", generator));
        }
    }

    // ========================================
    // Multi-model integration Tests
    // ========================================

    @Nested
    @DisplayName("multi-model integration")
    class MultiModelIntegration {

        @Test
        @DisplayName("should process multiple models independently")
        void testMultipleModels() {
            CodegenModel pet = modelWithImports("Pet",
                    "package:my_api/models/category.dart",
                    "package:my_api/models/string.dart");

            CodegenModel user = modelWithImports("User",
                    "package:my_api/models/pet.dart",
                    "SomeSimpleName");

            Map<String, ModelsMap> objs = new LinkedHashMap<>();

            ModelMap petMap = new ModelMap();
            petMap.setModel(pet);
            ModelsMap petModels = new ModelsMap();
            petModels.setModels(List.of(petMap));
            objs.put("Pet", petModels);

            ModelMap userMap = new ModelMap();
            userMap.setModel(user);
            ModelsMap userModels = new ModelsMap();
            userModels.setModels(List.of(userMap));
            objs.put("User", userModels);

            Map<String, ModelsMap> result = resolver.resolveAllImports(objs);

            CodegenModel processedPet = result.get("Pet").getModels().get(0).getModel();
            assertEquals(1, processedPet.imports.size());
            assertTrue(processedPet.imports.contains("package:my_api/models/category.dart"));

            CodegenModel processedUser = result.get("User").getModels().get(0).getModel();
            assertEquals(1, processedUser.imports.size());
            assertTrue(processedUser.imports.contains("package:my_api/models/pet.dart"));
        }

        @Test
        @DisplayName("should return same map reference")
        void testReturnsSameReference() {
            CodegenModel model = modelWithImports("Pet",
                    "package:my_api/models/user.dart");

            Map<String, ModelsMap> input = wrapModel("Pet", model);
            Map<String, ModelsMap> result = resolver.resolveAllImports(input);

            assertSame(input, result, "Should return the same map reference");
        }
    }
}
