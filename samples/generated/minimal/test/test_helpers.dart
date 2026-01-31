// AUTO-GENERATED FILE - DO NOT EDIT
import 'package:dio/dio.dart';
import 'package:http_mock_adapter/http_mock_adapter.dart';

/// Creates a mock Dio instance with a DioAdapter for testing
/// Returns a tuple of (Dio, DioAdapter)
({Dio dio, DioAdapter adapter}) createMockDio() {
  final dio = Dio(BaseOptions(baseUrl: 'https://api.example.com'));
  final adapter = DioAdapter(dio: dio);
  return (dio: dio, adapter: adapter);
}

/// Extension methods on DioAdapter for easier mock response setup
extension MockResponseHelpers on DioAdapter {
  // ── GET helpers ───────────────────────────────────────────────

  /// Mock a successful GET request with JSON response
  void onGetJson(String path, Map<String, dynamic> responseData, {int statusCode = 200}) {
    onGet(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a successful GET request with List response
  void onGetList(String path, List<dynamic> responseData, {int statusCode = 200}) {
    onGet(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a GET error response
  void onGetError(String path, {int statusCode = 500, String message = 'Internal Server Error'}) {
    onGet(
      path,
      (server) => server.reply(statusCode, {'error': message}),
    );
  }

  // ── POST helpers ──────────────────────────────────────────────

  /// Mock a successful POST request with JSON response
  void onPostJson(String path, Map<String, dynamic> responseData, {int statusCode = 201}) {
    onPost(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a successful POST request with List response
  void onPostList(String path, List<dynamic> responseData, {int statusCode = 200}) {
    onPost(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a POST error response
  void onPostError(String path, {int statusCode = 500, String message = 'Internal Server Error'}) {
    onPost(
      path,
      (server) => server.reply(statusCode, {'error': message}),
    );
  }

  // ── PUT helpers ───────────────────────────────────────────────

  /// Mock a successful PUT request with JSON response
  void onPutJson(String path, Map<String, dynamic> responseData, {int statusCode = 200}) {
    onPut(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a successful PUT request with List response
  void onPutList(String path, List<dynamic> responseData, {int statusCode = 200}) {
    onPut(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a PUT error response
  void onPutError(String path, {int statusCode = 500, String message = 'Internal Server Error'}) {
    onPut(
      path,
      (server) => server.reply(statusCode, {'error': message}),
    );
  }

  // ── PATCH helpers ─────────────────────────────────────────────

  /// Mock a successful PATCH request with JSON response
  void onPatchJson(String path, Map<String, dynamic> responseData, {int statusCode = 200}) {
    onPatch(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a successful PATCH request with List response
  void onPatchList(String path, List<dynamic> responseData, {int statusCode = 200}) {
    onPatch(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a PATCH error response
  void onPatchError(String path, {int statusCode = 500, String message = 'Internal Server Error'}) {
    onPatch(
      path,
      (server) => server.reply(statusCode, {'error': message}),
    );
  }

  // ── DELETE helpers ────────────────────────────────────────────

  /// Mock a successful DELETE request with JSON response
  void onDeleteJson(String path, Map<String, dynamic> responseData, {int statusCode = 200}) {
    onDelete(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a successful DELETE request with List response
  void onDeleteList(String path, List<dynamic> responseData, {int statusCode = 200}) {
    onDelete(
      path,
      (server) => server.reply(statusCode, responseData),
    );
  }

  /// Mock a DELETE error response
  void onDeleteError(String path, {int statusCode = 500, String message = 'Internal Server Error'}) {
    onDelete(
      path,
      (server) => server.reply(statusCode, {'error': message}),
    );
  }
}
