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
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = determineEndpoint(exchange.getRequestMethod(), exchange.getRequestURI().getPath());

        switch (endpoint) {
            case GET_ALL_SUBTASKS -> handleGetAllSubtasks(exchange);        // GET    /subtasks
            case GET_SUBTASK -> handleGetSubtask(exchange);                 // GET    /subtasks/{id}
            case CREATE_SUBTASK -> handleCreateSubtask(exchange);           // POST   /subtasks
            case UPDATE_SUBTASK -> handleUpdateSubtask(exchange);           // POST   /subtasks/{id}
            case DELETE_SUBTASK -> handleDeleteSubtask(exchange);           // DELETE /subtasks/{id}
            default -> sendNotFound(exchange);
        }
    }

    // GET /subtasks
    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        sendText(exchange, gson.toJson(subtasks));
    }

    // GET /subtasks/{id}
    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        try {
            Subtask subtask = taskManager.getSubtask(getRequestId(exchange));
            sendText(exchange, gson.toJson(subtask));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    // POST /subtasks
    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Subtask subtaskFromRequest = gson.fromJson(body, Subtask.class);
            if (isSubtaskDataInvalid(subtaskFromRequest)) {
                throw new IllegalArgumentException("Invalid subtask data");
            }
            if (isTimeInvalid(subtaskFromRequest)) {
                throw new TimeArgumentException("Invalid time parameters");
            }
            if (taskManager.isIntersection(subtaskFromRequest.getStartTime(), subtaskFromRequest.getDurationTime())) {
                throw new TimeInterectionException("Time intersection");
            }

            Epic parentEpic = taskManager.getEpic(subtaskFromRequest.getParentId());

            Subtask subtaskFromManager = taskManager.createSubtask(
                    parentEpic,
                    subtaskFromRequest.getName(),
                    subtaskFromRequest.getDescription(),
                    subtaskFromRequest.getStatus()
            );

            taskManager.setStartTimeAndDuration(subtaskFromManager, subtaskFromRequest.getStartTime(),
                    subtaskFromRequest.getDurationTime());

            sendText(exchange, gson.toJson(subtaskFromManager));

        } catch (JsonSyntaxException exception) {
            sendBadRequest(exchange, "Invalid Json");
        } catch (TimeSyntaxException | IllegalArgumentException | TimeArgumentException exception) {
            sendBadRequest(exchange, exception.getMessage());
        } catch (TimeInterectionException exception) {
            sendHasOverlaps(exchange);
        } catch (NotFoundException exception) {
            sendBadRequest(exchange, "Parent epic not found");
        }
    }

    // POST /subtasks/{id}
    private void handleUpdateSubtask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Subtask subtaskFromRequest = gson.fromJson(body, Subtask.class);
            if (isSubtaskDataInvalid(subtaskFromRequest)) {
                throw new IllegalArgumentException("Invalid subtask data");
            }
            if (isTimeInvalid(subtaskFromRequest)) {
                throw new TimeArgumentException("Invalid time parameters");
            }

            Subtask subtaskFromManager = taskManager.getSubtask(getRequestId(exchange));

            if (subtaskFromRequest.getParentId() != subtaskFromManager.getParentId()) {
                throw new IllegalArgumentException("Cannot change parent epic of existing subtask");
            }

            taskManager.setStartTimeAndDuration(subtaskFromManager, subtaskFromRequest.getStartTime(),
                    subtaskFromRequest.getDurationTime());
            taskManager.updateName(subtaskFromManager, subtaskFromRequest.getName());
            taskManager.updateDescription(subtaskFromManager, subtaskFromRequest.getDescription());
            taskManager.updateStatus(subtaskFromManager, subtaskFromRequest.getStatus());

            sendText(exchange, gson.toJson(subtaskFromManager));

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

    // DELETE /subtasks/{id}
    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        boolean deleted = taskManager.deleteSubtask(getRequestId(exchange));
        if (deleted) {
            sendResponse(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private boolean isSubtaskDataInvalid(Subtask subtask) {
        return subtask.getName() == null ||
                subtask.getDescription() == null ||
                subtask.getStatus() == null ||
                subtask.getParentId() <= 0;
    }
}