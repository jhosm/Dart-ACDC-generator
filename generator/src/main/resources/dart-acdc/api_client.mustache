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
  /// final dio = await ApiClient.createDio(config);
  /// ```
  static Future<Dio> createDio(AcdcConfig config) async {
    var builder = acdc.AcdcClientBuilder()
      .withBaseUrl(config.baseUrl);

    // Conditionally add features based on config
    if (config.auth != null) {
      builder = builder.withTokenRefreshEndpoint(
        url: config.auth!.tokenRefreshUrl,
        clientId: config.auth!.clientId ?? '',
      );
    }

    if (config.cache != null) {
      // Map our CacheConfig to dart_acdc's CacheConfig
      final acdcCacheConfig = acdc.CacheConfig(
        ttl: config.cache!.ttl,
        maxSize: config.cache!.maxDiskCacheSizeMB * 1024 * 1024,
        inMemoryMaxSize: config.cache!.maxMemoryCacheSizeMB * 1024 * 1024,
        cacheAuthenticatedRequests: true,
        inMemory: true,
      );
      builder = builder.withCache(acdcCacheConfig);
    }

    if (config.log != null) {
      builder = builder.withLogLevel(config.log!.level);
      if (config.log!.redactSensitiveData) {
        builder = builder.withSensitiveFields(['password', 'token', 'secret', 'authorization']);
      }
    }

    if (config.offline != null) {
      builder = builder.withOfflineDetection(
        failFast: config.offline!.failFast,
      );
    }

    if (config.security != null && config.security!.certificateFingerprints.isNotEmpty) {
      // Map our SecurityConfig to dart_acdc's CertificatePinningConfig
      // Group all fingerprints under the base URL's domain
      final domain = Uri.parse(config.baseUrl).host;
      final pinningConfig = acdc.CertificatePinningConfig(
        allowedPins: {
          domain: config.security!.certificateFingerprints
            .map((fp) => fp.startsWith('SHA256:') ? fp : 'SHA256:$fp')
            .toList(),
        },
        reportOnly: config.security!.allowSelfSigned,
      );
      builder = builder.withCertificatePinning(pinningConfig);
    }

    return await builder.build();
  }
}
