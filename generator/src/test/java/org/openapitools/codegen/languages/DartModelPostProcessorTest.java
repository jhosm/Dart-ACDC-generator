package org.openapitools.codegen.languages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DartModelPostProcessor.
 *
 * Tests model post-processing including import fixing, enum processing,
 * property import scanning, alternative imports, and import cleanup.
 */
@DisplayName("DartModelPostProcessor")
class DartModelPostProcessorTest {

    private DartAcdcGenerator generator;
    private DartEnumHandler enumHandler;
    private DartModelPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        generator = new DartAcdcGenerator();
        enumHandler = new DartEnumHandler(generator.reservedWords());
        postProcessor = new DartModelPostProcessor(generator, enumHandler);
    }

    /**
     * Helper to wrap a CodegenModel in a ModelsMap for postProcess() calls.
     */
    private ModelsMap wrapModel(CodegenModel model) {
        ModelMap modelMap = new ModelMap();
        modelMap.setModel(model);

        List<ModelMap> models = new ArrayList<>();
        models.add(modelMap);

        ModelsMap modelsMap = new ModelsMap();
        modelsMap.setModels(models);
        return modelsMap;
    }

    // ========================================
    // postProcess null/empty handling
    // ========================================

    @Nested
    @DisplayName("postProcess null/empty handling")
    class PostProcessNullHandling {

        @Test
        @DisplayName("should handle null ModelsMap")
        void testNullModelsMap() {
            ModelsMap result = postProcessor.postProcess(null);
            assertNull(result);
        }

        @Test
        @DisplayName("should handle ModelsMap with null models list")
        void testNullModelsList() {
            ModelsMap modelsMap = new ModelsMap();
            // Don't call setModels — getModels() returns null
            ModelsMap result = postProcessor.postProcess(modelsMap);
            assertNotNull(result);
        }

        @Test
        @DisplayName("should handle ModelMap with null model")
        void testNullModel() {
            ModelMap modelMap = new ModelMap();
            // Don't set a model — getModel() returns null

            List<ModelMap> models = new ArrayList<>();
            models.add(modelMap);

            ModelsMap modelsMap = new ModelsMap();
            modelsMap.setModels(models);

            assertDoesNotThrow(() -> postProcessor.postProcess(modelsMap));
        }

        @Test
        @DisplayName("should handle empty models list")
        void testEmptyModelsList() {
            ModelsMap modelsMap = new ModelsMap();
            modelsMap.setModels(new ArrayList<>());

            ModelsMap result = postProcessor.postProcess(modelsMap);
            assertNotNull(result);
            assertTrue(result.getModels().isEmpty());
        }
    }

    // ========================================
    // fixModelImports Tests
    // ========================================

    @Nested
    @DisplayName("fixModelImports")
    class FixModelImports {

        @Test
        @DisplayName("should convert simple model names to package import paths")
        void testSimpleNameToPackagePath() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new TreeSet<>();
            model.imports.add("Cat");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            // "Cat" should be converted to a package: import path
            assertTrue(processed.imports.stream()
                    .allMatch(i -> i.toString().startsWith("package:")),
                    "All imports should be package imports after fixing");
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("cat.dart")),
                    "Should contain cat.dart import");
        }

        @Test
        @DisplayName("should preserve existing package imports")
        void testPreservePackageImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new TreeSet<>();
            model.imports.add("package:dio/dio.dart");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().equals("package:dio/dio.dart")),
                    "Should preserve package:dio/dio.dart import");
        }

        @Test
        @DisplayName("should handle null imports")
        void testNullImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = null;

            assertDoesNotThrow(() -> postProcessor.postProcess(wrapModel(model)));
        }

        @Test
        @DisplayName("should handle empty imports")
        void testEmptyImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new TreeSet<>();

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Test
        @DisplayName("should handle Map-based imports")
        void testMapImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            // Use raw Set to add Map objects (simulates what base generator may produce)
            Set rawImports = new HashSet();

            Map<String, String> importMap = new HashMap<>();
            importMap.put("import", "package:my_api/models/user.dart");
            rawImports.add(importMap);
            model.imports = rawImports;

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("package:my_api/models/user.dart")),
                    "Should extract import from map");
        }
    }

    // ========================================
    // processEnums Tests
    // ========================================

    @Nested
    @DisplayName("processEnums")
    class ProcessEnums {

        @Test
        @DisplayName("should create enumVars for enum model")
        void testEnumModel() {
            CodegenModel model = new CodegenModel();
            model.classname = "Status";
            model.isEnum = true;
            model.dataType = "String";
            model.imports = new TreeSet<>();
            model.allowableValues = new HashMap<>();
            model.allowableValues.put("values", List.of("active", "inactive", "pending"));

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertNotNull(processed.allowableValues.get("enumVars"),
                    "Should create enumVars");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enumVars =
                    (List<Map<String, Object>>) processed.allowableValues.get("enumVars");
            assertEquals(3, enumVars.size(), "Should have 3 enum vars");
        }

        @Test
        @DisplayName("should skip non-enum models")
        void testNonEnumModel() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.isEnum = false;
            model.imports = new TreeSet<>();

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertNull(processed.allowableValues);
        }

        @Test
        @DisplayName("should skip enum with null allowableValues")
        void testEnumNullAllowableValues() {
            CodegenModel model = new CodegenModel();
            model.classname = "Status";
            model.isEnum = true;
            model.allowableValues = null;
            model.imports = new TreeSet<>();

            assertDoesNotThrow(() -> postProcessor.postProcess(wrapModel(model)));
        }

        @Test
        @DisplayName("should skip enum with empty values list")
        void testEnumEmptyValues() {
            CodegenModel model = new CodegenModel();
            model.classname = "Status";
            model.isEnum = true;
            model.imports = new TreeSet<>();
            model.allowableValues = new HashMap<>();
            model.allowableValues.put("values", List.of());

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            // enumVars should not be created for empty values
            assertNull(processed.allowableValues.get("enumVars"));
        }

        @Test
        @DisplayName("should default to string datatype when model dataType is null")
        void testNullDataType() {
            CodegenModel model = new CodegenModel();
            model.classname = "Status";
            model.isEnum = true;
            model.dataType = null;
            model.imports = new TreeSet<>();
            model.allowableValues = new HashMap<>();
            model.allowableValues.put("values", List.of("a", "b"));

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertNotNull(processed.allowableValues.get("enumVars"),
                    "Should create enumVars even with null dataType");
        }

        @Test
        @DisplayName("should handle integer enum values")
        void testIntegerEnumValues() {
            CodegenModel model = new CodegenModel();
            model.classname = "Priority";
            model.isEnum = true;
            model.dataType = "int";
            model.imports = new TreeSet<>();
            model.allowableValues = new HashMap<>();
            model.allowableValues.put("values", List.of(1, 2, 3));

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enumVars =
                    (List<Map<String, Object>>) processed.allowableValues.get("enumVars");
            assertEquals(3, enumVars.size());
        }
    }

    // ========================================
    // processPropertyImports Tests
    // ========================================

    @Nested
    @DisplayName("processPropertyImports")
    class ProcessPropertyImports {

        @Test
        @DisplayName("should add x-dart-import from property vendor extensions")
        void testDartImport() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();

            CodegenProperty prop = new CodegenProperty();
            prop.name = "file";
            prop.vendorExtensions.put("x-dart-import", "package:dio/dio.dart");
            model.vars = List.of(prop);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().equals("package:dio/dio.dart")),
                    "Should add the dart import from property");
        }

        @Test
        @DisplayName("should skip empty x-dart-import")
        void testEmptyDartImport() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();

            CodegenProperty prop = new CodegenProperty();
            prop.name = "file";
            prop.vendorExtensions.put("x-dart-import", "");
            model.vars = List.of(prop);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty(),
                    "Should not add empty imports");
        }

        @Test
        @DisplayName("should handle null vars list")
        void testNullVars() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();
            model.vars = null;

            assertDoesNotThrow(() -> postProcessor.postProcess(wrapModel(model)));
        }

        @Test
        @DisplayName("should add import for composition property")
        void testCompositionPropertyImport() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();

            CodegenProperty prop = new CodegenProperty();
            prop.name = "pet";
            prop.complexType = "Pet";
            prop.vendorExtensions.put("x-is-composition-property", true);
            model.vars = List.of(prop);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.vendorExtensions.containsKey("x-has-composition-property"),
                    "Should mark model as having composition property");
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("pet.dart")),
                    "Should add composition type import");
        }

        @Test
        @DisplayName("should add import for oneOf property")
        void testOneOfPropertyImport() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();

            CodegenProperty prop = new CodegenProperty();
            prop.name = "shape";
            prop.complexType = "Shape";
            prop.vendorExtensions.put("x-is-one-of-property", true);
            model.vars = List.of(prop);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.vendorExtensions.containsKey("x-has-composition-property"));
        }

        @Test
        @DisplayName("should add import for anyOf property")
        void testAnyOfPropertyImport() {
            CodegenModel model = new CodegenModel();
            model.classname = "MyModel";
            model.imports = new TreeSet<>();

            CodegenProperty prop = new CodegenProperty();
            prop.name = "value";
            prop.complexType = "MixedType";
            prop.vendorExtensions.put("x-is-any-of-property", true);
            model.vars = List.of(prop);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.vendorExtensions.containsKey("x-has-composition-property"));
        }
    }

    // ========================================
    // addAlternativeImports Tests
    // ========================================

    @Nested
    @DisplayName("addAlternativeImports")
    class AddAlternativeImports {

        @Test
        @DisplayName("should add imports for oneOf alternatives")
        void testOneOfAlternatives() {
            CodegenModel model = new CodegenModel();
            model.classname = "Animal";
            model.imports = new TreeSet<>();

            List<Map<String, Object>> alternatives = new ArrayList<>();
            Map<String, Object> alt1 = new HashMap<>();
            alt1.put("isRef", true);
            alt1.put("importPath", "package:my_api/models/dog.dart");
            alternatives.add(alt1);

            Map<String, Object> alt2 = new HashMap<>();
            alt2.put("isRef", true);
            alt2.put("importPath", "package:my_api/models/cat.dart");
            alternatives.add(alt2);

            model.vendorExtensions.put("x-is-one-of", true);
            model.vendorExtensions.put("x-one-of-alternatives", alternatives);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("dog.dart")),
                    "Should include Dog import");
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("cat.dart")),
                    "Should include Cat import");
        }

        @Test
        @DisplayName("should add imports for anyOf alternatives")
        void testAnyOfAlternatives() {
            CodegenModel model = new CodegenModel();
            model.classname = "Shape";
            model.imports = new TreeSet<>();

            List<Map<String, Object>> alternatives = new ArrayList<>();
            Map<String, Object> alt = new HashMap<>();
            alt.put("isRef", true);
            alt.put("importPath", "package:my_api/models/circle.dart");
            alternatives.add(alt);

            model.vendorExtensions.put("x-is-any-of", true);
            model.vendorExtensions.put("x-any-of-alternatives", alternatives);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("circle.dart")));
        }

        @Test
        @DisplayName("should skip non-ref alternatives")
        void testNonRefAlternative() {
            CodegenModel model = new CodegenModel();
            model.classname = "Animal";
            model.imports = new TreeSet<>();

            List<Map<String, Object>> alternatives = new ArrayList<>();
            Map<String, Object> alt = new HashMap<>();
            alt.put("isRef", false);
            alt.put("importPath", "package:my_api/models/inline.dart");
            alternatives.add(alt);

            model.vendorExtensions.put("x-is-one-of", true);
            model.vendorExtensions.put("x-one-of-alternatives", alternatives);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty(),
                    "Should not add imports for non-ref alternatives");
        }

        @Test
        @DisplayName("should skip alternatives with empty importPath")
        void testEmptyImportPath() {
            CodegenModel model = new CodegenModel();
            model.classname = "Animal";
            model.imports = new TreeSet<>();

            List<Map<String, Object>> alternatives = new ArrayList<>();
            Map<String, Object> alt = new HashMap<>();
            alt.put("isRef", true);
            alt.put("importPath", "");
            alternatives.add(alt);

            model.vendorExtensions.put("x-is-one-of", true);
            model.vendorExtensions.put("x-one-of-alternatives", alternatives);

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty(),
                    "Should not add empty import paths");
        }

        @Test
        @DisplayName("should handle non-list alternatives gracefully")
        void testNonListAlternatives() {
            CodegenModel model = new CodegenModel();
            model.classname = "Animal";
            model.imports = new TreeSet<>();

            model.vendorExtensions.put("x-is-one-of", true);
            model.vendorExtensions.put("x-one-of-alternatives", "not a list");

            assertDoesNotThrow(() -> postProcessor.postProcess(wrapModel(model)));
        }
    }

    // ========================================
    // cleanupImports Tests
    // ========================================

    @Nested
    @DisplayName("cleanupImports")
    class CleanupImports {

        @Test
        @DisplayName("should remove non-package imports")
        void testRemoveNonPackageImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new HashSet<>();
            // Add a package import that survives, and a bare name that gets cleaned
            model.imports.add("package:my_api/models/user.dart");
            model.imports.add("SomeRawName");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            // After fixModelImports, "SomeRawName" gets converted to a package: import
            // Then cleanupImports keeps only package: imports
            assertTrue(processed.imports.stream()
                    .allMatch(i -> i.toString().startsWith("package:")),
                    "All imports should be package: imports after cleanup");
        }

        @Test
        @DisplayName("should remove primitive type imports")
        void testRemovePrimitiveImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new HashSet<>();
            model.imports.add("package:my_api/models/string.dart");
            model.imports.add("package:my_api/models/int.dart");
            model.imports.add("package:my_api/models/pet.dart");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertFalse(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("string.dart")),
                    "Should remove string.dart primitive import");
            assertFalse(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("int.dart")),
                    "Should remove int.dart primitive import");
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().contains("pet.dart")),
                    "Should keep pet.dart model import");
        }

        @Test
        @DisplayName("should remove boolean and number primitive imports")
        void testRemoveMorePrimitives() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new HashSet<>();
            model.imports.add("package:my_api/models/boolean.dart");
            model.imports.add("package:my_api/models/double.dart");
            model.imports.add("package:my_api/models/dynamic.dart");
            model.imports.add("package:my_api/models/date_time.dart");
            model.imports.add("package:my_api/models/object.dart");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.isEmpty(),
                    "All primitive type imports should be removed");
        }

        @Test
        @DisplayName("should keep non-model package imports like dio")
        void testKeepNonModelImports() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new HashSet<>();
            model.imports.add("package:dio/dio.dart");

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertTrue(processed.imports.stream()
                    .anyMatch(i -> i.toString().equals("package:dio/dio.dart")),
                    "Should keep package:dio/dio.dart");
        }
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Nested
    @DisplayName("postProcess integration")
    class PostProcessIntegration {

        @Test
        @DisplayName("should process multiple models in a single ModelsMap")
        void testMultipleModels() {
            CodegenModel model1 = new CodegenModel();
            model1.classname = "Pet";
            model1.imports = new TreeSet<>();
            model1.imports.add("Cat");

            CodegenModel model2 = new CodegenModel();
            model2.classname = "User";
            model2.imports = new TreeSet<>();
            model2.imports.add("Pet");

            ModelMap mm1 = new ModelMap();
            mm1.setModel(model1);
            ModelMap mm2 = new ModelMap();
            mm2.setModel(model2);

            ModelsMap modelsMap = new ModelsMap();
            modelsMap.setModels(List.of(mm1, mm2));

            ModelsMap result = postProcessor.postProcess(modelsMap);

            CodegenModel processed1 = result.getModels().get(0).getModel();
            CodegenModel processed2 = result.getModels().get(1).getModel();

            // Both should have package: imports
            assertTrue(processed1.imports.stream()
                    .allMatch(i -> i.toString().startsWith("package:")));
            assertTrue(processed2.imports.stream()
                    .allMatch(i -> i.toString().startsWith("package:")));
        }

        @Test
        @DisplayName("should handle enum model with imports and properties together")
        void testEnumWithImportsAndProperties() {
            CodegenModel model = new CodegenModel();
            model.classname = "ColorEnum";
            model.isEnum = true;
            model.dataType = "String";
            model.imports = new TreeSet<>();
            model.allowableValues = new HashMap<>();
            model.allowableValues.put("values", List.of("red", "green", "blue"));
            model.vars = new ArrayList<>();

            ModelsMap result = postProcessor.postProcess(wrapModel(model));

            CodegenModel processed = result.getModels().get(0).getModel();
            assertNotNull(processed.allowableValues.get("enumVars"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enumVars =
                    (List<Map<String, Object>>) processed.allowableValues.get("enumVars");
            assertEquals(3, enumVars.size());
        }

        @Test
        @DisplayName("should return same ModelsMap reference")
        void testReturnsSameReference() {
            CodegenModel model = new CodegenModel();
            model.classname = "Pet";
            model.imports = new TreeSet<>();

            ModelsMap input = wrapModel(model);
            ModelsMap result = postProcessor.postProcess(input);

            assertSame(input, result, "Should return the same ModelsMap reference");
        }
    }
}
