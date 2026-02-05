package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.model.OperationsMap;

import java.util.*;

/**
 * Resolves and filters operation imports to include only models actually used
 * in operation signatures (parameters and return types).
 *
 * <p>OpenAPI Generator populates operation imports with all referenced schemas,
 * but many of these are internal types not directly used in method signatures.
 * This resolver filters imports to only include models that appear as parameter
 * types or return types, preventing unused imports in generated code.</p>
 *
 * <p><strong>Architecture:</strong> Layer 5 - Operation Processing</p>
 *
 * @see DartOperationPostProcessor
 * @see DartOperationEnricher
 */
public class DartOperationImportResolver {

    /**
     * Primitive type file names that should never be imported as models.
     */
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean", "int", "double", "num",
            "array", "object", "list", "map", "set", "dynamic", "datetime", "date_time");

    /**
     * The parent generator for delegating toModelImport calls.
     */
    private final DartAcdcGenerator generator;

    /**
     * The set of language-specific primitive types.
     */
    private final Set<String> languageSpecificPrimitives;

    /**
     * Creates a new DartOperationImportResolver.
     *
     * @param generator                  the parent generator for model import conversion
     * @param languageSpecificPrimitives the set of primitive types to exclude from imports
     */
    public DartOperationImportResolver(DartAcdcGenerator generator, Set<String> languageSpecificPrimitives) {
        this.generator = generator;
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    /**
     * Resolves and filters operation imports in the operations map.
     *
     * <p>This method performs two passes:</p>
     * <ol>
     *   <li><strong>Collection pass:</strong> Scans all operations to identify which
     *       models are actually referenced in return types and parameters</li>
     *   <li><strong>Filter pass:</strong> Filters the existing imports list to only
     *       include valid, non-primitive, actually-used model imports</li>
     * </ol>
     *
     * @param result the operations map containing the imports to filter
     * @param ops    the list of operations to scan for used types
     */
    public void resolveImports(OperationsMap result, List<CodegenOperation> ops) {
        // First pass: identify which models are actually used
        Set<String> usedModelImports = collectUsedModelImports(ops);

        // Second pass: fix and filter imports
        filterImports(result, usedModelImports);
    }

    /**
     * Scans all operations to collect model import paths for types actually
     * used in operation signatures (return types and parameters).
     *
     * @param ops the operations to scan
     * @return set of import paths for actually-used model types
     */
    Set<String> collectUsedModelImports(List<CodegenOperation> ops) {
        Set<String> usedModelImports = new HashSet<>();

        for (CodegenOperation operation : ops) {
            // Add imports for return types
            if (operation.returnType != null && !operation.returnType.equals("void")) {
                String returnModelImport = getModelImportFromType(
                        operation.returnBaseType != null ? operation.returnBaseType : operation.returnType);
                if (returnModelImport != null) {
                    usedModelImports.add(returnModelImport);
                }
            }

            // Add imports for parameters
            if (operation.allParams != null) {
                for (CodegenParameter param : operation.allParams) {
                    String paramModelImport = getModelImportFromType(
                            param.baseType != null ? param.baseType : param.dataType);
                    if (paramModelImport != null) {
                        usedModelImports.add(paramModelImport);
                    }
                }
            }
        }

        return usedModelImports;
    }

    /**
     * Filters the imports list in the operations map to only include valid,
     * non-primitive, actually-used model imports.
     *
     * <p>OpenAPI Generator populates imports as a list of Map objects with an
     * "import" key. This method extracts the actual import paths and filters
     * them against the set of used model imports.</p>
     *
     * @param result           the operations map containing the imports list
     * @param usedModelImports the set of import paths that are actually used
     */
    void filterImports(OperationsMap result, Set<String> usedModelImports) {
        List<?> imports = (List<?>) result.get("imports");
        if (imports == null || imports.isEmpty()) {
            return;
        }

        List<String> fixedImports = new ArrayList<>();
        for (Object importObj : imports) {
            String importPath = extractImportPath(importObj);

            // Only add valid package imports that:
            // 1. Don't reference primitive types
            // 2. Are actually used in operation signatures
            if (importPath != null &&
                    importPath.startsWith("package:") &&
                    !isPrimitiveTypeImport(importPath) &&
                    usedModelImports.contains(importPath)) {
                fixedImports.add(importPath);
            }
        }
        result.put("imports", fixedImports);
    }

    /**
     * Extracts the import path string from an import object.
     *
     * <p>Import objects can be either plain Strings or Maps with an "import" key,
     * depending on how OpenAPI Generator populated them.</p>
     *
     * @param importObj the import object (String or Map)
     * @return the import path string, or null if not extractable
     */
    String extractImportPath(Object importObj) {
        if (importObj instanceof String) {
            return (String) importObj;
        } else if (importObj instanceof Map) {
            Map<?, ?> importMap = (Map<?, ?>) importObj;
            Object importPathObj = importMap.get("import");
            if (importPathObj != null) {
                return importPathObj.toString();
            }
        }
        return null;
    }

    /**
     * Converts a type name to its corresponding model import path.
     * Filters out primitive types and language-specific primitives.
     *
     * @param typeName the type name (e.g., "Pet", "List", "int")
     * @return the model import path, or null if not a model type
     */
    String getModelImportFromType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }

        // Skip language-specific primitives
        if (languageSpecificPrimitives.contains(typeName)) {
            return null;
        }

        // Skip special Dart types that aren't models
        if (typeName.equals("MultipartFile") || typeName.equals("List") ||
                typeName.equals("Map") || typeName.equals("void") || typeName.equals("file")) {
            return null;
        }

        // Skip types with generic parameters (e.g., "List<int>", "Map<String, dynamic>")
        if (typeName.contains("<") || typeName.contains(">")) {
            return null;
        }

        // Convert model name to import path
        return generator.toModelImport(typeName);
    }

    /**
     * Checks if an import path references a primitive type file.
     * Primitive types should never be imported as models.
     *
     * @param importPath the import path to check
     * @return true if this is a primitive type import that should be filtered out
     */
    boolean isPrimitiveTypeImport(String importPath) {
        if (importPath == null || !importPath.contains("/models/")) {
            return false;
        }

        int lastSlash = importPath.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String filename = importPath.substring(lastSlash + 1);
        if (filename.endsWith(".dart")) {
            filename = filename.substring(0, filename.length() - 5);
        }

        return PRIMITIVE_TYPES.contains(filename);
    }
}
