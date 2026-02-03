// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dart_acdc/dart_acdc.dart' as acdc;

/// Authentication configuration for OAuth 2.1
class AuthConfig {
  /// URL for token refresh endpoint
  final String tokenRefreshUrl;

  /// OAuth client ID (optional)
  final String? clientId;

  /// OAuth client secret (optional)
  final String? clientSecret;

  /// Token refresh threshold in seconds (default: 300 = 5 minutes before expiry)
  final int refreshThreshold;

  /// Use secure token storage (default: true)
  final bool useSecureStorage;

  /// Custom token provider (optional, for advanced use cases)
  ///
  /// When set, this replaces the default token storage with a custom
  /// implementation of dart_acdc's [acdc.TokenProvider] interface.
  final acdc.TokenProvider? customTokenProvider;

  AuthConfig({
    this.tokenRefreshUrl = 'https://api.example.com/auth/refresh',
    this.clientId,
    this.clientSecret,
    this.refreshThreshold = 10 * 60, // Convert minutes to seconds
    this.useSecureStorage = false,
    this.customTokenProvider,
  });
}
