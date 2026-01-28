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

        final responseData = ;

        mockSetup.adapter.onJson('/test', responseData);

        // Act
        final result = await api.getTest();

        // Assert
        expect(result, isA<TestObject>());
      });

      test('server error throws exception', () async {
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
    });

  });
}
