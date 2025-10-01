package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
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
                    "status": "NEW"
                }
                """;
        String task2Json = """
                {
                    "name": "Task 2",
                    "description": "Description 2",
                    "status": "NEW"
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
                    "status": "NEW"
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
                    "status": "NEW"
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
                    "parentId": %d
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
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void testGetHistoryDuplicatesRemoved() throws Exception {
        String taskJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "NEW"
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

    @Test
    void testGetHistoryWithEpicAndSubtasks() throws Exception {
        String epicJson = """
                {
                    "name": "Parent Epic",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Subtask 1 Description",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Subtask 2 Description",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId);

        HttpResponse<String> subtask1Resp = sendPost("/subtasks", subtask1Json);
        HttpResponse<String> subtask2Resp = sendPost("/subtasks", subtask2Json);
        Task subtask1 = gson.fromJson(subtask1Resp.body(), Task.class);
        Task subtask2 = gson.fromJson(subtask2Resp.body(), Task.class);

        // Получаем в разном порядке
        sendGet("/epics/" + epicId);
        sendGet("/subtasks/" + subtask1.getId());
        sendGet("/subtasks/" + subtask2.getId());
        sendGet("/epics/" + epicId); // Дубликат

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertEquals(3, history.size(), "Дубликаты должны удаляться");
    }

    @Test
    void testGetHistoryAfterSubtaskDeletion() throws Exception {
        String epicJson = """
                {
                    "name": "Parent Epic",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtaskJson = """
                {
                    "name": "Subtask to Delete",
                    "description": "Subtask Description",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        HttpResponse<String> subtaskResp = sendPost("/subtasks", subtaskJson);
        Task subtask = gson.fromJson(subtaskResp.body(), Task.class);
        int subtaskId = subtask.getId();

        sendGet("/epics/" + epicId);
        sendGet("/subtasks/" + subtaskId);

        HttpResponse<String> historyResp1 = sendGet("/history");
        List<Task> history1 = gson.fromJson(historyResp1.body(), new TaskListTypeToken().getType());
        assertEquals(2, history1.size());

        sendDelete("/subtasks/" + subtaskId);

        HttpResponse<String> historyResp2 = sendGet("/history");
        List<Task> history2 = gson.fromJson(historyResp2.body(), new TaskListTypeToken().getType());
        assertEquals(1, history2.size());
        assertEquals(epicId, history2.getFirst().getId());
    }

    @Test
    void testGetHistoryAfterEpicDeletion() throws Exception {
        String epicJson = """
                {
                    "name": "Epic to Delete",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtaskJson = """
                {
                    "name": "Subtask",
                    "description": "Subtask Description",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        HttpResponse<String> subtaskResp = sendPost("/subtasks", subtaskJson);
        Task subtask = gson.fromJson(subtaskResp.body(), Task.class);
        int subtaskId = subtask.getId();

        sendGet("/epics/" + epicId);
        sendGet("/subtasks/" + subtaskId);

        HttpResponse<String> historyResp1 = sendGet("/history");
        List<Task> history1 = gson.fromJson(historyResp1.body(), new TaskListTypeToken().getType());
        assertEquals(2, history1.size());

        sendDelete("/epics/" + epicId);

        HttpResponse<String> historyResp2 = sendGet("/history");
        List<Task> history2 = gson.fromJson(historyResp2.body(), new TaskListTypeToken().getType());
        assertTrue(history2.isEmpty(), "История должна быть пустой после удаления эпика с подзадачами");
    }

    @Test
    void testGetHistoryComplexScenario() throws Exception {
        // Создаем задачи
        String task1Json = """
                {
                    "name": "Task 1",
                    "description": "Description 1",
                    "status": "NEW"
                }
                """;
        String task2Json = """
                {
                    "name": "Task 2",
                    "description": "Description 2",
                    "status": "IN_PROGRESS"
                }
                """;

        HttpResponse<String> task1Resp = sendPost("/tasks", task1Json);
        HttpResponse<String> task2Resp = sendPost("/tasks", task2Json);
        Task task1 = gson.fromJson(task1Resp.body(), Task.class);
        Task task2 = gson.fromJson(task2Resp.body(), Task.class);

        // Создаем эпик с подзадачами
        String epicJson = """
                {
                    "name": "Complex Epic",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtaskJson = """
                {
                    "name": "Complex Subtask",
                    "description": "Subtask Description",
                    "status": "DONE",
                    "parentId": %d
                }
                """.formatted(epicId);
        HttpResponse<String> subtaskResp = sendPost("/subtasks", subtaskJson);
        Task subtask = gson.fromJson(subtaskResp.body(), Task.class);

        sendGet("/tasks/" + task1.getId());
        sendGet("/epics/" + epicId);
        sendGet("/tasks/" + task2.getId());
        sendGet("/subtasks/" + subtask.getId());
        sendGet("/tasks/" + task1.getId()); // Дубликат
        sendGet("/epics/" + epicId); // Дубликат

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertEquals(4, history.size(), "Должны быть все уникальные задачи в правильном порядке");

        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(subtask.getId(), history.get(1).getId());
    }

    @Test
    void testGetHistoryWithTasksWithTime() throws Exception {
        String taskJson = """
                {
                    "name": "Timed Task",
                    "description": "Task with time",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        HttpResponse<String> taskResp = sendPost("/tasks", taskJson);
        Task task = gson.fromJson(taskResp.body(), Task.class);

        sendGet("/tasks/" + task.getId());

        HttpResponse<String> historyResp = sendGet("/history");

        assertEquals(200, historyResp.statusCode());

        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        System.out.println(history);
        assertEquals(1, history.size());

        Task historyTask = history.getFirst();
        assertEquals("Timed Task", historyTask.getName());
        assertEquals("2024-06-17T10:00", historyTask.getStartTime().toString());
        assertEquals(60, historyTask.getDurationTime().toMinutes());
    }
}