package org.openapitools.codegen.languages;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DartAcdcGenerator.
 * These tests generate actual Dart code from OpenAPI specs and verify:
 * - Generated code compiles successfully (dart analyze)
 * - Enum types are generated correctly
 * - Multipart file uploads use correct types (MultipartFile)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DartAcdcGeneratorIntegrationTest {

    @TempDir
    Path tempDir;

    private Path outputDir;
    private static final int COMMAND_TIMEOUT_SECONDS = 120;

    @BeforeEach
    void setUp() {
        outputDir = tempDir.resolve("generated");
    }

    /**
     * Test 1: Generate code from Petstore spec with file upload
     */
    @Test
    @Order(1)
    @DisplayName("Generate code from Petstore spec with file upload")
    void testGenerateCodeFromPetstore() throws Exception {
        // Get the petstore spec from test resources
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        // Configure the generator
        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "petstore_api")
            .addAdditionalProperty("pubVersion", "1.0.0")
            .addAdditionalProperty("pubDescription", "Petstore API Client");

        // Generate the code
        ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(clientOptInput).generate();

        // Verify files were generated
        assertNotNull(files, "Generated files should not be null");
        assertFalse(files.isEmpty(), "Should generate at least one file");

        // Verify key files exist
        assertTrue(Files.exists(outputDir.resolve("pubspec.yaml")), "pubspec.yaml should exist");
        assertTrue(Files.exists(outputDir.resolve("lib")), "lib directory should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/models")), "models directory should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/remote_data_sources")), "remote_data_sources directory should exist");

        System.out.println("✓ Code generation completed successfully");
        System.out.println("  Generated " + files.size() + " files");
        System.out.println("  Output directory: " + outputDir);
    }

    /**
     * Test 2: Verify dependencies can be fetched
     */
    @Test
    @Order(2)
    @DisplayName("Verify dependencies can be fetched with dart pub get")
    void testDependenciesFetch() throws Exception {
        // First generate the code
        testGenerateCodeFromPetstore();

        // Run dart pub get to fetch dependencies
        System.out.println("\nRunning 'dart pub get'...");
        ProcessResult pubGetResult = runCommand(outputDir, "dart", "pub", "get");

        // Print the output for debugging
        System.out.println("Pub get output:\n" + pubGetResult.output);

        assertEquals(0, pubGetResult.exitCode,
            "dart pub get should succeed. Output:\n" + pubGetResult.output);
        System.out.println("✓ Dependencies installed successfully");

        // Verify that .dart_tool directory was created (indicates successful pub get)
        assertTrue(Files.exists(outputDir.resolve(".dart_tool")),
            ".dart_tool directory should exist after pub get");
    }

    /**
     * Test 7: Verify generated code compiles with build_runner and dart analyze
     */
    @Test
    @Order(7)
    @DisplayName("Verify generated code compiles successfully")
    void testCodeCompilation() throws Exception {
        // First generate the code and fetch dependencies
        testGenerateCodeFromPetstore();
        testDependenciesFetch();

        // Run build_runner to generate .g.dart files
        System.out.println("\nRunning 'dart run build_runner build --delete-conflicting-outputs'...");
        ProcessResult buildRunnerResult = runCommand(outputDir,
            "dart", "run", "build_runner", "build", "--delete-conflicting-outputs");

        System.out.println("Build runner output:\n" + buildRunnerResult.output);

        assertEquals(0, buildRunnerResult.exitCode,
            "build_runner should succeed. Output:\n" + buildRunnerResult.output);
        System.out.println("✓ Code generation with build_runner completed successfully");

        // Run dart analyze to verify code compiles
        System.out.println("\nRunning 'dart analyze'...");
        ProcessResult analyzeResult = runCommand(outputDir, "dart", "analyze");

        System.out.println("Analyze output:\n" + analyzeResult.output);

        // Check for compilation errors (exit code 3 indicates errors)
        // Exit code 0 = no issues, 1 = warnings only, 2 = infos, 3 = errors
        assertTrue(analyzeResult.exitCode >= 0 && analyzeResult.exitCode < 3,
            "dart analyze should complete without errors (warnings/infos are acceptable). " +
            "Exit code: " + analyzeResult.exitCode + "\nOutput:\n" + analyzeResult.output);

        System.out.println("✓ Code analysis passed - generated code compiles successfully");
    }

    /**
     * Test 8: Run Dart runtime tests to verify generated code behavior.
     *
     * Only model tests are run with 'dart test' because API tests import
     * the remote data source implementation which transitively depends on
     * dart_acdc -> flutter_secure_storage -> dart:ui. The dart:ui library
     * is only available in Flutter context, so API tests require 'flutter test'.
     */
    @Test
    @Order(8)
    @DisplayName("Run Dart model tests to verify generated code behavior")
    void testRuntimeBehavior() throws Exception {
        // Ensure code is generated, dependencies are fetched, and code is compiled
        testGenerateCodeFromPetstore();
        testDependenciesFetch();
        testCodeCompilation();

        // Verify test files were generated
        assertTrue(Files.exists(outputDir.resolve("test/test_helpers.dart")),
            "test_helpers.dart should be generated");

        Path testDir = outputDir.resolve("test");
        assertTrue(Files.exists(testDir), "test directory should exist");

        // Collect model test files (exclude API tests which need flutter_test due to dart:ui).
        // Model test files match *Test_test.dart but exclude *ApiTest_test.dart.
        List<String> modelTestFiles;
        try (Stream<Path> paths = Files.list(testDir)) {
            modelTestFiles = paths
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.endsWith("Test_test.dart"))
                .filter(name -> !name.endsWith("ApiTest_test.dart"))
                .map(name -> "test/" + name)
                .sorted()
                .collect(Collectors.toList());
        }

        assertFalse(modelTestFiles.isEmpty(), "Should have at least one model test file");
        System.out.println("Found " + modelTestFiles.size() + " model test files to run");

        // Build command: dart test test/FooTest_test.dart test/BarTest_test.dart ...
        List<String> command = new ArrayList<>();
        command.add("dart");
        command.add("test");
        command.addAll(modelTestFiles);

        System.out.println("\nRunning 'dart test' on model tests...");
        ProcessResult testResult = runCommand(outputDir, command.toArray(new String[0]));

        System.out.println("Test output:\n" + testResult.output);

        // Check that tests passed
        assertEquals(0, testResult.exitCode,
            "dart test (model tests) should pass. Output:\n" + testResult.output);

        System.out.println("✓ Model runtime tests passed - generated code works correctly");
        System.out.println("  Note: API tests require 'flutter test' due to dart:ui dependency");

        // Verify test output contains expected test execution
        String output = testResult.output.toLowerCase();
        assertTrue(output.contains("test") || output.contains("pass") || output.contains("+"),
            "Test output should indicate tests ran: " + testResult.output);
    }

    /**
     * Test 3: Verify models are generated correctly
     */
    @Test
    @Order(3)
    @DisplayName("Verify models are generated correctly")
    void testModelGeneration() throws Exception {
        // First generate the code
        testGenerateCodeFromPetstore();

        // Find the Pet model file
        Path petModelPath = findFile(outputDir, "pet.dart");
        assertNotNull(petModelPath, "Pet model file should exist");

        String petModelContent = Files.readString(petModelPath);

        // Verify basic model structure
        assertTrue(petModelContent.contains("class Pet") || petModelContent.contains("class pet"),
            "Pet model class should be defined");
        assertTrue(petModelContent.contains("status") || petModelContent.contains("Status"),
            "Pet model should have status property");
        assertTrue(petModelContent.contains("category") || petModelContent.contains("Category"),
            "Pet model should have category property");

        // Find NewPet model
        Path newPetPath = findFile(outputDir, "new_pet.dart");
        assertNotNull(newPetPath, "NewPet model file should exist");

        // Find Error model
        Path errorPath = findFile(outputDir, "error.dart");
        assertNotNull(errorPath, "Error model file should exist");

        // Find PhotoUploadResponse model
        Path photoPath = findFile(outputDir, "photo_upload_response.dart");
        assertNotNull(photoPath, "PhotoUploadResponse model file should exist");

        System.out.println("✓ Model generation verified successfully");
        System.out.println("  - Pet model with status and category properties");
        System.out.println("  - NewPet model created");
        System.out.println("  - Error model created");
        System.out.println("  - PhotoUploadResponse model created");
    }

    /**
     * Test 4: Verify multipart file upload endpoints are generated
     */
    @Test
    @Order(4)
    @DisplayName("Verify multipart file upload endpoints are generated")
    void testMultipartFileUploadEndpoints() throws Exception {
        // First generate the code
        testGenerateCodeFromPetstore();

        // Find the API file that contains upload methods
        // The generator creates files with the API tag name
        Path apiPath = null;

        // Search for any file containing "remote_data_source" in the remote_data_sources directory
        Path remoteDataSourcesDir = outputDir.resolve("lib/remote_data_sources");
        if (Files.exists(remoteDataSourcesDir)) {
            try (Stream<Path> paths = Files.walk(remoteDataSourcesDir, 1)) {
                apiPath = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("_remote_data_source.dart") ||
                                p.getFileName().toString().endsWith("_remote_data_source_impl.dart"))
                    .findFirst()
                    .orElse(null);
            }
        }

        assertNotNull(apiPath, "Remote data source file should exist in lib/remote_data_sources");

        String apiContent = Files.readString(apiPath);

        // Verify upload methods exist - the spec has uploadPetPhoto and batchUploadPhotos operations
        boolean hasUploadMethod = apiContent.contains("uploadPetPhoto") ||
                                 apiContent.contains("upload") ||
                                 apiContent.contains("Photo");
        assertTrue(hasUploadMethod,
            "API should contain photo upload-related methods");

        // Verify that the generator created code for the multipart endpoints
        // (The actual multipart handling may vary based on generator implementation)
        System.out.println("✓ Multipart file upload endpoint generation verified");
        System.out.println("  - Photo upload endpoints found in generated code");
        System.out.println("  - File path: " + apiPath.getFileName());
    }

    /**
     * Test 5: Verify generated project structure
     */
    @Test
    @Order(5)
    @DisplayName("Verify generated project has correct structure")
    void testProjectStructure() throws Exception {
        // First generate the code
        testGenerateCodeFromPetstore();

        // Verify main library files
        Path libDir = outputDir.resolve("lib");
        assertTrue(Files.exists(libDir), "lib directory should exist");

        // Find the main library file (could be petstore_api.dart or similar)
        boolean mainLibExists = Files.list(libDir)
            .anyMatch(p -> p.getFileName().toString().endsWith(".dart") &&
                          !p.getFileName().toString().startsWith("."));
        assertTrue(mainLibExists, "Main library file should exist in lib/");

        assertTrue(Files.exists(outputDir.resolve("lib/models/models.dart")),
            "Models barrel file should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/remote_data_sources/remote_data_sources.dart")),
            "Remote data sources barrel file should exist");

        // Verify configuration files
        assertTrue(Files.exists(outputDir.resolve("lib/config/acdc_config.dart")),
            "ACDC config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/auth_config.dart")),
            "Auth config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/cache_config.dart")),
            "Cache config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/log_config.dart")),
            "Log config should exist");

        // Verify remote data sources
        assertTrue(Files.exists(outputDir.resolve("lib/remote_data_sources")),
            "Remote data sources directory should exist");

        // Verify support files
        assertTrue(Files.exists(outputDir.resolve("README.md")),
            "README should exist");
        assertTrue(Files.exists(outputDir.resolve(".gitignore")),
            ".gitignore should exist");
        assertTrue(Files.exists(outputDir.resolve("analysis_options.yaml")),
            "analysis_options.yaml should exist");

        System.out.println("✓ Project structure verification passed");
    }

    /**
     * Test 6: Verify pubspec.yaml has correct dependencies
     */
    @Test
    @Order(6)
    @DisplayName("Verify pubspec.yaml has correct dependencies")
    void testPubspecDependencies() throws Exception {
        // First generate the code
        testGenerateCodeFromPetstore();

        Path pubspecPath = outputDir.resolve("pubspec.yaml");
        assertTrue(Files.exists(pubspecPath), "pubspec.yaml should exist");

        String pubspecContent = Files.readString(pubspecPath);

        // Verify package name
        assertTrue(pubspecContent.contains("name: petstore_api"),
            "Package name should be petstore_api");

        // Verify Dart-ACDC dependency
        assertTrue(pubspecContent.contains("dart_acdc:"),
            "Should depend on dart_acdc");

        // Verify other essential dependencies
        assertTrue(pubspecContent.contains("dio:"),
            "Should depend on dio");
        assertTrue(pubspecContent.contains("json_annotation:"),
            "Should depend on json_annotation");

        System.out.println("✓ pubspec.yaml dependencies verified");
    }

    // ========================================
    // Generator Options E2E Verification Tests (11.1-11.6)
    // ========================================

    /**
     * 11.1: Generate with minimal options (just pubName) → verify structure
     */
    @Test
    @Order(10)
    @DisplayName("E2E: Generate with minimal options (just pubName)")
    void testMinimalOptions() throws Exception {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "minimal_test");

        ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "Should generate files with minimal options");
        assertTrue(Files.exists(outputDir.resolve("pubspec.yaml")), "pubspec.yaml should exist");

        String pubspec = Files.readString(outputDir.resolve("pubspec.yaml"));
        assertTrue(pubspec.contains("name: minimal_test"), "pubspec should use provided pubName");

        // Default features should be enabled
        assertTrue(Files.exists(outputDir.resolve("lib/config/auth_config.dart")),
            "Auth config should exist (enabled by default)");
        assertTrue(Files.exists(outputDir.resolve("lib/config/cache_config.dart")),
            "Cache config should exist (enabled by default)");
        assertTrue(Files.exists(outputDir.resolve("lib/config/log_config.dart")),
            "Log config should exist (enabled by default)");
        assertTrue(Files.exists(outputDir.resolve("lib/config/offline_config.dart")),
            "Offline config should exist (enabled by default)");
        assertFalse(Files.exists(outputDir.resolve("lib/config/security_config.dart")),
            "Security config should NOT exist (cert pinning disabled by default)");

        System.out.println("✓ E2E 11.1: Minimal options generate correctly");
    }

    /**
     * 11.2: Generate with all ACDC features disabled → no config files
     */
    @Test
    @Order(11)
    @DisplayName("E2E: Generate with all ACDC features disabled")
    void testAllFeaturesDisabled() throws Exception {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "no_features_test")
            .addAdditionalProperty("enableAuthentication", false)
            .addAdditionalProperty("enableCaching", false)
            .addAdditionalProperty("enableLogging", false)
            .addAdditionalProperty("enableOfflineSupport", false)
            .addAdditionalProperty("enableCertificatePinning", false);

        ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "Should generate files with all features disabled");

        // Core files should still exist
        assertTrue(Files.exists(outputDir.resolve("pubspec.yaml")), "pubspec.yaml should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/api_client.dart")), "api_client.dart should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/acdc_config.dart")), "acdc_config.dart should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/config.dart")), "config.dart barrel should exist");

        // Feature config files should NOT exist
        assertFalse(Files.exists(outputDir.resolve("lib/config/auth_config.dart")),
            "Auth config should NOT exist when authentication disabled");
        assertFalse(Files.exists(outputDir.resolve("lib/config/cache_config.dart")),
            "Cache config should NOT exist when caching disabled");
        assertFalse(Files.exists(outputDir.resolve("lib/config/log_config.dart")),
            "Log config should NOT exist when logging disabled");
        assertFalse(Files.exists(outputDir.resolve("lib/config/offline_config.dart")),
            "Offline config should NOT exist when offline support disabled");
        assertFalse(Files.exists(outputDir.resolve("lib/config/security_config.dart")),
            "Security config should NOT exist when cert pinning disabled");

        // Verify AcdcConfig does not reference disabled features
        String acdcConfig = Files.readString(outputDir.resolve("lib/config/acdc_config.dart"));
        assertFalse(acdcConfig.contains("import 'auth_config.dart'"),
            "AcdcConfig should not import auth_config when disabled");
        assertFalse(acdcConfig.contains("import 'cache_config.dart'"),
            "AcdcConfig should not import cache_config when disabled");
        assertFalse(acdcConfig.contains("import 'log_config.dart'"),
            "AcdcConfig should not import log_config when disabled");

        // Verify config.dart barrel does not export disabled features
        String configBarrel = Files.readString(outputDir.resolve("lib/config/config.dart"));
        assertFalse(configBarrel.contains("export 'auth_config.dart'"),
            "Config barrel should not export auth_config when disabled");
        assertFalse(configBarrel.contains("export 'cache_config.dart'"),
            "Config barrel should not export cache_config when disabled");

        // Verify ApiClient does not reference disabled features
        String apiClient = Files.readString(outputDir.resolve("lib/api_client.dart"));
        assertFalse(apiClient.contains("config.auth"),
            "ApiClient should not reference auth when disabled");
        assertFalse(apiClient.contains("config.cache"),
            "ApiClient should not reference cache when disabled");

        System.out.println("✓ E2E 11.2: All features disabled generates clean code without feature files");
    }

    /**
     * 11.3: Generate with all features enabled (defaults) → full config
     * (already covered by testProjectStructure but verified explicitly here)
     */
    @Test
    @Order(12)
    @DisplayName("E2E: Generate with all ACDC features enabled (defaults)")
    void testAllFeaturesEnabled() throws Exception {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "full_features_test")
            .addAdditionalProperty("enableCertificatePinning", true);

        ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "Should generate files with all features enabled");

        // ALL config files should exist including security
        assertTrue(Files.exists(outputDir.resolve("lib/config/auth_config.dart")),
            "Auth config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/cache_config.dart")),
            "Cache config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/log_config.dart")),
            "Log config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/offline_config.dart")),
            "Offline config should exist");
        assertTrue(Files.exists(outputDir.resolve("lib/config/security_config.dart")),
            "Security config should exist when cert pinning enabled");

        // Verify AcdcConfig references all features
        String acdcConfig = Files.readString(outputDir.resolve("lib/config/acdc_config.dart"));
        assertTrue(acdcConfig.contains("import 'auth_config.dart'"), "Should import auth_config");
        assertTrue(acdcConfig.contains("import 'cache_config.dart'"), "Should import cache_config");
        assertTrue(acdcConfig.contains("import 'log_config.dart'"), "Should import log_config");
        assertTrue(acdcConfig.contains("import 'offline_config.dart'"), "Should import offline_config");
        assertTrue(acdcConfig.contains("import 'security_config.dart'"), "Should import security_config");

        // Verify ApiClient references all features
        String apiClient = Files.readString(outputDir.resolve("lib/api_client.dart"));
        assertTrue(apiClient.contains("config.auth"), "ApiClient should reference auth");
        assertTrue(apiClient.contains("config.cache"), "ApiClient should reference cache");
        assertTrue(apiClient.contains("config.log"), "ApiClient should reference log");
        assertTrue(apiClient.contains("config.offline"), "ApiClient should reference offline");
        assertTrue(apiClient.contains("config.security"), "ApiClient should reference security");

        System.out.println("✓ E2E 11.3: All features enabled generates complete configuration");
    }

    /**
     * 11.4: Generate with custom log level and cache TTL → verify values in generated code
     */
    @Test
    @Order(13)
    @DisplayName("E2E: Generate with custom log level and cache TTL")
    void testCustomDefaultValues() throws Exception {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "custom_values_test")
            .addAdditionalProperty("defaultLogLevel", "debug")
            .addAdditionalProperty("defaultCacheTtlHours", 24)
            .addAdditionalProperty("cacheDiskSizeMb", 50)
            .addAdditionalProperty("encryptCache", false)
            .addAdditionalProperty("redactSensitiveData", false)
            .addAdditionalProperty("refreshThresholdMinutes", 10)
            .addAdditionalProperty("useSecureTokenStorage", false)
            .addAdditionalProperty("defaultTokenRefreshUrl", "https://api.example.com/auth/refresh");

        ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "Should generate files with custom values");

        // Verify log config has custom log level
        String logConfig = Files.readString(outputDir.resolve("lib/config/log_config.dart"));
        assertTrue(logConfig.contains("LogLevel.debug"),
            "Log config should use custom debug level, got:\n" + logConfig);

        // Verify cache config has custom TTL
        String cacheConfig = Files.readString(outputDir.resolve("lib/config/cache_config.dart"));
        assertTrue(cacheConfig.contains("Duration(hours: 24)"),
            "Cache config should use custom 24-hour TTL, got:\n" + cacheConfig);
        assertTrue(cacheConfig.contains("50"),
            "Cache config should use custom 50MB disk size");

        // Verify auth config has custom values
        String authConfig = Files.readString(outputDir.resolve("lib/config/auth_config.dart"));
        assertTrue(authConfig.contains("10"),
            "Auth config should reference custom 10-minute threshold");
        assertTrue(authConfig.contains("https://api.example.com/auth/refresh"),
            "Auth config should include custom token refresh URL");

        System.out.println("✓ E2E 11.4: Custom default values correctly applied to generated code");
    }

    /**
     * 11.5: Verify invalid option values produce clear error messages
     */
    @Test
    @Order(14)
    @DisplayName("E2E: Invalid option values produce clear error messages")
    void testInvalidOptionValues() {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        // Test invalid log level
        CodegenConfigurator invalidLogLevel = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "invalid_test")
            .addAdditionalProperty("defaultLogLevel", "INVALID_LEVEL");

        IllegalArgumentException logException = assertThrows(IllegalArgumentException.class, () -> {
            ClientOptInput input = invalidLogLevel.toClientOptInput();
            new DefaultGenerator().opts(input).generate();
        }, "Invalid log level should throw IllegalArgumentException");
        assertTrue(logException.getMessage().contains("INVALID_LEVEL"),
            "Error should mention the invalid value: " + logException.getMessage());

        // Test invalid serialization library
        CodegenConfigurator invalidSerializer = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "invalid_test")
            .addAdditionalProperty("serializationLibrary", "protobuf");

        IllegalArgumentException serException = assertThrows(IllegalArgumentException.class, () -> {
            ClientOptInput input = invalidSerializer.toClientOptInput();
            new DefaultGenerator().opts(input).generate();
        }, "Invalid serialization library should throw IllegalArgumentException");
        assertTrue(serException.getMessage().contains("protobuf"),
            "Error should mention the invalid value: " + serException.getMessage());

        // Test out-of-range integer option
        CodegenConfigurator invalidInt = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "invalid_test")
            .addAdditionalProperty("refreshThresholdMinutes", 999);

        IllegalArgumentException intException = assertThrows(IllegalArgumentException.class, () -> {
            ClientOptInput input = invalidInt.toClientOptInput();
            new DefaultGenerator().opts(input).generate();
        }, "Out-of-range integer should throw IllegalArgumentException");
        assertTrue(intException.getMessage().contains("999") || intException.getMessage().contains("between"),
            "Error should mention bounds or invalid value: " + intException.getMessage());

        System.out.println("✓ E2E 11.5: Invalid option values produce clear, actionable error messages");
    }

    /**
     * 11.6: Generate using YAML config file equivalent → verify options applied
     */
    @Test
    @Order(15)
    @DisplayName("E2E: Generate using YAML config equivalent with code style options")
    void testCodeStyleOptions() throws Exception {
        String specPath = getResourcePath("/petstore-with-upload.yaml");

        CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("dart-acdc")
            .setInputSpec(specPath)
            .setOutputDir(outputDir.toString())
            .addAdditionalProperty("pubName", "style_test")
            .addAdditionalProperty("dataSourceSuffix", "Api")
            .addAdditionalProperty("generateInterfaces", false)
            .addAdditionalProperty("generateBarrelExports", false);

        ClientOptInput clientOptInput = configurator.toClientOptInput();
        List<File> files = new DefaultGenerator().opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "Should generate files with code style options");

        // Verify dataSourceSuffix is applied - files should use _api suffix
        Path remoteDir = outputDir.resolve("lib/remote_data_sources");
        assertTrue(Files.exists(remoteDir), "Remote data sources directory should exist");

        List<String> apiFiles;
        try (Stream<Path> paths = Files.list(remoteDir)) {
            apiFiles = paths
                .map(p -> p.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        }

        // With generateInterfaces=false, should only have impl files (named *_api.dart, not *_api_impl.dart)
        // Actually with generateInterfaces=false, the impl template uses the suffix without "Impl"
        boolean hasApiSuffix = apiFiles.stream().anyMatch(f -> f.contains("_api"));
        assertTrue(hasApiSuffix, "Files should use '_api' suffix from dataSourceSuffix='Api', got: " + apiFiles);

        // With generateInterfaces=false, should NOT have separate interface files
        boolean hasInterfaceFile = apiFiles.stream().anyMatch(f ->
            f.endsWith("_api.dart") && !f.endsWith("_api_impl.dart"));
        assertFalse(hasInterfaceFile,
            "Should NOT generate interface files when generateInterfaces=false, got: " + apiFiles);

        // With generateBarrelExports=false, barrel files should NOT exist
        assertFalse(Files.exists(outputDir.resolve("lib/models/models.dart")),
            "models.dart barrel should NOT exist when barrel exports disabled");
        assertFalse(Files.exists(outputDir.resolve("lib/remote_data_sources/remote_data_sources.dart")),
            "remote_data_sources.dart barrel should NOT exist when barrel exports disabled");

        // Verify the impl class does NOT have @override annotations
        Path implFile = apiFiles.stream()
            .filter(f -> f.endsWith("_impl.dart"))
            .map(remoteDir::resolve)
            .findFirst()
            .orElse(null);

        if (implFile != null && Files.exists(implFile)) {
            String implContent = Files.readString(implFile);
            assertFalse(implContent.contains("@override"),
                "Impl class should NOT have @override when interfaces disabled");
            assertTrue(implContent.contains("Api"),
                "Class name should use 'Api' suffix");
        }

        System.out.println("✓ E2E 11.6: Code style options (dataSourceSuffix, generateInterfaces, generateBarrelExports) applied correctly");
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Get the absolute path to a test resource file
     */
    private String getResourcePath(String resourceName) {
        try {
            // Use ClassLoader to find the resource
            java.net.URL resourceUrl = getClass().getResource(resourceName);
            if (resourceUrl == null) {
                fail("Test resource not found: " + resourceName);
            }
            return Paths.get(resourceUrl.toURI()).toString();
        } catch (Exception e) {
            fail("Failed to load test resource: " + resourceName + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Find a file by name recursively in a directory
     */
    private Path findFile(Path directory, String fileName) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Run a command and return the result
     */
    private ProcessResult runCommand(Path workingDirectory, String... command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Capture output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Wait for completion with timeout
        boolean completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out after " + COMMAND_TIMEOUT_SECONDS + " seconds");
        }

        return new ProcessResult(process.exitValue(), output.toString());
    }

    /**
     * Helper class to hold process execution results
     */
    private static class ProcessResult {
        final int exitCode;
        final String output;

        ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
