package ru.practicum.moviehub.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;


/*class MoviesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Напишите реализацию, удовлетворяющую тест
        String response = "[]";
        Headers headers;

        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")){
            try (OutputStream os = ex.getResponseBody()) {
                headers = ex.getResponseHeaders();
                headers.set("Content-Type", "application/json; charset=UTF-8");
                ex.sendResponseHeaders(200, response.length());
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}*/

public class MoviesServer {
    private static HttpServer server;
    private static MoviesStore moviesStore;

    public MoviesServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Добавьте контекст для /movies и укажите созданный хендлер
            server.createContext("/movies", new MoviesHandler());

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public MoviesServer(MoviesStore newMoviesStore, int port){
        this(port);
        moviesStore = newMoviesStore;
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public static MoviesStore getMoviesStore() {
        return moviesStore;
    }
}