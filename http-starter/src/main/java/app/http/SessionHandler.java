package app.http;

import app.domain.model.*;
import app.domain.service.SessionService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class SessionHandler extends BaseHandler {
    private final SessionService service;

    public SessionHandler(SessionService service) {
        this.service = service;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // /sessions
        if (path.equals("/sessions") || path.equals("/sessions/")) {
            if (method.equals("GET")) {
                Long movieId = parseLong(queryParam(exchange, "movieId"));
                HallType type = parseHallType(queryParam(exchange, "hallType"));
                Boolean onlyAvail = Boolean.parseBoolean(queryParam(exchange, "onlyAvailable"));
                List<Session> sessions = service.findAll(movieId, type, onlyAvail);
                JsonUtil.sendJson(exchange, 200, sessions);
            } else if (method.equals("POST")) {
                Map<?, ?> body = JsonUtil.readBody(exchange, Map.class);
                if (body == null) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Body required"); return; }
                
                long movieId = body.get("movieId") instanceof Number n ? n.longValue() : -1;
                String startStr = (String) body.get("startTime");
                Instant startTime = null;
                try { if (startStr != null) startTime = Instant.parse(startStr); } 
                catch (DateTimeParseException e) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Invalid startTime format, use ISO-8601"); return; }
                
                HallType type = parseHallType((String) body.get("hallType"));
                double price = body.get("price") instanceof Number n ? n.doubleValue() : 0;

                Session session = service.create(movieId, startTime, type, price);
                JsonUtil.sendJson(exchange, 201, session);
            } else {
                JsonUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Use GET or POST");
            }
            return;
        }

        // /sessions/{id}/buy
        if (path.matches("/sessions/\\d+/buy")) {
            if (!method.equals("POST")) { JsonUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Use POST"); return; }
            String[] parts = path.split("/");
            long id = Long.parseLong(parts[2]);
            
            Map<?, ?> body = JsonUtil.readBody(exchange, Map.class);
            if (body == null) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Body required"); return; }
            int count = body.get("count") instanceof Number n ? n.intValue() : 0;

            Session s = service.buyTickets(id, count);
            JsonUtil.sendJson(exchange, 200, s);
            return;
        }

        // /sessions/{id}
        if (path.startsWith("/sessions/")) {
            String idPart = path.substring("/sessions/".length());
            long id;
            try { id = Long.parseLong(idPart); } 
            catch (NumberFormatException e) { JsonUtil.sendError(exchange, 400, "BAD_REQUEST", "Invalid ID"); return; }

            if (method.equals("GET")) {
                JsonUtil.sendJson(exchange, 200, service.findById(id));
            } else if (method.equals("DELETE")) {
                service.delete(id);
                JsonUtil.sendJson(exchange, 204, Map.of());
            } else {
                JsonUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Use GET or DELETE");
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

    private Long parseLong(String val) {
        if (val == null) return null;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return null; }
    }

    private HallType parseHallType(String val) {
        if (val == null || val.isBlank()) return null;
        try { return HallType.valueOf(val.toUpperCase()); } 
        catch (IllegalArgumentException e) { return null; }
    }
}