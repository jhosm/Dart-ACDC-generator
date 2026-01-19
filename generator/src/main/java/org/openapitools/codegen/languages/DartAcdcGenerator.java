package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.SupportingFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Dart-ACDC OpenAPI Generator
 *
 * Generates Dart API clients with full Dart-ACDC integration (Authentication, Caching, Debugging, Client).
 *
 * @see <a href="https://github.com/jhosm/Dart-ACDC">Dart-ACDC Library</a>
 */
public class DartAcdcGenerator extends DefaultCodegen implements CodegenConfig {

    /**
     * Dart reserved keywords that require escaping.
     * These cannot be used as identifiers in Dart code.
     */
    protected static final Set<String> DART_RESERVED_WORDS = new HashSet<>(Arrays.asList(
        // Keywords
        "abstract", "as", "assert", "async", "await",
        "break", "case", "catch", "class", "const",
        "continue", "covariant", "default", "deferred", "do",
        "dynamic", "else", "enum", "export", "extends",
        "extension", "external", "factory", "false", "final",
        "finally", "for", "Function", "get", "hide",
        "if", "implements", "import", "in", "interface",
        "is", "late", "library", "mixin", "new",
        "null", "on", "operator", "part", "required",
        "rethrow", "return", "set", "show", "static",
        "super", "switch", "sync", "this", "throw",
        "true", "try", "typedef", "var", "void",
        "while", "with", "yield"
    ));

    /**
     * Constructor - configures the generator with Dart-ACDC specific settings.
     */
    public DartAcdcGenerator() {
        super();

        // Set reserved words for the generator
        reservedWords.addAll(DART_RESERVED_WORDS);

        // Basic configuration
        outputFolder = "generated-code/dart-acdc";
        modelTemplateFiles.put("model.mustache", ".dart");
        apiTemplateFiles.put("api.mustache", ".dart");
        embeddedTemplateDir = templateDir = "dart-acdc";

        // Package configuration following Flutter/Dart conventions
        apiPackage = "lib.remote_data_sources";
        modelPackage = "lib.models";

        // Supporting files
        supportingFiles.add(new SupportingFile("pubspec.mustache", "", "pubspec.yaml"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("analysis_options.mustache", "", "analysis_options.yaml"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("api_client.mustache", "lib", "api_client.dart"));

        // Config supporting files
        supportingFiles.add(new SupportingFile("config.mustache", "lib/config", "config.dart"));
        supportingFiles.add(new SupportingFile("acdc_config.mustache", "lib/config", "acdc_config.dart"));
        supportingFiles.add(new SupportingFile("auth_config.mustache", "lib/config", "auth_config.dart"));
        supportingFiles.add(new SupportingFile("cache_config.mustache", "lib/config", "cache_config.dart"));
        supportingFiles.add(new SupportingFile("log_config.mustache", "lib/config", "log_config.dart"));
        supportingFiles.add(new SupportingFile("offline_config.mustache", "lib/config", "offline_config.dart"));
        supportingFiles.add(new SupportingFile("security_config.mustache", "lib/config", "security_config.dart"));

        // Language-specific primitives
        languageSpecificPrimitives = new HashSet<>(Arrays.asList(
            "String",
            "bool",
            "int",
            "double",
            "num",
            "Object",
            "DateTime",
            "List",
            "Map"
        ));

        // Type mappings: OpenAPI types -> Dart types
        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("number", "double");
        typeMapping.put("float", "double");
        typeMapping.put("double", "double");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "String");
        typeMapping.put("UUID", "String");
        typeMapping.put("date", "DateTime");
        typeMapping.put("DateTime", "DateTime");
        typeMapping.put("date-time", "DateTime");
        typeMapping.put("password", "String");
        typeMapping.put("binary", "List<int>");
        typeMapping.put("ByteArray", "List<int>");
        typeMapping.put("file", "MultipartFile");
        typeMapping.put("object", "Map<String, dynamic>");
        typeMapping.put("array", "List");
        typeMapping.put("map", "Map<String, dynamic>");
        typeMapping.put("AnyType", "Object");
    }

    /**
     * Returns the generator's unique identifier.
     * Used by OpenAPI Generator CLI with -g flag (e.g., -g dart-acdc).
     *
     * @return "dart-acdc"
     */
    @Override
    public String getName() {
        return "dart-acdc";
    }

    /**
     * Returns the help message describing this generator.
     *
     * @return Generator description
     */
    @Override
    public String getHelp() {
        return "Generates a Dart-ACDC client library with authentication, caching, debugging, and offline support.";
    }

    /**
     * Returns the generator type.
     *
     * @return CodegenType.CLIENT
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Escapes a reserved word by suffixing it with an underscore.
     * This is called for property/variable names.
     *
     * @param name the reserved word to escape
     * @return the escaped name with underscore suffix
     */
    @Override
    public String escapeReservedWord(String name) {
        // For property names, suffix with underscore
        return name + "_";
    }

    /**
     * Escapes reserved words in model/class names.
     * Dart reserved keywords are suffixed with "Model".
     *
     * @param name the model/class name
     * @return the escaped name if it's a reserved word, otherwise the original name
     */
    @Override
    public String toModelName(String name) {
        // First apply standard sanitization from parent class
        String sanitized = super.toModelName(name);

        // If the sanitized name is a Dart reserved keyword (case-insensitive check), suffix with "Model"
        if (isReservedWord(sanitized.toLowerCase())) {
            return sanitized + "Model";
        }

        return sanitized;
    }

    /**
     * Returns the file name for a model.
     * Ensures file names match the escaped model names for Dart conventions.
     *
     * @param name the model name (from OpenAPI schema)
     * @return the file name (without extension)
     */
    @Override
    public String toModelFilename(String name) {
        // Use the escaped model name for the file name to maintain Dart conventions
        return toModelName(name);
    }
}
