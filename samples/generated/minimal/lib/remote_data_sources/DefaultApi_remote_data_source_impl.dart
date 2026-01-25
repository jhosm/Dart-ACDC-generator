// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:minimal_client/models/ping200_response.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<ping_200_response> ping() async {
    try {
      final response = await _dio.get(
        '/ping',
      );

      // Handle single object response
      return ping_200_response.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

}
