// AUTO-GENERATED FILE - DO NOT EDIT

/// Offline detection and handling configuration
///
/// When [failFast] is true (default), requests immediately throw
/// [AcdcNetworkException] when the device is offline with no cached response.
/// When false, requests attempt the network call and rely on timeouts.
class OfflineConfig {
  /// Fail fast when offline (throw exception immediately, default: true)
  final bool failFast;

  OfflineConfig({
    this.failFast = true,
  });
}
