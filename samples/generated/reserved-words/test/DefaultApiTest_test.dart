// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:reserved_words_test/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:reserved_words_test/models/test_object.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('getTest tests', () {
      test('success case - returns TestObject', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = <String, dynamic>{'class': 'test_class', 'default': 'test_default', 'id': 'test_id'};

        mockSetup.adapter.onGetJson('/test', responseData);

        // Act
        final result = await api.getTest();

        // Assert
        expect(result, isA<TestObject>());
      });

      test('server error (500) throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/test', statusCode: 500);

        // Act & Assert
        expect(
          () => api.getTest(),
          throwsA(isA<DioException>()),
        );
      });

      test('client error (404) throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/test', statusCode: 404);

        // Act & Assert
        expect(
          () => api.getTest(),
          throwsA(isA<DioException>()),
        );
      });

      test('network error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetNetworkError('/test');

        // Act & Assert
        expect(
          () => api.getTest(),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
