// AUTO-GENERATED FILE - DO NOT EDIT
import 'auth_config.dart';
import 'cache_config.dart';
import 'log_config.dart';
import 'offline_config.dart';
import 'security_config.dart';

/// Main ACDC configuration class
///
/// Controls all aspects of the generated API client.
/// Set feature configs to null to disable that feature.
class AcdcConfig {
  /// Base URL for the API (required)
  final String baseUrl;

  /// Authentication configuration (optional, null = disabled)
  final AuthConfig? auth;

  /// Caching configuration (optional, null = disabled)
  final CacheConfig? cache;

  /// Logging configuration (optional, null = disabled)
  final LogConfig? log;

  /// Offline support configuration (optional, null = disabled)
  final OfflineConfig? offline;

  /// Security configuration for certificate pinning (optional, null = disabled)
  final SecurityConfig? security;

  AcdcConfig({
    required this.baseUrl,
    this.auth,
    this.cache,
    this.log,
    this.offline,
    this.security,
  });
}
