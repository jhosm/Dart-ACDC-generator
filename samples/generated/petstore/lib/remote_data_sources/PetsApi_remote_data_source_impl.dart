// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:dart_acdc/dart_acdc.dart';
import 'package:petstore_client/models/error.dart';
import 'package:petstore_client/models/new_pet.dart';
import 'package:petstore_client/models/pet.dart';

/// Implementation of [PetsApiRemoteDataSource] using Dio
class PetsApiRemoteDataSourceImpl implements PetsApiRemoteDataSource {
  final Dio _dio;

  PetsApiRemoteDataSourceImpl(this._dio);

  @override
  Future<Pet> createPet(NewPet newPet) async {
    try {
      final response = await _dio.post(
        '/pets',
        data: newPet.toJson(),
      );

      // Handle single object response
      return Pet.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<void> deletePet(int petId) async {
    try {
      final response = await _dio.delete(
        '/pets/{petId}'.replaceAll('{' + 'petId' + '}', petId.toString()),
      );

      // Void return type - no deserialization needed
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<List> listPets(int? limit) async {
    try {
      // Build query parameters, filtering out nulls
      final queryParameters = <String, dynamic>{};
      if (limit != null) {
        queryParameters['limit'] = limit;
      }

      final response = await _dio.get(
        '/pets',
        queryParameters: queryParameters,
      );

      // Handle single object response
      return List.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

  @override
  Future<Pet> showPetById(int petId) async {
    try {
      final response = await _dio.get(
        '/pets/{petId}'.replaceAll('{' + 'petId' + '}', petId.toString()),
      );

      // Handle single object response
      return Pet.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AcdcException.fromDioException(e);
    }
  }

}
