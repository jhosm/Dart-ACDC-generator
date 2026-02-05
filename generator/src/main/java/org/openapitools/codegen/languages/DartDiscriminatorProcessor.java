package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Processor for discriminator and sealed class handling in Dart-ACDC generator.
 *
 * Responsibilities:
 * - Process discriminator metadata for oneOf/anyOf schemas
 * - Track sealed class extension relationships (parent-child mapping)
 * - Register which models should extend sealed classes
 * - Handle discriminator property names and mappings
 *
 * Discriminators are used in OpenAPI to enable polymorphic type resolution.
 * In Dart, these are generated as sealed classes with the discriminator property
 * used for runtime type discrimination.
 */
public class DartDiscriminatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartDiscriminatorProcessor.class);

    /**
     * OpenAPI primitive types that can appear in composition alternatives.
     */
    private static final Set<String> OPENAPI_PRIMITIVE_TYPES = Set.of(
            "string", "integer", "number", "boolean");

    /**
     * Map tracking which schemas should extend sealed classes.
     * Key: child schema name (e.g., "Dog"), Value: parent sealed class name (e.g., "Animal")
     */
    private final Map<String, String> sealedClassExtensions = new HashMap<>();

    private final DartAcdcGenerator generator;
    private final DartTestDataGenerator testDataGenerator;

    /**
     * Constructs a DartDiscriminatorProcessor.
     *
     * @param generator         the parent generator for accessing utility methods
     * @param testDataGenerator helper for generating test data
     */
    public DartDiscriminatorProcessor(DartAcdcGenerator generator, DartTestDataGenerator testDataGenerator) {
        this.generator = generator;
        this.testDataGenerator = testDataGenerator;
    }

    /**
     * Processes discriminator information for oneOf/anyOf schemas.
     * Extracts discriminator property name and mapping metadata.
     *
     * @param name   the schema name
     * @param schema the schema (may have discriminator)
     * @param model  the codegen model to update
     */
    @SuppressWarnings("rawtypes")
    public void processDiscriminator(String name, Schema schema, CodegenModel model) {
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
     * Registers schemas referenced in oneOf/anyOf to extend the parent sealed class.
     *
     * @param parentName the parent sealed class name
     * @param schemas    the list of referenced schemas
     */
    @SuppressWarnings("rawtypes")
    public void registerSealedClassExtensions(String parentName, List<Schema> schemas) {
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
     * Gets the map of sealed class extensions.
     *
     * @return map where keys are child model names and values are parent sealed class names
     */
    public Map<String, String> getSealedClassExtensions() {
        return Collections.unmodifiableMap(sealedClassExtensions);
    }

    /**
     * Checks if an OpenAPI type is a primitive type (string, integer, number, boolean).
     * Arrays and objects are not considered primitive.
     *
     * @param type the OpenAPI type to check
     * @return true if the type is primitive (string/integer/number/boolean), false otherwise
     */
    public boolean isPrimitiveType(String type) {
        return type != null && OPENAPI_PRIMITIVE_TYPES.contains(type);
    }

    /**
     * Safely extracts the schema name from a $ref string.
     * Handles refs without '/' gracefully and validates input.
     *
     * @param ref the $ref string (e.g., "#/components/schemas/Pet")
     * @return the schema name (e.g., "Pet"), or "UnknownSchema" if extraction fails
     */
    public String extractSchemaNameFromRef(String ref) {
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
}
