package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File dir;
    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        dir = new File("resources/tests");
        file = File.createTempFile("test", ".csv", dir);
        file.deleteOnExit();
        manager = new FileBackedTaskManager(file);
    }

    // Тест, в котором проверяется сохранение в файл
    @Test
    void saveCreatesFileWithTasks() throws IOException {
        //Arrange
        Task task = manager.createTask("Task1", "test", Status.NEW);
        Epic epic = manager.createEpic("Epic1", "test");
        Subtask subtask = manager.createSubtask(epic, "Subtask1", "test", Status.NEW);

        //Act
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        //Assert
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals("id,type,name,status,description,epic", lines.getFirst(),
                "Первая строка должна быть заголовком CSV");

        boolean containsTask = false;
        for (String line : lines) {
            if (line.contains("Task1")) {
                containsTask = true;
                break;
            }
        }
        assertTrue(containsTask, "Файл должен содержать Task");

        boolean containsEpic = false;
        for (String line : lines) {
            if (line.contains("Epic1")) {
                containsEpic = true;
                break;
            }
        }
        assertTrue(containsEpic, "Файл должен содержать Epic");

        boolean containsSubtask = false;
        for (String line : lines) {
            if (line.contains("Subtask1")) {
                containsSubtask = true;
                break;
            }
        }
        assertTrue(containsSubtask, "Файл должен содержать Subtask");
    }

    // Тест, в котором проверяется загрузка из файла
    @Test
    void loadFromFileRestoresTasksCorrectly() {
        // Arrange
        Task task = manager.createTask("test", "test", Status.NEW);
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);

        // Act
        FileBackedTaskManager fbtm = FileBackedTaskManager.loadFromFile(file);

        // Assert
        assertEquals(fbtm.getTask(task.getId()), task, "Task должен восстановиться корректно");
        assertEquals(fbtm.getEpic(epic.getId()), epic, "Epic должен восстановиться корректно");
        assertEquals(fbtm.getSubtask(subtask.getId()), subtask, "Subtask должен восстановиться корректно");
        assertEquals(1, fbtm.returnAllEpics().size(), "При восстановлении длинна должна совпадать");
        assertEquals(1, fbtm.returnAllTasks().size(), "При восстановлении длинна должна совпадать");
        assertEquals(1, fbtm.returnAllTasks().size(), "При восстановлении длинна должна совпадать");

    }

    // Тест, в котором проверяется создание менеджера без загрузки если файл отсутствует
    @Test
    void loadFromMissingFileReturnsEmptyManager() throws IOException {
        // Arrange
        File missingFile = new File("resources/tests/sfasf.csv");
        missingFile.delete();

        // Act
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(missingFile);

        // Assert
        assertTrue(loadedManager.returnAllTasks().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.returnAllEpics().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.returnAllSubtasks().isEmpty(), "Менеджер должен быть пустым");
    }

    // Тест, в котором проверяется создание менеджера без загрузки если файл пустой
    @Test
    void loadFromEmptyFileReturnsEmptyManager() throws IOException {
        // Act
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Assert
        assertTrue(loadedManager.returnAllTasks().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.returnAllEpics().isEmpty(), "Менеджер должен быть пустым");
        assertTrue(loadedManager.returnAllSubtasks().isEmpty(), "Менеджер должен быть пустым");
    }

    // Тест, в котором проверяется неизменность задачи при добавлении в менеджер
    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        // Act
        Task newTask = manager.createTask("test", "test", Status.NEW);

        // Assert
        assertEquals(newTask, manager.getTask(newTask.getId()), "Task должен остаться прежним.");
    }

    // Проверка, что геттеры возвращают корректно.
    @Test
    void shouldReturnTaskSubtaskEpic() {
        // Act
        Task task = manager.createTask("test", "test", Status.NEW);
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);

        // Assert
        assertEquals(task, manager.getTask(task.getId()), "Должен возвращаться Task");
        assertEquals(subtask, manager.getSubtask(subtask.getId()), "Должен возвращаться Subtask");
        assertEquals(epic, manager.getEpic(epic.getId()), "Должен возвращаться Epic");
    }

    // Проверка, что удаление происходит корректно.
    @Test
    void shouldDeleteTask() {
        // Arrange
        Task task = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.deleteTask(task.getId());
        Task result = manager.getTask(task.getId());

        // Assert
        assertNull(result, "Task должен быть удалён.");
    }

    @Test
    void shouldDeleteSubtaskAndRemoveFromEpic() {
        // Arrange
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);

        // Act
        manager.deleteSubtask(subtask.getId());

        // Assert
        assertNull(manager.getSubtask(subtask.getId()), "Subtask должен быть удалён.");
        assertFalse(epic.getSubtasksId().contains(subtask.getId()), "Epic не должен содержать ID удалённых подзадач.");
    }

    @Test
    void shouldDeleteEpicAndItsSubtasks() {
        // Arrange
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask1 = manager.createSubtask(epic, "test", "test", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "test", "test", Status.DONE);

        // Act
        manager.deleteEpic(epic.getId());

        // Assert
        assertNull(manager.getEpic(epic.getId()), "Epic должен быть удалён.");
        assertNull(manager.getSubtask(subtask1.getId()), "Subtask должен быть удалён.");
        assertNull(manager.getSubtask(subtask2.getId()), "Subtask должен быть удалён.");
    }

    @Test
    void shouldClearAllTasks() {
        // Arrange
        Task task1 = manager.createTask("test", "test", Status.NEW);
        Task task2 = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.deleteAllTasks();

        // Assert
        assertNull(manager.getTask(task1.getId()), "Task должен быть удалены.");
        assertNull(manager.getTask(task2.getId()), "Task должен быть удалены.");
    }

    @Test
    void shouldClearAllEpicsAndSubtasks() {
        // Arrange
        Epic epic1 = manager.createEpic("test", "test");
        Epic epic2 = manager.createEpic("test", "test");
        Subtask subtask1 = manager.createSubtask(epic1, "test", "test", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic2, "test", "test", Status.DONE);

        // Act
        manager.deleteAllEpics();

        // Assert
        assertNull(manager.getEpic(epic1.getId()), "Epic должен быть удален.");
        assertNull(manager.getEpic(epic2.getId()), "Epic должен быть удален.");
        assertNull(manager.getSubtask(subtask1.getId()), "Subtask должна быть удалена.");
        assertNull(manager.getSubtask(subtask2.getId()), "Subtask должна быть удалена.");

    }

    @Test
    void shouldClearAllSubtasks() {
        // Arrange
        Epic epic1 = manager.createEpic("test", "test");
        Epic epic2 = manager.createEpic("test", "test");
        Subtask subtask1 = manager.createSubtask(epic1, "test", "test", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic2, "test", "test", Status.NEW);

        // Act
        manager.deleteAllSubtasks();

        // Assert
        assertNull(manager.getSubtask(subtask1.getId()), "Subtask должен быть удален.");
        assertNull(manager.getSubtask(subtask2.getId()), "Subtask должен быть удален.");
        assertTrue(epic1.getSubtasksId().contains(subtask1.getId()), "Epic не должен содержать ID удалённых подзадач.");
        assertTrue(epic2.getSubtasksId().contains(subtask2.getId()), "Epic не должен содержать ID удалённых подзадач.");
    }

    // Проверки, что обновление свойств объектов происходит корректно.
    @Test
    void shouldUpdateTaskStatus() {
        // Arrange
        Task task = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.updateStatus(task, Status.DONE);

        // Assert
        assertEquals(Status.DONE, task.getStatus(), "Статус Task должен быть обновлён.");
    }

    @Test
    void shouldUpdateSubtaskAndEpicStatus() {
        // Arrange
        Epic epic1 = manager.createEpic("test", "test");
        Subtask subtask1 = manager.createSubtask(epic1, "test", "test", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic1, "test", "test", Status.NEW);

        // Act
        manager.updateStatus(subtask2, Status.DONE);

        // Assert
        assertEquals(Status.DONE, subtask2.getStatus(), "Статус Task должен быть обновлён.");
        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Статус Epic должен быть обновлён.");
    }

    @Test
    void shouldUpdateEpicNameAndDescription() {
        // Arrange
        Epic epic = manager.createEpic("test", "test");

        // Act
        manager.updateName(epic, "New test1");
        manager.updateDescription(epic, "New test2");

        // Assert
        assertEquals("New test1", epic.getName(), "Имя Epic должно обновиться");
        assertEquals("New test2", epic.getDescription(), "Описание Epic должно обновиться");
    }

    @Test
    void shouldUpdateSubtaskNameAndDescription() {
        // Arrange
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);

        // Act
        manager.updateName(subtask, "New test1");
        manager.updateDescription(subtask, "New test2");

        // Assert
        assertEquals("New test1", subtask.getName(), "Имя Subtask должно обновиться");
        assertEquals("New test2", subtask.getDescription(), "Описание Subtask должно обновиться");
    }

    @Test
    void shouldUpdateTaskNameAndDescription() {
        // Arrange
        Task task = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.updateName(task, "New test1");
        manager.updateDescription(task, "New test2");

        // Assert
        assertEquals("New test1", task.getName(), "Имя Task должно обновиться");
        assertEquals("New test2", task.getDescription(), "Описание Task должно обновиться");
    }
}
