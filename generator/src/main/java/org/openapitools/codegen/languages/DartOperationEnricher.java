package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Enriches operations with test metadata for test template generation.
 *
 * <p>This class adds vendor extensions to operations and parameters that are
 * consumed by Mustache test templates to generate test cases with realistic
 * test data. Enrichment must happen <strong>after</strong> all type conversions
 * (multipart handling, return type fixing) to ensure test values match the
 * final parameter types.</p>
 *
 * <p><strong>Vendor extensions added:</strong></p>
 * <ul>
 *   <li>{@code httpMethodCapitalized} - PascalCase HTTP method (e.g., "Get", "Post")
 *       for method names like onGetJson, onPostJson</li>
 *   <li>{@code sampleResponseJson} - Sample JSON response for mock testing</li>
 *   <li>{@code testValue} - Dart code string for parameter test values</li>
 *   <li>{@code testValueRaw} - Raw string values for URL path embedding</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Layer 5 - Operation Processing</p>
 *
 * @see DartOperationPostProcessor
 * @see DartOperationImportResolver
 * @see DartTestDataGenerator
 */
public class DartOperationEnricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartOperationEnricher.class);

    /**
     * The test data generator for producing test values.
     */
    private final DartTestDataGenerator testDataGenerator;

    /**
     * The set of language-specific primitive types.
     */
    private final Set<String> languageSpecificPrimitives;

    /**
     * Creates a new DartOperationEnricher.
     *
     * @param testDataGenerator          the test data generator for producing test values
     * @param languageSpecificPrimitives the set of primitive types for type checks
     */
    public DartOperationEnricher(DartTestDataGenerator testDataGenerator, Set<String> languageSpecificPrimitives) {
        this.testDataGenerator = testDataGenerator;
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    /**
     * Enriches a list of operations with test metadata.
     *
     * <p>For each operation, this method:</p>
     * <ol>
     *   <li>Adds {@code httpMethodCapitalized} vendor extension</li>
     *   <li>Generates and adds {@code sampleResponseJson} vendor extension</li>
     *   <li>Adds {@code testValue} and {@code testValueRaw} to all parameter lists</li>
     * </ol>
     *
     * @param ops the list of operations to enrich
     */
    public void enrichOperations(List<CodegenOperation> ops) {
        for (CodegenOperation operation : ops) {
            enrichHttpMethod(operation);
            enrichSampleResponse(operation);
            enrichTestValues(operation);
        }
    }

    /**
     * Adds the {@code httpMethodCapitalized} vendor extension for test template
     * method names (e.g., onGetJson, onPostJson).
     *
     * @param operation the operation to enrich
     */
    void enrichHttpMethod(CodegenOperation operation) {
        if (operation.httpMethod != null) {
            String httpMethodLower = operation.httpMethod.toLowerCase();
            String httpMethodPascal = capitalize(httpMethodLower);
            operation.vendorExtensions.put("httpMethodCapitalized", httpMethodPascal);
        }
    }

    /**
     * Generates sample response JSON and adds it as a vendor extension.
     *
     * @param operation the operation to enrich
     */
    void enrichSampleResponse(CodegenOperation operation) {
        String sampleResponseJson = getSampleResponseJson(
                operation.returnType,
                operation.returnBaseType,
                operation.isArray);

        // Ensure sampleResponseJson is never empty - use fallback if needed
        if (sampleResponseJson == null || sampleResponseJson.trim().isEmpty()) {
            sampleResponseJson = operation.isArray ? "[<String, dynamic>{}]" : "<String, dynamic>{}";
            LOGGER.warn("sampleResponseJson was empty for operation {}, using fallback",
                    operation.operationId);
        }
        operation.vendorExtensions.put("sampleResponseJson", sampleResponseJson);
    }

    /**
     * Adds test values to all parameter lists of an operation.
     *
     * <p>OpenAPI Generator creates separate instances for allParams, pathParams,
     * queryParams, etc. We must set vendor extensions on ALL lists for template access.</p>
     *
     * @param operation the operation whose parameters to enrich
     */
    void enrichTestValues(CodegenOperation operation) {
        addTestValuesToParams(operation.allParams);
        addTestValuesToParams(operation.pathParams);
        addTestValuesToParams(operation.queryParams);
        addTestValuesToParams(operation.bodyParams);
        addTestValuesToParams(operation.headerParams);
        addTestValuesToParams(operation.formParams);
    }

    /**
     * Adds testValue and testValueRaw vendor extensions to a list of parameters.
     *
     * @param params the parameter list (may be null)
     */
    void addTestValuesToParams(List<CodegenParameter> params) {
        if (params == null) {
            return;
        }
        for (CodegenParameter param : params) {
            String testValue = testDataGenerator.getTestValueForType(param.dataType);
            param.vendorExtensions.put("testValue", testValue);
            String testValueRaw = testDataGenerator.getTestValueRawForType(param.dataType);
            param.vendorExtensions.put("testValueRaw", testValueRaw);
        }
    }

    /**
     * Generates sample JSON response data for a given return type.
     * Used by test templates to mock API responses.
     *
     * @param returnType     the return type (e.g., "Pet", "List<Pet>")
     * @param returnBaseType the base type for arrays (e.g., "Pet" for "List<Pet>")
     * @param isArray        whether the return type is an array
     * @return a Dart code string representing sample response JSON
     */
    String getSampleResponseJson(String returnType, String returnBaseType, boolean isArray) {
        // Handle null, empty, or void return types
        if (returnType == null || returnType.isEmpty() || returnType.trim().isEmpty() || returnType.equals("void")) {
            return "null";
        }

        // Trim whitespace
        returnType = returnType.trim();

        // Determine the model name to use for generating sample data
        String modelName = (returnBaseType != null && !returnBaseType.isEmpty()) ? returnBaseType : returnType;

        // Handle array responses FIRST (before primitive check, since "List" is a primitive)
        if (isArray) {
            String elementJson = testDataGenerator.generateTestJsonForModel(modelName);
            return "[" + elementJson + "]";
        }

        // Handle primitive return types
        if (languageSpecificPrimitives.contains(returnType)) {
            return switch (returnType) {
                case "int", "double", "num" -> "42";
                case "bool" -> "true";
                case "String" -> "'test_response'";
                case "DateTime" -> "'2024-01-01T00:00:00.000Z'";
                default -> "<String, dynamic>{}";
            };
        }

        // Handle object responses (model types)
        return testDataGenerator.generateTestJsonForModel(modelName);
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
