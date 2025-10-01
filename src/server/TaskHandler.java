package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TimeArgumentException;
import exceptions.TimeInterectionException;
import exceptions.TimeSyntaxException;
import manager.TaskManager;
import model.Endpoint;
import model.Task;

import java.io.IOException;
import java.util.List;

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
            default -> sendNotFound(exchange);
        }
    }

    // GET /task
    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendText(exchange, gson.toJson(tasks));
    }

    // GET  /task/{id}
    private void handleGetTask(HttpExchange exchange) throws IOException {
        try {
            Task task = taskManager.getTask(getRequestId(exchange));
            sendText(exchange, gson.toJson(task));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    // POST /task
    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Task taskFromRequest = gson.fromJson(body, Task.class);
            if (isTaskDataInvalid(taskFromRequest)) {
                throw new IllegalArgumentException("Invalid task data");
            }
            if (isTimeInvalid(taskFromRequest)) {
                throw new TimeArgumentException("Invalid time parameters");
            }
            if (taskManager.isIntersection(taskFromRequest.getStartTime(), taskFromRequest.getDurationTime())) {
                throw new TimeInterectionException("Time intersection");
            }

            Task taskFromManager = taskManager.createTask(taskFromRequest.getName(), taskFromRequest.getDescription(),
                    taskFromRequest.getStatus());

            taskManager.setStartTimeAndDuration(taskFromManager, taskFromRequest.getStartTime(),
                    taskFromRequest.getDurationTime());

            sendText(exchange, gson.toJson(taskFromManager));

        } catch (JsonSyntaxException exception) {
            sendBadRequest(exchange, "Invalid Json");
        } catch (TimeSyntaxException | IllegalArgumentException | TimeArgumentException exception) {
            sendBadRequest(exchange, exception.getMessage());
        } catch (TimeInterectionException exception) {
            sendHasOverlaps(exchange);
        }
    }

    // POST /task/{id}
    private void handleUpdateTask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Task taskFromRequest = gson.fromJson(body, Task.class);
            if (isTaskDataInvalid(taskFromRequest)) {
                throw new IllegalArgumentException("Invalid task data");
            }
            if (isTimeInvalid(taskFromRequest)) {
                throw new TimeArgumentException("Invalid time parameters");
            }

            Task taskFromManager = taskManager.getTask(getRequestId(exchange));

            taskManager.setStartTimeAndDuration(taskFromManager, taskFromRequest.getStartTime(),
                    taskFromRequest.getDurationTime());
            taskManager.updateName(taskFromManager, taskFromRequest.getName());
            taskManager.updateDescription(taskFromManager, taskFromRequest.getDescription());
            taskManager.updateStatus(taskFromManager, taskFromRequest.getStatus());

            sendText(exchange, gson.toJson(taskFromManager));

        } catch (JsonSyntaxException exception) {
            sendBadRequest(exchange, "Invalid Json");
        } catch (TimeSyntaxException | IllegalArgumentException | TimeArgumentException exception) {
            sendBadRequest(exchange, exception.getMessage());
        } catch (NotFoundException exception) {
            sendNotFound(exchange);
        } catch (TimeInterectionException exception) {
            sendHasOverlaps(exchange);
        }
    }

    // DELETE /task/{id}
    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        boolean deleted = taskManager.deleteTask(getRequestId(exchange));
        if (deleted) {
            sendResponse(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private boolean isTaskDataInvalid(Task task) {
        return task.getName() == null || task.getDescription() == null || task.getStatus() == null;
    }
}