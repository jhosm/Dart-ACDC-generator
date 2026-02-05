package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Processor for anyOf composition in Dart-ACDC generator.
 *
 * Handles anyOf schema composition by:
 * - Delegating alternative enrichment to DartModelEnricher
 * - Delegating sealed class registration to DartDiscriminatorProcessor
 * - Marking models with anyOf metadata
 *
 * Note: anyOf never has discriminators (unlike oneOf). The deserialization
 * uses a try-each approach instead of discriminator-based routing.
 *
 * AnyOf schemas are generated as sealed classes in Dart, with each alternative
 * being either a subclass or a wrapper class (for primitives).
 */
public class DartAnyOfProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DartAnyOfProcessor.class);

    private final DartDiscriminatorProcessor discriminatorProcessor;
    private final DartModelEnricher modelEnricher;

    /**
     * Constructs a DartAnyOfProcessor.
     *
     * @param discriminatorProcessor  processor for discriminator and sealed class handling
     * @param modelEnricher           enricher for composition alternative metadata
     */
    public DartAnyOfProcessor(DartDiscriminatorProcessor discriminatorProcessor,
                              DartModelEnricher modelEnricher) {
        this.discriminatorProcessor = discriminatorProcessor;
        this.modelEnricher = modelEnricher;
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

        // Delegate alternative enrichment to DartModelEnricher
        List<Map<String, Object>> alternatives = modelEnricher.enrichWithCompositionAlternatives(
                name, anyOfSchemas, "anyOf");

        // Delegate sealed class registration to DartDiscriminatorProcessor
        discriminatorProcessor.registerSealedClassExtensions(name, anyOfSchemas);

        // Store alternatives
        model.vendorExtensions.put("x-any-of-alternatives", alternatives);

        LOGGER.info("Processed anyOf for '{}': {} alternatives", name, alternatives.size());
    }
}
