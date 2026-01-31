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
    final response = await _dio.get<Response>(
      '/ping',
    );

    // Handle single object response
    return Ping200Response.fromJson(response.data as Map<String, dynamic>);
  }

}
