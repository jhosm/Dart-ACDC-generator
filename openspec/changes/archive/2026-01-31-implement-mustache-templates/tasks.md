# Tasks: Implement Mustache Templates

## 1. Model Templates

- [x] 1.1 Create `model.mustache` with `@JsonSerializable()` annotation
- [x] 1.2 Add `@JsonKey(name: '...')` for properties with different JSON names
- [x] 1.3 Handle required vs optional properties (nullable types)
- [x] 1.4 Generate `fromJson` factory and `toJson` method
- [x] 1.5 Add documentation comments from OpenAPI descriptions
- [x] 1.6 Create `models.mustache` barrel export file

## 2. Remote Data Source Interface Templates

- [x] 2.1 Create `remote_data_source.mustache` for abstract interface
- [x] 2.2 Generate method signatures for each OpenAPI operation
- [x] 2.3 Use `*RemoteDataSource` naming convention per ADR-001
- [x] 2.4 Add class-level documentation listing possible exceptions
- [x] 2.5 Handle path parameters as positional required parameters
- [x] 2.6 Handle query parameters as named optional parameters
- [x] 2.7 Handle request body as positional parameter
- [x] 2.8 Add method documentation from OpenAPI summary/description

## 3. Remote Data Source Implementation Templates

- [x] 3.1 Create `remote_data_source_impl.mustache` for concrete implementation
- [x] 3.2 Implement `Dio` constructor injection
- [x] 3.3 Implement each method with `try/catch` for DioException
- [x] 3.4 Add `AcdcException.fromDioException(e)` error conversion
- [x] 3.5 Handle path parameter interpolation in URL
- [x] 3.6 Handle query parameters with null filtering (`if (x != null)`)
- [x] 3.7 Handle header parameters in `Options`
- [x] 3.8 Handle request body serialization (`.toJson()`)
- [x] 3.9 Handle response deserialization (`.fromJson()`)
- [x] 3.10 Handle `void` return types (no deserialization)
- [x] 3.11 Handle `List<T>` response types
- [x] 3.12 Create `remote_data_sources.mustache` barrel export

## 4. Configuration Templates

- [x] 4.1 Create `acdc_config.mustache` with main config class
- [x] 4.2 Create `auth_config.mustache` for authentication settings
- [x] 4.3 Create `cache_config.mustache` for caching settings
- [x] 4.4 Create `log_config.mustache` for logging settings
- [x] 4.5 Create `offline_config.mustache` for offline support settings
- [x] 4.6 Create `security_config.mustache` for certificate pinning
- [x] 4.7 Create `config.mustache` barrel export

## 5. API Client Template

- [x] 5.1 Create `api_client.mustache` with `createDio()` factory
- [x] 5.2 Handle conditional feature inclusion based on config
- [x] 5.3 Add proper imports for Dart-ACDC types

## 6. Supporting File Templates

- [x] 6.1 Create `pubspec.mustache` with all dependencies
- [x] 6.2 Create `analysis_options.mustache` with linting rules
- [x] 6.3 Create `README.mustache` with usage documentation
- [x] 6.4 Create `gitignore.mustache` for generated .g.dart files
- [x] 6.5 Create main barrel export template (`lib/{pubName}.dart`)

## 7. Codegen Class Updates

- [x] 7.1 Register all template files in `DartAcdcGenerator`
- [x] 7.2 Add supporting files list
- [x] 7.3 Configure file naming conventions

## 8. Test Templates

- [x] 8.1 Create `test/test_helpers.mustache` with mock Dio helpers
- [x] 8.2 Create `test/api_test.mustache` for API integration tests
- [x] 8.3 Create `test/model_test.mustache` for model serialization tests
- [x] 8.4 Register test templates in DartAcdcGenerator
- [x] 8.5 Add http_mock_adapter dependency to pubspec.mustache

## 9. Verification

- [x] 9.1 Generate Petstore client
- [x] 9.2 Run `dart analyze` on generated code (no critical errors)
- [x] 9.3 Run `dart pub get` successfully
- [x] 9.4 Run `dart run build_runner build` for json_serializable
- [x] 9.5 Verify all barrel exports work correctly
- [x] 9.6 Manual review of generated code quality
