package server;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.Test;
import util.EpicListTypeToken;
import util.SubtaskListTypeToken;
import util.TaskListTypeToken;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicHttpTest extends HttpServerBaseTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    private int createEpic() throws Exception {
        String epicJson = """
                {
                    "name": "Test Epic",
                    "description": "Test Description"
                }
                """;
        HttpResponse<String> epicResp = sendPost("/epics", epicJson);
        Epic createdEpic = gson.fromJson(epicResp.body(), Epic.class);
        return createdEpic.getId();
    }

    private int createSubtaskForEpic(int epicId) throws Exception {
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
        Subtask createdSubtask = gson.fromJson(subtaskResp.body(), Subtask.class);
        return createdSubtask.getId();
    }

    // Get Epic
    @Test
    void testGetAllEpics() throws Exception {
        createEpic();

        HttpResponse<String> getResp = sendGet("/epics");

        List<Epic> epicsList = gson.fromJson(getResp.body(), new EpicListTypeToken().getType());
        assertEquals(200, getResp.statusCode());
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(1, epicsList.size());
        assertEquals(epicsList.getFirst(), taskManager.getAllEpics().getFirst());
    }

    @Test
    void testGetAllEpicsEmpty() throws Exception {
        HttpResponse<String> getResp = sendGet("/epics");

        assertEquals(200, getResp.statusCode());
        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals("[]", getResp.body());
    }

    @Test
    void testGetEpicById() throws Exception {
        int epicId = createEpic();

        HttpResponse<String> getResp = sendGet("/epics/" + epicId);

        assertEquals(200, getResp.statusCode());
        Epic retrievedEpic = gson.fromJson(getResp.body(), Epic.class);
        assertEquals("Test Epic", retrievedEpic.getName());
        assertEquals("Test Description", retrievedEpic.getDescription());
        assertEquals(Status.NEW, retrievedEpic.getStatus());
        assertTrue(retrievedEpic.getSubtasksId().isEmpty());
    }

    @Test
    void testGetEpicSubtasks() throws Exception {
        int epicId = createEpic();
        createSubtaskForEpic(epicId);

        HttpResponse<String> getResp = sendGet("/epics/" + epicId + "/subtasks");

        assertEquals(200, getResp.statusCode());
        List<Subtask> subtasks = gson.fromJson(getResp.body(), new SubtaskListTypeToken().getType());
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.getFirst().getName());
        assertEquals(epicId, subtasks.getFirst().getParentId());
    }

    @Test
    void testGetEpicSubtasksEmpty() throws Exception {
        int epicId = createEpic();

        HttpResponse<String> getResp = sendGet("/epics/" + epicId + "/subtasks");

        assertEquals(200, getResp.statusCode());
        List<Subtask> subtasks = gson.fromJson(getResp.body(), new SubtaskListTypeToken().getType());
        assertTrue(subtasks.isEmpty());
    }

    // Create Epic
    @Test
    void testCreateEpic() throws Exception {
        String epicJson = """
                {
                    "name": "New Epic",
                    "description": "New Description"
                }
                """;

        HttpResponse<String> postResp = sendPost("/epics", epicJson);

        assertEquals(200, postResp.statusCode());
        Epic createdEpic = gson.fromJson(postResp.body(), Epic.class);
        assertEquals("New Epic", createdEpic.getName());
        assertEquals("New Description", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus());
        assertTrue(createdEpic.getSubtasksId().isEmpty());
        assertEquals(1, taskManager.getAllEpics().size());
    }

    // Delete Epic
    @Test
    void testDeleteEpic() throws Exception {
        int epicId = createEpic();
        createSubtaskForEpic(epicId);

        HttpResponse<String> deleteResp = sendDelete("/epics/" + epicId);

        assertEquals(200, deleteResp.statusCode());
        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubtasks().size());

        ApiResponse apiResponse = gson.fromJson(deleteResp.body(), ApiResponse.class);
        assertEquals(200, apiResponse.getStatus());
        assertEquals("Success", apiResponse.getMessage());
    }

    // Тесты ошибок
    @Test
    void shouldReturn404ForInvalidEndpoint() throws Exception {
        HttpResponse<String> response = sendGet("/epics/invalid-endpoint");
        assertEquals(404, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(404, apiResponse.getStatus());
        assertEquals("Endpoint not found", apiResponse.getMessage());
    }

    @Test
    void shouldReturn404WhenGettingNonExistentEpic() throws Exception {
        HttpResponse<String> response = sendGet("/epics/999");
        assertEquals(404, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(404, apiResponse.getStatus());
        assertEquals("Epic not found", apiResponse.getMessage());
    }

    @Test
    void shouldReturn404WhenGettingSubtasksOfNonExistentEpic() throws Exception {
        HttpResponse<String> response = sendGet("/epics/999/subtasks");
        assertEquals(404, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(404, apiResponse.getStatus());
        assertEquals("Epic or subtasks not found", apiResponse.getMessage());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentEpic() throws Exception {
        HttpResponse<String> response = sendDelete("/epics/999");
        assertEquals(404, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(404, apiResponse.getStatus());
        assertEquals("Epic not found", apiResponse.getMessage());
    }

    @Test
    void shouldReturn400CreatingWhenMissingRequiredFields() throws Exception {
        String invalidJson = """
                {
                    "name": "Epic without description"
                }
                """;

        HttpResponse<String> response = sendPost("/epics", invalidJson);
        assertEquals(400, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(400, apiResponse.getStatus());
        assertTrue(apiResponse.getMessage().contains("Missing required fields"));
    }

    @Test
    void shouldReturn400CreatingForInvalidJSON() throws Exception {
        String invalidJson = "{ invalid json }";

        HttpResponse<String> response = sendPost("/epics", invalidJson);
        assertEquals(400, response.statusCode());

        ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
        assertEquals(400, apiResponse.getStatus());
        assertEquals("Invalid JSON syntax", apiResponse.getMessage());
    }

    @Test
    void shouldReturn400CreatingWithExtraFields() throws Exception {
        String invalidJson = """
                {
                    "name": "Epic",
                    "description": "Description",
                    "status": "NEW",
                    "startTime": null,
                    "duration": 0
                }
                """;

        HttpResponse<String> response = sendPost("/epics", invalidJson);
        assertEquals(200, response.statusCode());

        Epic createdEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals("Epic", createdEpic.getName());
        assertEquals("Description", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus());
    }

    @Test
    void testEpicStatusCalculationNew() throws Exception {
        int epicId = createEpic();

        createSubtaskForEpic(epicId);
        createSubtaskForEpic(epicId);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void testEpicStatusCalculationDone() throws Exception {
        int epicId = createEpic();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Description 1",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Description 2",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void testEpicStatusCalculationInProgress() throws Exception {
        int epicId = createEpic();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Description 1",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Description 2",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicStatusCalculationInProgressMixed() throws Exception {
        int epicId = createEpic();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Description 1",
                    "status": "IN_PROGRESS",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Description 2",
                    "status": "DONE",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicWithNoSubtasks() throws Exception {
        int epicId = createEpic();

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtasksId().isEmpty());
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDurationTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void testDeleteEpicRemovesSubtasks() throws Exception {
        int epicId = createEpic();
        createSubtaskForEpic(epicId);
        createSubtaskForEpic(epicId);

        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(2, taskManager.getAllSubtasks().size());

        sendDelete("/epics/" + epicId);

        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubtasks().size());
    }

    @Test
    void testEpicTimeCalculation() throws Exception {
        int epicId = createEpic();

        String subtask1Json = """
                {
                    "name": "Subtask 1",
                    "description": "Description 1",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask1Json);

        String subtask2Json = """
                {
                    "name": "Subtask 2",
                    "description": "Description 2",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 12:00:00",
                    "duration": 90
                }
                """.formatted(epicId);
        sendPost("/subtasks", subtask2Json);

        HttpResponse<String> epicResp = sendGet("/epics/" + epicId);
        Epic epic = gson.fromJson(epicResp.body(), Epic.class);

        assertEquals("2024-06-17T10:00", epic.getStartTime().toString());
        assertEquals("2024-06-17T13:30", epic.getEndTime().toString());
        assertEquals(210, epic.getDurationTime().toMinutes());
    }

    @Test
    void testEpicTimeResetWhenSubtasksDeleted() throws Exception {
        int epicId = createEpic();
        int subtaskId = createSubtaskForEpic(epicId);

        String updateSubtaskJson = """
                {
                    "name": "Updated Subtask",
                    "description": "Updated Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks/" + subtaskId, updateSubtaskJson);

        HttpResponse<String> epicResp1 = sendGet("/epics/" + epicId);
        Epic epic1 = gson.fromJson(epicResp1.body(), Epic.class);
        assertNotNull(epic1.getStartTime());
        assertNotNull(epic1.getEndTime());

        sendDelete("/subtasks/" + subtaskId);

        HttpResponse<String> epicResp2 = sendGet("/epics/" + epicId);
        Epic epic2 = gson.fromJson(epicResp2.body(), Epic.class);
        assertNull(epic2.getStartTime());
        assertNull(epic2.getEndTime());
        assertEquals(Duration.ZERO, epic2.getDurationTime());
    }

    @Test
    void testMultipleEpicsIndependent() throws Exception {
        String epic1Json = """
                {
                    "name": "Epic 1",
                    "description": "Description 1"
                }
                """;
        HttpResponse<String> epic1Resp = sendPost("/epics", epic1Json);
        Epic epic1 = gson.fromJson(epic1Resp.body(), Epic.class);

        String epic2Json = """
                {
                    "name": "Epic 2",
                    "description": "Description 2"
                }
                """;
        HttpResponse<String> epic2Resp = sendPost("/epics", epic2Json);
        Epic epic2 = gson.fromJson(epic2Resp.body(), Epic.class);

        String subtask1Json = """
                {
                    "name": "Subtask for Epic 1",
                    "description": "Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": null,
                    "duration": 0
                }
                """.formatted(epic1.getId());
        sendPost("/subtasks", subtask1Json);

        HttpResponse<String> allEpicsResp = sendGet("/epics");
        List<Epic> allEpics = gson.fromJson(allEpicsResp.body(), new EpicListTypeToken().getType());
        assertEquals(2, allEpics.size());

        HttpResponse<String> epic1SubtasksResp = sendGet("/epics/" + epic1.getId() + "/subtasks");
        List<Subtask> epic1Subtasks = gson.fromJson(epic1SubtasksResp.body(), new SubtaskListTypeToken().getType());
        assertEquals(1, epic1Subtasks.size());

        HttpResponse<String> epic2SubtasksResp = sendGet("/epics/" + epic2.getId() + "/subtasks");
        List<Subtask> epic2Subtasks = gson.fromJson(epic2SubtasksResp.body(), new SubtaskListTypeToken().getType());
        assertTrue(epic2Subtasks.isEmpty());
    }

    @Test
    void testEpicDeletionFromHistoryAndPriority() throws Exception {
        int epicId = createEpic();
        int subtaskId = createSubtaskForEpic(epicId);

        String updateSubtaskJson = """
                {
                    "name": "Timed Subtask",
                    "description": "Description",
                    "status": "NEW",
                    "parentId": %d,
                    "startTime": "17.06.2024 10:00:00",
                    "duration": 60
                }
                """.formatted(epicId);
        sendPost("/subtasks/" + subtaskId, updateSubtaskJson);

        sendGet("/epics/" + epicId);
        sendGet("/subtasks/" + subtaskId);

        sendDelete("/epics/" + epicId);

        HttpResponse<String> historyResp = sendGet("/history");
        List<Task> history = gson.fromJson(historyResp.body(), new TaskListTypeToken().getType());
        assertFalse(history.stream().anyMatch(task -> task.getId() == epicId));
        assertFalse(history.stream().anyMatch(task -> task.getId() == subtaskId));

        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubtasks().size());
    }
}