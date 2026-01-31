// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:file_upload_client/models/profile.dart';
import 'package:file_upload_client/models/upload_response.dart';
import 'package:file_upload_client/remote_data_sources/DefaultApi_remote_data_source.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<UploadResponse> uploadFile(MultipartFile file, String? description) async {
    // Build form data for multipart/form-data request
    final formData = FormData.fromMap({
      'file': file,
      'description': description,
    });

    final response = await _dio.post<Map<String, dynamic>>(
      '/upload',
      data: formData,
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return UploadResponse.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected UploadResponse response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

  @override
  Future<List<UploadResponse>> uploadMultipleFiles(List files, List? tags) async {
    // Build form data for multipart/form-data request
    final formData = FormData.fromMap({
      'files': files,
      'tags': tags,
    });

    final response = await _dio.post<List<dynamic>>(
      '/upload/multiple',
      data: formData,
    );

    // Handle List response
    final data = response.data;
    if (data != null) {
      return data
          .map((item) => UploadResponse.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw AcdcClientException(
      message: 'Expected List response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

  @override
  Future<Profile> uploadProfile(String name, MultipartFile avatar, String? bio) async {
    // Build form data for multipart/form-data request
    final formData = FormData.fromMap({
      'name': name,
      'bio': bio,
      'avatar': avatar,
    });

    final response = await _dio.post<Map<String, dynamic>>(
      '/upload/profile',
      data: formData,
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return Profile.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected Profile response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

}
