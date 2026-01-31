# Tasks: Bootstrap Generator

## 1. Environment Setup

- [x] 1.1 Create `generator/` directory structure for standalone generator
- [x] 1.2 Create `generator/pom.xml` with OpenAPI Generator dependencies (pin to version 7.10.0)
- [x] 1.3 Verify JDK 11+ is available and document in README (using JDK 21)
- [x] 1.4 Create `.gitignore` for generator build artifacts (target/, *.class, etc.)

## 2. Codegen Class Creation

- [x] 2.1 Create `DartAcdcGenerator.java` extending `DefaultCodegen`
- [x] 2.2 Implement required interface methods (`getName()`, `getHelp()`, `getTag()`)
- [x] 2.3 Configure basic Dart type mappings (string→String, integer→int, boolean→bool, etc.)
- [x] 2.4 Configure extended type mappings (long→int, binary→List<int>, uuid→String, nullable types)
- [x] 2.5 Set up language-specific primitives (`String`, `bool`, `int`, `double`, `num`, `Object`)
- [x] 2.6 Configure output folder structure (`lib/models`, `lib/remote_data_sources`, etc.)
- [x] 2.7 Implement reserved keyword escaping for Dart (~60 reserved words)
- [x] 2.8 Implement pubName sanitization (lowercase, underscores only)
- [x] 2.9 Configure enum type generation (Dart enums from OpenAPI enum values)
- [x] 2.9.1 Implement enum value camelCase conversion
- [x] 2.9.2 Implement enum collision resolution (suffix with index)
- [x] 2.9.3 Implement numeric enum value handling (prefix with `value`)
- [x] 2.10 Configure model reference handling ($ref → model type)
- [x] 2.11 Configure file/binary type mapping (MultipartFile for uploads)
- [x] 2.12 Implement allOf composition (merge properties, last-wins conflict resolution)
- [x] 2.13 Implement allOf required property merging (required if ANY schema requires)
- [x] 2.14 Implement oneOf with discriminator (sealed class + switch on discriminator)
- [x] 2.15 Implement oneOf without discriminator (sealed class + try-each-alternative)
- [x] 2.16 Implement oneOf primitive alternatives (wrapper classes with value property)
- [x] 2.17 Implement anyOf composition (same as oneOf with doc comment)
- [x] 2.18 Implement nested composition handling (allOf containing oneOf, etc.)
- [x] 2.19 Implement circular reference handling (nullable types + factory constructors)
- [x] 2.20 Implement oneOf inline schema naming (`{Base}Option{index}` pattern)

## 3. SPI Registration

- [x] 3.1 Create `META-INF/services/org.openapitools.codegen.CodegenConfig`
- [x] 3.2 Register `DartAcdcGenerator` class

## 4. Template Directory Setup

- [x] 4.1 Create `generator/src/main/resources/dart-acdc/` directory
- [x] 4.2 Add placeholder `api.mustache` template (generates RemoteDataSource interface + impl)
- [x] 4.3 Add placeholder `model.mustache` template (generates model class with json_serializable)
- [x] 4.4 Add placeholder `api_client.mustache` template (generates ApiClient factory)
- [x] 4.5 Add placeholder `pubspec.mustache` template (with dart_acdc, dio dependencies)
- [x] 4.6 Add placeholder `README.mustache` template
- [x] 4.7 Add placeholder `analysis_options.mustache` template
- [x] 4.8 Add placeholder `lib.mustache` barrel export template ({pubName}.dart)

## 5. Test Configuration

- [x] 5.1 Create `bin/configs/dart-acdc-petstore.yaml` pointing to Petstore spec
- [x] 5.2 Add `samples/petstore.yaml` OpenAPI spec for testing
- [x] 5.3 Add `samples/minimal.yaml` OpenAPI spec (empty/minimal for edge case testing)
- [x] 5.4 Add `samples/composition.yaml` OpenAPI spec (allOf/oneOf/anyOf examples)
- [x] 5.5 Add `samples/file-upload.yaml` OpenAPI spec (multipart file upload examples)
- [x] 5.6 Add `samples/enums.yaml` OpenAPI spec (enum collision, numeric values)
- [x] 5.7 Create build script `scripts/build.sh`
- [x] 5.8 Create generation script `scripts/generate-samples.sh`

## 6. Verification

- [x] 6.1 Run `mvn clean package -DskipTests` successfully
- [x] 6.2 Verify generator appears in `java -jar openapi-generator-cli.jar list`
- [x] 6.3 Generate sample Petstore client
- [x] 6.4 Generate minimal spec client (edge case verification)
- [x] 6.5 Generate composition spec client (allOf/oneOf/anyOf verification)
- [x] 6.6 Generate file-upload spec client (MultipartFile verification)
- [x] 6.7 Generate enums spec client (collision and numeric enum verification)
- [x] 6.8 Verify generated files exist in expected locations
- [x] 6.9 Verify generated Dart code has no syntax errors (`dart analyze`)
- [x] 6.10 Verify `dart pub get` succeeds on generated code
- [x] 6.11 Verify sealed classes generated correctly for oneOf/anyOf
- [x] 6.12 Verify enum collision resolution works correctly
- [x] 6.13 Update project README with build instructions
- [x] 6.14 Document OpenAPI Generator version compatibility
