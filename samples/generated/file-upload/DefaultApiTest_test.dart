// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:file_upload_client/remote_data_sources/DefaultApi_remote_data_source_impl.dart';
import 'package:file_upload_client/models/profile.dart';
import 'package:file_upload_client/models/upload_response.dart';
import 'test_helpers.dart';

void main() {
  group('DefaultApiRemoteDataSource tests', () {
    group('uploadFile tests', () {
      test('success case - returns UploadResponse', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/upload', responseData);

        // Act
        final result = await api.uploadFile(, 'test_description');

        // Assert
        expect(result, isA<UploadResponse>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/upload', statusCode: 500);

        // Act & Assert
        expect(
          () => api.uploadFile(, 'test'),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('uploadMultipleFiles tests', () {
      test('success case - returns List<UploadResponse>', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onList('/upload/multiple', responseData);

        // Act
        final result = await api.uploadMultipleFiles(, );

        // Assert
        expect(result, isA<List<UploadResponse>>());
        expect(result.length, equals(1));
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/upload/multiple', statusCode: 500);

        // Act & Assert
        expect(
          () => api.uploadMultipleFiles(, ),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('uploadProfile tests', () {
      test('success case - returns Profile', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/upload/profile', responseData);

        // Act
        final result = await api.uploadProfile('test_name', , 'test_bio');

        // Assert
        expect(result, isA<Profile>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = DefaultApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/upload/profile', statusCode: 500);

        // Act & Assert
        expect(
          () => api.uploadProfile('test', , 'test'),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
