package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Coordinates all schema preprocessing operations for Dart code generation.
 *
 * <p>This class orchestrates the preprocessing pipeline:
 * <ol>
 *   <li><strong>Schema Extraction:</strong> Extracts schemas from OpenAPI spec via DartSchemaRegistry</li>
 *   <li><strong>AllOf Flattening:</strong> Merges allOf compositions via DartAllOfFlattener</li>
 *   <li><strong>Circular Detection:</strong> Detects and marks circular refs via DartCircularReferenceDetector</li>
 *   <li><strong>Schema Registration:</strong> Registers processed schemas back to registry</li>
 * </ol>
 *
 * <p><strong>Why preprocess?</strong> Preprocessing optimizes the schema structure before
 * code generation, making templates simpler and generated code cleaner.
 *
 * <h3>Example Transformations:</h3>
 * <pre>
 * Before:
 *   Dog:
 *     allOf:
 *       - $ref: '#/components/schemas/Animal'
 *       - properties: { breed: string }
 *
 * After:
 *   Dog:
 *     properties:
 *       name: string      # from Animal
 *       breed: string     # from Dog
 * </pre>
 *
 * @see DartSchemaRegistry Schema storage and lookup
 * @see DartAllOfFlattener AllOf composition flattening
 * @see DartCircularReferenceDetector Circular reference detection
 */
public class DartSchemaPreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartSchemaPreprocessor.class);

    private final DartSchemaRegistry schemaRegistry;
    private final DartAllOfFlattener allOfFlattener;
    private final DartCircularReferenceDetector circularReferenceDetector;

    /**
     * Creates a preprocessor with its dependencies.
     *
     * @param schemaRegistry         the schema registry for storage and lookup
     * @param allOfFlattener         the allOf flattener
     * @param circularReferenceDetector the circular reference detector
     */
    public DartSchemaPreprocessor(
            DartSchemaRegistry schemaRegistry,
            DartAllOfFlattener allOfFlattener,
            DartCircularReferenceDetector circularReferenceDetector) {
        this.schemaRegistry = schemaRegistry;
        this.allOfFlattener = allOfFlattener;
        this.circularReferenceDetector = circularReferenceDetector;
    }

    /**
     * Preprocesses the OpenAPI specification by executing the full preprocessing pipeline.
     *
     * <p>Pipeline stages:
     * <ol>
     *   <li>Extract schemas from OpenAPI spec</li>
     *   <li>Flatten allOf compositions</li>
     *   <li>Detect and mark circular references</li>
     *   <li>Register processed schemas for code generation</li>
     * </ol>
     *
     * @param openAPI the OpenAPI specification to preprocess
     */
    @SuppressWarnings("rawtypes")
    public void preprocess(OpenAPI openAPI) {
        LOGGER.info("Preprocessing OpenAPI spec for allOf composition");

        // Stage 1: Extract schemas
        Map<String, Schema> schemas = extractSchemas(openAPI);
        if (schemas == null) {
            LOGGER.info("No schemas to preprocess");
            return;
        }

        LOGGER.info("Found {} schemas to process", schemas.size());

        // Stage 2: Flatten allOf compositions
        LOGGER.info("Stage 1/2: Flattening allOf compositions");
        allOfFlattener.flattenAllOf(schemas);

        // Stage 3: Detect circular references
        LOGGER.info("Stage 2/2: Detecting circular references");
        circularReferenceDetector.detectAll(schemas);

        // Stage 4: Register processed schemas
        LOGGER.info("Registering processed schemas");
        schemaRegistry.updateSchemas(schemas);

        LOGGER.info("Preprocessing complete");
    }

    /**
     * Extracts schemas from the OpenAPI specification.
     * Delegates to the schema registry for extraction logic.
     *
     * @param openAPI the OpenAPI specification
     * @return the schemas map, or null if no schemas are available
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Schema> extractSchemas(OpenAPI openAPI) {
        // Register schemas in the registry and return them
        schemaRegistry.registerSchemas(openAPI);
        Map<String, Schema> schemas = schemaRegistry.getAllSchemas();
        return schemas.isEmpty() ? null : schemas;
    }
}
