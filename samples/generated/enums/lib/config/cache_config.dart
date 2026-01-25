// AUTO-GENERATED FILE - DO NOT EDIT

/// Caching configuration for two-tier caching (memory + disk)
class CacheConfig {
  /// Time-to-live for cached responses
  final Duration ttl;

  /// Enable disk caching (default: true)
  final bool enableDiskCache;

  /// Encrypt cached data with AES-256 (default: true)
  final bool encryptCache;

  /// Maximum memory cache size in MB (default: 10)
  final int maxMemoryCacheSizeMB;

  /// Maximum disk cache size in MB (default: 50)
  final int maxDiskCacheSizeMB;

  /// Enable user isolation for cached data (default: true)
  final bool userIsolation;

  CacheConfig({
    this.ttl = const Duration(hours: 1),
    this.enableDiskCache = true,
    this.encryptCache = true,
    this.maxMemoryCacheSizeMB = 10,
    this.maxDiskCacheSizeMB = 50,
    this.userIsolation = true,
  });
}
