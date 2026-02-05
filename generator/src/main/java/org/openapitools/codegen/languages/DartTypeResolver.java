package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;

/**
 * Resolves Dart type declarations with context awareness.
 *
 * <p>This class handles type declaration resolution for Dart code generation,
 * particularly for context-dependent types like binary/file types that map
 * differently depending on usage (multipart vs non-multipart contexts).</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Detect and resolve binary/file types to List&lt;int&gt;</li>
 *   <li>Delegate to DartTypeMapper for binary type detection</li>
 * </ul>
 *
 * <p><strong>Layer:</strong> Request/Response Processing (Layer 4)</p>
 *
 * @see DartTypeMapper
 */
public class DartTypeResolver {
    private static final String DART_TYPE_LIST_INT = "List<int>";

    private final DartTypeMapper typeMapper;

    /**
     * Creates a new DartTypeResolver.
     *
     * @param typeMapper the type mapper for binary type detection
     */
    public DartTypeResolver(DartTypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    /**
     * Resolves binary/file types to their Dart type declaration.
     *
     * <p>For binary/file types, returns {@code List<int>}. For non-binary types,
     * returns {@code null} to indicate that the caller should use default type mapping.</p>
     *
     * <p>Multipart-specific mapping ({@code MultipartFile}) is handled separately
     * in property creation, not here.</p>
     *
     * @param schema the OpenAPI schema to resolve
     * @return "List&lt;int&gt;" for binary types, null for non-binary types
     */
    public String resolveBinaryType(Schema schema) {
        if (schema == null) {
            return null;
        }

        // Check if this is a binary/file type
        if (typeMapper.isBinaryType(schema)) {
            // For binary types, use default mapping (List<int>)
            // Multipart-specific mapping (MultipartFile) is handled in fromProperty
            return DART_TYPE_LIST_INT;
        }

        return null;
    }
}
