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

        final responseData = <String, dynamic>{};

        mockSetup.adapter.onPostJson('/pets', responseData);

        // Act
        final result = await api.createPet(NewPet.fromJson(const <String, dynamic>{}));

        // Assert
        expect(result, isA<Pet>());
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onPostError('/pets', statusCode: 500);

        // Act & Assert
        expect(
          () => api.createPet(NewPet.fromJson(const <String, dynamic>{})),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('deletePet tests', () {
      test('success case - returns void', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onDelete(
          '/pets/{petId}'.replaceAll('petId', '42'),
          (server) => server.reply(204, null),
        );

        // Act
        await api.deletePet(42);

        // Assert
      });

      test('path parameters are correctly substituted', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        // The mock adapter matches the exact path, so a successful response
        // verifies that path parameters were correctly substituted
        mockSetup.adapter.onDelete(
          '/pets/{petId}'.replaceAll('petId', '42'),
          (server) => server.reply(204, null),
        );

        // Act & Assert - if the path parameter substitution is wrong,
        // the mock adapter won't match and the request will fail
        await api.deletePet(42);
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onDeleteError('/pets/{petId}'.replaceAll('petId', '42'), statusCode: 500);

        // Act & Assert
        expect(
          () => api.deletePet(42),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('listPets tests', () {
      test('success case - returns List<Pet>', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = [<String, dynamic>{}];

        mockSetup.adapter.onGetList('/pets', responseData);

        // Act
        final result = await api.listPets(42);

        // Assert
        expect(result, isA<List<Pet>>());
        expect(result.length, equals(1));
      });

      test('query parameters are correctly constructed', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGet(
          '/pets',
          (server) => server.reply(200, <dynamic>[]),
        );

        // Act & Assert - request succeeds with query parameters
        await api.listPets(42);
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets', statusCode: 500);

        // Act & Assert
        expect(
          () => api.listPets(42),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('showPetById tests', () {
      test('success case - returns Pet', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        final responseData = <String, dynamic>{};

        mockSetup.adapter.onGetJson('/pets/{petId}'.replaceAll('petId', '42'), responseData);

        // Act
        final result = await api.showPetById(42);

        // Assert
        expect(result, isA<Pet>());
      });

      test('path parameters are correctly substituted', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        // The mock adapter matches the exact path, so a successful response
        // verifies that path parameters were correctly substituted
        mockSetup.adapter.onGet(
          '/pets/{petId}'.replaceAll('petId', '42'),
          (server) => server.reply(200, <String, dynamic>{}),
        );

        // Act & Assert - if the path parameter substitution is wrong,
        // the mock adapter won't match and the request will fail
        await api.showPetById(42);
      });

      test('server error throws exception', () async {
        // Arrange
        final mockSetup = createMockDio();
        final api = PetsApiRemoteDataSourceImpl(mockSetup.dio);

        mockSetup.adapter.onGetError('/pets/{petId}'.replaceAll('petId', '42'), statusCode: 500);

        // Act & Assert
        expect(
          () => api.showPetById(42),
          throwsA(isA<DioException>()),
        );
      });
    });

  });
}
