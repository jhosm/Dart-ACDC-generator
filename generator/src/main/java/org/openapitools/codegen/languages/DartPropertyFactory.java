package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenProperty;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Factory for creating CodegenProperty instances from OpenAPI Schema specifications.
 *
 * <p>Handles property creation with special support for:</p>
 * <ul>
 *   <li>Context-aware binary/file type mapping (multipart vs non-multipart)</li>
 *   <li>Composition schema detection (oneOf/anyOf) for custom JSON converters</li>
 *   <li>Vendor extension marking for template rendering</li>
 * </ul>
 *
 * <p><strong>Binary Type Mapping:</strong></p>
 * <ul>
 *   <li>Multipart context: {@code type=string,format=binary} → {@code MultipartFile}</li>
 *   <li>Non-multipart context: {@code type=string,format=binary} → {@code List<int>}</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Layer 4 - Request/Response Processing</p>
 */
public class DartPropertyFactory {

    // Constants for vendor extensions
    private static final String VENDOR_EXTENSION_IS_MULTIPART_FILE = "x-is-multipart-file";
    private static final String VENDOR_EXTENSION_DART_IMPORT = "x-dart-import";
    private static final String DART_IMPORT_DIO = "package:dio/dio.dart";

    /**
     * The parent generator for delegating to base functionality.
     */
    private final DartAcdcGenerator generator;

    /**
     * Type mapper for binary type detection and multipart context.
     */
    private final DartTypeMapper typeMapper;

    /**
     * Discriminator processor for sealed class extension tracking.
     */
    private final DartDiscriminatorProcessor discriminatorProcessor;

    /**
     * Constructs a DartPropertyFactory with required dependencies.
     *
     * @param generator               the parent generator instance for delegation
     * @param typeMapper              the type mapper for binary type detection
     * @param discriminatorProcessor  the discriminator processor for composition detection
     */
    public DartPropertyFactory(
            DartAcdcGenerator generator,
            DartTypeMapper typeMapper,
            DartDiscriminatorProcessor discriminatorProcessor) {
        this.generator = generator;
        this.typeMapper = typeMapper;
        this.discriminatorProcessor = discriminatorProcessor;
    }

    /**
     * Creates a CodegenProperty from an OpenAPI Schema with context-aware type mapping.
     *
     * <p>This method applies special handling for:</p>
     * <ol>
     *   <li><strong>Binary types:</strong> Maps to {@code MultipartFile} in multipart context,
     *       {@code List<int>} otherwise</li>
     *   <li><strong>Composition schemas:</strong> Detects oneOf/anyOf properties and marks them
     *       for custom JSON converter generation</li>
     *   <li><strong>Sealed class references:</strong> Detects properties referencing sealed
     *       classes and marks them appropriately</li>
     * </ol>
     *
     * <p><strong>Vendor Extensions Added:</strong></p>
     * <ul>
     *   <li>{@code x-is-multipart-file} - Marks binary properties in multipart context</li>
     *   <li>{@code x-dart-import} - Adds Dio import for MultipartFile usage</li>
     *   <li>{@code x-is-one-of-property} - Marks properties with oneOf composition</li>
     *   <li>{@code x-is-any-of-property} - Marks properties with anyOf composition</li>
     *   <li>{@code x-is-composition-property} - Marks properties referencing sealed classes</li>
     * </ul>
     *
     * @param name                             the property name
     * @param schema                           the property schema
     * @param required                         whether the property is required
     * @param schemaIsFromAdditionalProperties whether this schema comes from additionalProperties
     * @return the CodegenProperty with context-aware type mapping and composition markers
     */
    public CodegenProperty createFromProperty(
            String name,
            Schema schema,
            boolean required,
            boolean schemaIsFromAdditionalProperties) {

        // Delegate to parent generator's base implementation
        CodegenProperty property = generator.superFromProperty(name, schema, required, schemaIsFromAdditionalProperties);

        if (property == null || schema == null) {
            return property;
        }

        // Check if this is a binary type (type=string, format=binary)
        boolean isBinary = typeMapper.isBinaryType(schema);

        if (isBinary && typeMapper.isInMultipartContext()) {
            // We're in multipart/form-data context - use MultipartFile
            String multipartType = typeMapper.getMultipartFileType();
            property.dataType = multipartType;
            property.datatypeWithEnum = multipartType;
            property.baseType = multipartType;
            property.isBinary = true;

            // Mark for template usage
            property.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
            property.vendorExtensions.put(VENDOR_EXTENSION_DART_IMPORT, DART_IMPORT_DIO);
        }
        // else: non-multipart context - keep the default List<int> from typeMapping

        // Check if this property references a oneOf/anyOf/allOf composition
        // These need custom JSON converters since they're abstract/sealed classes
        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            property.vendorExtensions.put("x-is-one-of-property", true);
        } else if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            property.vendorExtensions.put("x-is-any-of-property", true);
        } else if (schema.get$ref() != null) {
            // Check if this $ref points to a oneOf/anyOf schema by checking the dataType
            // against our tracking maps
            String refName = property.dataType;
            if (refName != null) {
                // Check if this type is a sealed class parent (oneOf/anyOf schema)
                boolean isCompositionType = discriminatorProcessor.getSealedClassExtensions().containsValue(refName);
                if (isCompositionType) {
                    property.vendorExtensions.put("x-is-composition-property", true);
                }
            }
        }

        return property;
    }
}
