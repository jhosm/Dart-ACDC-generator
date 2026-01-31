// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:file_upload_client/models/profile.dart';

void main() {
  group('Profile tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'name': 'test_name',
        'bio': 'test_bio',
        'avatarUrl': 'test_avatarUrl',
      };

      final model = Profile.fromJson(json);

      expect(model, isA<Profile>());
      expect(model.id, isNotNull);
      expect(model.name, isNotNull);
      expect(model.avatarUrl, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'name': 'test_name',
        'avatarUrl': 'test_avatarUrl',
      };

      final model = Profile.fromJson(json);

      expect(model, isA<Profile>());
      expect(model.id, isNotNull);
      expect(model.name, isNotNull);
      expect(model.bio, isNull);
      expect(model.avatarUrl, isNotNull);
    });

    test('toJson produces valid JSON', () {
      final model = Profile(
        id: 'test_id',
        name: 'test_name',
        avatarUrl: 'test_avatarUrl',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('id'), isTrue);
      expect(json.containsKey('name'), isTrue);
      expect(json.containsKey('avatarUrl'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'id': 'test_id',
        'name': 'test_name',
        'bio': 'test_bio',
        'avatarUrl': 'test_avatarUrl',
      };

      final model = Profile.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['name'], equals(originalJson['name']));
      expect(serializedJson['avatarUrl'], equals(originalJson['avatarUrl']));
    });
  });
}
