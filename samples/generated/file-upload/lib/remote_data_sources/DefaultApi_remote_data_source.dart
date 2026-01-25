// AUTO-GENERATED FILE - DO NOT EDIT
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
  Future<UploadResponse> uploadFile(, );

  /// Upload multiple files
  Future<List> uploadMultipleFiles(, );

  /// Upload profile with avatar
  Future<Profile> uploadProfile(, , );

}
