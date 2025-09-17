package manager;

import exceptions.ManagerReadException;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            File tempFile = File.createTempFile("test", ".csv", new File("resources/tests"));
            tempFile.deleteOnExit();
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл для менеджера", e);
        }
    }

    // Тест, в котором проверяется создание менеджера без загрузки если файл отсутствует
    @Test
    void loadFromMissingFileReturnsEmptyManager() throws IOException {
        File missingFile = new File("resources/tests/sfasf.csv");
        missingFile.delete();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(missingFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Менеджер должен быть пустым");
    }

    // Тест, в котором проверяется создание менеджера без загрузки если файл пустой
    @Test
    void loadFromEmptyFileReturnsEmptyManager() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(manager.getFile());

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Менеджер должен быть пустым");
    }

    // Тест, в котором проверяется корректная загрузка Task без времени из файла.
    @Test
    void saveAndLoadTaskWithoutTime() {
        Task task = manager.createTask("Test Task", "Description", Status.NEW);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertNull(loadedTask.getStartTime());
        assertEquals(Duration.ZERO, loadedTask.getDurationTime());
        assertNull(loadedTask.getEndTime());
        assertFalse(loaded.getTasksByPriority(true).contains(task));
        assertFalse(loaded.getTasksByPriority(false).contains(task));
        assertTrue(loaded.getAllTasks().contains(task));
    }

    // Тест, в котором проверяется корректная загрузка Task со временем из файла.
    @Test
    void saveAndLoadTaskWithTime() {
        Task task = manager.createTask("Test Task", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task,
                LocalDateTime.of(2025, 9, 15, 10, 0),
                Duration.ofMinutes(60));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDurationTime(), loadedTask.getDurationTime());
        assertEquals(task.getEndTime(), loadedTask.getEndTime());
        assertTrue(loaded.getTasksByPriority(true).contains(task));
        assertTrue(loaded.getTasksByPriority(false).contains(task));
        assertTrue(loaded.getAllTasks().contains(task));
    }

    // Тест, в котором проверяется корректная загрузка Epic из файла без subtask.
    @Test
    void saveAndLoadEpicWithoutSubtasks() {
        Epic epic = manager.createEpic("Epic Test", "Epic Description");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Epic loadedEpic = loaded.getEpic(epic.getId());
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertNull(loadedEpic.getStartTime());
        assertEquals(Duration.ZERO, loadedEpic.getDurationTime());
        assertNull(loadedEpic.getEndTime());
        assertEquals(epic.getSubtasksId(), loadedEpic.getSubtasksId());
        assertFalse(loaded.getTasksByPriority(true).contains(epic));
        assertFalse(loaded.getTasksByPriority(false).contains(epic));
        assertTrue(loaded.getAllEpics().contains(epic));
    }

    // Тест, в котором проверяется корректная загрузка Epic из файла c subtask.
    @Test
    void saveAndLoadEpicWithSubtasks() {
        Epic epic = manager.createEpic("Epic Test", "Epic Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Epic loadedEpic = loaded.getEpic(epic.getId());
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertNull(loadedEpic.getStartTime());
        assertEquals(Duration.ZERO, loadedEpic.getDurationTime());
        assertNull(loadedEpic.getEndTime());
        assertEquals(epic.getSubtasksId(), loadedEpic.getSubtasksId());
        assertFalse(loaded.getTasksByPriority(true).contains(epic));
        assertFalse(loaded.getTasksByPriority(false).contains(epic));
        assertTrue(loaded.getAllEpics().contains(epic));
    }

    // Тест, в котором проверяется корректная загрузка Epic со временем из файла.
    @Test
    void saveAndLoadEpicWithSubtasksWithTime() {
        Epic epic = manager.createEpic("Epic 1", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask2, LocalDateTime.of(2025, 9, 17, 11, 30), Duration.ofMinutes(90));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Epic loadedEpic = loaded.getEpic(epic.getId());
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertEquals(epic.getStartTime(), loadedEpic.getStartTime());
        assertEquals(epic.getDurationTime(), loadedEpic.getDurationTime());
        assertEquals(epic.getEndTime(), loadedEpic.getEndTime());
        assertEquals(epic.getSubtasksId(), loadedEpic.getSubtasksId());
        assertFalse(loaded.getTasksByPriority(true).contains(epic));
        assertFalse(loaded.getTasksByPriority(false).contains(epic));
        assertTrue(loaded.getAllEpics().contains(epic));
    }

    // Тест, в котором проверяется корректная загрузка Subtask из файла.
    @Test
    void saveAndLoadSubtasks() {
        Epic epic = manager.createEpic("Epic 1", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getParentId(), loadedSubtask.getParentId());
        assertNull(loadedSubtask.getStartTime());
        assertEquals(Duration.ZERO, loadedSubtask.getDurationTime());
        assertNull(loadedSubtask.getEndTime());
        assertFalse(loaded.getTasksByPriority(true).contains(subtask));
        assertFalse(loaded.getTasksByPriority(false).contains(subtask));
        assertTrue(loaded.getAllSubtasks().contains(subtask));
    }

    // Тест, в котором проверяется корректная загрузка Subtask со временем из файла.
    @Test
    void saveAndLoadSubtasksWithTime() {
        Epic epic = manager.createEpic("Epic 1", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask, LocalDateTime.of(2025, 9, 17, 11, 30), Duration.ofMinutes(90));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(manager.getFile());

        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getParentId(), loadedSubtask.getParentId());
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
        assertEquals(subtask.getDurationTime(), loadedSubtask.getDurationTime());
        assertEquals(subtask.getEndTime(), loadedSubtask.getEndTime());
        assertTrue(loaded.getTasksByPriority(true).contains(subtask));
        assertTrue(loaded.getTasksByPriority(false).contains(subtask));
        assertTrue(loaded.getAllSubtasks().contains(subtask));
    }

    @Test
    void testReadException() throws IOException {
        File dir = new File("resources/tests/directoryInsteadOfFile");

        assertThrows(ManagerReadException.class, () -> FileBackedTaskManager.loadFromFile(dir));
    }

    @Test
    void testSaveException() throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File("resources/tests/directoryInsteadOfFile"));

        assertThrows(ManagerSaveException.class, () -> manager.createTask("task", "des", Status.NEW));
    }
}
