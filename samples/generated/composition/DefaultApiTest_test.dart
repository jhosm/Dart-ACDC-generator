// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:composition_client/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:composition_client/models/entity.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('getEntities tests', () {
      test('success case - returns List<Entity>', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onList('/entities', responseData);

        // Act
        final result = await api.getEntities();

        // Assert
        expect(result, isA<List<Entity>>());
        expect(result.length, equals(1));
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/entities', statusCode: 500);

        // Act & Assert
        expect(
          () => api.getEntities(),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
