# Tasks: Implement Mustache Templates

## 1. Model Templates

- [ ] 1.1 Create `model.mustache` with `@JsonSerializable()` annotation
- [ ] 1.2 Add `@JsonKey(name: '...')` for properties with different JSON names
- [ ] 1.3 Handle required vs optional properties (nullable types)
- [ ] 1.4 Generate `fromJson` factory and `toJson` method
- [ ] 1.5 Add documentation comments from OpenAPI descriptions
- [ ] 1.6 Create `models.mustache` barrel export file

## 2. Remote Data Source Interface Templates

- [ ] 2.1 Create `remote_data_source.mustache` for abstract interface
- [ ] 2.2 Generate method signatures for each OpenAPI operation
- [ ] 2.3 Use `*RemoteDataSource` naming convention per ADR-001
- [ ] 2.4 Add class-level documentation listing possible exceptions
- [ ] 2.5 Handle path parameters as positional required parameters
- [ ] 2.6 Handle query parameters as named optional parameters
- [ ] 2.7 Handle request body as positional parameter
- [ ] 2.8 Add method documentation from OpenAPI summary/description

## 3. Remote Data Source Implementation Templates

- [ ] 3.1 Create `remote_data_source_impl.mustache` for concrete implementation
- [ ] 3.2 Implement `Dio` constructor injection
- [ ] 3.3 Implement each method with `try/catch` for DioException
- [ ] 3.4 Add `AcdcException.fromDioException(e)` error conversion
- [ ] 3.5 Handle path parameter interpolation in URL
- [ ] 3.6 Handle query parameters with null filtering (`if (x != null)`)
- [ ] 3.7 Handle header parameters in `Options`
- [ ] 3.8 Handle request body serialization (`.toJson()`)
- [ ] 3.9 Handle response deserialization (`.fromJson()`)
- [ ] 3.10 Handle `void` return types (no deserialization)
- [ ] 3.11 Handle `List<T>` response types
- [ ] 3.12 Create `remote_data_sources.mustache` barrel export

## 4. Configuration Templates

- [ ] 4.1 Create `acdc_config.mustache` with main config class
- [ ] 4.2 Create `auth_config.mustache` for authentication settings
- [ ] 4.3 Create `cache_config.mustache` for caching settings
- [ ] 4.4 Create `log_config.mustache` for logging settings
- [ ] 4.5 Create `offline_config.mustache` for offline support settings
- [ ] 4.6 Create `security_config.mustache` for certificate pinning
- [ ] 4.7 Create `config.mustache` barrel export

## 5. API Client Template

- [ ] 5.1 Create `api_client.mustache` with `createDio()` factory
- [ ] 5.2 Handle conditional feature inclusion based on config
- [ ] 5.3 Add proper imports for Dart-ACDC types

## 6. Supporting File Templates

- [ ] 6.1 Create `pubspec.mustache` with all dependencies
- [ ] 6.2 Create `analysis_options.mustache` with linting rules
- [ ] 6.3 Create `README.mustache` with usage documentation
- [ ] 6.4 Create `gitignore.mustache` for generated .g.dart files
- [ ] 6.5 Create main barrel export template (`lib/{pubName}.dart`)

## 7. Codegen Class Updates

- [ ] 7.1 Register all template files in `DartAcdcClientCodegen`
- [ ] 7.2 Add supporting files list
- [ ] 7.3 Configure file naming conventions

## 8. Verification

- [ ] 8.1 Generate Petstore client
- [ ] 8.2 Run `dart analyze` on generated code (no errors)
- [ ] 8.3 Run `dart pub get` successfully
- [ ] 8.4 Run `dart run build_runner build` for json_serializable
- [ ] 8.5 Verify all barrel exports work correctly
- [ ] 8.6 Manual review of generated code quality
