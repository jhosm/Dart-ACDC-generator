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
      if (config.auth!.customTokenProvider != null) {
        builder = builder.withCustomTokenProvider(config.auth!.customTokenProvider!);
      } else {
        builder = builder.withAuthentication(
          tokenRefreshUrl: config.auth!.tokenRefreshUrl,
          clientId: config.auth!.clientId,
          clientSecret: config.auth!.clientSecret,
          refreshThreshold: Duration(seconds: config.auth!.refreshThreshold),
        );
      }
    }

    if (config.cache != null) {
      builder = builder.withCache(
        ttl: config.cache!.ttl,
        maxDiskSize: config.cache!.maxDiskCacheSizeMB * 1024 * 1024,
        maxMemorySize: config.cache!.maxMemoryCacheSizeMB * 1024 * 1024,
        encrypt: config.cache!.encryptCache,
        cacheAuthenticatedRequests: true,
      );
    }

    if (config.log != null) {
      builder = builder.withLogging(
        level: config.log!.level,
        sensitiveFields: config.log!.redactSensitiveData
            ? const ['password', 'token', 'secret', 'authorization']
            : null,
        delegate: config.log!.customLogger,
      );
    }

    if (config.offline != null) {
      builder = builder.withOfflineDetection(
        failFast: config.offline!.failFast,
      );
    }

    if (config.security != null && config.security!.certificateFingerprints.isNotEmpty) {
      final domain = Uri.parse(config.baseUrl).host;
      final pinningConfig = acdc.CertificatePinningConfig(
        pins: {
          domain: config.security!.certificateFingerprints
            .map((fp) => fp.startsWith('sha256/') ? fp : 'sha256/$fp')
            .toList(),
        },
        enforced: !config.security!.reportOnlyMode,
      );
      builder = builder.withCertificatePinning(config: pinningConfig);
    }

    return await builder.build();
  }
}
