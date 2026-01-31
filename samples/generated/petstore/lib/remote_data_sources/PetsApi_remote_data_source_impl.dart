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
    final response = await _dio.post<Map<String, dynamic>>(
      '/pets',
      data: newPet.toJson(),
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return Pet.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected Pet response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

  @override
  Future<void> deletePet(int petId) async {
    await _dio.delete<void>(
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

    final response = await _dio.get<List<dynamic>>(
      '/pets',
      queryParameters: queryParameters,
    );

    // Handle List response
    final data = response.data;
    if (data != null) {
      return data
          .map((item) => Pet.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw AcdcClientException(
      message: 'Expected List response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

  @override
  Future<Pet> showPetById(int petId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/pets/{petId}'.replaceAll('{' + 'petId' + '}', petId.toString()),
    );

    // Handle single object response
    final data = response.data;
    if (data != null) {
      return Pet.fromJson(data);
    }
    throw AcdcClientException(
      message: 'Expected Pet response but got null',
      statusCode: response.statusCode ?? 0,
      requestOptions: response.requestOptions,
      originalException: null,
    );
  }

}
