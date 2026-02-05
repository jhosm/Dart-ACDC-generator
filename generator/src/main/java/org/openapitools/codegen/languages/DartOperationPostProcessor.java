package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post-processes operations for Dart code generation.
 *
 * <p>This class handles the core operation transformations required to convert
 * OpenAPI Generator's raw operation output into Dart-compatible types and
 * conventions:</p>
 *
 * <ul>
 *   <li><strong>HTTP method normalization:</strong> Converts to lowercase for Dio</li>
 *   <li><strong>Return type fixing:</strong> Converts raw schema names to PascalCase</li>
 *   <li><strong>Multipart handling:</strong> Detects multipart/form-data and converts
 *       binary parameters ({@code List<int>}) to {@code MultipartFile}</li>
 *   <li><strong>Import scanning:</strong> Collects special imports from parameters</li>
 *   <li><strong>Array type fixing:</strong> Ensures {@code List<T>} has generic parameter</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Layer 5 - Operation Processing</p>
 *
 * @see DartOperationEnricher
 * @see DartOperationImportResolver
 */
public class DartOperationPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartOperationPostProcessor.class);

    // Constants for content types and media
    private static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String MEDIA_TYPE_KEY = "mediaType";

    // Constants for vendor extensions
    private static final String VENDOR_EXTENSION_IS_MULTIPART = "x-is-multipart";
    private static final String VENDOR_EXTENSION_IS_MULTIPART_FILE = "x-is-multipart-file";
    private static final String VENDOR_EXTENSION_DART_IMPORT = "x-dart-import";

    // Constants for Dart types and imports
    private static final String DART_TYPE_MULTIPART_FILE = "MultipartFile";
    private static final String DART_TYPE_LIST_INT = "List<int>";
    private static final String DART_IMPORT_DIO = "package:dio/dio.dart";

    /**
     * The parent generator for delegating toModelName calls.
     */
    private final DartAcdcGenerator generator;

    /**
     * The set of language-specific primitive types.
     */
    private final Set<String> languageSpecificPrimitives;

    /**
     * Creates a new DartOperationPostProcessor.
     *
     * @param generator                  the parent generator for model name conversion
     * @param languageSpecificPrimitives the set of primitive types to exclude from fixing
     */
    public DartOperationPostProcessor(DartAcdcGenerator generator, Set<String> languageSpecificPrimitives) {
        this.generator = generator;
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    /**
     * Post-processes a list of operations for Dart code generation.
     *
     * <p>For each operation, this method:</p>
     * <ol>
     *   <li>Normalizes the HTTP method to lowercase</li>
     *   <li>Fixes return type and base type to PascalCase</li>
     *   <li>Detects and handles multipart/form-data operations</li>
     *   <li>Scans parameters for special import requirements</li>
     *   <li>Fixes array return types to include generic parameter</li>
     * </ol>
     *
     * @param ops the list of operations to post-process
     */
    public void postProcessOperations(List<CodegenOperation> ops) {
        for (CodegenOperation operation : ops) {
            normalizeHttpMethod(operation);
            fixReturnTypes(operation);
            handleMultipartOperation(operation);
            collectParameterImports(operation);
            fixArrayReturnType(operation);
        }
    }

    /**
     * Converts HTTP method to lowercase for Dio method calls (GET → get, POST → post).
     *
     * @param operation the operation to normalize
     */
    void normalizeHttpMethod(CodegenOperation operation) {
        if (operation.httpMethod != null) {
            operation.httpMethod = operation.httpMethod.toLowerCase();
        }
    }

    /**
     * Fixes returnType and returnBaseType to use proper Dart PascalCase class names.
     *
     * <p>OpenAPI Generator may set these to raw schema names (e.g., "ping_200_response")
     * but Dart classes are generated with PascalCase (e.g., "Ping200Response").</p>
     *
     * @param operation the operation whose return types to fix
     */
    void fixReturnTypes(CodegenOperation operation) {
        if (operation.returnType != null && !operation.returnType.isEmpty()) {
            if (!languageSpecificPrimitives.contains(operation.returnType) &&
                    !operation.returnType.equals("void") &&
                    !operation.returnType.contains("<") &&
                    !operation.returnType.contains(">")) {
                String fixedReturnType = generator.toModelName(operation.returnType);
                if (!fixedReturnType.equals(operation.returnType)) {
                    LOGGER.info("Fixed returnType from '{}' to '{}' for operation {}",
                            operation.returnType, fixedReturnType, operation.operationId);
                    operation.returnType = fixedReturnType;
                }
            }
        }
        if (operation.returnBaseType != null && !operation.returnBaseType.isEmpty()) {
            if (!languageSpecificPrimitives.contains(operation.returnBaseType) &&
                    !operation.returnBaseType.equals("void") &&
                    !operation.returnBaseType.contains("<") &&
                    !operation.returnBaseType.contains(">")) {
                String fixedBaseType = generator.toModelName(operation.returnBaseType);
                if (!fixedBaseType.equals(operation.returnBaseType)) {
                    LOGGER.info("Fixed returnBaseType from '{}' to '{}' for operation {}",
                            operation.returnBaseType, fixedBaseType, operation.operationId);
                    operation.returnBaseType = fixedBaseType;
                }
            }
        }
    }

    /**
     * Detects multipart/form-data operations and converts binary parameters
     * from {@code List<int>} to {@code MultipartFile}.
     *
     * <p>When a multipart operation is detected, this method:</p>
     * <ol>
     *   <li>Marks the operation with {@code x-is-multipart} vendor extension</li>
     *   <li>Adds the Dio import for MultipartFile</li>
     *   <li>Converts binary parameters to MultipartFile type</li>
     *   <li>Synchronizes type changes across all parameter lists</li>
     * </ol>
     *
     * @param operation the operation to check and process
     */
    void handleMultipartOperation(CodegenOperation operation) {
        boolean isMultipartOperation = operation.hasConsumes && operation.consumes != null &&
                operation.consumes.stream().anyMatch(consume -> {
                    Object mediaType = consume.get(MEDIA_TYPE_KEY);
                    return mediaType instanceof String && CONTENT_TYPE_MULTIPART_FORM_DATA.equals(mediaType);
                });

        if (!isMultipartOperation) {
            return;
        }

        // Mark the operation
        operation.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART, true);

        // Add MultipartFile import for this operation
        operation.imports.add(DART_IMPORT_DIO);

        // Process all parameters to change binary types to MultipartFile
        if (operation.allParams != null) {
            for (CodegenParameter param : operation.allParams) {
                if (param.isBinary || DART_TYPE_LIST_INT.equals(param.dataType)) {
                    param.dataType = DART_TYPE_MULTIPART_FILE;
                    param.datatypeWithEnum = DART_TYPE_MULTIPART_FILE;
                    param.baseType = DART_TYPE_MULTIPART_FILE;
                    param.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
                    param.vendorExtensions.put(VENDOR_EXTENSION_DART_IMPORT, DART_IMPORT_DIO);

                    // Update in all parameter lists
                    updateParameterInLists(operation, param);
                }
            }
        }
    }

    /**
     * Scans all parameters for any that require special imports (e.g., Dio for MultipartFile).
     *
     * @param operation the operation whose parameters to scan
     */
    void collectParameterImports(CodegenOperation operation) {
        if (operation.allParams == null) {
            return;
        }
        for (CodegenParameter param : operation.allParams) {
            if (param.vendorExtensions.containsKey(VENDOR_EXTENSION_DART_IMPORT)) {
                String dartImport = (String) param.vendorExtensions.get(VENDOR_EXTENSION_DART_IMPORT);
                if (dartImport != null && !dartImport.isEmpty()) {
                    operation.imports.add(dartImport);
                }
            }
        }
    }

    /**
     * Fixes array return types to ensure {@code List<T>} has a generic parameter.
     *
     * <p>OpenAPI Generator may set returnType to bare "List" without the generic
     * parameter. This method adds it from returnBaseType when needed.</p>
     *
     * @param operation the operation whose return type to fix
     */
    void fixArrayReturnType(CodegenOperation operation) {
        if (operation.returnType == null || !operation.isArray || operation.returnBaseType == null) {
            return;
        }
        if ("List".equals(operation.returnType)) {
            operation.returnType = "List<" + operation.returnBaseType + ">";
        }
        operation.vendorExtensions.put("isListContainer", true);
    }

    /**
     * Synchronizes parameter type information across all parameter lists
     * (allParams, bodyParams, formParams).
     *
     * <p>Required because OpenAPI Generator creates separate instances for each list,
     * so type changes in allParams are not automatically reflected in bodyParams
     * or formParams.</p>
     *
     * @param operation the operation containing the parameter lists
     * @param param     the parameter with updated type information to propagate
     */
    void updateParameterInLists(CodegenOperation operation, CodegenParameter param) {
        if (operation.bodyParams != null) {
            for (CodegenParameter p : operation.bodyParams) {
                if (p.paramName.equals(param.paramName)) {
                    p.dataType = param.dataType;
                    p.datatypeWithEnum = param.datatypeWithEnum;
                    p.baseType = param.baseType;
                }
            }
        }

        if (operation.formParams != null) {
            for (CodegenParameter p : operation.formParams) {
                if (p.paramName.equals(param.paramName)) {
                    p.dataType = param.dataType;
                    p.datatypeWithEnum = param.datatypeWithEnum;
                    p.baseType = param.baseType;
                }
            }
        }
    }
}
