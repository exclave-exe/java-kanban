package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import model.Endpoint;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_ALL_EPICS -> handleGetAllEpics(exchange);           // GET    /epics
            case GET_EPIC -> handleGetEpic(exchange);                    // GET    /epics/{id}
            case GET_EPIC_SUBTASKS -> handleGetEpicSubtasks(exchange);   // GET    /epics/{id}/subtasks
            case CREATE_EPIC -> handleCreateEpic(exchange);              // POST   /epics
            case DELETE_EPIC -> handleDeleteEpic(exchange);              // DELETE /epics/{id}
            default -> sendNotFound(exchange);
        }
    }

    // GET /epics
    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendText(exchange, gson.toJson(epics));
    }

    // GET /epics/{id}
    private void handleGetEpic(HttpExchange exchange) throws IOException {
        try {
            Epic epic = taskManager.getEpic(getRequestId(exchange));
            sendText(exchange, gson.toJson(epic));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    // GET /epics/{id}/subtasks
    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        try {
            Epic epic = taskManager.getEpic(getRequestId(exchange));
            List<Subtask> subtasks = epic.getSubtasksId().stream()
                    .map(taskManager::getSubtask)
                    .toList();
            sendText(exchange, gson.toJson(subtasks));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    // POST /epics
    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Epic epicFromRequest = gson.fromJson(body, Epic.class);
            if (isEpicDataInvalid(epicFromRequest)) {
                throw new IllegalArgumentException("Invalid epic data");
            }

            Epic epicFromManager = taskManager.createEpic(
                    epicFromRequest.getName(),
                    epicFromRequest.getDescription()
            );

            sendText(exchange, gson.toJson(epicFromManager));

        } catch (JsonSyntaxException exception) {
            sendBadRequest(exchange, "Invalid Json");
        } catch (IllegalArgumentException exception) {
            sendBadRequest(exchange, exception.getMessage());
        }
    }

    // DELETE /epics/{id}
    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        boolean deleted = taskManager.deleteEpic(getRequestId(exchange));
        if (deleted) {
            sendResponse(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private boolean isEpicDataInvalid(Epic epic) {
        return epic.getName() == null ||
                epic.getDescription() == null;
    }
}