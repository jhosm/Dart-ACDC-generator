// AUTO-GENERATED FILE - DO NOT EDIT

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

  /// Custom token provider function (optional, for advanced use cases)
  final Function? customTokenProvider;

  AuthConfig({
    required this.tokenRefreshUrl,
    this.clientId,
    this.clientSecret,
    this.refreshThreshold = 300,
    this.useSecureStorage = true,
    this.customTokenProvider,
  });
}
