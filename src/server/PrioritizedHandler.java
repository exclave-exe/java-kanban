package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Endpoint;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    protected PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_PRIORITIZED -> handleGetPrioritized(exchange); // GET /prioritized
            default -> sendNotFound(exchange);
        }
    }

    // GET /prioritized
    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getTasksByPriority(true)));
    }
}
