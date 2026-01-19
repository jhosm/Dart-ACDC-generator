# Creating OpenAPI Generator Client Generators

This guide covers how to create custom client generators for OpenAPI Generator, with specific focus on Dart/Flutter development.

## Overview

OpenAPI Generator creates code from OpenAPI specifications (v2.0 and 3.x) using a three-step process:
1. Read an OpenAPI specification
2. Create a normalized data structure from the document
3. Apply that structure to Mustache templates
4. Generate output files

## Dart-Dio Generator Example

The dart-dio generator is a **STABLE, CLIENT-type generator** that generates a Dart Dio client library.

### Key Features
- **Type**: Client generator
- **Language**: Dart
- **Status**: STABLE
- **Templating Engine**: Mustache (default)

### Configuration Options

The dart-dio generator offers numerous customizable properties:

- **Date Library**: Choose between Dart's core DateTime or Time Machine library
- **Serialization**: Select built_value (default) or json_serializable for object serialization
- **Enum Handling**: Enable unknown enum case fallback with `enumUnknownDefaultCase`
- **Property Management**: Options for marking properties as final and sorting by required flags
- **Publication Metadata**: Configure author, version, homepage, and repository details via pub* options

### Generated Output Structure

The generator produces pub package files with a configurable source folder (default: `src`). Generated code includes:
- API clients
- Models
- Documentation reflecting your OpenAPI specification

### Feature Support

**Strong Support**:
- JSON serialization
- Basic authentication
- OAuth2 implicit flow
- Comprehensive data types (Int32, Int64, Double, Date, DateTime)
- Polymorphism
- Arrays and maps

**Limited Support**:
- No XML support
- No OpenID Connect
- No multiserver or parameterized server configurations from OpenAPI 3.0

## Creating a New Generator

### Method 1: Using the Bootstrap Script (Recommended)

Run the `./new.sh` script with appropriate flags:

```bash
./new.sh -n my-dart-generator -c -t
```

**Flags**:
- `-n` — generator name (kebab-cased)
- `-c` — create a client generator
- `-s` — create a server generator
- `-d` — create a documentation generator
- `-H` — create a schema generator
- `-f` — create a config generator
- `-t` — create test files

### Method 2: Using the Meta Command

```bash
java -jar openapi-generator-cli.jar meta \
  -o out/generators/my-codegen \
  -n my-codegen \
  -p com.my.company.codegen
```

**Parameters**:
- `-o`: Output directory
- `-n`: Generator name
- `-p`: Package name for the generator

This creates a complete project structure with all necessary files including a README.md.

## Required Files

To create a new generator, you need:

### 1. Codegen Class

**Location**: `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/`

This Java class defines:
- Language options
- Framework options
- OpenAPI feature set support

**Key Configuration in Constructor**:
```java
outputFolder = "generated-code/my-generator";
modelTemplateFiles.put("model.mustache", ".dart");
apiTemplateFiles.put("api.mustache", ".dart");
embeddedTemplateDir = templateDir = "my-generator";
apiPackage = "lib.api";
modelPackage = "lib.model";
```

### 2. SPI Registration

**Location**: `modules/openapi-generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig`

Add a reference to your Codegen class to allow classpath extension.

### 3. Template Files

**Location**: `modules/openapi-generator/src/main/resources/`

Create core templates:
- **README.mustache** — Overview documentation
- **api.mustache** — API endpoint generation
- **model.mustache** — Data model generation

### 4. Config File

**Location**: `./bin/configs/`

Add a configuration file providing a "real life" example of generated output.

## Mustache Templating System

OpenAPI Generator uses the **jmustache** library for Mustache templating.

### Key Mustache Features

**Loops**:
```mustache
{{#operations}}
  {{#operation}}
    // Operation: {{operationId}}
  {{/operation}}
{{/operations}}
```

**Conditionals**:
```mustache
{{#isDeprecated}}
  @deprecated
{{/isDeprecated}}
```

**Variables**:
```mustache
{{operationId}}
{{classname}}
{{description}}
```

**Lambda Functions** (built-in transformations):
```mustache
{{#lambda.lowercase}}TEXT{{/lambda.lowercase}}
{{#lambda.uppercase}}text{{/lambda.uppercase}}
{{#lambda.titlecase}}some text{{/lambda.titlecase}}
```

### Template Resolution Priority

