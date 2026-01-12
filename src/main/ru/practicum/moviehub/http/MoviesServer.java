package ru.practicum.moviehub.http;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;
import java.io.IOException;
import java.net.InetSocketAddress;

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

    public MoviesServer(MoviesStore newMoviesStore, int port) {
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