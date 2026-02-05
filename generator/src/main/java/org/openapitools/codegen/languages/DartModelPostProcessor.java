package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Post-processes CodegenModel instances for Dart-ACDC generator.
 *
 * This class is responsible for:
 * - Fixing model imports (converting simple names to package paths)
 * - Processing enum variables with collision-resistant naming
 * - Adding special imports for properties (MultipartFile, composition types)
 * - Adding imports for oneOf/anyOf sealed class alternatives
 * - Initial import cleanup (removing primitive type imports)
 *
 * This processor runs after model creation but before final import resolution.
 * Final import cleanup happens in DartModelImportResolver.
 */
public class DartModelPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartModelPostProcessor.class);

    /**
     * Vendor extension key for Dart-specific imports (e.g., package:dio/dio.dart for MultipartFile).
     */
    private static final String VENDOR_EXTENSION_DART_IMPORT = "x-dart-import";

    /**
     * Primitive type file names that should never be imported.
     * These are used to filter out invalid imports to non-existent primitive type files.
     * Includes both OpenAPI types and Dart-specific type names.
     */
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean", "int", "double", "num", "array", "object",
            "list", "map", "set", "dynamic", "datetime", "date_time");

    private final DartAcdcGenerator generator;
    private final DartEnumHandler enumHandler;

    /**
     * Constructs a DartModelPostProcessor.
     *
     * @param generator   the parent generator for accessing utility methods
     * @param enumHandler handler for enum variable creation
     */
    public DartModelPostProcessor(DartAcdcGenerator generator, DartEnumHandler enumHandler) {
        this.generator = generator;
        this.enumHandler = enumHandler;
    }

    /**
     * Post-processes a ModelsMap containing CodegenModel instances.
     * This is called after initial model creation to add metadata, fix imports,
     * and process enums.
     *
     * @param objs the models map to process
     * @return the processed models map
     */
    public ModelsMap postProcess(ModelsMap objs) {
        if (objs == null || objs.getModels() == null) {
            return objs;
        }

        for (ModelMap modelMap : objs.getModels()) {
            CodegenModel model = modelMap.getModel();
            if (model == null) {
                continue;
            }

            // Fix model imports: convert from simple model names to proper import paths
            fixModelImports(model);

            // Process enums
            processEnums(model);

            // Scan properties for special imports
            processPropertyImports(model);

            // Add imports for oneOf/anyOf sealed class alternatives
            addAlternativeImports(model);

            // Clean up imports: keep only valid package imports
            // Note: Final cleanup happens in DartModelImportResolver after base class adds more imports
            cleanupImports(model);
        }

        return objs;
    }

    /**
     * Fixes model imports by converting simple model names to proper package import paths.
     * Example: "Cat" -> "package:openapi/model/cat.dart"
     *
     * @param model the model to fix imports for
     */
    private void fixModelImports(CodegenModel model) {
        if (model.imports == null || model.imports.isEmpty()) {
            return;
        }

        Set<String> fixedImports = new HashSet<>();
        for (Object importObj : model.imports) {
            if (importObj instanceof String) {
                String importStr = (String) importObj;
                // Check if this is already a full path (starts with package:)
                if (importStr.startsWith("package:")) {
                    fixedImports.add(importStr);
                } else {
                    // Convert model name to import path
                    String importPath = generator.toModelImport(importStr);
                    if (importPath != null && !importPath.isEmpty()) {
                        fixedImports.add(importPath);
                    }
                }
            } else if (importObj instanceof Map) {
                // Extract import path from map
                Map<?, ?> importMap = (Map<?, ?>) importObj;
                Object importPath = importMap.get("import");
                if (importPath != null) {
                    fixedImports.add(importPath.toString());
                }
            }
        }
        model.imports.clear();
        model.imports.addAll(fixedImports);
    }

    /**
     * Processes enum values and creates enumVars with collision-resistant naming.
     * Delegates to DartEnumHandler for actual enum variable creation.
     *
     * @param model the model to process enums for
     */
    private void processEnums(CodegenModel model) {
        if (!model.isEnum || model.allowableValues == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) model.allowableValues.get("values");

        if (values != null && !values.isEmpty()) {
            // Determine datatype from model
            String datatype = model.dataType != null ? model.dataType : "string";

            // Create enumVars with collision-resistant naming
            List<Map<String, Object>> enumVars = enumHandler.createEnumVars(values, datatype);
            model.allowableValues.put("enumVars", enumVars);

            LOGGER.debug("Processed enum for model '{}': {} values", model.classname, values.size());
        }
    }

    /**
     * Scans model properties for special imports (MultipartFile, composition properties).
     * Adds necessary imports to the model.
     *
     * @param model the model to scan properties for
     */
    private void processPropertyImports(CodegenModel model) {
        if (model.vars == null) {
            return;
        }

        for (CodegenProperty prop : model.vars) {
            // Add imports for properties with special Dart imports (e.g., MultipartFile)
            if (prop.vendorExtensions.containsKey(VENDOR_EXTENSION_DART_IMPORT)) {
                String dartImport = (String) prop.vendorExtensions.get(VENDOR_EXTENSION_DART_IMPORT);
                if (dartImport != null && !dartImport.isEmpty()) {
                    model.imports.add(dartImport);
                    LOGGER.debug("Added special import for property '{}': {}", prop.name, dartImport);
                }
            }

            // Add imports for oneOf/anyOf composition properties
            if (prop.vendorExtensions.containsKey("x-is-composition-property") ||
                prop.vendorExtensions.containsKey("x-is-one-of-property") ||
                prop.vendorExtensions.containsKey("x-is-any-of-property")) {

                // Mark model as having composition properties (for test generation)
                model.vendorExtensions.put("x-has-composition-property", true);

                // The property's complexType contains the model name that needs to be imported
                String importPath = generator.toModelImport(prop.complexType);
                if (importPath != null && !importPath.isEmpty()) {
                    model.imports.add(importPath);
                    LOGGER.debug("Added composition import for property '{}': {}", prop.name, importPath);
                }
            }
        }
    }

    /**
     * Adds imports for oneOf/anyOf sealed class alternatives.
     * Checks both x-one-of-alternatives and x-any-of-alternatives vendor extensions.
     *
     * @param model the model to add alternative imports for
     */
    private void addAlternativeImports(CodegenModel model) {
        if (model.vendorExtensions.containsKey("x-is-one-of")) {
            addAlternativeImportsForKey(model, "x-one-of-alternatives");
        }
        if (model.vendorExtensions.containsKey("x-is-any-of")) {
            addAlternativeImportsForKey(model, "x-any-of-alternatives");
        }
    }

    /**
     * Adds imports for sealed class alternatives from a specific vendor extension key.
     * Extracts import paths from alternative metadata and adds them to model.imports.
     *
     * @param model           the codegen model
     * @param alternativesKey the vendor extension key containing alternatives
     *                        ("x-one-of-alternatives" or "x-any-of-alternatives")
     */
    @SuppressWarnings("unchecked")
    private void addAlternativeImportsForKey(CodegenModel model, String alternativesKey) {
        Object alternativesObj = model.vendorExtensions.get(alternativesKey);
        if (!(alternativesObj instanceof List)) {
            return;
        }

        List<Map<String, Object>> alternatives = (List<Map<String, Object>>) alternativesObj;
        for (Map<String, Object> alternative : alternatives) {
            // Only add imports for reference types (not primitives or inline schemas)
            Object isRef = alternative.get("isRef");
            if (Boolean.TRUE.equals(isRef)) {
                Object importPathObj = alternative.get("importPath");
                if (importPathObj instanceof String) {
                    String importPath = (String) importPathObj;
                    if (!importPath.isEmpty()) {
                        model.imports.add(importPath);
                        LOGGER.info("Added import for sealed class alternative: {}", importPath);
                    }
                }
            }
        }
    }

    /**
     * Cleans up model imports by removing invalid imports (primitives, non-package imports).
     * This is an initial cleanup - final cleanup happens in DartModelImportResolver.
     *
     * @param model the model to cleanup imports for
     */
    private void cleanupImports(CodegenModel model) {
        if (model.imports == null || model.imports.isEmpty()) {
            return;
        }

        Set<String> validImports = new HashSet<>();
        for (Object importObj : model.imports) {
            String importStr = importObj.toString();
            // Only keep imports that start with "package:" and don't reference primitive types
            if (importStr.startsWith("package:") && !isPrimitiveTypeImport(importStr)) {
                validImports.add(importStr);
            }
        }
        model.imports = new TreeSet<>(validImports);
    }

    /**
     * Checks if an import path references a primitive type file that doesn't exist.
     *
     * @param importPath the import path to check
     * @return true if this is an invalid primitive type import
     */
    private boolean isPrimitiveTypeImport(String importPath) {
        if (importPath == null || !importPath.contains("/models/")) {
            return false;
        }

        // Extract the filename (e.g., "string.dart" from "package:foo/models/string.dart")
        int lastSlash = importPath.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String filename = importPath.substring(lastSlash + 1);
        // Remove .dart extension
        if (filename.endsWith(".dart")) {
            filename = filename.substring(0, filename.length() - 5);
        }

        return PRIMITIVE_TYPES.contains(filename);
    }
}
