package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handles flattening of allOf compositions in OpenAPI schemas.
 *
 * <p>AllOf compositions merge multiple schemas into one. This class:
 * <ul>
 *   <li>Merges properties from all schemas in an allOf array</li>
 *   <li>Handles nested compositions (allOf containing oneOf/anyOf)</li>
 *   <li>Resolves property conflicts (last definition wins)</li>
 *   <li>Preserves required fields from all schemas</li>
 * </ul>
 *
 * <p><strong>Example:</strong>
 * <pre>
 * schemas:
 *   Animal:
 *     type: object
 *     properties:
 *       name: string
 *   Dog:
 *     allOf:
 *       - $ref: '#/components/schemas/Animal'
 *       - type: object
 *         properties:
 *           breed: string
 * </pre>
 * Result: Dog gets both 'name' and 'breed' properties.
 *
 * @see <a href="https://swagger.io/specification/#composition-and-inheritance-polymorphism">OpenAPI allOf Specification</a>
 */
public class DartAllOfFlattener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartAllOfFlattener.class);

    /**
     * Flattens all allOf compositions in the schema map.
     * Modifies the schemas in place, replacing allOf schemas with flattened versions.
     *
     * @param schemas the schemas to process (modified in place)
     */
    @SuppressWarnings("rawtypes")
    public void flattenAllOf(Map<String, Schema> schemas) {
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema schema = entry.getValue();

            if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
                LOGGER.info("Processing allOf for schema: {}", schemaName);
                Schema flattenedSchema = composeAllOfSchema(schemaName, schema, schemas);
                schemas.put(schemaName, flattenedSchema);
                LOGGER.info("Replaced schema {} with flattened version", schemaName);
            }
        }
    }

    /**
     * Composes an allOf schema by merging all properties from referenced schemas.
     * Handles nested composition (allOf containing oneOf/anyOf references).
     *
     * @param name       the schema name
     * @param schema     the schema with allOf
     * @param allSchemas all available schemas for $ref resolution
     * @return a new flattened schema with merged properties
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Schema composeAllOfSchema(String name, Schema schema, Map<String, Schema> allSchemas) {
        if (schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
            return schema;
        }

        LOGGER.info("Composing allOf schema for: {}", name);

        Map<String, Schema> mergedProperties = new LinkedHashMap<>();
        Set<String> mergedRequired = new LinkedHashSet<>();

        // Process each schema in the allOf array
        List<Schema> allOfSchemas = (List<Schema>) schema.getAllOf();
        LOGGER.info("allOf has {} schemas to merge", allOfSchemas.size());

        for (Schema allOfSchema : allOfSchemas) {
            processAllOfElement(name, allOfSchema, allSchemas, mergedProperties, mergedRequired);
        }

        // Create and configure the composed schema
        Schema composedSchema = createComposedSchema(schema, mergedProperties, mergedRequired);

        LOGGER.info("Composed allOf schema for '{}': {} properties, {} required",
                name, mergedProperties.size(), mergedRequired.size());

        return composedSchema;
    }

    /**
     * Processes a single element from an allOf array, merging properties or
     * handling nested composition.
     *
     * @param parentName       the parent schema name
     * @param allOfSchema      the allOf element schema
     * @param allSchemas       all available schemas for $ref resolution
     * @param mergedProperties accumulator for merged properties
     * @param mergedRequired   accumulator for merged required properties
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processAllOfElement(String parentName, Schema allOfSchema, Map<String, Schema> allSchemas,
            Map<String, Schema> mergedProperties, Set<String> mergedRequired) {
        Schema resolvedSchema = allOfSchema;
        String referencedSchemaName = null;

        // Resolve $ref if present
        if (allOfSchema.get$ref() != null) {
            String ref = allOfSchema.get$ref();
            referencedSchemaName = extractSchemaNameFromRef(ref);
            resolvedSchema = allSchemas.get(referencedSchemaName);

            if (resolvedSchema == null) {
                LOGGER.warn("Unable to resolve $ref: {} in allOf for schema: {}", ref, parentName);
                return;
            }
        }

        // Handle nested composition (allOf containing oneOf/anyOf)
        if (isCompositionSchema(resolvedSchema) && referencedSchemaName != null) {
            handleNestedComposition(referencedSchemaName, mergedProperties, mergedRequired);
            return;
        }

        // Regular object schema: Merge properties
        mergeSchemaProperties(parentName, resolvedSchema, mergedProperties);

        // Merge required arrays
        if (resolvedSchema.getRequired() != null) {
            mergedRequired.addAll(resolvedSchema.getRequired());
        }
    }

    /**
     * Checks if a schema is a composition schema (oneOf or anyOf).
     *
     * @param schema the schema to check
     * @return true if the schema has oneOf or anyOf
     */
    @SuppressWarnings("rawtypes")
    private boolean isCompositionSchema(Schema schema) {
        return (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) ||
                (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty());
    }

    /**
     * Handles nested composition by creating a property typed as the composition
     * schema.
     *
     * <p>When allOf references a oneOf/anyOf schema, we create a property
     * with the composition type rather than trying to merge it.</p>
     *
     * @param referencedSchemaName the name of the composition schema
     * @param mergedProperties     accumulator for merged properties
     * @param mergedRequired       accumulator for merged required properties
     */
    @SuppressWarnings("rawtypes")
    private void handleNestedComposition(String referencedSchemaName, Map<String, Schema> mergedProperties,
            Set<String> mergedRequired) {
        LOGGER.info("Detected nested composition: allOf contains oneOf/anyOf reference to '{}'", referencedSchemaName);

        // Create a property with type = the referenced schema name
        Schema propertySchema = new Schema();
        propertySchema.set$ref("#/components/schemas/" + referencedSchemaName);

        // Use camelCase schema name as property name
        String propertyName = toCamelCase(referencedSchemaName);
        mergedProperties.put(propertyName, propertySchema);
        mergedRequired.add(propertyName);

        LOGGER.info("Created property '{}' typed as '{}'", propertyName, referencedSchemaName);
    }

    /**
     * Merges properties from a resolved schema into the merged properties map.
     * Detects and logs property conflicts.
     *
     * @param parentName       the parent schema name (for logging)
     * @param resolvedSchema   the schema to merge from
     * @param mergedProperties accumulator for merged properties
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void mergeSchemaProperties(String parentName, Schema resolvedSchema, Map<String, Schema> mergedProperties) {
        if (resolvedSchema.getProperties() == null) {
            return;
        }

        Map<String, Schema> properties = (Map<String, Schema>) resolvedSchema.getProperties();
        for (Map.Entry<String, Schema> propEntry : properties.entrySet()) {
            String propName = propEntry.getKey();
            Schema propSchema = propEntry.getValue();

            // Check for property conflict
            if (mergedProperties.containsKey(propName)) {
                logPropertyConflict(parentName, propName, mergedProperties.get(propName), propSchema);
            }

            // Last definition wins
            mergedProperties.put(propName, propSchema);
        }
    }

    /**
     * Logs a warning when a property conflict is detected during allOf merging.
     * A conflict occurs when multiple schemas in allOf define the same property name.
     *
     * @param schemaName     the schema name
     * @param propertyName   the conflicting property name
     * @param existingSchema the existing property schema
     * @param newSchema      the new property schema
     */
    @SuppressWarnings("rawtypes")
    private void logPropertyConflict(String schemaName, String propertyName, Schema existingSchema, Schema newSchema) {
        String existingType = existingSchema.getType();
        String newType = newSchema.getType();

        if (!Objects.equals(existingType, newType)) {
            LOGGER.warn("Property conflict in allOf for schema '{}': property '{}' " +
                    "has different types ({} vs {}). Using last definition.",
                    schemaName, propertyName, existingType, newType);
        }
    }

    /**
     * Creates the final composed schema from merged data.
     * Copies relevant attributes from the original schema while replacing
     * the allOf with merged properties.
     *
     * @param originalSchema   the original schema with allOf
     * @param mergedProperties the merged properties
     * @param mergedRequired   the merged required properties
     * @return the composed schema
     */
    @SuppressWarnings("rawtypes")
    private Schema createComposedSchema(Schema originalSchema, Map<String, Schema> mergedProperties,
            Set<String> mergedRequired) {
        Schema composedSchema = new Schema();

        // Apply merged properties and required
        if (!mergedProperties.isEmpty()) {
            composedSchema.setProperties(mergedProperties);
        }

        if (!mergedRequired.isEmpty()) {
            composedSchema.setRequired(new ArrayList<>(mergedRequired));
        }

        // Copy other relevant attributes from the original schema
        composedSchema.setType(originalSchema.getType() != null ? originalSchema.getType() : "object");

        if (originalSchema.getDescription() != null) {
            composedSchema.setDescription(originalSchema.getDescription());
        }

        if (originalSchema.getTitle() != null) {
            composedSchema.setTitle(originalSchema.getTitle());
        }

        return composedSchema;
    }

    /**
     * Extracts the schema name from a $ref string.
     *
     * <p>Example: "#/components/schemas/Dog" → "Dog"</p>
     *
     * @param ref the $ref string
     * @return the schema name, or "UnknownSchema" if the ref is invalid
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
     * Converts a string to camelCase (first letter lowercase).
     *
     * <p>Example: "Animal" → "animal"</p>
     *
     * @param str the string to convert
     * @return the camelCase string
     */
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
