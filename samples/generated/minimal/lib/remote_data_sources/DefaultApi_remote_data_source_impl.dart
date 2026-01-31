// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:minimal_client/models/ping200_response.dart';
import 'package:minimal_client/remote_data_sources/DefaultApi_remote_data_source.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<Ping200Response> ping() async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/ping',
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return Ping200Response.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected Ping200Response response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

}
