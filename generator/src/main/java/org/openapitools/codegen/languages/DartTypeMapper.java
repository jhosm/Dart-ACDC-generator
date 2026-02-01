package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Utility class for mapping OpenAPI types to Dart types.
 * 
 * This class handles:
 * - OpenAPI to Dart type conversion
 * - Context-aware type mapping (multipart vs non-multipart)
 * - Binary/file type detection and mapping
 */
public class DartTypeMapper {

    // Constants for content types
    private static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";

    // Constants for Dart types
    private static final String DART_TYPE_MULTIPART_FILE = "MultipartFile";
    private static final String DART_TYPE_LIST_INT = "List<int>";

    /**
     * ThreadLocal to track whether we're currently processing a multipart/form-data
     * request body.
     * This allows context-aware type mapping for file/binary types.
     */
    private static final ThreadLocal<Boolean> IS_MULTIPART_CONTEXT = ThreadLocal.withInitial(() -> false);

    /**
     * Checks if the given schema represents a binary type.
     * 
     * A binary type is defined as type=string with format=binary.
     *
     * @param schema the schema to check
     * @return true if the schema is a binary type, false otherwise
     */
    public boolean isBinaryType(Schema schema) {
        if (schema == null) {
            return false;
        }

        String format = schema.getFormat();
        String type = schema.getType();

        return "string".equals(type) && "binary".equals(format);
    }

    /**
     * Gets the Dart type declaration for a binary schema based on current context.
     * 
     * In multipart/form-data context: returns MultipartFile
     * In non-multipart context: returns List&lt;int&gt;
     *
     * @param schema the binary schema
     * @return the appropriate Dart type (MultipartFile or List&lt;int&gt;)
     */
    public String getBinaryTypeDeclaration(Schema schema) {
        if (!isBinaryType(schema)) {
            return null; // Not a binary type - caller should use default mapping
        }

        // Check context - if in multipart form-data, use MultipartFile
        // Otherwise use List<int> for binary data
        if (IS_MULTIPART_CONTEXT.get()) {
            return DART_TYPE_MULTIPART_FILE;
        } else {
            return DART_TYPE_LIST_INT;
        }
    }

    /**
     * Detects if the given content map contains multipart/form-data media type.
     *
     * @param content the content map from a request body
     * @return true if multipart/form-data is present, false otherwise
     */
    public boolean isMultipartContent(Content content) {
        if (content == null) {
            return false;
        }
        return content.containsKey(CONTENT_TYPE_MULTIPART_FORM_DATA);
    }

    /**
     * Sets the multipart context flag to true.
     * This affects binary type mapping during property processing.
     * 
     * Should be called before processing multipart/form-data request bodies.
     */
    public void enterMultipartContext() {
        IS_MULTIPART_CONTEXT.set(true);
    }

    /**
     * Clears the multipart context flag.
     * 
     * Should always be called after processing request bodies to avoid
     * memory leaks and incorrect context for subsequent operations.
     */
    public void exitMultipartContext() {
        IS_MULTIPART_CONTEXT.remove();
    }

    /**
     * Checks if currently in a multipart/form-data processing context.
     *
     * @return true if in multipart context, false otherwise
     */
    public boolean isInMultipartContext() {
        return IS_MULTIPART_CONTEXT.get();
    }

    /**
     * Gets the Dart type name for MultipartFile.
     *
     * @return "MultipartFile"
     */
    public String getMultipartFileType() {
        return DART_TYPE_MULTIPART_FILE;
    }

    /**
     * Gets the Dart type name for binary data (List&lt;int&gt;).
     *
     * @return "List&lt;int&gt;"
     */
    public String getListIntType() {
        return DART_TYPE_LIST_INT;
    }
}
