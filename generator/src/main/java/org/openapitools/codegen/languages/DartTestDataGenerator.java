package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for generating test data for Dart types.
 * 
 * This class handles:
 * - Test value generation for primitive and complex Dart types
 * - Test JSON generation for models with required fields
 * - MultipartFile test data generation
 */
public class DartTestDataGenerator {

    /**
     * Map to store model schemas for test data generation.
     * Key: model name (e.g., "Pet", "NewPet"), Value: the schema definition
     */
    private final Map<String, Schema> modelSchemas;

    /**
     * Set of language-specific primitive types used for type checking.
     */
    private final Set<String> languageSpecificPrimitives;

    /**
     * Constructor.
     *
     * @param modelSchemas              map of model schemas
     * @param languageSpecificPrimitives set of primitive types
     */
    public DartTestDataGenerator(Map<String, Schema> modelSchemas, Set<String> languageSpecificPrimitives) {
        this.modelSchemas = modelSchemas;
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    /**
     * Generates a test value for a given Dart data type.
     * Used by test templates to generate valid test data for parameters.
     *
     * @param dataType the Dart data type (e.g., "int", "String", "Pet")
     * @return a Dart code string representing a test value
     */
    public String getTestValueForType(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            return "null";
        }

        // Handle primitive types using enhanced switch expression
        return switch (dataType) {
            case "int" -> "42";
            case "double" -> "3.14";
            case "num" -> "123.45";
            case "bool" -> "true";
            case "String" -> "'test_value'";
            case "DateTime" -> "DateTime.parse('2024-01-01T00:00:00.000Z')";
            default -> {
                // Handle bare List type (without generics)
                if (dataType.equals("List")) {
                    yield "const []";
                }
                // Handle List types with generics
                else if (dataType.startsWith("List<")) {
                    String innerType = extractGenericType(dataType);
                    if (isPrimitiveOrSimpleType(innerType)) {
                        yield "[]";
                    }
                    // For lists of complex types, use empty list
                    yield "const []";
                }
                // Handle Map types
                else if (dataType.startsWith("Map<")) {
                    yield "const <String, dynamic>{}";
                }
                // Handle MultipartFile
                else if (dataType.equals("MultipartFile")) {
                    yield "MultipartFile.fromString('test', filename: 'test.txt')";
                }
                // For model types, generate fromJson with valid test data for required fields
                // This ensures tests compile and run successfully
                else {
                    String testJson = generateTestJsonForModel(dataType);
                    yield dataType + ".fromJson(const " + testJson + ")";
                }
            }
        };
    }

    /**
     * Generates a raw (unquoted) test value for URL path embedding.
     * Unlike getTestValueForType(), this returns values without Dart string
     * delimiters.
     * Used in path parameter replacement: .replaceAll('{param}', 'rawValue')
     *
     * @param dataType the Dart data type
     * @return a raw string suitable for URL embedding
     */
    public String getTestValueRawForType(String dataType) {
        if (dataType == null || dataType.isEmpty()) {
            return "null";
        }

        return switch (dataType) {
            case "int" -> "42";
            case "double" -> "3.14";
            case "num" -> "123.45";
            case "bool" -> "true";
            case "String" -> "test_value";
            case "DateTime" -> "2024-01-01T00:00:00.000Z";
            default -> "test_value";
        };
    }

    /**
     * Generates valid test JSON for a model type with required fields populated.
     * Looks up the model schema and generates appropriate test values for all
     * required fields.
     *
     * @param modelName the model name (e.g., "Pet", "NewPet")
     * @return a JSON string with required fields populated, or empty JSON if no
     *         required fields
     */
    @SuppressWarnings("rawtypes")
    public String generateTestJsonForModel(String modelName) {
        if (modelSchemas == null || !modelSchemas.containsKey(modelName)) {
            return "<String, dynamic>{}";
        }

        Schema schema = modelSchemas.get(modelName);
        if (schema == null || schema.getProperties() == null) {
            return "<String, dynamic>{}";
        }

        // Get required fields
        List<String> required = schema.getRequired();
        if (required == null || required.isEmpty()) {
            return "<String, dynamic>{}";
        }

        // Build JSON with required fields
        StringBuilder json = new StringBuilder("<String, dynamic>{");
        boolean first = true;

        for (String fieldName : required) {
            Schema fieldSchema = (Schema) schema.getProperties().get(fieldName);
            if (fieldSchema == null) {
                continue;
            }

            if (!first) {
                json.append(", ");
            }
            first = false;

            // Add field name
            json.append("'").append(fieldName).append("': ");

            // Add field value based on type
            String fieldType = fieldSchema.getType();
            if (fieldType != null) {
                switch (fieldType) {
                    case "string":
                        json.append("'test_").append(fieldName).append("'");
                        break;
                    case "integer":
                    case "number":
                        json.append("42");
                        break;
                    case "boolean":
                        json.append("true");
                        break;
                    case "array":
                        json.append("[]");
                        break;
                    case "object":
                        json.append("{}");
                        break;
                    default:
                        json.append("null");
                        break;
                }
            } else {
                // Handle $ref or complex types
                json.append("null");
            }
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Extracts the inner type from a generic type declaration.
     * E.g., "List&lt;Pet&gt;" -> "Pet", "Map&lt;String, Pet&gt;" -> "Pet"
     *
     * @param genericType the generic type string
     * @return the inner type, or empty string if not found
     */
    private String extractGenericType(String genericType) {
        int start = genericType.indexOf('<');
        int end = genericType.lastIndexOf('>');
        if (start != -1 && end != -1 && end > start) {
            String inner = genericType.substring(start + 1, end).trim();
            // For Map<String, Pet>, extract the last type
            int commaIndex = inner.lastIndexOf(',');
            if (commaIndex != -1) {
                return inner.substring(commaIndex + 1).trim();
            }
            return inner;
        }
        return "";
    }

    /**
     * Checks if a type is a primitive or simple Dart type.
     *
     * @param type the type to check
     * @return true if primitive or simple type, false if model type
     */
    private boolean isPrimitiveOrSimpleType(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        return languageSpecificPrimitives.contains(type) ||
                type.equals("Object") ||
                type.equals("dynamic");
    }
}
