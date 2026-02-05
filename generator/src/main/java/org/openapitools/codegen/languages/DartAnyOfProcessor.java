package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Processor for anyOf composition in Dart-ACDC generator.
 *
 * Handles anyOf schema composition by:
 * - Processing anyOf alternatives (references, primitives, inline schemas)
 * - Generating metadata for sealed class patterns
 * - Delegating sealed class registration to DartDiscriminatorProcessor
 *
 * Note: anyOf never has discriminators (unlike oneOf). The deserialization
 * uses a try-each approach instead of discriminator-based routing.
 *
 * AnyOf schemas are generated as sealed classes in Dart, with each alternative
 * being either a subclass or a wrapper class (for primitives).
 */
public class DartAnyOfProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartAnyOfProcessor.class);

    private final DartAcdcGenerator generator;
    private final DartTestDataGenerator testDataGenerator;
    private final DartDiscriminatorProcessor discriminatorProcessor;

    /**
     * Constructs a DartAnyOfProcessor.
     *
     * @param generator               the parent generator for accessing utility methods
     * @param testDataGenerator       helper for generating test data
     * @param discriminatorProcessor  processor for discriminator and sealed class handling
     */
    public DartAnyOfProcessor(DartAcdcGenerator generator,
                              DartTestDataGenerator testDataGenerator,
                              DartDiscriminatorProcessor discriminatorProcessor) {
        this.generator = generator;
        this.testDataGenerator = testDataGenerator;
        this.discriminatorProcessor = discriminatorProcessor;
    }

    /**
     * Processes an anyOf composition schema and adds metadata to the CodegenModel.
     * anyOf is treated identically to oneOf but without discriminator support.
     *
     * @param name   the schema name
     * @param schema the schema with anyOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void processAnyOf(String name, Schema schema, CodegenModel model) {
        LOGGER.info("Processing anyOf composition for schema: {}", name);

        // Mark this model as an anyOf composition
        model.vendorExtensions.put("x-is-any-of", true);

        // anyOf never has discriminator (treat as try-each)
        model.vendorExtensions.put("x-has-discriminator", false);

        // Get anyOf alternatives
        List<Schema> anyOfSchemas = (List<Schema>) schema.getAnyOf();

        // Process alternatives
        List<Map<String, Object>> alternatives = processCompositionAlternatives(name, anyOfSchemas, "anyOf");

        // Delegate sealed class registration to DartDiscriminatorProcessor
        discriminatorProcessor.registerSealedClassExtensions(name, anyOfSchemas);

        // Store alternatives
        model.vendorExtensions.put("x-any-of-alternatives", alternatives);

        LOGGER.info("Processed anyOf for '{}': {} alternatives", name, alternatives.size());
    }

    /**
     * Processes composition alternatives (anyOf) into a list of metadata maps.
     * Handles references, primitive types, and inline schemas.
     *
     * @param parentName      the parent schema name
     * @param schemas         the list of alternative schemas
     * @param compositionType the composition type ("anyOf") for logging
     * @return list of alternative metadata maps
     */
    @SuppressWarnings("rawtypes")
    private List<Map<String, Object>> processCompositionAlternatives(String parentName, List<Schema> schemas,
                                                                      String compositionType) {
        List<Map<String, Object>> alternatives = new ArrayList<>();

        for (int i = 0; i < schemas.size(); i++) {
            Schema alternativeSchema = schemas.get(i);
            boolean hasNext = i < schemas.size() - 1;
            Map<String, Object> alternative = createAlternativeMetadata(parentName, alternativeSchema, i, hasNext,
                    compositionType);

            if (alternative != null) {
                alternatives.add(alternative);
            }
        }

        return alternatives;
    }

    /**
     * Creates metadata for a single composition alternative.
     *
     * @param parentName      the parent schema name
     * @param schema          the alternative schema
     * @param index           the index of this alternative
     * @param hasNext         whether there are more alternatives after this one
     * @param compositionType the composition type ("anyOf") for logging
     * @return metadata map, or null if the schema is invalid
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Object> createAlternativeMetadata(String parentName, Schema schema, int index, boolean hasNext,
                                                           String compositionType) {
        if (schema.get$ref() != null) {
            // Reference to another schema
            String ref = schema.get$ref();
            String schemaName = discriminatorProcessor.extractSchemaNameFromRef(ref);
            // Use the actual schema name as the subclass name (e.g., "Dog", not "AnimalDog")
            String subclassName = generator.toModelName(schemaName);

            // Generate test JSON for this alternative schema
            String testJson = testDataGenerator.generateTestJsonForModel(schemaName);

            return Map.of(
                    "parentClassName", parentName,
                    "isRef", true,
                    "schemaName", schemaName,
                    "subclassName", subclassName,
                    "importPath", generator.toModelImport(schemaName),
                    "testJson", testJson,
                    "hasNext", hasNext);
        } else if (schema.getType() != null) {
            // Inline schema (primitive or object)
            String type = schema.getType();
            boolean isPrimitive = discriminatorProcessor.isPrimitiveType(type);

            if (isPrimitive) {
                // Wrapper class for primitive
                String dartType = generator.getTypeDeclaration(schema);
                String wrapperName = generator.toModelName(parentName + capitalize(dartType));

                // Generate test value for primitive
                String testValue = testDataGenerator.getTestValueForType(dartType);

                return Map.of(
                        "parentClassName", parentName,
                        "isPrimitive", true,
                        "dartType", dartType,
                        "subclassName", wrapperName,
                        "testValue", testValue,
                        "hasNext", hasNext);
            } else {
                // Inline complex type - use Option naming
                String subclassName = generator.toModelName(parentName + "Option" + (index + 1));
                return Map.of(
                        "parentClassName", parentName,
                        "isInline", true,
                        "subclassName", subclassName,
                        "index", index + 1,
                        "testJson", "<String, dynamic>{}",
                        "hasNext", hasNext);
            }
        } else {
            // Schema has neither $ref nor type - log warning and skip
            LOGGER.warn("{} schema at index {} has neither $ref nor type for schema '{}'. Skipping.",
                    compositionType, index, parentName);
            return null;
        }
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
