// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:enums_client/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:enums_client/models/enum_test_model.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('testEnums tests', () {
      test('success case - returns EnumTestModel', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/test', responseData);

        // Act
        final result = await api.testEnums();

        // Assert
        expect(result, isA<EnumTestModel>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/test', statusCode: 500);

        // Act & Assert
        expect(
          () => api.testEnums(),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
