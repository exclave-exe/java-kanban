package server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.EpicInput;
import exceptions.NotFoundException;
import manager.TaskManager;
import model.ApiResponse;
import model.Endpoint;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            default -> sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Endpoint not found")));
        }
    }

    // GET /epics
    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getAllEpics()));
    }

    // GET /epics/{id}
    private void handleGetEpic(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            sendText(exchange, gson.toJson(taskManager.getEpic(id)));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Epic not found")));
        }
    }

    // GET /epics/{id}/subtasks
    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        int epicId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            Epic epic = taskManager.getEpic(epicId);
            List<Subtask> subtasks = epic.getSubtasksId().stream()
                    .map(taskManager::getSubtask)
                    .collect(Collectors.toList());
            sendText(exchange, gson.toJson(subtasks));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Epic or subtasks not found")));
        }
    }

    // POST /epics
    private void handleCreateEpic(HttpExchange exchange) throws IOException {

        //    Предполагается что при запросе в теле будет приходить JSON следующего вида:
        //    {
        //        "name": "name",
        //        "description": "description"
        //    }

        JsonObject jsonObject = parseJson(exchange);
        if (jsonObject == null) return;

        EpicInput epicInput = parseEpicInput(jsonObject, exchange);
        if (epicInput == null) return;

        Epic epic = taskManager.createEpic(epicInput.name, epicInput.description);
        sendText(exchange, gson.toJson(epic));
    }

    // DELETE /epics/{id}
    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        boolean deleted = taskManager.deleteEpic(id);
        if (deleted) {
            sendText(exchange, gson.toJson(new ApiResponse(200, "Success")));
        } else {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Epic not found")));
        }
    }

    private EpicInput parseEpicInput(JsonObject jsonObject, HttpExchange exchange) throws IOException {
        // Проверка обязательных полей
        if (!jsonObject.has("name") || !jsonObject.has("description")) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Missing required fields: name, description")));
            return null;
        }

        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();

        return new EpicInput(name, description);
    }
}