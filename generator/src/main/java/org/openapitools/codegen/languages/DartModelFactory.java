package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenModel;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Factory for creating and configuring CodegenModel instances.
 *
 * Responsibilities:
 * - Create CodegenModel from Schema using parent generator
 * - Set up basic model properties
 * - Coordinate composition processing (oneOf/anyOf)
 * - Handle sealed class extension relationships
 *
 * This class acts as a coordinator, delegating specialized processing to
 * composition processors while maintaining the core model creation flow.
 */
public class DartModelFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartModelFactory.class);

    /**
     * Reference to the main generator for delegating to parent methods.
     */
    private final DartAcdcGenerator generator;

    /**
     * Processor for discriminator and sealed class handling.
     */
    private final DartDiscriminatorProcessor discriminatorProcessor;

    /**
     * Creates a new DartModelFactory.
     *
     * @param generator               the main generator instance for delegation
     * @param discriminatorProcessor  processor for discriminator and sealed class handling
     */
    public DartModelFactory(DartAcdcGenerator generator, DartDiscriminatorProcessor discriminatorProcessor) {
        this.generator = generator;
        this.discriminatorProcessor = discriminatorProcessor;
    }

    /**
     * Creates a CodegenModel from a schema with full processing.
     *
     * Processing steps:
     * 1. Create base model using parent generator
     * 2. Detect and mark standalone enum schemas
     * 3. Process oneOf/anyOf composition (delegates to generator)
     * 4. Apply sealed class extension relationships
     *
     * @param name   the model name
     * @param schema the schema definition
     * @return fully processed CodegenModel
     */
    @SuppressWarnings("rawtypes")
    public CodegenModel createModel(String name, Schema schema) {
        // Step 1: Create base model using parent generator's fromModel
        CodegenModel model = generator.createBaseModel(name, schema);

        // Step 2: Check if this schema has enum values and no properties (standalone enum)
        // This step overrides parent's enum detection to ensure models with properties
        // are not marked as enums even if they have enum-valued fields
        if (schema != null && schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            if (schema.getProperties() == null || schema.getProperties().isEmpty()) {
                model.isEnum = true;
                LOGGER.debug("Marked model '{}' as standalone enum", name);
                // Note: allowableValues and enumVars are processed in postProcessModels
            } else {
                // Model has properties - ensure it's not marked as enum
                model.isEnum = false;
                LOGGER.debug("Model '{}' has enum values but also has properties - not marking as standalone enum", name);
            }
        }

        // Step 3: Process composition schemas
        // Delegate to generator for now (will be moved to dedicated processors in future iterations)
        if (schema != null && schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            generator.processOneOfCompositionForFactory(name, schema, model);
        }

        if (schema != null && schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            generator.processAnyOfCompositionForFactory(name, schema, model);
        }

        // Step 4: Check if this model should extend a sealed class
        String parentSealedClass = discriminatorProcessor.getSealedClassExtensions().get(model.classname);
        if (parentSealedClass != null) {
            model.parent = parentSealedClass;
            model.vendorExtensions.put("x-extends-sealed-class", true);
            model.vendorExtensions.put("x-sealed-parent", parentSealedClass);
            model.vendorExtensions.put("x-sealed-parent-filename", generator.toModelFilename(parentSealedClass));
            LOGGER.info("Model {} will extend sealed class {}", model.classname, parentSealedClass);
        }

        return model;
    }
}
