// AUTO-GENERATED FILE - DO NOT EDIT

/// Security configuration for certificate pinning
class SecurityConfig {
  /// List of certificate SHA-256 fingerprints to pin
  final List<String> certificateFingerprints;

  /// Allow self-signed certificates for development (default: false, DO NOT use in production)
  final bool allowSelfSigned;

  SecurityConfig({
    required this.certificateFingerprints,
    this.allowSelfSigned = false,
  });
}
