// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:file_upload_client/models/profile.dart';
import 'package:file_upload_client/models/upload_response.dart';

/// Remote data source for DefaultApi
///
/// All methods may throw the following exceptions:
/// - [AcdcAuthException] - Authentication/authorization failures
/// - [AcdcNetworkException] - Network connectivity issues
/// - [AcdcServerException] - Server errors (5xx responses)
/// - [AcdcClientException] - Client errors (4xx responses)
/// - [AcdcSecurityException] - Certificate pinning or security violations
abstract class DefaultApiRemoteDataSource {
  /// Upload a single file
  Future<UploadResponse> uploadFile(MultipartFile file, String? description);

  /// Upload multiple files
  Future<List<UploadResponse>> uploadMultipleFiles(List files, List? tags);

  /// Upload profile with avatar
  Future<Profile> uploadProfile(String name, MultipartFile avatar, String? bio);

}
