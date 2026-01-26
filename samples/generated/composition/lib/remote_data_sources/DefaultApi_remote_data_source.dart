// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dart_acdc/dart_acdc.dart';
import 'package:composition_client/models/entity.dart';

/// Remote data source for DefaultApi
///
/// All methods may throw the following exceptions:
/// - [AcdcAuthException] - Authentication/authorization failures
/// - [AcdcNetworkException] - Network connectivity issues
/// - [AcdcServerException] - Server errors (5xx responses)
/// - [AcdcClientException] - Client errors (4xx responses)
/// - [AcdcSecurityException] - Certificate pinning or security violations
abstract class DefaultApiRemoteDataSource {
  /// Get entities
  Future<List<Entity>> getEntities();

}
