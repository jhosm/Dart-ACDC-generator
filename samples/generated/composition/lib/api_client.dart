// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'config/config.dart';

/// Main API client factory
///
/// Provides factory methods for creating configured Dio instances with ACDC features.
class ApiClient {
  /// Creates a Dio instance configured with ACDC features based on the provided config.
  ///
  /// Features are enabled/disabled based on the presence of their config objects:
  /// - Authentication: enabled if config.auth is not null
  /// - Caching: enabled if config.cache is not null
  /// - Logging: enabled if config.log is not null
  /// - Offline detection: enabled if config.offline is not null
  /// - Certificate pinning: enabled if config.security is not null
  ///
  /// Example:
  /// ```dart
  /// final config = AcdcConfig(
  ///   baseUrl: 'https://api.example.com',
  ///   auth: AuthConfig(tokenRefreshUrl: 'https://api.example.com/auth/refresh'),
  ///   cache: CacheConfig(ttl: Duration(hours: 1)),
  ///   log: LogConfig(level: LogLevel.info),
  /// );
  /// final dio = ApiClient.createDio(config);
  /// ```
  static Dio createDio(AcdcConfig config) {
    final builder = AcdcClientBuilder()
      .withBaseUrl(config.baseUrl);

    // Conditionally add features based on config
    if (config.auth != null) {
      builder.withAuthentication(
        tokenRefreshUrl: config.auth!.tokenRefreshUrl,
        clientId: config.auth!.clientId,
        clientSecret: config.auth!.clientSecret,
        refreshThreshold: config.auth!.refreshThreshold,
        useSecureStorage: config.auth!.useSecureStorage,
      );
    }

    if (config.cache != null) {
      builder.withCache(
        ttl: config.cache!.ttl,
        enableDiskCache: config.cache!.enableDiskCache,
        encryptCache: config.cache!.encryptCache,
        maxMemoryCacheSizeMB: config.cache!.maxMemoryCacheSizeMB,
        maxDiskCacheSizeMB: config.cache!.maxDiskCacheSizeMB,
        userIsolation: config.cache!.userIsolation,
      );
    }

    if (config.log != null) {
      builder.withLogging(
        level: config.log!.level,
        redactSensitiveData: config.log!.redactSensitiveData,
      );
    }

    if (config.offline != null) {
      builder.withOfflineDetection(
        failFast: config.offline!.failFast,
      );
    }

    if (config.security != null) {
      builder.withCertificatePinning(
        certificateFingerprints: config.security!.certificateFingerprints,
        allowSelfSigned: config.security!.allowSelfSigned,
      );
    }

    return builder.build();
  }
}
