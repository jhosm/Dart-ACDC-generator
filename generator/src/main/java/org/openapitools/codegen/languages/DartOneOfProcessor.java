package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Processor for oneOf composition in Dart-ACDC generator.
 *
 * Handles oneOf schema composition by:
 * - Processing oneOf alternatives (references, primitives, inline schemas)
 * - Processing discriminator information for type resolution
 * - Generating metadata for sealed class patterns
 * - Registering sealed class extensions
 *
 * OneOf schemas are generated as sealed classes in Dart, with each alternative
 * being either a subclass or a wrapper class (for primitives).
 */
public class DartOneOfProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartOneOfProcessor.class);

    /**
     * OpenAPI primitive types that can appear in oneOf alternatives.
     */
    private static final Set<String> OPENAPI_PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean");

    private final DartAcdcGenerator generator;
    private final DartTestDataGenerator testDataGenerator;
    private final Map<String, String> sealedClassExtensions;

    /**
     * Constructs a DartOneOfProcessor.
     *
     * @param generator             the parent generator for accessing utility methods
     * @param testDataGenerator     helper for generating test data
     * @param sealedClassExtensions map tracking which models extend sealed classes
     */
    public DartOneOfProcessor(DartAcdcGenerator generator,
                              DartTestDataGenerator testDataGenerator,
                              Map<String, String> sealedClassExtensions) {
        this.generator = generator;
        this.testDataGenerator = testDataGenerator;
        this.sealedClassExtensions = sealedClassExtensions;
    }

    /**
     * Processes a oneOf composition schema and adds metadata to the CodegenModel.
     *
     * @param name   the schema name
     * @param schema the schema with oneOf
     * @param model  the codegen model to update
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void processOneOf(String name, Schema schema, CodegenModel model) {
        LOGGER.info("Processing oneOf composition for schema: {}", name);

        // Mark this model as a oneOf composition
        model.vendorExtensions.put("x-is-one-of", true);

        // Process discriminator if present
        processDiscriminator(name, schema, model);

        // Get oneOf alternatives
        List<Schema> oneOfSchemas = (List<Schema>) schema.getOneOf();

        // Process alternatives
        List<Map<String, Object>> alternatives = processCompositionAlternatives(name, oneOfSchemas, "oneOf");

        // Track which schemas should extend this sealed class
        registerSealedClassExtensions(name, oneOfSchemas);

        // Store alternatives
        model.vendorExtensions.put("x-one-of-alternatives", alternatives);

        LOGGER.info("Processed oneOf for '{}': {} alternatives", name, alternatives.size());
    }

    /**
     * Processes discriminator information for oneOf schemas.
     * Extracts discriminator property name and mapping metadata.
     *
     * @param name   the schema name
     * @param schema the schema (may have discriminator)
     * @param model  the codegen model to update
     */
    @SuppressWarnings("rawtypes")
    private void processDiscriminator(String name, Schema schema, CodegenModel model) {
        if (schema.getDiscriminator() == null) {
            model.vendorExtensions.put("x-has-discriminator", false);
            return;
        }

        String discriminatorPropertyName = schema.getDiscriminator().getPropertyName();

        // Validate discriminator property name
        if (discriminatorPropertyName == null || discriminatorPropertyName.isEmpty()) {
            LOGGER.warn("Discriminator property name is null or empty for schema: {}", name);
            model.vendorExtensions.put("x-has-discriminator", false);
            return;
        }

        model.vendorExtensions.put("x-has-discriminator", true);
        model.vendorExtensions.put("x-discriminator-name", discriminatorPropertyName);

        // Process discriminator mapping
        if (schema.getDiscriminator().getMapping() != null) {
            List<Map<String, Object>> discriminatorMapping = new ArrayList<>();
            Map<String, String> mapping = schema.getDiscriminator().getMapping();

            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String mappingKey = entry.getKey();
                String schemaRef = entry.getValue();

                // Extract schema name from $ref (e.g., "#/components/schemas/Dog" -> "Dog")
                String schemaName = extractSchemaNameFromRef(schemaRef);
                // Use the actual schema name as the subclass name (e.g., "Dog", not "AnimalDog")
                String subclassName = generator.toModelName(schemaName);

                // Generate test JSON for this discriminator alternative
                String testJson = testDataGenerator.generateTestJsonForModel(schemaName);

                Map<String, Object> mappingEntry = Map.of(
                        "mappingKey", mappingKey,
                        "schemaName", schemaName,
                        "subclassName", subclassName,
                        "testJson", testJson);
                discriminatorMapping.add(mappingEntry);
            }

            model.vendorExtensions.put("x-discriminator-mapping", discriminatorMapping);
        }
    }

    /**
     * Processes composition alternatives (oneOf) into a list of metadata maps.
     * Handles references, primitive types, and inline schemas.
     *
     * @param parentName      the parent schema name
     * @param schemas         the list of alternative schemas
     * @param compositionType the composition type ("oneOf") for logging
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
     * @param compositionType the composition type ("oneOf") for logging
     * @return metadata map, or null if the schema is invalid
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Object> createAlternativeMetadata(String parentName, Schema schema, int index, boolean hasNext,
                                                           String compositionType) {
        if (schema.get$ref() != null) {
            // Reference to another schema
            String ref = schema.get$ref();
            String schemaName = extractSchemaNameFromRef(ref);
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
            boolean isPrimitive = isPrimitiveType(type);

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
     * Registers schemas referenced in oneOf to extend the parent sealed class.
     *
     * @param parentName the parent sealed class name
     * @param schemas    the list of referenced schemas
     */
    @SuppressWarnings("rawtypes")
    private void registerSealedClassExtensions(String parentName, List<Schema> schemas) {
        for (Schema schema : schemas) {
            if (schema.get$ref() != null) {
                // Only register for references (not inline primitives or objects)
                String ref = schema.get$ref();
                String childSchemaName = extractSchemaNameFromRef(ref);
                String childModelName = generator.toModelName(childSchemaName);
                sealedClassExtensions.put(childModelName, parentName);
                LOGGER.info("Registered {} to extend sealed class {}", childModelName, parentName);
            }
        }
    }

    /**
     * Checks if an OpenAPI type is a primitive type (string, integer, number, boolean).
     * Arrays and objects are not considered primitive.
     *
     * @param type the OpenAPI type to check
     * @return true if the type is primitive (string/integer/number/boolean), false otherwise
     */
    private boolean isPrimitiveType(String type) {
        return type != null && OPENAPI_PRIMITIVE_TYPES.contains(type);
    }

    /**
     * Safely extracts the schema name from a $ref string.
     * Handles refs without '/' gracefully and validates input.
     *
     * @param ref the $ref string (e.g., "#/components/schemas/Pet")
     * @return the schema name (e.g., "Pet"), or "UnknownSchema" if extraction fails
     */
    private String extractSchemaNameFromRef(String ref) {
        if (ref == null || ref.isEmpty()) {
            LOGGER.warn("Received null or empty $ref");
            return "UnknownSchema";
        }

        int lastSlashIndex = ref.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            LOGGER.warn("Malformed $ref without '/': {}", ref);
            return ref; // Return the whole string as fallback
        }

        return ref.substring(lastSlashIndex + 1);
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
