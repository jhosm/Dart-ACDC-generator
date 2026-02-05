package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Enriches CodegenModel instances with composition alternative metadata for Dart-ACDC generator.
 *
 * This class is responsible for:
 * - Processing oneOf/anyOf composition alternatives
 * - Creating metadata maps for each alternative (references, primitives, inline schemas)
 * - Generating test data for alternatives
 * - Adding vendor extensions to models
 *
 * This centralized enrichment logic is shared by DartOneOfProcessor and DartAnyOfProcessor
 * to avoid code duplication and maintain consistency.
 */
public class DartModelEnricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartModelEnricher.class);

    private final DartAcdcGenerator generator;
    private final DartTestDataGenerator testDataGenerator;
    private final DartDiscriminatorProcessor discriminatorProcessor;

    /**
     * Constructs a DartModelEnricher.
     *
     * @param generator               the parent generator for accessing utility methods
     * @param testDataGenerator       helper for generating test data
     * @param discriminatorProcessor  processor for discriminator and primitive type checks
     */
    public DartModelEnricher(DartAcdcGenerator generator,
                             DartTestDataGenerator testDataGenerator,
                             DartDiscriminatorProcessor discriminatorProcessor) {
        this.generator = generator;
        this.testDataGenerator = testDataGenerator;
        this.discriminatorProcessor = discriminatorProcessor;
    }

    /**
     * Enriches a model with composition alternatives (oneOf or anyOf).
     * Processes each schema in the list and creates alternative metadata.
     *
     * @param parentName      the parent schema name
     * @param schemas         the list of alternative schemas (from oneOf or anyOf)
     * @param compositionType the composition type ("oneOf" or "anyOf") for logging
     * @return list of alternative metadata maps for template use
     */
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> enrichWithCompositionAlternatives(
            String parentName,
            List<Schema> schemas,
            String compositionType) {

        if (schemas == null || schemas.isEmpty()) {
            LOGGER.warn("No {} schemas found for parent: {}", compositionType, parentName);
            return Collections.emptyList();
        }

        List<Map<String, Object>> alternatives = new ArrayList<>();

        for (int i = 0; i < schemas.size(); i++) {
            Schema alternativeSchema = schemas.get(i);
            boolean hasNext = i < schemas.size() - 1;
            Map<String, Object> alternative = createAlternativeMetadata(
                    parentName, alternativeSchema, i, hasNext, compositionType);

            if (alternative != null) {
                alternatives.add(alternative);
            }
        }

        LOGGER.info("Enriched '{}' with {} {} alternatives", parentName, alternatives.size(), compositionType);
        return alternatives;
    }

    /**
     * Creates metadata for a single composition alternative.
     * Handles three cases:
     * 1. Reference schemas ($ref) - creates reference metadata with import paths
     * 2. Primitive schemas (string, integer, etc.) - creates wrapper class metadata
     * 3. Inline complex schemas - creates inline schema metadata
     *
     * @param parentName      the parent schema name
     * @param schema          the alternative schema
     * @param index           the index of this alternative in the list
     * @param hasNext         whether there are more alternatives after this one
     * @param compositionType the composition type ("oneOf" or "anyOf") for logging
     * @return metadata map for templates, or null if the schema is invalid
     */
    @SuppressWarnings("rawtypes")
    public Map<String, Object> createAlternativeMetadata(
            String parentName,
            Schema schema,
            int index,
            boolean hasNext,
            String compositionType) {

        // Case 1: Reference to another schema
        if (schema.get$ref() != null) {
            return createReferenceAlternativeMetadata(parentName, schema, hasNext, compositionType);
        }

        // Case 2 & 3: Inline schema (primitive or complex)
        if (schema.getType() != null) {
            String type = schema.getType();
            boolean isPrimitive = discriminatorProcessor.isPrimitiveType(type);

            if (isPrimitive) {
                return createPrimitiveAlternativeMetadata(parentName, schema, hasNext);
            } else {
                return createInlineAlternativeMetadata(parentName, index, hasNext);
            }
        }

        // Invalid schema: no $ref and no type
        LOGGER.warn("{} schema at index {} has neither $ref nor type for parent '{}'. Skipping.",
                compositionType, index, parentName);
        return null;
    }

    /**
     * Creates metadata for a reference alternative ($ref).
     * Example: { $ref: "#/components/schemas/Dog" }
     *
     * @param parentName      the parent schema name
     * @param schema          the schema with $ref
     * @param hasNext         whether there are more alternatives
     * @param compositionType the composition type for logging
     * @return metadata map with schema name, import path, and test JSON
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Object> createReferenceAlternativeMetadata(
            String parentName,
            Schema schema,
            boolean hasNext,
            String compositionType) {

        String ref = schema.get$ref();
        String schemaName = discriminatorProcessor.extractSchemaNameFromRef(ref);
        // Use the actual schema name as the subclass name (e.g., "Dog", not "AnimalDog")
        String subclassName = generator.toModelName(schemaName);

        // Generate test JSON for this alternative schema
        String testJson = testDataGenerator.generateTestJsonForModel(schemaName);

        LOGGER.debug("Created reference alternative for {} -> {} ({})", parentName, subclassName, compositionType);

        return Map.of(
                "parentClassName", parentName,
                "isRef", true,
                "schemaName", schemaName,
                "subclassName", subclassName,
                "importPath", generator.toModelImport(schemaName),
                "testJson", testJson,
                "hasNext", hasNext);
    }

    /**
     * Creates metadata for a primitive alternative (string, integer, number, boolean).
     * Primitives require wrapper classes since they can't extend sealed classes directly.
     * Example: For "Animal" with primitive "string", creates "AnimalString" wrapper.
     *
     * @param parentName the parent schema name
     * @param schema     the primitive schema
     * @param hasNext    whether there are more alternatives
     * @return metadata map with dart type, wrapper name, and test value
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Object> createPrimitiveAlternativeMetadata(
            String parentName,
            Schema schema,
            boolean hasNext) {

        String dartType = generator.getTypeDeclaration(schema);
        String wrapperName = generator.toModelName(parentName + capitalize(dartType));

        // Generate test value for primitive
        String testValue = testDataGenerator.getTestValueForType(dartType);

        LOGGER.debug("Created primitive alternative for {} -> {} (type: {})", parentName, wrapperName, dartType);

        return Map.of(
                "parentClassName", parentName,
                "isPrimitive", true,
                "dartType", dartType,
                "subclassName", wrapperName,
                "testValue", testValue,
                "hasNext", hasNext);
    }

    /**
     * Creates metadata for an inline complex alternative (object, array).
     * Inline schemas use "Option" naming: e.g., "AnimalOption1", "AnimalOption2".
     *
     * @param parentName the parent schema name
     * @param index      the index of this alternative
     * @param hasNext    whether there are more alternatives
     * @return metadata map with option naming and empty test JSON
     */
    private Map<String, Object> createInlineAlternativeMetadata(
            String parentName,
            int index,
            boolean hasNext) {

        String subclassName = generator.toModelName(parentName + "Option" + (index + 1));

        LOGGER.debug("Created inline alternative for {} -> {}", parentName, subclassName);

        return Map.of(
                "parentClassName", parentName,
                "isInline", true,
                "subclassName", subclassName,
                "index", index + 1,
                "testJson", "<String, dynamic>{}",
                "hasNext", hasNext);
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string, or empty string if input is null/empty
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
