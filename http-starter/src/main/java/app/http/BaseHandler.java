package app.http;

import app.domain.exception.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public abstract class BaseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            process(exchange);
        } catch (NotFoundException e) {
            JsonUtil.sendError(exchange, 404, "NOT_FOUND", e.getMessage());
        } catch (ValidationException e) {
            JsonUtil.sendError(exchange, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (ConflictException e) {
            JsonUtil.sendError(exchange, 409, "CONFLICT", e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(exchange, 400, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(exchange, 500, "INTERNAL_ERROR", "Server error");
        } finally {
            exchange.close();
        }
    }

    protected abstract void process(HttpExchange exchange) throws IOException;
}