// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:reserved_words_test/models/test_object.dart';
import 'package:reserved_words_test/remote_data_sources/DefaultApi_remote_data_source.dart';

/// Implementation of [DefaultApiRemoteDataSource] using Dio
class DefaultApiRemoteDataSourceImpl implements DefaultApiRemoteDataSource {
  final Dio _dio;

  DefaultApiRemoteDataSourceImpl(this._dio);

  @override
  Future<TestObject> getTest() async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/test',
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return TestObject.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected TestObject response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

}
