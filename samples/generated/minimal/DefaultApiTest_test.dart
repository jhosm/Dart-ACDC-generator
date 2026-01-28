// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:minimal_client/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:minimal_client/models/ping200_response.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('ping tests', () {
      test('success case - returns ping_200_response', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/ping', responseData);

        // Act
        final result = await api.ping();

        // Assert
        expect(result, isA<ping_200_response>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/ping', statusCode: 500);

        // Act & Assert
        expect(
          () => api.ping(),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
