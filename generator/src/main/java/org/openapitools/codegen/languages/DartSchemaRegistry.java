package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for OpenAPI schemas.
 * Provides storage, lookup, and extraction of schemas from OpenAPI specifications.
 */
public class DartSchemaRegistry {

    /**
     * Central schema storage.
     * Key: schema name (e.g., "Pet", "NewPet"), Value: the schema definition
     */
    private final Map<String, Schema> schemas = new HashMap<>();

    /**
     * Extracts and registers all schemas from an OpenAPI specification.
     * Clears any previously registered schemas.
     *
     * @param openAPI the OpenAPI specification
     * @return the number of schemas registered, or 0 if none found
     */
    @SuppressWarnings("rawtypes")
    public int registerSchemas(OpenAPI openAPI) {
        schemas.clear();

        Map<String, Schema> extractedSchemas = extractSchemas(openAPI);
        if (extractedSchemas != null && !extractedSchemas.isEmpty()) {
            schemas.putAll(extractedSchemas);
        }

        return schemas.size();
    }

    /**
     * Registers a map of schemas.
     * Clears any previously registered schemas.
     *
     * @param schemasMap the schemas to register
     */
    @SuppressWarnings("rawtypes")
    public void registerSchemas(Map<String, Schema> schemasMap) {
        schemas.clear();
        if (schemasMap != null) {
            schemas.putAll(schemasMap);
        }
    }

    /**
     * Updates the schema registry with processed schemas.
     * Does NOT clear existing schemas, allowing incremental updates.
     *
     * @param schemasMap the schemas to update
     */
    @SuppressWarnings("rawtypes")
    public void updateSchemas(Map<String, Schema> schemasMap) {
        if (schemasMap != null) {
            schemas.putAll(schemasMap);
        }
    }

    /**
     * Safely extracts schemas from the OpenAPI specification.
     *
     * @param openAPI the OpenAPI specification
     * @return the schemas map, or null if not available
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Schema> extractSchemas(OpenAPI openAPI) {
        if (openAPI == null || openAPI.getComponents() == null) {
            return null;
        }
        return openAPI.getComponents().getSchemas();
    }

    /**
     * Gets a schema by name.
     *
     * @param name the schema name
     * @return the schema, or null if not found
     */
    @SuppressWarnings("rawtypes")
    public Schema getSchema(String name) {
        return schemas.get(name);
    }

    /**
     * Checks if a schema exists in the registry.
     *
     * @param name the schema name
     * @return true if the schema exists, false otherwise
     */
    public boolean hasSchema(String name) {
        return schemas.containsKey(name);
    }

    /**
     * Gets all registered schemas.
     * Returns a copy to prevent external modification.
     *
     * @return a map of all schemas
     */
    @SuppressWarnings("rawtypes")
    public Map<String, Schema> getAllSchemas() {
        return new HashMap<>(schemas);
    }

    /**
     * Gets the number of registered schemas.
     *
     * @return the schema count
     */
    public int getSchemaCount() {
        return schemas.size();
    }

    /**
     * Clears all registered schemas.
     */
    public void clear() {
        schemas.clear();
    }
}
