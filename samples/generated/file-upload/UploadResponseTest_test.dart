// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:test/test.dart';
import 'package:file_upload_client/models/upload_response.dart';

void main() {
  group('UploadResponse tests', () {
    test('fromJson with all fields populated', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'filename': 'test_filename',
        'url': 'test_url',
        'size': 42,
        'mimeType': 'test_mimeType',
      };

      final model = UploadResponse.fromJson(json);

      expect(model, isA<UploadResponse>());
      expect(model.id, isNotNull);
      expect(model.filename, isNotNull);
      expect(model.url, isNotNull);
    });

    test('fromJson with only required fields', () {
      final json = <String, dynamic>{
        'id': 'test_id',
        'filename': 'test_filename',
        'url': 'test_url',
      };

      final model = UploadResponse.fromJson(json);

      expect(model, isA<UploadResponse>());
      expect(model.id, isNotNull);
      expect(model.filename, isNotNull);
      expect(model.url, isNotNull);
      expect(model.size, isNull);
      expect(model.mimeType, isNull);
    });

    test('toJson produces valid JSON', () {
      final model = UploadResponse(
        id: 'test_id',
        filename: 'test_filename',
        url: 'test_url',
      );

      final json = model.toJson();

      expect(json, isA<Map<String, dynamic>>());
      expect(json.containsKey('id'), isTrue);
      expect(json.containsKey('filename'), isTrue);
      expect(json.containsKey('url'), isTrue);
    });

    test('roundtrip serialization preserves data', () {
      final originalJson = <String, dynamic>{
        'id': 'test_id',
        'filename': 'test_filename',
        'url': 'test_url',
        'size': 42,
        'mimeType': 'test_mimeType',
      };

      final model = UploadResponse.fromJson(originalJson);
      final serializedJson = model.toJson();

      // Verify all required fields match
      expect(serializedJson['id'], equals(originalJson['id']));
      expect(serializedJson['filename'], equals(originalJson['filename']));
      expect(serializedJson['url'], equals(originalJson['url']));
    });
  });
}
