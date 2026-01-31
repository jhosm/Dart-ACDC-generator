// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:composition_client/models/entity.dart';
import 'package:composition_client/remote_data_sources/DefaultApi_remote_data_source.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<List<Entity>> getEntities() async {
    final response = await _dio.get<List<dynamic>>(
      '/entities',
    );

    // Handle List response
    final data = response.data;
    if (data != null) {
      return data
          .map((item) => Entity.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw AcdcClientException(
      message: 'Expected List response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

}
