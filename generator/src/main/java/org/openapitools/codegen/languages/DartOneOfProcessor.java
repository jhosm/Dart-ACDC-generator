package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Processor for oneOf composition in Dart-ACDC generator.
 *
 * Handles oneOf schema composition by:
 * - Delegating discriminator processing to DartDiscriminatorProcessor
 * - Delegating alternative enrichment to DartModelEnricher
 * - Registering sealed class extensions
 * - Marking models with oneOf metadata
 *
 * OneOf schemas are generated as sealed classes in Dart, with each alternative
 * being either a subclass or a wrapper class (for primitives).
 */
public class DartOneOfProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartOneOfProcessor.class);

    private final DartDiscriminatorProcessor discriminatorProcessor;
    private final DartModelEnricher modelEnricher;

    /**
     * Constructs a DartOneOfProcessor.
     *
     * @param discriminatorProcessor  processor for discriminator and sealed class handling
     * @param modelEnricher           enricher for composition alternative metadata
     */
    public DartOneOfProcessor(DartDiscriminatorProcessor discriminatorProcessor,
                              DartModelEnricher modelEnricher) {
        this.discriminatorProcessor = discriminatorProcessor;
        this.modelEnricher = modelEnricher;
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

        // Delegate discriminator processing to DartDiscriminatorProcessor
        discriminatorProcessor.processDiscriminator(name, schema, model);

        // Get oneOf alternatives
        List<Schema> oneOfSchemas = (List<Schema>) schema.getOneOf();

        // Delegate alternative enrichment to DartModelEnricher
        List<Map<String, Object>> alternatives = modelEnricher.enrichWithCompositionAlternatives(
                name, oneOfSchemas, "oneOf");

        // Delegate sealed class registration to DartDiscriminatorProcessor
        discriminatorProcessor.registerSealedClassExtensions(name, oneOfSchemas);

        // Store alternatives
        model.vendorExtensions.put("x-one-of-alternatives", alternatives);

        LOGGER.info("Processed oneOf for '{}': {} alternatives", name, alternatives.size());
    }
}
