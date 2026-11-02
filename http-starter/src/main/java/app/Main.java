package app;

import app.domain.service.MovieService;
import app.domain.service.SessionService;
import app.http.MovieHandler;
import app.http.SessionHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        SessionService sessionService = new SessionService();
        MovieService movieService = new MovieService(sessionService);
        sessionService.setMovieService(movieService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/ping", new PingHandler());
        server.createContext("/movies", new MovieHandler(movieService));
        server.createContext("/sessions", new SessionHandler(sessionService));

        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT);
    }
}