// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:composition_client/models/entity.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<List> getEntities() async {
    try {
      final response = await _dio.get(
        '/entities',
      );

      // Handle single object response
      return List.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

}
