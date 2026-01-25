// AUTO-GENERATED FILE - DO NOT EDIT
/// Remote data source for PetsApi
///
/// All methods may throw the following exceptions:
/// - [AcdcAuthException] - Authentication/authorization failures
/// - [AcdcNetworkException] - Network connectivity issues
/// - [AcdcServerException] - Server errors (5xx responses)
/// - [AcdcClientException] - Client errors (4xx responses)
/// - [AcdcSecurityException] - Certificate pinning or security violations
abstract class PetsApiRemoteDataSource {
  /// Create a pet
  Future<Pet> createPet(NewPet newPet);

  /// Delete a pet
  Future<void> deletePet(int petId);

  /// List all pets
  Future<List> listPets(int? limit);

  /// Info for a specific pet
  Future<Pet> showPetById(int petId);

}
