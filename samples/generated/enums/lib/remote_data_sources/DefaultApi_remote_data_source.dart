// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:enums_client/models/enum_test_model.dart';

/// Remote data source for DefaultApi
///
/// All methods may throw the following exceptions:
/// - [AcdcAuthException] - Authentication/authorization failures
/// - [AcdcNetworkException] - Network connectivity issues
/// - [AcdcServerException] - Server errors (5xx responses)
/// - [AcdcClientException] - Client errors (4xx responses)
/// - [AcdcSecurityException] - Certificate pinning or security violations
abstract class DefaultApiRemoteDataSource {
  /// Test endpoint
  Future<EnumTestModel> testEnums();

}
