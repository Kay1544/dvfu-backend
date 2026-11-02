package app.http;

import app.domain.model.Movie;
import app.domain.service.MovieService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MovieHandler extends BaseHandler {
    private final MovieService service;

    public MovieHandler(MovieService service) {
        this.service = service;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        // /movies
        if (path.equals("/movies") || path.equals("/movies/")) {
            if (method.equals("GET")) {
                String title = queryParam(exchange, "title");
                List<Movie> movies = service.findAll(title);
                JsonUtil.sendJson(exchange, 200, movies);
            } else if (method.equals("POST")) {
                Map<?, ?> body = JsonUtil.readBody(exchange, Map.class);
                if (body == null) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Body required"); return; }
                String title = (String) body.get("title");
                int duration = body.get("durationMinutes") instanceof Number n ? n.intValue() : -1;
                Movie movie = service.create(title, duration);
                JsonUtil.sendJson(exchange, 201, movie);
            } else {
                JsonUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Use GET or POST");
            }
            return;
        }

        // /movies/{id}
        if (path.startsWith("/movies/")) {
            String idPart = path.substring("/movies/".length());
            long id;
            try { id = Long.parseLong(idPart); } 
            catch (NumberFormatException e) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Invalid ID"); return; }

            if (method.equals("GET")) {
                JsonUtil.sendJson(exchange, 200, service.findById(id));
            } else if (method.equals("PUT")) {
                Map<?, ?> body = JsonUtil.readBody(exchange, Map.class);
                if (body == null) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Body required"); return; }
                String title = (String) body.get("title");
                int duration = body.get("durationMinutes") instanceof Number n ? n.intValue() : -1;
                JsonUtil.sendJson(exchange, 200, service.update(id, title, duration));
            } else if (method.equals("DELETE")) {
                service.delete(id);
                JsonUtil.sendJson(exchange, 204, Map.of());
            } else {
                JsonUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Use GET, PUT, DELETE");
            }
            return;
        }

        JsonUtil.sendError(exchange, 404, "NOT_FOUND", "Unknown path");
    }

    private String queryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) return pair[1];
        }
        return null;
    }
}