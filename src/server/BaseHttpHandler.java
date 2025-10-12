package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Endpoint;
import model.Task;
import util.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler {
    protected final TaskManager taskManager;

    protected final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    protected BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendBadRequest(HttpExchange exchange, String text) throws IOException {
        byte[] resp = ("{\"error\":\"%s\"}".formatted(text)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(400, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        byte[] resp = "{\"error\":\"Not Found\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        byte[] resp = "{\"error\":\"Task time overlaps\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        byte[] resp = "{\"error\":\"Internal Server Error\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(500, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, -1);
        exchange.close();
    }

    protected Endpoint determineEndpoint(String method, String path) {
        String[] pathParts = path.split("/");

        switch (method) {
            case "GET":
                if (pathParts.length == 2) {
                    return switch (pathParts[1]) {
                        case "tasks" -> Endpoint.GET_ALL_TASKS;                // GET /tasks
                        case "epics" -> Endpoint.GET_ALL_EPICS;                // GET /epics
                        case "subtasks" -> Endpoint.GET_ALL_SUBTASKS;          // GET /subtasks
                        case "history" -> Endpoint.GET_HISTORY;                // GET /history
                        case "prioritized" -> Endpoint.GET_PRIORITIZED;        // GET /prioritized
                        default -> Endpoint.UNKNOWN;
                    };
                } else if (pathParts.length == 3 && isNumber(pathParts[2])) {
                    return switch (pathParts[1]) {
                        case "tasks" -> Endpoint.GET_TASK;                     // GET /tasks/{id}
                        case "epics" -> Endpoint.GET_EPIC;                     // GET /epics/{id}
                        case "subtasks" -> Endpoint.GET_SUBTASK;               // GET /subtasks/{id}
                        default -> Endpoint.UNKNOWN;
                    };
                } else if (pathParts.length == 4 && pathParts[1].equals("epics") && isNumber(pathParts[2])
                        && pathParts[3].equals("subtasks")) {
                    return Endpoint.GET_EPIC_SUBTASKS;                         // GET /epic/{id}/subtasks

                } else {
                    return Endpoint.UNKNOWN;
                }

            case "POST":
                if (pathParts.length == 2) {
                    return switch (pathParts[1]) {
                        case "tasks" -> Endpoint.CREATE_TASK;                  // POST /tasks
                        case "epics" -> Endpoint.CREATE_EPIC;                  // POST /epics
                        case "subtasks" -> Endpoint.CREATE_SUBTASK;            // POST /subtasks
                        default -> Endpoint.UNKNOWN;
                    };
                } else if (pathParts.length == 3 && isNumber(pathParts[2])) {
                    return switch (pathParts[1]) {
                        case "tasks" -> Endpoint.UPDATE_TASK;                  // POST /tasks/{id}
                        case "subtasks" -> Endpoint.UPDATE_SUBTASK;            // POST /subtasks/{id}
                        default -> Endpoint.UNKNOWN;
                    };
                } else {
                    return Endpoint.UNKNOWN;
                }

            case "DELETE":
                if (pathParts.length == 3 && isNumber(pathParts[2])) {
                    return switch (pathParts[1]) {
                        case "tasks" -> Endpoint.DELETE_TASK;                   // DELETE /tasks/{id}
                        case "epics" -> Endpoint.DELETE_EPIC;                   // DELETE /epic/{id}
                        case "subtasks" -> Endpoint.DELETE_SUBTASK;             // DELETE /subtask/{id}
                        default -> Endpoint.UNKNOWN;
                    };
                } else {
                    return Endpoint.UNKNOWN;
                }

            default:
                return Endpoint.UNKNOWN;
        }
    }

    private boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected int getRequestId(HttpExchange exchange) throws IOException {
        return Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
    }

    protected boolean isTimeInvalid(Task task) {
        return task.getStartTime() == null && !task.getDurationTime().isZero() ||
                task.getStartTime() != null && task.getDurationTime().isZero();
    }
}