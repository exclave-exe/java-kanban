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

class PrioritizedHttpTest extends HttpServerBaseTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void testGetPrioritizedTasks() throws Exception {
        String task1Json = """
                {
                    "name": "Early Task",
                    "description": "Early Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        String task2Json = """
                {
                    "name": "Late Task",
                    "description": "Late Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 12:00:00",
                    "duration": 30
                }
                """;

        sendPost("/tasks", task1Json);
        sendPost("/tasks", task2Json);

        HttpResponse<String> response = sendGet("/prioritized");

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Early Task", prioritizedTasks.get(0).getName());
        assertEquals("Late Task", prioritizedTasks.get(1).getName());
    }

    @Test
    void testGetPrioritizedTasksWithSubtasks() throws Exception {
        String epicJson = """
                {
                    "name": "Test Epic",
                    "description": "Epic Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        int epicId = gson.fromJson(epicResp.body(), model.Epic.class).getId();

        String subtask1Json = """
                {
                    "name": "Early Subtask",
                    "description": "Early Subtask Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 09:00:00",
                    "duration": 45
                }
                """.formatted(epicId);

        String subtask2Json = """
                {
                    "name": "Late Subtask",
                    "description": "Late Subtask Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 11:00:00",
                    "duration": 60
                }
                """.formatted(epicId);

        sendPost("/subtasks", subtask1Json);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> response = sendGet("/prioritized");

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Early Subtask", prioritizedTasks.get(0).getName());
        assertEquals("Late Subtask", prioritizedTasks.get(1).getName());
    }

    @Test
    void testGetPrioritizedTasksMixed() throws Exception {
        String taskJson = """
                {
                    "name": "Task Late",
                    "description": "Task Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:30:00",
                    "duration": 30
                }
                """;
        sendPost("/tasks", taskJson);

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
                    "name": "Subtask Early",
                    "description": "Subtask Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 09:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);

        HttpResponse<String> response = sendGet("/prioritized");

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Subtask Early", prioritizedTasks.get(0).getName());
        assertEquals("Task Late", prioritizedTasks.get(1).getName());
    }

    @Test
    void testGetPrioritizedTasksWithoutTime() throws Exception {
        String task1Json = """
                {
                    "name": "Task Without Time",
                    "description": "Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;
        String task2Json = """
                {
                    "name": "Another Task Without Time",
                    "description": "Another Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;

        sendPost("/tasks", task1Json);
        sendPost("/tasks", task2Json);

        HttpResponse<String> response = sendGet("/prioritized");

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertTrue(prioritizedTasks.isEmpty(), "Задачи без времени не должны попадать в prioritized");
    }

    @Test
    void testGetPrioritizedTasksEmpty() throws Exception {
        HttpResponse<String> response = sendGet("/prioritized");

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertTrue(prioritizedTasks.isEmpty(), "Должен возвращаться пустой список когда нет задач с временем");
    }

    @Test
    void shouldReturn404ForInvalidEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/prioritized/invalid");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void testGetPrioritizedAfterTaskDeletion() throws Exception {
        String taskJson = """
                {
                    "name": "Task to Delete",
                    "description": "Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        HttpResponse<String> taskResp = sendPost("/tasks", taskJson);
        Task createdTask = gson.fromJson(taskResp.body(), Task.class);
        int taskId = createdTask.getId();

        HttpResponse<String> response1 = sendGet("/prioritized");
        List<Task> prioritizedTasks1 = gson.fromJson(response1.body(), new TaskListTypeToken().getType());
        assertEquals(1, prioritizedTasks1.size());

        sendDelete("/tasks/" + taskId);

        HttpResponse<String> response2 = sendGet("/prioritized");
        List<Task> prioritizedTasks2 = gson.fromJson(response2.body(), new TaskListTypeToken().getType());
        assertTrue(prioritizedTasks2.isEmpty());
    }
}