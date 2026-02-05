package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.media.Content;

import java.util.Set;

/**
 * Factory for creating CodegenParameter instances from RequestBody specifications.
 *
 * <p>Handles request body parameter creation with special support for:</p>
 * <ul>
 *   <li>Multipart/form-data detection and context management</li>
 *   <li>Binary/file type mapping in multipart context</li>
 *   <li>Vendor extension marking for template rendering</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Layer 4 - Request/Response Processing</p>
 */
public class DartRequestBodyFactory {

    // Constants for vendor extensions
    private static final String VENDOR_EXTENSION_IS_MULTIPART_CONTEXT = "x-is-multipart-context";
    private static final String VENDOR_EXTENSION_IS_MULTIPART_FILE = "x-is-multipart-file";

    /**
     * The parent generator for delegating to base functionality.
     */
    private final DartAcdcGenerator generator;

    /**
     * Type mapper for multipart content detection and context management.
     */
    private final DartTypeMapper typeMapper;

    /**
     * Constructs a DartRequestBodyFactory with required dependencies.
     *
     * @param generator  the parent generator instance for delegation
     * @param typeMapper the type mapper for multipart detection
     */
    public DartRequestBodyFactory(DartAcdcGenerator generator, DartTypeMapper typeMapper) {
        this.generator = generator;
        this.typeMapper = typeMapper;
    }

    /**
     * Creates a CodegenParameter from a RequestBody specification.
     *
     * <p>This method handles multipart/form-data detection and sets the appropriate
     * context for downstream type mapping. When multipart content is detected,
     * binary/file types will be mapped to {@code MultipartFile} instead of
     * {@code List<int>}.</p>
     *
     * <p><strong>Vendor Extensions Added:</strong></p>
     * <ul>
     *   <li>{@code x-is-multipart-context} - Marks parameters in multipart requests</li>
     *   <li>{@code x-is-multipart-file} - Marks binary/file parameters in multipart context</li>
     * </ul>
     *
     * @param requestBody       the request body specification from OpenAPI
     * @param imports           the imports set to populate with required imports
     * @param bodyParameterName the name for the body parameter
     * @return the CodegenParameter with multipart context information, or null if requestBody processing fails
     */
    public CodegenParameter createFromRequestBody(
            RequestBody requestBody,
            Set<String> imports,
            String bodyParameterName) {

        try {
            // Detect if this is a multipart/form-data request BEFORE calling generator
            if (requestBody != null) {
                Content content = requestBody.getContent();
                boolean isMultipart = typeMapper.isMultipartContent(content);

                if (isMultipart) {
                    // Set multipart context for property processing
                    typeMapper.enterMultipartContext();
                }
            }

            // Delegate to parent generator's base implementation
            CodegenParameter parameter = generator.superFromRequestBody(requestBody, imports, bodyParameterName);

            if (parameter == null) {
                return parameter;
            }

            // Mark the parameter with multipart context information
            if (typeMapper.isInMultipartContext()) {
                parameter.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_CONTEXT, true);

                // If this parameter itself is a file/binary type, mark it specifically
                if (parameter.isBinary || "file".equals(parameter.baseType)) {
                    parameter.vendorExtensions.put(VENDOR_EXTENSION_IS_MULTIPART_FILE, true);
                }
            }

            return parameter;
        } finally {
            // Always clear context after processing to avoid memory leaks
            typeMapper.exitMultipartContext();
        }
    }
}
