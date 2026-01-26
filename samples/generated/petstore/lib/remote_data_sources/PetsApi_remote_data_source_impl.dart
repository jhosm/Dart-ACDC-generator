// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:petstore_client/models/new_pet.dart';
import 'package:petstore_client/models/pet.dart';
import 'package:petstore_client/remote_data_sources/PetsApi_remote_data_source.dart';

/// Implementation of [PetsApiRemoteDataSource] using Dio
class PetsApiRemoteDataSourceImpl implements PetsApiRemoteDataSource {
  final Dio _dio;

  PetsApiRemoteDataSourceImpl(this._dio);

  @override
  Future<Pet> createPet(NewPet newPet) async {
    final response = await _dio.post(
      '/pets',
      data: newPet.toJson(),
    );

    // Handle single object response
    return Pet.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<void> deletePet(int petId) async {
    final response = await _dio.delete(
      '/pets/{petId}'.replaceAll('{' + 'petId' + '}', petId.toString()),
    );

  }

  @override
  Future<List<Pet>> listPets(int? limit) async {
    // Build query parameters, filtering out nulls
    final queryParameters = <String, dynamic>{};
    if (limit != null) {
      queryParameters['limit'] = limit;
    }

    final response = await _dio.get(
      '/pets',
      queryParameters: queryParameters,
    );

    // Handle List response
    if (response.data is List) {
      return (response.data as List)
          .map((item) => Pet.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw AcdcClientException(
      message: 'Expected List response but got: ${response.data.runtimeType}',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

  @override
  Future<Pet> showPetById(int petId) async {
    final response = await _dio.get(
      '/pets/{petId}'.replaceAll('{' + 'petId' + '}', petId.toString()),
    );

    // Handle single object response
    return Pet.fromJson(response.data as Map<String, dynamic>);
  }

}
