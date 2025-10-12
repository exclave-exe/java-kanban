package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import util.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

abstract class HttpServerBaseTest {
    protected TaskManager taskManager;
    protected HttpTaskServer httpTaskServer;
    protected HttpClient httpClient;
    protected URI baseUri;
    protected Gson gson;

    protected abstract TaskManager createManager();

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
        httpClient = HttpClient.newHttpClient();
        baseUri = URI.create("http://localhost:8080");
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stop();
    }

    protected HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUri.resolve(path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendPost(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUri.resolve(path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUri.resolve(path))
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}