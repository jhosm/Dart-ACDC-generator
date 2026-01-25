// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:file_upload_client/models/profile.dart';
import 'package:file_upload_client/models/upload_response.dart';
import 'package:file_upload_client/models/file.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<UploadResponse> uploadFile(, ) async {
    try {
      final response = await _dio.post(
        '/upload',
      );

      // Handle single object response
      return UploadResponse.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<List> uploadMultipleFiles(, ) async {
    try {
      final response = await _dio.post(
        '/upload/multiple',
      );

      // Handle single object response
      return List.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<Profile> uploadProfile(, , ) async {
    try {
      final response = await _dio.post(
        '/upload/profile',
      );

      // Handle single object response
      return Profile.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

}
