package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Endpoint;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    protected HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_HISTORY -> handleGetHistory(exchange); // GET /history
            default -> sendNotFound(exchange);
        }
    }

    // GET /history
    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getHistory()));
    }
}
