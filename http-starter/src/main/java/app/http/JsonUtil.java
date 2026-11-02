package app.http;

import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JsonUtil {
    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    public static <T> T readBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        if (bytes.length == 0) return null;
        return MAPPER.readValue(bytes, clazz);
    }

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int status, String errorCode, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", errorCode, "message", message));
    }
}