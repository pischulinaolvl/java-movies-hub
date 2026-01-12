package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {

    private String extractYearParam(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("year=")) {
                return param.substring(5);
            }
        }
        return null;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Напишите реализацию, удовлетворяющую тест
        String method = ex.getRequestMethod();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String path = ex.getRequestURI().getPath();
        String[] pathList =  path.split("/");

        if (method.equalsIgnoreCase("GET")) {
            try {
                if (pathList.length == 2) {
                    String query = ex.getRequestURI().getQuery();
                    if (query != null) {
                        String yearParam = extractYearParam(query);
                        if (yearParam != null) {
                            try {
                                int year = Integer.parseInt(yearParam);
                                sendJson(ex, 200, gson.toJson(MoviesServer.getMoviesStore().getMoviesByYear(year)));
                            } catch (NumberFormatException e) {
                                sendJson(ex, 400, "{\n" +
                                        "     \"error\": \"Некорректный параметр запроса — year\",\n" +
                                        "   }");
                            }
                        } else {
                            sendJson(ex, 400, "{\n" +
                                    "     \"error\": \"Параметр year не указан\",\n" +
                                    "   }");
                        }
                    } else {
                        sendJson(ex, 200, gson.toJson(MoviesServer.getMoviesStore().getMovies().values()));
                    }
                } else {
                    int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                    HashMap<Integer, Movie> movies = MoviesServer.getMoviesStore().getMovies();
                    if (movies.containsKey(idValue)) {
                        sendJson(ex, 200, gson.toJson(movies.get(idValue)));
                    } else {
                        sendJson(ex, 404, "{\n" +
                                "     \"error\": \"Фильм не найден\",\n" +
                                "   }");
                    }
                }
            } catch (NumberFormatException e) {
                sendJson(ex, 400, "{\n" +
                        "     \"error\": \"Некорректный ID\",\n" +
                        "   }");
            } catch (Exception exception) {
                sendJson(ex, 400, " {\n" +
                        "     \"error\": \"Ошибка вывода информации о фильмах\",\n" +
                        "   }");
            }
        } else if (method.equalsIgnoreCase("POST")) {
            Map<String, List<String>> headers = ex.getRequestHeaders();
            if (headers.containsKey("Content-Type")) {
                List<String> contentTypes = headers.get("Content-Type");
                String contentType = contentTypes.get(0); // Получаем первое значение заголовка

                if (!contentType.equals("application/json; charset=UTF-8")) {
                    sendJson(ex, 415, "");
                    return;
                }
            } else {
                sendJson(ex, 415, "");
                return;
            }

            String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            try {
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String title;
                    if (jsonObject.get("title").isJsonNull()) {
                        title = null;
                    } else {
                        title = jsonObject.get("title").getAsString();
                    }
                    int year = jsonObject.get("year").getAsInt();

                    Movie movie = new Movie(title, year);
                    List<String> details = Movie.checkMovie(movie);

                    if (details.isEmpty()) {
                        int id = MoviesServer.getMoviesStore().addMovie(movie);
                        sendJson(ex, 200, "{\"id\": " + id + "}");
                    } else {
                        sendJson(ex, 422, "{\"error\": \"Ошибка валидации\", \"details\": " + gson.toJson(details) + "}");
                    }
                } else {
                    sendJson(ex, 422, "{\"error\": \"Ошибка добавления фильма\", \"details\": \"Данные переданы не в формате JSON\"}");
                }
            } catch (Exception exception) {
                sendJson(ex, 422, " {\n" +
                        "     \"error\": \"Ошибка добавления фильма\",\n" +
                        "   }");
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            try {
                if (pathList.length == 3) {
                    int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                    HashMap<Integer, Movie> movies = MoviesServer.getMoviesStore().getMovies();
                    if (movies.containsKey(idValue)) {
                        movies.remove(idValue);
                        sendJson(ex, 204, "Фильм удален");
                    } else {
                        sendJson(ex, 404, "{\n" +
                                "     \"error\": \"Фильм не найден\",\n" +
                                "   }");
                    }
                } else {
                    sendJson(ex, 400, "{\n" +
                            "     \"error\": \"Ошибка при удалении фильма\",\n" +
                            "   }");
                }
            } catch (NumberFormatException e) {
                sendJson(ex, 400, "{\n" +
                        "     \"error\": \"Некорректный ID\",\n" +
                        "   }");
            } catch (Exception exception) {
                sendJson(ex, 400, "{\n" +
                        "     \"error\": \"Ошибка при удалении фильма\",\n" +
                        "   }");
            }
        }
    }
}
