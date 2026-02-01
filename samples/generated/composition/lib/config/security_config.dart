// AUTO-GENERATED FILE - DO NOT EDIT

/// Security configuration for certificate pinning
///
/// Provide SHA-256 fingerprints of expected server certificates.
/// In enforced mode (default), connections fail if the certificate
/// doesn't match any pin. In report-only mode, mismatches are
/// logged but connections proceed.
class SecurityConfig {
  /// List of certificate SHA-256 fingerprints to pin
  ///
  /// Provide as base64-encoded hashes, e.g. `'AAAA...='`.
  /// The `sha256/` prefix is added automatically if missing.
  final List<String> certificateFingerprints;

  /// Report-only mode (default: false)
  ///
  /// When true, certificate validation failures are logged but
  /// connections are not blocked. Useful for gradual rollouts.
  /// When false (default), mismatches throw [AcdcSecurityException].
  final bool reportOnlyMode;

  SecurityConfig({
    required this.certificateFingerprints,
    this.reportOnlyMode = false,
  });
}
