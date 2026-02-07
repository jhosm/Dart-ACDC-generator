// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart' as acdc;
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
  ///
  /// Example:
  /// ```dart
  /// final config = AcdcConfig(
  ///   baseUrl: 'https://api.example.com',
  ///   auth: AuthConfig(tokenRefreshUrl: 'https://api.example.com/auth/refresh'),
  ///   cache: CacheConfig(ttl: Duration(hours: 1)),
  ///   log: LogConfig(level: LogLevel.info),
  /// );
  /// final dio = await ApiClient.createDio(config);
  /// ```
  static Future<Dio> createDio(AcdcConfig config) async {
    var builder = acdc.AcdcClientBuilder()
      .withBaseUrl(config.baseUrl);

    // Conditionally add authentication based on config
    if (config.auth != null) {
      if (config.auth!.customTokenProvider != null) {
        builder = builder.withTokenProvider(config.auth!.customTokenProvider!);
      } else if (config.auth!.clientId != null) {
        builder = builder.withTokenRefreshEndpoint(
          url: config.auth!.tokenRefreshUrl,
          clientId: config.auth!.clientId!,
        );

        if (config.auth!.refreshThreshold > 0) {
          builder = builder.withTokenRefreshThreshold(
            Duration(seconds: config.auth!.refreshThreshold),
          );
        }
      }
    }

    if (config.cache != null) {
      builder = builder.withCache(
        acdc.CacheConfig(
          ttl: config.cache!.ttl,
          maxSize: config.cache!.maxDiskCacheSizeMB * 1024 * 1024,
          inMemoryMaxSize: config.cache!.maxMemoryCacheSizeMB * 1024 * 1024,
          cacheAuthenticatedRequests: true,
        ),
      );
    }

    if (config.log != null) {
      builder = builder.withLogLevel(config.log!.level);

      if (config.log!.redactSensitiveData) {
        builder = builder.withSensitiveFields(
          const ['password', 'token', 'secret', 'authorization'],
        );
      }

      if (config.log!.customLogger != null) {
        builder = builder.withLogDelegate(config.log!.customLogger!);
      }
    }

    if (config.offline != null) {
      builder = builder.withOfflineDetection(
        failFast: config.offline!.failFast,
      );
    }

    return await builder.build();
  }
}
