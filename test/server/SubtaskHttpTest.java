package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;
import util.SubtaskListTypeToken;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHttpTest extends HttpServerBaseTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    private int createEpicForSubtask() throws Exception {
        String epicJson = """
                {
                    "name": "Parent Epic",
                    "description": "Epic for subtasks"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        Epic createdEpic = gson.fromJson(epicResp.body(), Epic.class);
        return createdEpic.getId();
    }

    // Get Subtask
    @Test
    void testGetAllSubtasks() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Contain",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);

        HttpResponse<String> getResp = sendGet("/subtasks");

        List<Subtask> subtasksList = gson.fromJson(getResp.body(), new SubtaskListTypeToken().getType());
        assertEquals(200, getResp.statusCode());
        assertEquals(1, taskManager.getAllSubtasks().size());
        assertEquals(1, subtasksList.size());
        assertEquals(subtasksList.getFirst(), taskManager.getAllSubtasks().getFirst());
    }

    @Test
    void testGetAllSubtasksEmpty() throws Exception {
        HttpResponse<String> getResp = sendGet("/subtasks");

        assertEquals(200, getResp.statusCode());
        assertEquals(0, taskManager.getAllSubtasks().size());
        assertEquals("[]", getResp.body());
    }

    @Test
    void testGetSubtaskById() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Test Subtask",
                    "description": "Test Description",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId);
        HttpResponse<String> postResp = sendPost("/subtasks", subtaskJson);
        Subtask createdSubtask = gson.fromJson(postResp.body(), Subtask.class);
        int subtaskId = createdSubtask.getId();

        HttpResponse<String> getResp = sendGet("/subtasks/" + subtaskId);

        assertEquals(200, getResp.statusCode());
        Subtask retrievedSubtask = gson.fromJson(getResp.body(), Subtask.class);
        assertEquals(createdSubtask, retrievedSubtask);
        assertEquals("Test Subtask", retrievedSubtask.getName());
        assertEquals("Test Description", retrievedSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, retrievedSubtask.getStatus());
        assertEquals(epicId, retrievedSubtask.getParentId());
    }

    // Create Subtask
    @Test
    void testCreateSubtaskWithoutTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "WithoutTime",
                    "description": "Test Description",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);

        HttpResponse<String> postResp = sendPost("/subtasks", subtaskJson);

        assertEquals(200, postResp.statusCode());
        Subtask createdSubtask = gson.fromJson(postResp.body(), Subtask.class);
        assertEquals("WithoutTime", createdSubtask.getName());
        assertEquals("Test Description", createdSubtask.getDescription());
        assertEquals(Status.NEW, createdSubtask.getStatus());
        assertEquals(epicId, createdSubtask.getParentId());
        assertEquals(Duration.ZERO, createdSubtask.getDurationTime());
        assertNull(createdSubtask.getStartTime());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testCreateSubtaskWithTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Timed Subtask",
                    "description": "Test Description",
                    "status": "IN_PROGRESS",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> postResp = sendPost("/subtasks", subtaskJson);

        assertEquals(200, postResp.statusCode());
        Subtask createdSubtask = gson.fromJson(postResp.body(), Subtask.class);
        assertEquals("Timed Subtask", createdSubtask.getName());
        assertEquals("Test Description", createdSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, createdSubtask.getStatus());
        assertEquals(epicId, createdSubtask.getParentId());
        assertEquals(60, createdSubtask.getDurationTime().toMinutes());
        assertEquals("2024-06-17T10:00", createdSubtask.getStartTime().toString());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    // Update subtask
    @Test
    void testUpdateSubtaskAddTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Old Name",
                    "description": "Old Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "New Name",
                    "description": "New Desc",
                    "status": "IN_PROGRESS",
                    "parentId": %d,
                    "startTime": "15.06.2000 00:00:00",
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> updateResp = sendPost("/subtasks/" + subtaskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Subtask updatedSubtask = gson.fromJson(updateResp.body(), Subtask.class);
        assertEquals("New Name", updatedSubtask.getName());
        assertEquals("New Desc", updatedSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedSubtask.getStatus());
        assertEquals(60, updatedSubtask.getDurationTime().toMinutes());
        assertEquals("2000-06-15T00:00", updatedSubtask.getStartTime().toString());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testUpdateSubtaskChangeTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Subtask With Time",
                    "description": "Has start time",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "01.01.2020 10:00:00",
                    "duration": 30
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "Subtask With New Time",
                    "description": "Updated Desc",
                    "status": "IN_PROGRESS",
                    "parentId": %d,
                    "startTime": "02.01.2020 15:00:00",
                    "duration": 45
                }
                """.formatted(epicId);

        HttpResponse<String> updateResp = sendPost("/subtasks/" + subtaskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Subtask updatedSubtask = gson.fromJson(updateResp.body(), Subtask.class);
        assertEquals("Subtask With New Time", updatedSubtask.getName());
        assertEquals("Updated Desc", updatedSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedSubtask.getStatus());
        assertEquals(45, updatedSubtask.getDurationTime().toMinutes());
        assertEquals("2020-01-02T15:00", updatedSubtask.getStartTime().toString());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testUpdateSubtaskRemoveTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Subtask With Time",
                    "description": "Has time initially",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "01.01.2020 12:00:00",
                    "duration": 20
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String updateJson = """
                {
                    "name": "Subtask No Time",
                    "description": "Removed time",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);

        HttpResponse<String> updateResp = sendPost("/subtasks/" + subtaskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Subtask updatedSubtask = gson.fromJson(updateResp.body(), Subtask.class);
        assertEquals("Subtask No Time", updatedSubtask.getName());
        assertEquals("Removed time", updatedSubtask.getDescription());
        assertEquals(Status.DONE, updatedSubtask.getStatus());
        assertEquals(Duration.ZERO, updatedSubtask.getDurationTime());
        assertNull(updatedSubtask.getStartTime());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testUpdateSubtaskWithoutChangingTime() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "Original Subtask",
                    "description": "Original Desc",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "01.01.2020 10:00:00",
                    "duration": 30
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();

        String updateJson = """
                {
                    "name": "Updated Subtask",
                    "description": "Updated Desc",
                    "status": "DONE",
                    "parentId": %d
                }
                """.formatted(epicId);

        HttpResponse<String> updateResp = sendPost("/subtasks/" + subtaskId, updateJson);

        assertEquals(200, updateResp.statusCode());
        Subtask updatedSubtask = gson.fromJson(updateResp.body(), Subtask.class);
        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals("Updated Desc", updatedSubtask.getDescription());
        assertEquals(Status.DONE, updatedSubtask.getStatus());
        assertEquals(0, updatedSubtask.getDurationTime().toMinutes());
        assertNull(updatedSubtask.getStartTime());
    }

    // Delete Subtask
    @Test
    void testDeleteSubtask() throws Exception {
        int epicId = createEpicForSubtask();
        String subtaskJson = """
                {
                    "name": "To Delete",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        HttpResponse<String> deleteResp = sendDelete("/subtasks/" + subtaskId);
        assertEquals(201, deleteResp.statusCode());
        assertEquals(0, taskManager.getAllSubtasks().size());
    }

    // Тесты ошибок
    @Test
    void shouldReturn404ForInvalidEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/subtasks/invalid-endpoint");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenGettingNonExistentSubtask() throws Exception {
        HttpResponse<String> response = sendGet("/subtasks/999");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentSubtask() throws Exception {
        int epicId = createEpicForSubtask();
        String updateJson = """
                {
                    "name": "Updated Subtask",
                    "description": "Updated Description",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks/999", updateJson);
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSubtask() throws Exception {
        HttpResponse<String> response = sendDelete("/subtasks/999");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturn400WhenCreatingWithNonExistentParent() throws Exception {
        String subtaskJson = """
                {
                    "name": "Orphan Subtask",
                    "description": "No Parent",
                    "status": "NEW",
                    "parentId": 999
                }
                """;

        HttpResponse<String> response = sendPost("/subtasks", subtaskJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Parent epic not found"));
    }

    @Test
    void shouldReturn406WhenCreatingTimeOverlaps() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "First Subtask",
                    "description": "First Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Second Subtask",
                    "description": "Second Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:30:00",
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks", subtask2Json);
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task time overlaps"));
    }

    @Test
    void shouldReturn406WhenUpdatingTimeOverlaps() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "Subtask1",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Subtask2",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        HttpResponse<String> subtask2Resp = sendPost("/subtasks", subtask2Json);
        Subtask subtask2 = gson.fromJson(subtask2Resp.body(), Subtask.class);
        int subtask2Id = subtask2.getId();

        String overlapJson = """
                {
                    "name": "Subtask2",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:30:00",
                    "duration": 30
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks/" + subtask2Id, overlapJson);
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task time overlaps"));
    }

    @Test
    void shouldReturn400WhenCreatingIncorrectDurationAndStartTime() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "First Subtask",
                    "description": "First Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Second Subtask",
                    "description": "Second Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> response1 = sendPost("/subtasks", subtask2Json);
        assertEquals(400, response1.statusCode());
        assertTrue(response1.body().contains("Invalid time parameters"));

        String subtask3Json = """
                {
                    "name": "Third Subtask",
                    "description": "Third Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "18.06.2024 10:00:00",
                    "duration": 0
                }
                """.formatted(epicId);

        HttpResponse<String> response2 = sendPost("/subtasks", subtask3Json);
        assertEquals(400, response2.statusCode());
        assertTrue(response2.body().contains("Invalid time parameters"));
    }

    @Test
    void shouldReturn400WhenUpdatingIncorrectDurationAndStartTime() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "First Subtask",
                    "description": "First Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();

        String subtask2Json = """
                {
                    "name": "Second Subtask",
                    "description": "Second Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> response1 = sendPost("/subtasks/" + subtaskId, subtask2Json);
        assertEquals(400, response1.statusCode());
        assertTrue(response1.body().contains("Invalid time parameters"));

        String subtask3Json = """
                {
                    "name": "Third Subtask",
                    "description": "Third Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "18.06.2024 10:00:00",
                    "duration": 0
                }
                """.formatted(epicId);

        HttpResponse<String> response2 = sendPost("/subtasks/" + subtaskId, subtask3Json);
        assertEquals(400, response2.statusCode());
        assertTrue(response2.body().contains("Invalid time parameters"));
    }

    @Test
    void shouldReturn400CreatingWhenMissingRequiredFields() throws Exception {
        String invalidJson = """
                {
                    "name": "Subtask without status"
                }
                """;

        HttpResponse<String> response = sendPost("/subtasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid subtask data"));
    }

    @Test
    void shouldReturn400UpdatingWhenMissingRequiredFields() throws Exception {
        int epicId = createEpicForSubtask();
        sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Subtask without status"
                }
                """;

        HttpResponse<String> response = sendPost("/subtasks/" + subtaskId, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid subtask data"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidStatusValue() throws Exception {
        int epicId = createEpicForSubtask();
        sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "INVALID",
                    "parentId": %d
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks/" + subtaskId, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid subtask data") || response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidStatusValue() throws Exception {
        int epicId = createEpicForSubtask();
        HttpResponse<String> response = sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "INVALID",
                    "parentId": %d
                }
                """.formatted(epicId));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid subtask data") || response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidStartTimeFormat() throws Exception {
        int epicId = createEpicForSubtask();
        sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();

        String invalidJson = """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "2024-01-01 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks/" + subtaskId, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid date format"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidStartTimeFormat() throws Exception {
        int epicId = createEpicForSubtask();
        HttpResponse<String> response = sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "2024-01-01 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid date format"));
    }

    @Test
    void shouldReturn400CreatingForInvalidJSON() throws Exception {
        String invalidJson = "{ invalid json }";

        HttpResponse<String> response = sendPost("/subtasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400UpdatingForInvalidJSON() throws Exception {
        int epicId = createEpicForSubtask();
        sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String invalidJson = "{ invalid json }";

        HttpResponse<String> response = sendPost("/subtasks/" + subtaskId, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenCreateInvalidDuration() throws Exception {
        int epicId = createEpicForSubtask();
        String invalidJson = """
                {
                    "name": "Test Subtask",
                    "description": "Test Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": "invalid"
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks", invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenUpdateInvalidDuration() throws Exception {
        int epicId = createEpicForSubtask();
        sendPost("/subtasks", """
                {
                    "name": "Subtask",
                    "description": "Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();
        String invalidJson = """
                {
                    "name": "Test Subtask",
                    "description": "Test Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": "invalid"
                }
                """.formatted(epicId);

        HttpResponse<String> response = sendPost("/subtasks/" + subtaskId, invalidJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid Json"));
    }

    @Test
    void shouldReturn400WhenUpdateChangeParentId() throws Exception {
        int epicId1 = createEpicForSubtask();
        int epicId2 = createEpicForSubtask();

        sendPost("/subtasks", """
                {
                    "name": "Original Subtask",
                    "description": "Original Desc",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId1));
        int subtaskId = taskManager.getAllSubtasks().getFirst().getId();

        String updateJson = """
                {
                    "name": "Changed Parent Subtask",
                    "description": "Changed Desc",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId2);

        HttpResponse<String> updateResp = sendPost("/subtasks/" + subtaskId, updateJson);

        assertEquals(400, updateResp.statusCode());
        assertTrue(updateResp.body().contains("Cannot change parent epic of existing subtask"));
    }

    @Test
    void testSubtaskCreationUpdatesEpicStatus() throws Exception {
        int epicId = createEpicForSubtask();

        String subtaskJson = """
                {
                    "name": "Done Subtask",
                    "description": "Done Description",
                    "status": "DONE",
                    "parentId": %d
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void testSubtaskTimeUpdatesEpicTime() throws Exception {
        int epicId = createEpicForSubtask();

        String subtaskJson = """
                {
                    "name": "Timed Subtask",
                    "description": "Timed Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtaskJson);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals("2024-06-17T10:00", epic.getStartTime().toString());
        assertEquals(60, epic.getDurationTime().toMinutes());
        assertEquals("2024-06-17T11:00", epic.getEndTime().toString());
    }

    @Test
    void testSubtaskDeletionUpdatesEpic() throws Exception {
        int epicId = createEpicForSubtask();

        String subtaskJson = """
                {
                    "name": "Subtask to Delete",
                    "description": "Will be deleted",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        HttpResponse<String> postResp = sendPost("/subtasks", subtaskJson);
        Subtask createdSubtask = gson.fromJson(postResp.body(), Subtask.class);
        int subtaskId = createdSubtask.getId();

        sendDelete("/subtasks/" + subtaskId);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.NEW, epic.getStatus());
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDurationTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void testGetEpicSubtasks() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Desc 1",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Desc 2",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId);

        sendPost("/subtasks", subtask1Json);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> getResp = sendGet("/epics/" + epicId + "/subtasks");
        List<Subtask> subtasks = gson.fromJson(getResp.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, getResp.statusCode());
        assertEquals(2, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getName());
        assertEquals("Subtask 2", subtasks.get(1).getName());
    }

    @Test
    void testCreateMultipleSubtasks() throws Exception {
        int epicId = createEpicForSubtask();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Desc 1",
                    "status": "NEW",
                    "parentId": %d
                }
                """.formatted(epicId);
        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Desc 2",
                    "status": "IN_PROGRESS",
                    "parentId": %d
                }
                """.formatted(epicId);

        sendPost("/subtasks", subtask1Json);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> getResp = sendGet("/subtasks");
        List<Subtask> subtasks = gson.fromJson(getResp.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, getResp.statusCode());
        assertEquals(2, subtasks.size());
        assertEquals(2, taskManager.getAllSubtasks().size());
    }
}