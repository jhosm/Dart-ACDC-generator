# Dart/Flutter Generator Quick Reference

> **Note**: This guide uses the `http` package in examples for simplicity and to demonstrate basic OpenAPI Generator concepts. The actual **dart-acdc-generator** project uses **Dio** (via Dart-ACDC) as the HTTP client. See [ADR-001](./adr-001-generated-code-architecture.md) for the production architecture decisions.

## Quick Start: Creating a Dart Client Generator

### Step 1: Bootstrap the Generator

```bash
# Navigate to OpenAPI Generator repository
cd openapi-generator

# Create a new client generator
./new.sh -n dart-custom -c -t
```

This creates:
- Java class in `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartCustomGenerator.java`
- Template directory in `modules/openapi-generator/src/main/resources/dart-custom/`
- Test files (if `-t` flag used)

### Step 2: Configure the Codegen Class

**Location**: `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartCustomGenerator.java`

```java
public class DartCustomGenerator extends DefaultCodegen implements CodegenConfig {

    public DartCustomGenerator() {
        super();

        // Basic configuration
        outputFolder = "generated-code/dart-custom";
        modelTemplateFiles.put("model.mustache", ".dart");
        apiTemplateFiles.put("api.mustache", ".dart");
        embeddedTemplateDir = templateDir = "dart-custom";

        // Package configuration
        apiPackage = "lib.api";
        modelPackage = "lib.model";

        // Supporting files
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("pubspec.mustache", "", "pubspec.yaml"));
        supportingFiles.add(new SupportingFile("analysis_options.mustache", "", "analysis_options.yaml"));

        // Language-specific configuration
        languageSpecificPrimitives = new HashSet<>(Arrays.asList(
            "String", "bool", "int", "double", "num", "Object"
        ));

        // Type mappings
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("number", "double");
        typeMapping.put("float", "double");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "String");
        typeMapping.put("date", "DateTime");
        typeMapping.put("DateTime", "DateTime");
        typeMapping.put("object", "Object");
        typeMapping.put("array", "List");
        typeMapping.put("map", "Map");
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "dart-custom";
    }

    @Override
    public String getHelp() {
        return "Generates a Dart client library with custom features.";
    }
}
```

### Step 3: Register the Generator

**Location**: `modules/openapi-generator/src/main/resources/META-INF/services/org.openapitools.codegen.CodegenConfig`

Add this line:
```
org.openapitools.codegen.languages.DartCustomGenerator
```

### Step 4: Create Templates

#### README.mustache
```mustache
# {{appName}}

{{#appDescription}}
{{appDescription}}
{{/appDescription}}

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  {{pubName}}: ^{{pubVersion}}
```

## Usage

