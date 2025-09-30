package server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.SubtaskInput;
import exceptions.NotFoundException;
import exceptions.TimeInterectionException;
import manager.TaskManager;
import model.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_ALL_SUBTASKS -> handleGetAllSubtasks(exchange);     // GET    /subtasks
            case GET_SUBTASK -> handleGetSubtask(exchange);              // GET    /subtasks/{id}
            case CREATE_SUBTASK -> handleCreateSubtask(exchange);        // POST   /subtasks
            case UPDATE_SUBTASK -> handleUpdateSubtask(exchange);        // POST   /subtasks/{id}
            case DELETE_SUBTASK -> handleDeleteSubtask(exchange);        // DELETE /subtasks/{id}
            default -> sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Endpoint not found")));
        }
    }

    // GET /subtasks
    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getAllSubtasks()));
    }

    // GET /subtasks/{id}
    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            sendText(exchange, gson.toJson(taskManager.getSubtask(id)));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Subtask not found")));
        }
    }

    // POST /subtasks
    private void handleCreateSubtask(HttpExchange exchange) throws IOException {

        //    Предполагается что при запросе в теле будет приходить JSON следующего вида:
        //    {
        //        "name": "name",
        //        "description": "description",
        //        "status": "NEW",
        //        "parentId": "1"
        //        "startTime": null / "dd.MM.yyyy HH:mm:ss",
        //        "duration": 0
        //    }
        //    startTime и duration должны задаваться вместе: либо оба null/0, либо оба заданы корректно,
        //    чтобы проходить проверки.

        JsonObject jsonObject = parseJson(exchange);
        if (jsonObject == null) return;

        SubtaskInput subtaskInput = parseSubtaskInput(jsonObject, exchange);
        if (subtaskInput == null) return;

        try {
            Epic parent = taskManager.getEpic(subtaskInput.parentId);
            Subtask subtask;

            if (subtaskInput.startTime == null && subtaskInput.duration == 0) {
                subtask = taskManager.createSubtask(parent, subtaskInput.name,
                        subtaskInput.description, subtaskInput.status);
                sendText(exchange, gson.toJson(subtask));
            } else {
                if (!taskManager.isIntersection(subtaskInput.startTime, Duration.ofMinutes(subtaskInput.duration))) {
                    subtask = taskManager.createSubtask(parent, subtaskInput.name,
                            subtaskInput.description, subtaskInput.status);
                    taskManager.setStartTimeAndDuration(subtask, subtaskInput.startTime,
                            Duration.ofMinutes(subtaskInput.duration));
                    sendText(exchange, gson.toJson(subtask));
                } else {
                    throw new TimeInterectionException("Time intersection");
                }
            }

        } catch (TimeInterectionException e) {
            sendHasOverlaps(exchange, gson.toJson(new ApiResponse(406, "Subtask time overlaps")));
        } catch (IllegalArgumentException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, e.getMessage())));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Parent not found")));
        }
    }

    // POST /subtasks/{id}
    private void handleUpdateSubtask(HttpExchange exchange) throws IOException {

        //    Предполагается что при запросе в теле будет приходить JSON следующего вида:
        //    {
        //        "name": "name",
        //        "description": "description",
        //        "status": "NEW",
        //        "parentId": "1"
        //        "startTime": null / "dd.MM.yyyy HH:mm:ss",
        //        "duration": 0
        //    }
        //    startTime и duration должны задаваться вместе: либо оба null/0, либо оба заданы корректно,
        //    чтобы проходить проверки.

        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);

        JsonObject jsonObject = parseJson(exchange);
        if (jsonObject == null) return;

        SubtaskInput subtaskInput = parseSubtaskInput(jsonObject, exchange);
        if (subtaskInput == null) return;

        try {
            Subtask subtask = taskManager.getSubtask(id);

            if (subtask.getParentId() != subtaskInput.parentId) {
                badRequest(exchange, gson.toJson(new ApiResponse(400, "Cannot change parentId of existing subtask")));
                return;
            }

            taskManager.updateName(subtask, subtaskInput.name);
            taskManager.updateDescription(subtask, subtaskInput.description);
            taskManager.updateStatus(subtask, subtaskInput.status);
            taskManager.setStartTimeAndDuration(subtask, subtaskInput.startTime, Duration.ofMinutes(subtaskInput.duration));
            sendText(exchange, gson.toJson(subtask));

        } catch (TimeInterectionException e) {
            sendHasOverlaps(exchange, gson.toJson(new ApiResponse(406, "Subtask time overlaps")));
        } catch (IllegalArgumentException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, e.getMessage())));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Subtask not found")));
        }
    }

    // DELETE /subtasks/{id}
    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        boolean deleted = taskManager.deleteSubtask(id);
        if (deleted) {
            sendText(exchange, gson.toJson(new ApiResponse(200, "Success")));
        } else {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Subtask not found")));
        }
    }

    private SubtaskInput parseSubtaskInput(JsonObject jsonObject, HttpExchange exchange) throws IOException {
        // Проверка обязательных полей
        if (!jsonObject.has("name") || !jsonObject.has("description") ||
                !jsonObject.has("status") || !jsonObject.has("parentId") ||
                !jsonObject.has("startTime") || !jsonObject.has("duration")) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Missing required fields: name, description, status, parentId, startTime, duration")));
            return null;
        }

        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();

        // Status
        Status status;
        try {
            status = Status.valueOf(jsonObject.get("status").getAsString().toUpperCase());
        } catch (IllegalArgumentException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Invalid status value. Use: " + Arrays.toString(Status.values()))));
            return null;
        }

        // ParentId
        int parentId;
        try {
            parentId = jsonObject.get("parentId").getAsInt();
        } catch (Exception e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, "Invalid parentId value")));
            return null;
        }

        // StartTime
        LocalDateTime startTime = null;
        try {
            startTime = gson.fromJson(jsonObject.get("startTime"), LocalDateTime.class);
        } catch (DateTimeParseException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Invalid startTime format. Expected dd.MM.yyyy HH:mm:ss")));
        }

        // Duration
        long duration;
        try {
            duration = jsonObject.get("duration").getAsLong();
        } catch (Exception e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, "Invalid duration value")));
            return null;
        }

        return new SubtaskInput(name, description, status, parentId, startTime, duration);
    }
}