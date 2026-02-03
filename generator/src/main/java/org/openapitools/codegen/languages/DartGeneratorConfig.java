package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import org.openapitools.codegen.CliOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration management for DartAcdcGenerator.
 * Handles CLI option registration, validation, and processing.
 * Provides smart defaults derived from OpenAPI specification.
 */
public class DartGeneratorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DartGeneratorConfig.class);
    private static final String DEFAULT_PACKAGE_NAME = "openapi_client";

    // Package metadata
    private String pubName = DEFAULT_PACKAGE_NAME;
    private String pubVersion = "1.0.0";
    private String pubDescription;
    private String pubAuthor;
    private String pubHomepage;

    // ACDC feature toggles
    private boolean enableAuthentication = true;
    private boolean enableCaching = true;
    private boolean enableLogging = true;
    private boolean enableOfflineSupport = true;
    private boolean enableCertificatePinning = false;

    // Authentication default values
    private String defaultTokenRefreshUrl;
    private boolean useSecureTokenStorage = true;
    private int refreshThresholdMinutes = 5;

    // Cache default values
    private int defaultCacheTtlHours = 1;
    private int cacheDiskSizeMb = 20;
    private boolean encryptCache = true;
    private boolean enableUserCacheIsolation = true;

    // Logging default values
    private String defaultLogLevel = "LogLevel.info";
    private boolean redactSensitiveData = true;

    private final DartNameSanitizer nameSanitizer;

    /**
     * Creates a new configuration instance.
     *
     * @param nameSanitizer the name sanitizer for package name validation
     */
    public DartGeneratorConfig(DartNameSanitizer nameSanitizer) {
        this.nameSanitizer = nameSanitizer;
    }

    /**
     * Registers all CLI options for the generator.
     *
     * @return list of CLI options
     */
    public List<CliOption> registerCliOptions() {
        List<CliOption> options = new ArrayList<>();

        // Package metadata options
        options.add(CliOption.newString("pubName",
                "Package name for pubspec.yaml (derived from OpenAPI info.title if not provided)"));
        options.add(CliOption.newString("pubVersion",
                "Package version for pubspec.yaml (derived from OpenAPI info.version if not provided)")
                .defaultValue("1.0.0"));
        options.add(CliOption.newString("pubDescription",
                "Package description for pubspec.yaml (derived from OpenAPI info.description if not provided)"));
        options.add(CliOption.newString("pubAuthor",
                "Package author for pubspec.yaml"));
        options.add(CliOption.newString("pubHomepage",
                "Package homepage URL for pubspec.yaml"));

        // ACDC feature toggle options
        options.add(CliOption.newBoolean("enableAuthentication",
                "Enable OAuth 2.1 authentication with automatic token refresh",
                true));
        options.add(CliOption.newBoolean("enableCaching",
                "Enable two-tier caching (memory + disk) with encryption",
                true));
        options.add(CliOption.newBoolean("enableLogging",
                "Enable configurable logging with sensitive data redaction",
                true));
        options.add(CliOption.newBoolean("enableOfflineSupport",
                "Enable offline detection and support",
                true));
        options.add(CliOption.newBoolean("enableCertificatePinning",
                "Enable certificate pinning for enhanced security",
                false));

        // Authentication default value options
        options.add(CliOption.newString("defaultTokenRefreshUrl",
                "Default token refresh URL for authentication (e.g., https://api.example.com/auth/refresh)"));
        options.add(CliOption.newBoolean("useSecureTokenStorage",
                "Enable secure token storage using platform-specific secure storage")
                .defaultValue("true"));
        options.add(CliOption.newString("refreshThresholdMinutes",
                "Threshold in minutes before token expiration to trigger refresh")
                .defaultValue("5"));

        // Cache default value options
        options.add(CliOption.newString("defaultCacheTtlHours",
                "Default cache time-to-live in hours")
                .defaultValue("1"));
        options.add(CliOption.newString("cacheDiskSizeMb",
                "Maximum disk cache size in megabytes")
                .defaultValue("20"));
        options.add(CliOption.newBoolean("encryptCache",
                "Enable cache encryption using AES-256")
                .defaultValue("true"));
        options.add(CliOption.newBoolean("enableUserCacheIsolation",
                "Enable user-specific cache isolation")
                .defaultValue("true"));

        // Logging default value options
        CliOption logLevelOption = CliOption.newString("defaultLogLevel",
                "Default logging level (none, error, warning, info, debug, verbose)")
                .defaultValue("info");
        logLevelOption.setEnum(Map.of(
                "none", "LogLevel.none",
                "error", "LogLevel.error",
                "warning", "LogLevel.warning",
                "info", "LogLevel.info",
                "debug", "LogLevel.debug",
                "verbose", "LogLevel.verbose"
        ));
        options.add(logLevelOption);

        options.add(CliOption.newBoolean("redactSensitiveData",
                "Enable automatic redaction of sensitive data in logs")
                .defaultValue("true"));

        return options;
    }

    /**
     * Processes CLI options and applies configuration with smart defaults from OpenAPI spec.
     *
     * @param additionalProperties the properties map from generator
     * @param openAPI the OpenAPI specification
     */
    public void processOptions(Map<String, Object> additionalProperties, OpenAPI openAPI) {
        // Process package metadata with smart defaults
        processPubName(additionalProperties, openAPI);
        processPubVersion(additionalProperties, openAPI);
        processPubDescription(additionalProperties, openAPI);
        processPubAuthor(additionalProperties);
        processPubHomepage(additionalProperties);

        // Process ACDC feature toggles
        processFeatureToggles(additionalProperties);

        // Process ACDC default values
        processAuthenticationDefaults(additionalProperties);
        processCacheDefaults(additionalProperties);
        processLoggingDefaults(additionalProperties);
    }

    /**
     * Applies processed configuration to additionalProperties for template rendering.
     *
     * @param additionalProperties the properties map to update
     */
    public void applyToAdditionalProperties(Map<String, Object> additionalProperties) {
        // Package metadata
        additionalProperties.put("pubName", pubName);
        additionalProperties.put("pubVersion", pubVersion);
        additionalProperties.put("pubDescription", pubDescription);
        additionalProperties.put("pubAuthor", pubAuthor);
        additionalProperties.put("pubHomepage", pubHomepage);

        // ACDC feature toggles (must be Boolean objects for Mustache conditionals)
        additionalProperties.put("enableAuthentication", enableAuthentication);
        additionalProperties.put("enableCaching", enableCaching);
        additionalProperties.put("enableLogging", enableLogging);
        additionalProperties.put("enableOfflineSupport", enableOfflineSupport);
        additionalProperties.put("enableCertificatePinning", enableCertificatePinning);

        // Authentication defaults
        additionalProperties.put("defaultTokenRefreshUrl", defaultTokenRefreshUrl);
        additionalProperties.put("useSecureTokenStorage", useSecureTokenStorage);
        additionalProperties.put("refreshThresholdMinutes", refreshThresholdMinutes);

        // Cache defaults
        additionalProperties.put("defaultCacheTtlHours", defaultCacheTtlHours);
        additionalProperties.put("cacheDiskSizeMb", cacheDiskSizeMb);
        additionalProperties.put("encryptCache", encryptCache);
        additionalProperties.put("enableUserCacheIsolation", enableUserCacheIsolation);

        // Logging defaults
        additionalProperties.put("defaultLogLevel", defaultLogLevel);
        additionalProperties.put("redactSensitiveData", redactSensitiveData);
    }

    // Processing methods for each option group

    private void processPubName(Map<String, Object> additionalProperties, OpenAPI openAPI) {
        String value;
        if (additionalProperties.containsKey("pubName")) {
            value = (String) additionalProperties.get("pubName");
        } else if (openAPI != null && openAPI.getInfo() != null && openAPI.getInfo().getTitle() != null) {
            value = openAPI.getInfo().getTitle();
            LOGGER.info("Derived pubName from OpenAPI info.title: {}", value);
        } else {
            value = DEFAULT_PACKAGE_NAME;
        }

        // Sanitize and store
        this.pubName = nameSanitizer.sanitizePubName(value);
        LOGGER.info("Using pubName: {}", this.pubName);
    }

    private void processPubVersion(Map<String, Object> additionalProperties, OpenAPI openAPI) {
        if (additionalProperties.containsKey("pubVersion")) {
            this.pubVersion = (String) additionalProperties.get("pubVersion");
        } else if (openAPI != null && openAPI.getInfo() != null && openAPI.getInfo().getVersion() != null) {
            this.pubVersion = openAPI.getInfo().getVersion();
            LOGGER.info("Derived pubVersion from OpenAPI info.version: {}", this.pubVersion);
        }
        // else: keep default "1.0.0"
    }

    private void processPubDescription(Map<String, Object> additionalProperties, OpenAPI openAPI) {
        if (additionalProperties.containsKey("pubDescription")) {
            this.pubDescription = (String) additionalProperties.get("pubDescription");
        } else if (openAPI != null && openAPI.getInfo() != null && openAPI.getInfo().getDescription() != null) {
            this.pubDescription = openAPI.getInfo().getDescription();
            LOGGER.info("Derived pubDescription from OpenAPI info.description");
        }
    }

    private void processPubAuthor(Map<String, Object> additionalProperties) {
        if (additionalProperties.containsKey("pubAuthor")) {
            this.pubAuthor = (String) additionalProperties.get("pubAuthor");
        }
    }

    private void processPubHomepage(Map<String, Object> additionalProperties) {
        if (additionalProperties.containsKey("pubHomepage")) {
            this.pubHomepage = (String) additionalProperties.get("pubHomepage");
        }
    }

    private void processFeatureToggles(Map<String, Object> additionalProperties) {
        this.enableAuthentication = convertToBoolean(additionalProperties.get("enableAuthentication"), true);
        this.enableCaching = convertToBoolean(additionalProperties.get("enableCaching"), true);
        this.enableLogging = convertToBoolean(additionalProperties.get("enableLogging"), true);
        this.enableOfflineSupport = convertToBoolean(additionalProperties.get("enableOfflineSupport"), true);
        this.enableCertificatePinning = convertToBoolean(additionalProperties.get("enableCertificatePinning"), false);
    }

    private void processAuthenticationDefaults(Map<String, Object> additionalProperties) {
        this.defaultTokenRefreshUrl = processStringOption(additionalProperties, "defaultTokenRefreshUrl", null);
        this.useSecureTokenStorage = processBooleanOption(additionalProperties, "useSecureTokenStorage", true);
        this.refreshThresholdMinutes = processIntegerOption(additionalProperties, "refreshThresholdMinutes", 5, 1, 60);
    }

    private void processCacheDefaults(Map<String, Object> additionalProperties) {
        this.defaultCacheTtlHours = processIntegerOption(additionalProperties, "defaultCacheTtlHours", 1, 1, 720);
        this.cacheDiskSizeMb = processIntegerOption(additionalProperties, "cacheDiskSizeMb", 20, 1, 1024);
        this.encryptCache = processBooleanOption(additionalProperties, "encryptCache", true);
        this.enableUserCacheIsolation = processBooleanOption(additionalProperties, "enableUserCacheIsolation", true);
    }

    private void processLoggingDefaults(Map<String, Object> additionalProperties) {
        this.defaultLogLevel = processLogLevelOption(additionalProperties);
        this.redactSensitiveData = processBooleanOption(additionalProperties, "redactSensitiveData", true);
    }

    // Option processing utility methods

    private String processStringOption(Map<String, Object> additionalProperties, String optionName, String defaultValue) {
        String value;
        if (additionalProperties.containsKey(optionName)) {
            value = (String) additionalProperties.get(optionName);
        } else {
            value = defaultValue;
        }
        LOGGER.debug("Processed string option '{}': {}", optionName, value);
        return value;
    }

    private boolean processBooleanOption(Map<String, Object> additionalProperties, String optionName, boolean defaultValue) {
        boolean value = convertToBoolean(additionalProperties.get(optionName), defaultValue);
        LOGGER.debug("Processed boolean option '{}': {}", optionName, value);
        return value;
    }

    private int processIntegerOption(Map<String, Object> additionalProperties, String optionName,
                                     int defaultValue, int minValue, int maxValue) {
        int value = defaultValue;

        if (additionalProperties.containsKey(optionName)) {
            Object optionValue = additionalProperties.get(optionName);
            try {
                if (optionValue instanceof Integer) {
                    value = (Integer) optionValue;
                } else if (optionValue instanceof String) {
                    value = Integer.parseInt((String) optionValue);
                } else {
                    LOGGER.warn("Option '{}' has unexpected type: {}. Using default: {}",
                            optionName, optionValue.getClass().getName(), defaultValue);
                    value = defaultValue;
                }

                // Validate bounds
                if (value < minValue || value > maxValue) {
                    throw new IllegalArgumentException(
                            String.format("Option '%s' must be between %d and %d, got: %d",
                                    optionName, minValue, maxValue, value));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("Option '%s' must be a valid integer, got: %s", optionName, optionValue), e);
            }
        }

        LOGGER.info("Processed integer option '{}': {}", optionName, value);
        return value;
    }

    private String processLogLevelOption(Map<String, Object> additionalProperties) {
        final String optionName = "defaultLogLevel";
        final String defaultValue = "info";
        final Set<String> validLevels = Set.of("none", "error", "warning", "info", "debug", "verbose");

        String value = defaultValue;
        if (additionalProperties.containsKey(optionName)) {
            Object optionValue = additionalProperties.get(optionName);
            if (optionValue instanceof String) {
                value = ((String) optionValue).toLowerCase().trim();

                // Validate against allowed enum values
                if (!validLevels.contains(value)) {
                    throw new IllegalArgumentException(
                            String.format("Invalid value for option '%s': '%s'. Valid values are: %s",
                                    optionName, optionValue, String.join(", ", validLevels)));
                }
            } else {
                LOGGER.warn("Option '{}' has unexpected type: {}. Using default: {}",
                        optionName, optionValue.getClass().getName(), defaultValue);
                value = defaultValue;
            }
        }

        // Convert to Dart LogLevel enum format for templates
        String dartLogLevel = "LogLevel." + value;
        LOGGER.info("Processed log level option '{}': {}", optionName, dartLogLevel);
        return dartLogLevel;
    }

    private boolean convertToBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            String strValue = (String) value;
            if ("true".equalsIgnoreCase(strValue)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(strValue)) {
                return Boolean.FALSE;
            }
        }

        // If we can't parse the value, use default
        LOGGER.warn("Unable to convert '{}' to Boolean, using default: {}", value, defaultValue);
        return defaultValue;
    }

    // Getters

    public String getPubName() {
        return pubName;
    }

    public String getPubVersion() {
        return pubVersion;
    }

    public String getPubDescription() {
        return pubDescription;
    }

    public String getPubAuthor() {
        return pubAuthor;
    }

    public String getPubHomepage() {
        return pubHomepage;
    }

    public boolean isEnableAuthentication() {
        return enableAuthentication;
    }

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public boolean isEnableOfflineSupport() {
        return enableOfflineSupport;
    }

    public boolean isEnableCertificatePinning() {
        return enableCertificatePinning;
    }

    public String getDefaultTokenRefreshUrl() {
        return defaultTokenRefreshUrl;
    }

    public boolean isUseSecureTokenStorage() {
        return useSecureTokenStorage;
    }

    public int getRefreshThresholdMinutes() {
        return refreshThresholdMinutes;
    }

    public int getDefaultCacheTtlHours() {
        return defaultCacheTtlHours;
    }

    public int getCacheDiskSizeMb() {
        return cacheDiskSizeMb;
    }

    public boolean isEncryptCache() {
        return encryptCache;
    }

    public boolean isEnableUserCacheIsolation() {
        return enableUserCacheIsolation;
    }

    public String getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public boolean isRedactSensitiveData() {
        return redactSensitiveData;
    }
}
