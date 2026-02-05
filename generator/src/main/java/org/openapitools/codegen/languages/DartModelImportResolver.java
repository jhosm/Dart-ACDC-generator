package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Final import resolution and cleanup for Dart-ACDC generator.
 *
 * This class performs the final pass over all models to:
 * - Remove simple-name imports added by the base generator
 * - Deduplicate imports across all models
 * - Filter out invalid primitive type imports
 * - Ensure only valid package imports remain
 *
 * This runs after postProcessModels() and after the base generator
 * (DefaultCodegen.postProcessAllModels) adds simple-name imports based
 * on model references.
 */
public class DartModelImportResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartModelImportResolver.class);

    /**
     * Primitive type file names that should never be imported.
     * These are used to filter out invalid imports to non-existent primitive type files.
     * Includes both OpenAPI types and Dart-specific type names.
     */
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean", "int", "double", "num", "array", "object",
            "list", "map", "set", "dynamic", "datetime", "date_time");

    /**
     * Resolves and cleans up all model imports.
     * This is the final import processing step before template rendering.
     *
     * The base generator (DefaultCodegen.postProcessAllModels) adds simple-name imports
     * like "Cat", "Dog" based on model references. This method filters those out and
     * ensures only valid package imports (package:xxx/models/yyy.dart) remain.
     *
     * @param objs the map of all models (key: model name, value: ModelsMap)
     * @return the processed models map with cleaned imports
     */
    public Map<String, ModelsMap> resolveAllImports(Map<String, ModelsMap> objs) {
        if (objs == null || objs.isEmpty()) {
            return objs;
        }

        int totalModels = 0;
        int totalImportsProcessed = 0;
        int totalImportsRemoved = 0;

        // Process each model in the map
        for (Map.Entry<String, ModelsMap> entry : objs.entrySet()) {
            ModelsMap modelsMap = entry.getValue();
            if (modelsMap == null || modelsMap.getModels() == null) {
                continue;
            }

            for (ModelMap modelMap : modelsMap.getModels()) {
                CodegenModel model = modelMap.getModel();
                if (model == null || model.imports == null || model.imports.isEmpty()) {
                    continue;
                }

                totalModels++;
                int originalSize = model.imports.size();
                totalImportsProcessed += originalSize;

                // Filter and deduplicate imports
                Set<String> validImports = filterAndDeduplicateImports(model.imports);

                int removed = originalSize - validImports.size();
                totalImportsRemoved += removed;

                // Replace imports with the cleaned set
                model.imports = new TreeSet<>(validImports);

                if (removed > 0) {
                    LOGGER.debug("Cleaned {} imports for model '{}': {} -> {} imports",
                            removed, model.classname, originalSize, validImports.size());
                }
            }
        }

        LOGGER.info("Import resolution complete: {} models processed, {} imports removed from {} total",
                totalModels, totalImportsRemoved, totalImportsProcessed);

        return objs;
    }

    /**
     * Filters and deduplicates a collection of import objects.
     * Removes invalid imports (non-package imports, primitive type imports) and
     * ensures each import appears only once.
     *
     * @param imports the collection of import objects to filter
     * @return deduplicated set of valid import strings
     */
    private Set<String> filterAndDeduplicateImports(Collection<?> imports) {
        Set<String> validImports = new TreeSet<>();

        for (Object importObj : imports) {
            String importStr = importObj.toString();

            // Only keep imports that:
            // 1. Start with "package:" (valid Dart import)
            // 2. Don't reference primitive types (no string.dart, int.dart, etc.)
            if (isValidImport(importStr)) {
                validImports.add(importStr);
            }
        }

        return validImports;
    }

    /**
     * Checks if an import is valid for Dart code generation.
     * An import is valid if it's a package import and doesn't reference a primitive type.
     *
     * @param importStr the import string to validate
     * @return true if the import is valid, false otherwise
     */
    private boolean isValidImport(String importStr) {
        if (importStr == null || importStr.isEmpty()) {
            return false;
        }

        // Must be a package import
        if (!importStr.startsWith("package:")) {
            return false;
        }

        // Must not be a primitive type import
        if (isPrimitiveTypeImport(importStr)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if an import path references a primitive type file that doesn't exist.
     * Primitive types in Dart (int, String, bool, etc.) are built-in and don't
     * have corresponding .dart files in the models directory.
     *
     * @param importPath the import path to check (e.g., "package:foo/models/string.dart")
     * @return true if this is an invalid primitive type import, false otherwise
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

    /**
     * Converts a type name to its corresponding model import path.
     * This is a utility method that can be used by other processors.
     *
     * @param typeName    the type name to convert (e.g., "Cat", "Dog")
     * @param generator   the generator instance for accessing toModelImport
     * @return the import path, or null if the type is a primitive
     */
    public String getModelImportFromType(String typeName, DartAcdcGenerator generator) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }

        // Don't generate imports for primitive types
        if (PRIMITIVE_TYPES.contains(typeName.toLowerCase())) {
            return null;
        }

        return generator.toModelImport(typeName);
    }
}