```dart
import 'package:{{pubName}}/api.dart';

void main() {
  var apiInstance = {{classname}}();
  // Use the API
}
```
```

#### pubspec.mustache
```mustache
name: {{pubName}}
version: {{pubVersion}}
description: {{pubDescription}}
{{#pubAuthor}}
author: {{pubAuthor}}
{{/pubAuthor}}
{{#pubHomepage}}
homepage: {{pubHomepage}}
{{/pubHomepage}}

environment:
  sdk: '>=3.0.0 <4.0.0'

dependencies:
  http: ^1.1.0
  json_annotation: ^4.8.0

dev_dependencies:
  build_runner: ^2.4.0
  json_serializable: ^6.7.0
  test: ^1.24.0
```

#### api.mustache
```mustache
// AUTO GENERATED FILE, DO NOT EDIT.
//
// @dart=3.0

import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;

{{#imports}}
import '{{import}}';
{{/imports}}

{{#operations}}
class {{classname}} {
  final String basePath;
  final http.Client client;

  {{classname}}({
    required this.basePath,
    http.Client? client,
  }) : client = client ?? http.Client();

{{#operation}}
  /// {{summary}}
  {{#notes}}
  /// {{notes}}
  {{/notes}}
  Future<{{#returnType}}{{returnType}}{{/returnType}}{{^returnType}}void{{/returnType}}> {{operationId}}(
    {{#allParams}}
    {{#required}}required {{/required}}{{dataType}} {{paramName}},
    {{/allParams}}
  ) async {
    // Build the request path
    var path = '{{{path}}}'{{#pathParams}}.replaceAll('{{{baseName}}}', {{paramName}}.toString()){{/pathParams}};

    // Build query parameters
    var queryParams = <String, String>{};
    {{#queryParams}}
    {{#required}}
    queryParams['{{baseName}}'] = {{paramName}}.toString();
    {{/required}}
    {{^required}}
    if ({{paramName}} != null) {
      queryParams['{{baseName}}'] = {{paramName}}.toString();
    }
    {{/required}}
    {{/queryParams}}

    // Build the URI
    var uri = Uri.parse('$basePath$path').replace(queryParameters: queryParams);

    // Make the request
    var response = await client.{{httpMethod}}(
      uri,
      {{#bodyParam}}
      body: jsonEncode({{paramName}}),
      {{/bodyParam}}
      headers: {
        'Content-Type': 'application/json',
        {{#headerParams}}
        '{{baseName}}': {{paramName}}.toString(),
        {{/headerParams}}
      },
    );

    // Handle the response
    if (response.statusCode >= 200 && response.statusCode < 300) {
      {{#returnType}}
      return {{returnType}}.fromJson(jsonDecode(response.body));
      {{/returnType}}
      {{^returnType}}
      return;
      {{/returnType}}
    } else {
      throw Exception('Request failed with status: ${response.statusCode}');
    }
  }

{{/operation}}
}
{{/operations}}
```

#### model.mustache
```mustache
// AUTO GENERATED FILE, DO NOT EDIT.
//
// @dart=3.0

import 'package:json_annotation/json_annotation.dart';

part '{{classFilename}}.g.dart';

{{#models}}
{{#model}}
{{#description}}
/// {{description}}
{{/description}}
@JsonSerializable()
class {{classname}} {
{{#vars}}
  {{#description}}
  /// {{description}}
  {{/description}}
  @JsonKey(name: '{{baseName}}')
  {{#required}}required {{/required}}{{dataType}}{{^required}}?{{/required}} {{name}};

{{/vars}}

  {{classname}}({
{{#vars}}
    {{#required}}required {{/required}}this.{{name}},
{{/vars}}
  });

  factory {{classname}}.fromJson(Map<String, dynamic> json) => _${{classname}}FromJson(json);
  Map<String, dynamic> toJson() => _${{classname}}ToJson(this);
}
{{/model}}
{{/models}}
```

### Step 5: Create Config File

**Location**: `./bin/configs/dart-custom-petstore.yaml`

```yaml
generatorName: dart-custom
outputDir: samples/client/petstore/dart-custom
inputSpec: modules/openapi-generator/src/test/resources/3_0/petstore.yaml
templateDir: modules/openapi-generator/src/main/resources/dart-custom
additionalProperties:
  pubName: openapi_petstore
  pubVersion: 1.0.0
  pubDescription: OpenAPI Petstore client
  pubAuthor: OpenAPI Generator
```

### Step 6: Build and Test

```bash
# Compile the generator
mvn clean package -DskipTests

# Generate sample code
./bin/generate-samples.sh ./bin/configs/dart-custom-petstore.yaml

# Check the generated code
cd samples/client/petstore/dart-custom
dart pub get
dart analyze
dart test
```

## Key Mustache Variables for Dart

### API Level
- `{{classname}}` - API class name
- `{{operations}}` - List of operations
- `{{imports}}` - List of imports

### Operation Level
- `{{operationId}}` - Operation method name
- `{{httpMethod}}` - HTTP method (get, post, put, delete)
- `{{path}}` - Endpoint path
- `{{summary}}` - Operation summary
- `{{notes}}` - Operation description
- `{{returnType}}` - Return type
- `{{allParams}}` - All parameters
- `{{pathParams}}` - Path parameters
- `{{queryParams}}` - Query parameters
- `{{headerParams}}` - Header parameters
- `{{bodyParam}}` - Body parameter

### Parameter Level
- `{{paramName}}` - Parameter variable name
- `{{baseName}}` - Parameter name in spec
- `{{dataType}}` - Parameter type
- `{{required}}` - Is required (boolean)
- `{{description}}` - Parameter description

### Model Level
- `{{classname}}` - Model class name
- `{{classFilename}}` - Model filename
- `{{description}}` - Model description
- `{{vars}}` - List of properties
- `{{imports}}` - List of imports

### Property Level
- `{{name}}` - Property variable name
- `{{baseName}}` - Property name in spec
- `{{dataType}}` - Property type
- `{{required}}` - Is required (boolean)
- `{{description}}` - Property description
- `{{isNullable}}` - Is nullable (boolean)

## Common Dart Type Mappings

```java
// In your Generator class
typeMapping.put("integer", "int");
typeMapping.put("long", "int");
typeMapping.put("number", "double");
typeMapping.put("float", "double");
typeMapping.put("double", "double");
typeMapping.put("boolean", "bool");
typeMapping.put("string", "String");
typeMapping.put("UUID", "String");
typeMapping.put("URI", "String");
typeMapping.put("date", "DateTime");
typeMapping.put("DateTime", "DateTime");
typeMapping.put("password", "String");
typeMapping.put("file", "MultipartFile");
typeMapping.put("binary", "List<int>");
typeMapping.put("ByteArray", "List<int>");
typeMapping.put("object", "Object");
typeMapping.put("AnyType", "Object");
typeMapping.put("array", "List");
typeMapping.put("map", "Map<String, dynamic>");
```

## Testing Your Generator

### 1. Unit Tests
Create tests in `modules/openapi-generator/src/test/java/org/openapitools/codegen/dart/custom/`

### 2. Integration Tests
Use the Petstore spec to validate:
```bash
./bin/generate-samples.sh ./bin/configs/dart-custom-petstore.yaml
cd samples/client/petstore/dart-custom
dart analyze
dart test
```

### 3. Manual Testing
Generate from your own OpenAPI spec:
```bash
openapi-generator generate \
  -g dart-custom \
  -i your-spec.yaml \
  -o output-dir \
  --additional-properties pubName=my_client,pubVersion=1.0.0
```

## Common Customizations

### Add Generator Options
```java
public DartCustomGenerator() {
    super();

    cliOptions.add(CliOption.newBoolean("useNullSafety",
        "Use Dart null safety").defaultValue("true"));

    cliOptions.add(CliOption.newString("httpLibrary",
        "HTTP client library (http, dio, etc.)").defaultValue("http"));
}

@Override
public void processOpts() {
    super.processOpts();

    if (additionalProperties.containsKey("useNullSafety")) {
        setUseNullSafety(convertPropertyToBoolean("useNullSafety"));
    }
}
```

### Override Methods
```java
@Override
public String toApiName(String name) {
    return camelize(name) + "Api";
}

@Override
public String toModelName(String name) {
    return camelize(name);
}

@Override
public String toVarName(String name) {
    return underscore(name);
}
```

## Resources

- Main guide: `research/creating-generators.md`
- OpenAPI Generator repo: https://github.com/OpenAPITools/openapi-generator
- Dart-dio generator source: `modules/openapi-generator/src/main/java/org/openapitools/codegen/languages/DartDioClientCodegen.java`
