package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.ApiResponse;
import model.Task;
import org.junit.jupiter.api.Test;
import util.TaskListTypeToken;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryHttpTest extends HttpServerBaseTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void testGetHistoryOrder() throws Exception {
        String task1Json = """
                {
                    "name": "Task 1",
                    "description": "Description 1",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;
        String task2Json = """
                {
                    "name": "Task 2",
                    "description": "Description 2",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;

        HttpResponse<String> task1Resp = sendPost("/tasks", task1Json);
        HttpResponse<String> task2Resp = sendPost("/tasks", task2Json);
        Task task1 = gson.fromJson(task1Resp.body(), Task.class);
        Task task2 = gson.fromJson(task2Resp.body(), Task.class);

        sendGet("/tasks/" + task1.getId());
        sendGet("/tasks/" + task2.getId());

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }

    @Test
    void testGetHistoryEmpty() throws Exception {
        HttpResponse<String> response = sendGet("/history");

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertTrue(history.isEmpty(), "Должен возвращаться пустой список когда история пуста");
    }

    @Test
    void testGetHistoryAfterDeletion() throws Exception {
        String taskJson = """
                {
                    "name": "Task to Delete",
                    "description": "Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;
        HttpResponse<String> taskResp = sendPost("/tasks", taskJson);
        Task createdTask = gson.fromJson(taskResp.body(), Task.class);
        int taskId = createdTask.getId();

        sendGet("/tasks/" + taskId);


        HttpResponse<String> historyResp1 = sendGet("/history");
        List<Task> history1 = gson.fromJson(historyResp1.body(), new TaskListTypeToken().getType());
        assertEquals(1, history1.size());

        sendDelete("/tasks/" + taskId);

        HttpResponse<String> historyResp2 = sendGet("/history");
        List<Task> history2 = gson.fromJson(historyResp2.body(), new TaskListTypeToken().getType());
        assertTrue(history2.isEmpty());
    }

    @Test
    void testGetHistoryWithMixedTypes() throws Exception {
        String taskJson = """
                {
                    "name": "Regular Task",
                    "description": "Task Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;
        HttpResponse<String> taskResp = sendPost("/tasks", taskJson);
        Task task = gson.fromJson(taskResp.body(), Task.class);

        String epicJson = """
                {
                    "name": "Test Epic",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtaskJson = """
                {
                    "name": "Test Subtask",
                    "description": "Subtask Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        HttpResponse<String> subtaskResp = sendPost("/subtasks", subtaskJson);
        Task subtask = gson.fromJson(subtaskResp.body(), Task.class);

        sendGet("/tasks/" + task.getId());
        sendGet("/epics/" + epicId);
        sendGet("/subtasks/" + subtask.getId());

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertEquals(3, history.size());
    }

    @Test
    void shouldReturn404ForInvalidEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/history/invalid");

        assertEquals(404, response.statusCode());
        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(404, apiResponse.getStatus());
        assertEquals("Endpoint not found", apiResponse.getMessage());
    }

    @Test
    void testGetHistoryDuplicatesRemoved() throws Exception {
        String taskJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;
        HttpResponse<String> taskResp = sendPost("/tasks", taskJson);
        Task createdTask = gson.fromJson(taskResp.body(), Task.class);
        int taskId = createdTask.getId();

        sendGet("/tasks/" + taskId);
        sendGet("/tasks/" + taskId);
        sendGet("/tasks/" + taskId);

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertEquals(1, history.size(), "Дубликаты должны удаляться из истории");
        assertEquals(taskId, history.getFirst().getId());
    }
}