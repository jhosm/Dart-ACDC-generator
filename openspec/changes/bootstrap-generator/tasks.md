# Tasks: Bootstrap Generator

## 1. Environment Setup

- [ ] 1.1 Create `generator/` directory structure for standalone generator
- [ ] 1.2 Create `generator/pom.xml` with OpenAPI Generator dependencies (pin to version 7.10.0)
- [ ] 1.3 Verify JDK 11+ is available and document in README
- [ ] 1.4 Create `.gitignore` for generator build artifacts (target/, *.class, etc.)

## 2. Codegen Class Creation

- [ ] 2.1 Create `DartAcdcGenerator.java` extending `DefaultCodegen`
- [ ] 2.2 Implement required interface methods (`getName()`, `getHelp()`, `getTag()`)
- [ ] 2.3 Configure basic Dart type mappings (string→String, integer→int, boolean→bool, etc.)
- [ ] 2.4 Configure extended type mappings (long→int, binary→List<int>, uuid→String, nullable types)
- [ ] 2.5 Set up language-specific primitives (`String`, `bool`, `int`, `double`, `num`, `Object`)
- [ ] 2.6 Configure output folder structure (`lib/models`, `lib/remote_data_sources`, etc.)
- [ ] 2.7 Implement reserved keyword escaping for Dart (~60 reserved words)
- [ ] 2.8 Implement pubName sanitization (lowercase, underscores only)
- [ ] 2.9 Configure enum type generation (Dart enums from OpenAPI enum values)
- [ ] 2.9.1 Implement enum value camelCase conversion
- [ ] 2.9.2 Implement enum collision resolution (suffix with index)
- [ ] 2.9.3 Implement numeric enum value handling (prefix with `value`)
- [ ] 2.10 Configure model reference handling ($ref → model type)
- [ ] 2.11 Configure file/binary type mapping (MultipartFile for uploads)
- [ ] 2.12 Implement allOf composition (merge properties, last-wins conflict resolution)
- [ ] 2.13 Implement allOf required property merging (required if ANY schema requires)
- [ ] 2.14 Implement oneOf with discriminator (sealed class + switch on discriminator)
- [ ] 2.15 Implement oneOf without discriminator (sealed class + try-each-alternative)
- [ ] 2.16 Implement oneOf primitive alternatives (wrapper classes with value property)
- [ ] 2.17 Implement anyOf composition (same as oneOf with doc comment)
- [ ] 2.18 Implement nested composition handling (allOf containing oneOf, etc.)
- [ ] 2.19 Implement circular reference handling (nullable types + factory constructors)
- [ ] 2.20 Implement oneOf inline schema naming (`{Base}Option{index}` pattern)

## 3. SPI Registration

- [ ] 3.1 Create `META-INF/services/org.openapitools.codegen.CodegenConfig`
- [ ] 3.2 Register `DartAcdcGenerator` class

## 4. Template Directory Setup

- [ ] 4.1 Create `generator/src/main/resources/dart-acdc/` directory
- [ ] 4.2 Add placeholder `api.mustache` template (generates RemoteDataSource interface + impl)
- [ ] 4.3 Add placeholder `model.mustache` template (generates model class with json_serializable)
- [ ] 4.4 Add placeholder `api_client.mustache` template (generates ApiClient factory)
- [ ] 4.5 Add placeholder `pubspec.mustache` template (with dart_acdc, dio dependencies)
- [ ] 4.6 Add placeholder `README.mustache` template
- [ ] 4.7 Add placeholder `analysis_options.mustache` template
- [ ] 4.8 Add placeholder `lib.mustache` barrel export template ({pubName}.dart)

## 5. Test Configuration

- [ ] 5.1 Create `bin/configs/dart-acdc-petstore.yaml` pointing to Petstore spec
- [ ] 5.2 Add `samples/petstore.yaml` OpenAPI spec for testing
- [ ] 5.3 Add `samples/minimal.yaml` OpenAPI spec (empty/minimal for edge case testing)
- [ ] 5.4 Add `samples/composition.yaml` OpenAPI spec (allOf/oneOf/anyOf examples)
- [ ] 5.5 Add `samples/file-upload.yaml` OpenAPI spec (multipart file upload examples)
- [ ] 5.6 Add `samples/enums.yaml` OpenAPI spec (enum collision, numeric values)
- [ ] 5.7 Create build script `scripts/build.sh`
- [ ] 5.8 Create generation script `scripts/generate-samples.sh`

## 6. Verification

- [ ] 6.1 Run `mvn clean package -DskipTests` successfully
- [ ] 6.2 Verify generator appears in `java -jar openapi-generator-cli.jar list`
- [ ] 6.3 Generate sample Petstore client
- [ ] 6.4 Generate minimal spec client (edge case verification)
- [ ] 6.5 Generate composition spec client (allOf/oneOf/anyOf verification)
- [ ] 6.6 Generate file-upload spec client (MultipartFile verification)
- [ ] 6.7 Generate enums spec client (collision and numeric enum verification)
- [ ] 6.8 Verify generated files exist in expected locations
- [ ] 6.9 Verify generated Dart code has no syntax errors (`dart analyze`)
- [ ] 6.10 Verify `dart pub get` succeeds on generated code
- [ ] 6.11 Verify sealed classes generated correctly for oneOf/anyOf
- [ ] 6.12 Verify enum collision resolution works correctly
- [ ] 6.13 Update project README with build instructions
- [ ] 6.14 Document OpenAPI Generator version compatibility
