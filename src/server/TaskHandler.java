package server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.TaskInput;
import exceptions.NotFoundException;
import exceptions.TimeInterectionException;
import manager.TaskManager;
import model.ApiResponse;
import model.Endpoint;
import model.Status;
import model.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_ALL_TASKS -> handleGetAllTasks(exchange); // GET    /task
            case GET_TASK -> handleGetTask(exchange);          // GET    /task/{id}
            case CREATE_TASK -> handleCreateTask(exchange);    // POST   /task
            case UPDATE_TASK -> handleUpdateTask(exchange);    // POST   /task/{id}
            case DELETE_TASK -> handleDeleteTask(exchange);    // DELETE /task/{id}
            default -> sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Endpoint not found")));
        }
    }

    // GET /task
    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getAllTasks()));
    }

    // GET  /task/{id}
    private void handleGetTask(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            sendText(exchange, gson.toJson(taskManager.getTask(id)));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Task not found")));
        }
    }

    // POST /task
    private void handleCreateTask(HttpExchange exchange) throws IOException {

        //    Предполагается что при запросе в теле будет приходить JSON следующего вида:
        //    {
        //        "name": "name",
        //        "description": "description",
        //        "status": "NEW",
        //        "startTime": null / "dd.MM.yyyy HH:mm:ss",
        //        "duration": 0
        //    }
        //    startTime и duration должны задаваться вместе: либо оба null/0, либо оба заданы корректно,
        //    чтобы проходить проверки.

        JsonObject jsonObject = parseJson(exchange);
        if (jsonObject == null) return;

        TaskInput taskInput = parseTaskInput(jsonObject, exchange);
        if (taskInput == null) return;

        try {
            Task task;

            if (taskInput.startTime == null && taskInput.duration == 0) {
                task = taskManager.createTask(taskInput.name, taskInput.description, taskInput.status);
                sendText(exchange, gson.toJson(task));
            } else {
                if (!taskManager.isIntersection(taskInput.startTime, Duration.ofMinutes(taskInput.duration))) {
                    task = taskManager.createTask(taskInput.name, taskInput.description, taskInput.status);
                    taskManager.setStartTimeAndDuration(task, taskInput.startTime, Duration.ofMinutes(taskInput.duration));
                    sendText(exchange, gson.toJson(task));
                } else {
                    throw new TimeInterectionException("Time intersection");
                }
            }

        } catch (TimeInterectionException e) {
            sendHasOverlaps(exchange, gson.toJson(new ApiResponse(406, "Task time overlaps")));
        } catch (IllegalArgumentException e) { // Возникает в setStartTimeAndDuration
            badRequest(exchange, gson.toJson(new ApiResponse(400, e.getMessage())));
        }
    }

    // POST /task/{id}
    private void handleUpdateTask(HttpExchange exchange) throws IOException {

        //    Предполагается что при запросе в теле будет приходить JSON следующего вида:
        //    {
        //        "name": "name",
        //        "description": "description",
        //        "status": "NEW",
        //        "startTime": null / "dd.MM.yyyy HH:mm:ss",
        //        "duration": 0
        //    }
        //    startTime и duration должны задаваться вместе: либо оба null/0, либо оба заданы корректно,
        //    чтобы проходить проверки.

        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);

        JsonObject jsonObject = parseJson(exchange);
        if (jsonObject == null) return;

        TaskInput taskInput = parseTaskInput(jsonObject, exchange);
        if (taskInput == null) return;

        try {
            Task task = taskManager.getTask(id);
            taskManager.updateName(task, taskInput.name);
            taskManager.updateDescription(task, taskInput.description);
            taskManager.updateStatus(task, taskInput.status);
            taskManager.setStartTimeAndDuration(task, taskInput.startTime, Duration.ofMinutes(taskInput.duration));
            sendText(exchange, gson.toJson(task));
        } catch (TimeInterectionException e) {
            sendHasOverlaps(exchange, gson.toJson(new ApiResponse(406, "Task time overlaps")));
        } catch (IllegalArgumentException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, e.getMessage())));
        } catch (NotFoundException e) {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Task not found")));
        }
    }

    // DELETE /task/{id}
    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        int id = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        boolean deleted = taskManager.deleteTask(id);
        if (deleted) {
            sendText(exchange, gson.toJson(new ApiResponse(200, "Success")));
        } else {
            sendNotFound(exchange, gson.toJson(new ApiResponse(404, "Task not found")));
        }
    }

    // DTO для входных данных
    private TaskInput parseTaskInput(JsonObject jsonObject, HttpExchange exchange) throws IOException {
        if (!jsonObject.has("name") || !jsonObject.has("description") ||
                !jsonObject.has("status") || !jsonObject.has("startTime") ||
                !jsonObject.has("duration")) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Missing required fields: name, description, status, startTime or duration")));
            return null;
        }

        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();

        // status
        Status status;
        try {
            status = Status.valueOf(jsonObject.get("status").getAsString());
        } catch (IllegalArgumentException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, "Invalid status value")));
            return null;
        }

        // startTime
        LocalDateTime startTime = null;
        try {
            startTime = gson.fromJson(jsonObject.get("startTime"), LocalDateTime.class);
        } catch (DateTimeParseException e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400,
                    "Invalid startTime format. Expected dd.MM.yyyy HH:mm:ss")));
        }

        // duration
        long duration;
        try {
            duration = jsonObject.get("duration").getAsLong();
        } catch (Exception e) {
            badRequest(exchange, gson.toJson(new ApiResponse(400, "Invalid duration value")));
            return null;
        }

        return new TaskInput(name, description, status, startTime, duration);
    }
}