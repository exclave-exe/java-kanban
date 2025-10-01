package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;
import util.TaskListTypeToken;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskHttpTest extends HttpServerBaseTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    // Get Task
    @Test
    void testGetAllTasks() throws Exception {
        String taskJson = """
                {
                    "name": "Contain",
                    "description": "Desc",
                    "status": "NEW"
                }
                """;
        sendPost("/tasks", taskJson);

        HttpResponse<String> getResp = sendGet("/tasks");

        List<Task> tasksList = gson.fromJson(getResp.body(), new TaskListTypeToken().getType());
        assertEquals(200, getResp.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(1, tasksList.size());
        assertEquals(tasksList.getFirst(), taskManager.getAllTasks().getFirst());
    }

    @Test
    void testGetAllTasksEmpty() throws Exception {
        HttpResponse<String> getResp = sendGet("/tasks");

        assertEquals(200, getResp.statusCode());
        assertEquals(0, taskManager.getAllTasks().size());
        assertEquals("[]", getResp.body());
    }

    // Create Task
    @Test
    void testCreateTaskWithoutTime() throws Exception {
        String taskJson = """
                {
                    "name": "WithoutTime",
                    "description": "Test Description",
                    "status": "NEW"
                }
                """;

        HttpResponse<String> postResp = sendPost("/tasks", taskJson);

        assertEquals(200, postResp.statusCode());
        Task createdTask = gson.fromJson(postResp.body(), Task.class);
        assertEquals("WithoutTime", createdTask.getName());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Status.NEW, createdTask.getStatus());
        assertEquals(Duration.ZERO, createdTask.getDurationTime());
        assertNull(createdTask.getStartTime());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void testCreateTaskWithTime() throws Exception {
        String taskJson = """
                {
                    "name": "Timed Task",
                    "description": "Test Description",
                    "status": "IN_PROGRESS",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;

        HttpResponse<String> postResp = sendPost("/tasks", taskJson);

        assertEquals(200, postResp.statusCode());
        Task createdTask = gson.fromJson(postResp.body(), Task.class);
        assertEquals("Timed Task", createdTask.getName());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Status.IN_PROGRESS, createdTask.getStatus());
        assertEquals(60, createdTask.getDurationTime().toMinutes());
        assertEquals("2024-06-17T10:00", createdTask.getStartTime().toString());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    // Update task
    @Test
    void testUpdateTaskAddTime() throws Exception {
        String taskJson = """
                {
                    "name": "Old Name",
                    "description": "Old Desc",
                    "status": "NEW"
                }
                """;
        sendPost("/tasks", taskJson);
        int taskId = taskManager.getAllTasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "New Name",
                    "description": "New Desc",
                    "status": "IN_PROGRESS",
                    "startTime": "15.06.2000 00:00:00",
                    "duration": 60
                }
                """;

        HttpResponse<String> updateResp = sendPost("/tasks/" + taskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Task updatedTask = gson.fromJson(updateResp.body(), Task.class);
        assertEquals("New Name", updatedTask.getName());
        assertEquals("New Desc", updatedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
        assertEquals(60, updatedTask.getDurationTime().toMinutes());
        assertEquals("2000-06-15T00:00", updatedTask.getStartTime().toString());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void testUpdateTaskChangeTime() throws Exception {
        String taskJson = """
                {
                    "name": "Task With Time",
                    "description": "Has start time",
                    "status": "NEW",
                    "startTime": "01.01.2020 10:00:00",
                    "duration": 30
                }
                """;
        sendPost("/tasks", taskJson);
        int taskId = taskManager.getAllTasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "Task With New Time",
                    "description": "Updated Desc",
                    "status": "IN_PROGRESS",
                    "startTime": "02.01.2020 15:00:00",
                    "duration": 45
                }
                """;

        HttpResponse<String> updateResp = sendPost("/tasks/" + taskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Task updatedTask = gson.fromJson(updateResp.body(), Task.class);
        assertEquals("Task With New Time", updatedTask.getName());
        assertEquals("Updated Desc", updatedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
        assertEquals(45, updatedTask.getDurationTime().toMinutes());
        assertEquals("2020-01-02T15:00", updatedTask.getStartTime().toString());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void testUpdateTaskRemoveTime() throws Exception {
        String taskJson = """
                {
                    "name": "Task With Time",
                    "description": "Has time initially",
                    "status": "NEW",
                    "startTime": "01.01.2020 12:00:00",
                    "duration": 20
                }
                """;
        sendPost("/tasks", taskJson);
        int taskId = taskManager.getAllTasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "Task No Time",
                    "description": "Removed time",
                    "status": "DONE",
                    "startTime": null,
                    "duration": 0
                }
                """;

        HttpResponse<String> updateResp = sendPost("/tasks/" + taskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Task updatedTask = gson.fromJson(updateResp.body(), Task.class);
        assertEquals("Task No Time", updatedTask.getName());
        assertEquals("Removed time", updatedTask.getDescription());
        assertEquals(Status.DONE, updatedTask.getStatus());
        assertEquals(Duration.ZERO, updatedTask.getDurationTime());
        assertNull(updatedTask.getStartTime());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    // Delete Task
    @Test
    void testDeleteTask() throws Exception {
        String taskJson = """
                {
                    "name": "To Delete",
                    "description": "Desc",
                    "status": "NEW"
                }
                """;
        sendPost("/tasks", taskJson);
        int taskId = taskManager.getAllTasks().get(0).getId();
        HttpResponse<String> deleteResp = sendDelete("/tasks/" + taskId);
        assertEquals(201, deleteResp.statusCode());
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    void testGetTaskById() throws Exception {
        String taskJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "IN_PROGRESS"
                }
                """;
        HttpResponse<String> postResp = sendPost("/tasks", taskJson);
        Task createdTask = gson.fromJson(postResp.body(), Task.class);
        int taskId = createdTask.getId();

        HttpResponse<String> getResp = sendGet("/tasks/" + taskId);

        assertEquals(200, getResp.statusCode());
        Task retrievedTask = gson.fromJson(getResp.body(), Task.class);
        assertEquals(createdTask, retrievedTask);
        assertEquals("Test Task", retrievedTask.getName());
        assertEquals("Test Description", retrievedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, retrievedTask.getStatus());
    }

    // Тесты ошибок
    @Test
    void shouldReturn404ForInvalidEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/tasks/invalid-endpoint");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenGettingNonExistentTask() throws Exception {
        HttpResponse<String> response = sendGet("/tasks/999");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        String updateJson = """
                {
                    "name": "Updated Task",
                    "description": "Updated Description",
                    "status": "IN_PROGRESS"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/999", updateJson);
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        HttpResponse<String> response = sendDelete("/tasks/999");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn406WhenCreatingTimeOverlaps() throws Exception {
        String task1Json = """
                {
                    "name": "First Task",
                    "description": "First Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        sendPost("/tasks", task1Json);

        String task2Json = """
                {
                    "name": "Second Task",
                    "description": "Second Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:30:00",
                    "duration": 60
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", task2Json);
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task time overlaps"));
    }

    @Test
    void shouldReturn406WhenUpdatingTimeOverlaps() throws Exception {
        String task1Json = """
                {
                    "name": "Task1",
                    "description": "Desc",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        HttpResponse<String> task1Resp = sendPost("/tasks", task1Json);
        Task task1 = gson.fromJson(task1Resp.body(), Task.class);
        int task1Id = task1.getId();

        String task2Json = """
                {
                    "name": "Task2",
                    "description": "Desc",
                    "status": "NEW"
                }
                """;
        HttpResponse<String> task2Resp = sendPost("/tasks", task2Json);
        Task task2 = gson.fromJson(task2Resp.body(), Task.class);
        int task2Id = task2.getId();

        String overlapJson = """
                {
                    "name": "Task2",
                    "description": "Desc",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:30:00",
                    "duration": 30
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/" + task2Id, overlapJson);
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task time overlaps"));
    }

    @Test
    void shouldReturn400WhenCreatingIncorrectDurationAndStartTime() throws Exception {
        String task1Json = """
                {
                    "name": "First Task",
                    "description": "First Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        sendPost("/tasks", task1Json);

        String task2Json = """
                {
                    "name": "Second Task",
                    "description": "Second Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 60
                }
                """;

        HttpResponse<String> response1 = sendPost("/tasks", task2Json);
        assertEquals(400, response1.statusCode());
        assertTrue(response1.body().contains("Invalid time parameters"));

        String task3Json = """
                {
                    "name": "Second Task",
                    "description": "Second Description",
                    "status": "NEW",
                    "startTime": "18.06.2024 10:00:00",
                    "duration": 0
                }
                """;

        HttpResponse<String> response2 = sendPost("/tasks", task3Json);
        assertEquals(400, response2.statusCode());
        assertTrue(response2.body().contains("Invalid time parameters"));
    }

    @Test
    void shouldReturn400WhenUpdatingIncorrectDurationAndStartTime() throws Exception {
        String task1Json = """
                {
                    "name": "First Task",
                    "description": "First Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """;
        sendPost("/tasks", task1Json);
        int idTask = taskManager.getAllTasks().getFirst().getId();

        String task2Json = """
                {
                    "name": "Second Task",
                    "description": "Second Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 60
                }
                """;

        HttpResponse<String> response1 = sendPost("/tasks/" + idTask, task2Json);
        assertEquals(400, response1.statusCode());
        assertTrue(response1.body().contains("Invalid time parameters"));

        String task3Json = """
                {
                    "name": "Second Task",
                    "description": "Second Description",
                    "status": "NEW",
                    "startTime": "18.06.2024 10:00:00",
                    "duration": 0
                }
                """;

        HttpResponse<String> response2 = sendPost("/tasks/" + idTask, task3Json);
        assertEquals(400, response2.statusCode());
        assertTrue(response2.body().contains("Invalid time parameters"));
    }

    @Test
    void shouldReturn400CreatingWhenMissingRequiredFields() throws Exception {
        String invalidJson = """
                {
                    "name": "Task without status"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data"));
    }

    @Test
    void shouldReturn400UpdatingWhenMissingRequiredFields() throws Exception {
        sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW"
                }
                """);
        int idTask = taskManager.getAllTasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Task without status"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/" + idTask, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidStatusValue() throws Exception {
        sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW"
                }
                """);
        int idTask = taskManager.getAllTasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "INVALID"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/" + idTask, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data") || response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidStatusValue() throws Exception {
        HttpResponse<String> response = sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "INVALID"
                }
                """);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data") || response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidStartTimeFormat() throws Exception {
        sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW"
                }
                """);
        int idTask = taskManager.getAllTasks().getFirst().getId();

        String invalidJson = """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW",
                    "startTime": "2024-01-01 10:00:00",
                    "duration": 60
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/" + idTask, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid date format"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidStartTimeFormat() throws Exception {
        HttpResponse<String> response = sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW",
                    "startTime": "2024-01-01 10:00:00",
                    "duration": 60
                }
                """);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid date format"));
    }

    @Test
    void shouldReturn400CreatingForInvalidJSON() throws Exception {
        String invalidJson = "{ invalid json }";

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400UpdatingForInvalidJSON() throws Exception {
        sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW"
                }
                """);
        int idTask = taskManager.getAllTasks().getFirst().getId();
        System.out.println(idTask);
        String invalidJson = "{ invalid json }";

        HttpResponse<String> response = sendPost("/tasks/" + idTask, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidDuration() throws Exception {
        String invalidJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": "invalid"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidDuration() throws Exception {
        sendPost("/tasks", """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": "NEW"
                }
                """);
        int idTask = taskManager.getAllTasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "NEW",
                    "startTime": "17.06.2024 10:00:00",
                    "duration": "invalid"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks/" + idTask, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    // Дополнительные тесты для полного покрытия

    @Test
    void shouldReturn400WhenCreatingTaskWithNullName() throws Exception {
        String invalidJson = """
                {
                    "name": null,
                    "description": "Desc",
                    "status": "NEW"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data"));
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithNullDescription() throws Exception {
        String invalidJson = """
                {
                    "name": "Task",
                    "description": null,
                    "status": "NEW"
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data"));
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithNullStatus() throws Exception {
        String invalidJson = """
                {
                    "name": "Task",
                    "description": "Desc",
                    "status": null
                }
                """;

        HttpResponse<String> response = sendPost("/tasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid task data"));
    }

    @Test
    void testUpdateTaskWithoutChangingTime() throws Exception {
        String taskJson = """
                {
                    "name": "Original Task",
                    "description": "Original Desc",
                    "status": "NEW",
                    "startTime": "01.01.2020 10:00:00",
                    "duration": 30
                }
                """;
        sendPost("/tasks", taskJson);
        int taskId = taskManager.getAllTasks().getFirst().getId();

        String updateJson = """
                {
                    "name": "Updated Task",
                    "description": "Updated Desc",
                    "status": "DONE"
                }
                """;

        HttpResponse<String> updateResp = sendPost("/tasks/" + taskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Task updatedTask = gson.fromJson(updateResp.body(), Task.class);
        assertEquals("Updated Task", updatedTask.getName());
        assertEquals("Updated Desc", updatedTask.getDescription());
        assertEquals(Status.DONE, updatedTask.getStatus());
        assertEquals(0, updatedTask.getDurationTime().toMinutes());
        assertNull(updatedTask.getStartTime());
    }

    @Test
    void testCreateMultipleTasks() throws Exception {
        String task1Json = """
                {
                    "name": "Task 1",
                    "description": "Desc 1",
                    "status": "NEW"
                }
                """;
        String task2Json = """
                {
                    "name": "Task 2",
                    "description": "Desc 2",
                    "status": "IN_PROGRESS"
                }
                """;

        sendPost("/tasks", task1Json);
        sendPost("/tasks", task2Json);

        HttpResponse<String> getResp = sendGet("/tasks");
        List<Task> tasks = gson.fromJson(getResp.body(), new TaskListTypeToken().getType());

        assertEquals(200, getResp.statusCode());
        assertEquals(2, tasks.size());
        assertEquals(2, taskManager.getAllTasks().size());
    }
}