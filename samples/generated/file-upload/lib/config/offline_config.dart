// AUTO-GENERATED FILE - DO NOT EDIT

/// Offline detection and handling configuration
class OfflineConfig {
  /// Fail fast when offline (throw exception immediately, default: true)
  final bool failFast;

  /// Custom offline detector function (optional, for advanced use cases)
  final Function? customOfflineDetector;

  OfflineConfig({
    this.failFast = true,
    this.customOfflineDetector,
  });
}