1. User customized library path
2. User customized top-level path
3. Embedded library path
4. Embedded top-level path
5. Common embedded path

## Customization Approaches

### 1. User-Defined Templates (Simplest)

Override built-in templates without creating a full custom generator.

Create a `config.yaml` file:
```yaml
templateDir: my_custom_templates
files:
  AUTHORS.md: {}
  api.mustache:
    templateType: API
    destinationFilename: Impl.dart
```

User-defined templates will merge with built-in template definitions.

### 2. Extracting Templates

Extract existing templates to customize:
```bash
openapi-generator author template -g dart-dio --library dio
```

Specify custom templates via:
- **CLI**: `-t/--template` option
- **Maven Plugin**: `templateDirectory`
- **Gradle Plugin**: `templateDir`

### 3. Full Custom Generator

After creating with `meta` command or `new.sh` script:
1. Customize the generated Codegen class
2. Modify templates
3. Compile via `mvn package`
4. Add the JAR to your classpath when running the CLI

## Project Structure

```
openapi-generator/
├── modules/
│   └── openapi-generator/
│       └── src/
│           └── main/
│               ├── java/
│               │   └── org/openapitools/codegen/languages/
│               │       └── MyDartGenerator.java
│               └── resources/
│                   ├── META-INF/services/
│                   │   └── org.openapitools.codegen.CodegenConfig
│                   └── my-dart-generator/
│                       ├── README.mustache
│                       ├── api.mustache
│                       └── model.mustache
└── bin/
    └── configs/
        └── my-dart-generator.yaml
```

## Compilation and Testing

### Quick Compilation
```bash
mvn clean package -DskipTests
```

### Generate Samples
```bash
./bin/generate-samples.sh ./bin/configs/my-config.yaml
```

### Verify Before Submitting
```bash
./bin/utils/ensure-up-to-date
```

## Configuration Options

Customize behavior through configuration files (JSON or YAML):
```yaml
generatorName: my-dart-generator
outputDir: ./generated
apiPackage: api
modelPackage: model
additionalProperties:
  pubName: my_api_client
  pubVersion: 1.0.0
  pubAuthor: Your Name
```

Discover available options for a language:
```bash
openapi-generator config-help -g dart-dio
```

## Additional Customization Features

- **Selective Generation**: Control which models, APIs, and tests generate
- **Ignore Files**: Use `.openapi-generator-ignore` for fine-grained output control
- **Name Mapping**: Rename properties, parameters, models, and operations
- **OpenAPI Normalizer**: Transform non-conformant specifications automatically

## Alternative Template Engines

As of version 4.0.0, OpenAPI Generator supports:
- **Mustache** (default, via jmustache)
- **Handlebars** (experimental)
- **Custom engines** via plugins (by extending `TemplatingEngineAdapter`)

## Next Steps for Dart/Flutter Generator

To create a custom Dart/Flutter client generator:

1. **Study Existing Dart Generators**:
   - dart-dio (uses Dio HTTP client)
   - dart (uses built-in HTTP)

2. **Define Your Target**:
   - HTTP client library (Dio, http, etc.)
   - Serialization approach (json_serializable, built_value, freezed)
   - Null safety support
   - Flutter-specific features

3. **Plan Your Templates**:
   - API client files
   - Model files with serialization
   - Supporting files (pubspec.yaml, README, etc.)

4. **Extend Base Classes**:
   - Extend `DefaultCodegen` or existing Dart generator
   - Override necessary methods for custom behavior

5. **Test Thoroughly**:
   - Use Petstore OpenAPI spec for testing
   - Verify generated code compiles
   - Test against real API endpoints

## Resources

- [OpenAPI Generator Official Docs](https://openapi-generator.tech/)
- [Dart-Dio Generator](https://openapi-generator.tech/docs/generators/dart-dio)
- [Create New Generator Guide](https://openapi-generator.tech/docs/new-generator/)
- [Customization Guide](https://openapi-generator.tech/docs/customization/)
- [Templating Documentation](https://openapi-generator.tech/docs/templating/)
- [GitHub Repository](https://github.com/OpenAPITools/openapi-generator)

### Project-Specific

- [ADR-001: Generated Code Architecture](./adr-001-generated-code-architecture.md) - Architectural decisions for the dart-acdc-generator including naming conventions, mocking support, and configuration classes
