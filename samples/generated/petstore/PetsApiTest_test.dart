// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:dio/dio.dart';
import 'package:petstore_client/remote_data_sources/PetsApi_remote_data_source_impl.dart';
import 'package:petstore_client/models/new_pet.dart';
import 'package:petstore_client/models/pet.dart';
import 'test_helpers.dart';

void main() {
  group('PetsApiRemoteDataSource tests', () {
    group('createPet tests', () {
      test('success case - returns Pet', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/pets', responseData);

        // Act
        final result = await api.createPet();

        // Assert
        expect(result, isA<Pet>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets', statusCode: 500);

        // Act & Assert
        expect(
          () => api.createPet(),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('deletePet tests', () {
      test('success case - returns void', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.on(
          '/pets/{petId}'.replaceAll('petId', 'test_petId'),
          (RequestOptions server) => server.reply(204, null),
        );

        // Act
        final result = await api.deletePet('test_petId');

        // Assert
        // No assertion needed for void return
      });

      test('path parameters are correctly substituted', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        String capturedPath = '';
        mockSetup.adapter.on(
          '/pets/{petId}'.replaceAll('petId', 'param_value_petId'),
          (RequestOptions server) {
            capturedPath = server.request.uri.path;
            return server.reply(204, null);
          },
        );

        // Act
        await api.deletePet('param_value_petId');

        // Assert
        expect(capturedPath, contains('param_value'));
        expect(capturedPath, contains('param_value_petId'));
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets/{petId}'.replaceAll('petId', 'test_petId'), statusCode: 500);

        // Act & Assert
        expect(
          () => api.deletePet('test_petId'),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('listPets tests', () {
      test('success case - returns List<Pet>', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onList('/pets', responseData);

        // Act
        final result = await api.listPets(limit: 42);

        // Assert
        expect(result, isA<List<Pet>>());
        expect(result.length, equals(1));
      });

      test('query parameters are correctly constructed', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        Map<String, dynamic> capturedQueryParams = {};
        mockSetup.adapter.on(
          '/pets',
          (RequestOptions server) {
            capturedQueryParams = Map.from(server.request.uri.queryParameters);
            return server.reply(200, []);
          },
        );

        // Act
        await api.listPets(limit: 123);

        // Assert
        expect(capturedQueryParams.containsKey('limit'), isTrue);
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets', statusCode: 500);

        // Act & Assert
        expect(
          () => api.listPets(limit: 0),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('showPetById tests', () {
      test('success case - returns Pet', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = ;

        mockSetup.adapter.onJson('/pets/{petId}'.replaceAll('petId', 'test_petId'), responseData);

        // Act
        final result = await api.showPetById('test_petId');

        // Assert
        expect(result, isA<Pet>());
      });

      test('path parameters are correctly substituted', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        String capturedPath = '';
        mockSetup.adapter.on(
          '/pets/{petId}'.replaceAll('petId', 'param_value_petId'),
          (RequestOptions server) {
            capturedPath = server.request.uri.path;
            return server.reply(200, <String, dynamic>{});
          },
        );

        // Act
        await api.showPetById('param_value_petId');

        // Assert
        expect(capturedPath, contains('param_value'));
        expect(capturedPath, contains('param_value_petId'));
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets/{petId}'.replaceAll('petId', 'test_petId'), statusCode: 500);

        // Act & Assert
        expect(
          () => api.showPetById('test_petId'),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
