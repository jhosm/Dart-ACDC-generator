package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Detects circular references in OpenAPI schema dependencies.
 *
 * <p>A circular reference occurs when a schema references itself through a chain
 * of property references. For example:
 * <pre>
 * Person:
 *   properties:
 *     company:
 *       $ref: '#/components/schemas/Company'
 *
 * Company:
 *   properties:
 *     ceo:
 *       $ref: '#/components/schemas/Person'
 * </pre>
 *
 * <p>The detector:
 * <ul>
 *   <li>Traverses the schema property graph depth-first</li>
 *   <li>Tracks visited schemas in the current path to detect cycles</li>
 *   <li>Marks properties involved in circular references as nullable</li>
 *   <li>Records all detected circular reference paths for querying</li>
 * </ul>
 *
 * <p><strong>Why mark as nullable?</strong> Breaking circular references at the property
 * level allows code generators to safely instantiate models without infinite recursion.
 *
 * @see <a href="https://swagger.io/docs/specification/using-ref/">OpenAPI $ref Specification</a>
 */
public class DartCircularReferenceDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartCircularReferenceDetector.class);

    /**
     * Map tracking which schemas have circular references.
     * Key: schema name, Value: set of schema names this schema circularly references
     */
    private final Map<String, Set<String>> circularReferences = new HashMap<>();

    /**
     * Detects all circular references in the schema map.
     * Modifies schemas in place by marking circular properties as nullable.
     *
     * @param schemas all schemas to check for circular references
     */
    @SuppressWarnings("rawtypes")
    public void detectAll(Map<String, Schema> schemas) {
        LOGGER.info("Detecting circular references");
        circularReferences.clear(); // Reset tracking

        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema schema = entry.getValue();
            detectCircularReferences(schemaName, schema, schemas, new HashSet<>());
        }

        // Log summary
        if (!circularReferences.isEmpty()) {
            LOGGER.info("Found {} schemas with circular references", circularReferences.size());
            for (Map.Entry<String, Set<String>> entry : circularReferences.entrySet()) {
                LOGGER.info("  {} -> {}", entry.getKey(), entry.getValue());
            }
        } else {
            LOGGER.info("No circular references found");
        }
    }

    /**
     * Checks if a schema has circular references.
     *
     * @param schemaName the schema name to check
     * @return true if the schema has circular references
     */
    public boolean hasCircularReference(String schemaName) {
        return circularReferences.containsKey(schemaName);
    }

    /**
     * Gets the set of schemas that this schema circularly references.
     *
     * @param schemaName the schema name
     * @return set of circularly referenced schema names, or empty set if none
     */
    public Set<String> getCircularPaths(String schemaName) {
        return circularReferences.getOrDefault(schemaName, Collections.emptySet());
    }

    /**
     * Detects circular references in a schema by traversing the property graph.
     * Marks properties involved in circular references as nullable.
     *
     * <p>This method uses depth-first traversal with path tracking:
     * <ol>
     *   <li>Add current schema to visited path</li>
     *   <li>For each property that references another schema:</li>
     *   <li>  - If referenced schema is already in path → circular reference found</li>
     *   <li>  - Otherwise, recursively check referenced schema</li>
     * </ol>
     *
     * @param schemaName  the current schema name
     * @param schema      the current schema
     * @param allSchemas  all available schemas for reference resolution
     * @param visitedPath set of schema names in the current path (for cycle detection)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void detectCircularReferences(String schemaName, Schema schema, Map<String, Schema> allSchemas,
            Set<String> visitedPath) {
        if (schema == null || schema.getProperties() == null) {
            return;
        }

        // Add current schema to path
        visitedPath.add(schemaName);

        Map<String, Schema> properties = (Map<String, Schema>) schema.getProperties();

        for (Map.Entry<String, Schema> propEntry : properties.entrySet()) {
            String propName = propEntry.getKey();
            Schema propSchema = propEntry.getValue();

            // Check if this property references another schema
            String referencedSchemaName = extractSchemaNameFromPropertySchema(propSchema);

            if (referencedSchemaName != null) {
                // Check if this creates a cycle
                if (visitedPath.contains(referencedSchemaName)) {
                    // Circular reference detected!
                    LOGGER.info("Circular reference detected: {} -> {} (in property '{}')",
                            schemaName, referencedSchemaName, propName);

                    // Track circular reference
                    circularReferences.computeIfAbsent(schemaName, k -> new HashSet<>())
                            .add(referencedSchemaName);

                    // Mark property as nullable to break the cycle
                    propSchema.setNullable(true);
                    LOGGER.info("Marked property '{}' in schema '{}' as nullable", propName, schemaName);
                } else {
                    // Continue traversal
                    Schema referencedSchema = allSchemas.get(referencedSchemaName);
                    if (referencedSchema != null) {
                        // Create a new visited set for this branch (copy current path)
                        Set<String> newPath = new HashSet<>(visitedPath);
                        detectCircularReferences(referencedSchemaName, referencedSchema, allSchemas, newPath);
                    }
                }
            }
        }
    }

    /**
     * Extracts the referenced schema name from a property schema.
     * Handles both direct references ($ref) and array references (items.$ref).
     *
     * @param propSchema the property schema
     * @return the referenced schema name, or null if not a reference
     */
    @SuppressWarnings("rawtypes")
    private String extractSchemaNameFromPropertySchema(Schema propSchema) {
        if (propSchema.get$ref() != null) {
            // Direct reference: { $ref: '#/components/schemas/Person' }
            return extractSchemaNameFromRef(propSchema.get$ref());
        } else if ("array".equals(propSchema.getType()) && propSchema.getItems() != null
                && propSchema.getItems().get$ref() != null) {
            // Array of references: { type: 'array', items: { $ref: '#/components/schemas/Person' } }
            return extractSchemaNameFromRef(propSchema.getItems().get$ref());
        }
        return null;
    }

    /**
     * Extracts the schema name from a $ref string.
     *
     * <p>Example: "#/components/schemas/Person" → "Person"</p>
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
}
