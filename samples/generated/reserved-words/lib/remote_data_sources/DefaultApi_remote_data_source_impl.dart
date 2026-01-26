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
    final response = await _dio.get(
      '/test',
    );

    // Handle single object response
    return TestObject.fromJson(response.data as Map<String, dynamic>);
  }

}
