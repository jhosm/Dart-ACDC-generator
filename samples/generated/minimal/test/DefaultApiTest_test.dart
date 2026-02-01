// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:minimal_client/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:minimal_client/models/ping200_response.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('ping tests', () {
      test('success case - returns Ping200Response', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = <String, dynamic>{};

        mockSetup.adapter.onGetJson('/ping', responseData);

        // Act
        final result = await api.ping();

        // Assert
        expect(result, isA<Ping200Response>());
      });

      test('server error (500) throws exception', () async {
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

      test('client error (404) throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/ping', statusCode: 404);

        // Act & Assert
        expect(
          () => api.ping(),
          throwsA(isA<DioException>()),
        );
      });

      test('network error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetNetworkError('/ping');

        // Act & Assert
        expect(
          () => api.ping(),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
