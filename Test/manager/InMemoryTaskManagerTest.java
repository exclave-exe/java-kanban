package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import static org.junit.jupiter.api.Assertions.*;
class InMemoryTaskManagerTest {

    // Тест, в котором проверяется неизменность задачи при добавлении в менеджер
    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        // Arrange
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task newTask = inMemoryTaskManager.createTask("test", "test", Status.NEW);

        // Act & Assert
        assertEquals(newTask, inMemoryTaskManager.getTask(newTask.getId()), "Task должен остаться прежним.");
    }

    // Проверка, что геттеры возвращают корректно.
    @Test
    void shouldReturnTaskSubtaskEpic() {
        // Arrange
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = manager.createTask("test", "test", Status.NEW);
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);

        // Act & Assert
        assertEquals(task, manager.getTask(task.getId()), "Должен возвращаться Task");
        assertEquals(subtask, manager.getSubtask(subtask.getId()), "Должен возвращаться Subtask");
        assertEquals(epic, manager.getEpic(epic.getId()), "Должен возвращаться Epic");
    }

    // Проверка, что удаление происходит корректно.
    @Test
    void shouldDeleteTask() {
        // Arrange
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.updateStatus(task, Status.DONE);

        // Assert
        assertEquals(Status.DONE, task.getStatus(), "Статус Task должен быть обновлён.");
    }

    @Test
    void shouldUpdateSubtaskAndEpicStatus() {
        // Arrange
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
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
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Epic epic = manager.createEpic("test", "test");
        Subtask subtask = manager.createSubtask(epic, "test", "test", Status.NEW);


        // Act
        manager.updateName(subtask,"New test1");
        manager.updateDescription(subtask,"New test2");


        // Assert
        assertEquals("New test1", subtask.getName(), "Имя Subtask должно обновиться");
        assertEquals("New test2", subtask.getDescription(), "Описание Subtask должно обновиться");
    }

    @Test
    void shouldUpdateTaskNameAndDescription() {
        // Arrange
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = manager.createTask("test", "test", Status.NEW);

        // Act
        manager.updateName(task, "New test1");
        manager.updateDescription(task, "New test2");

        // Assert
        assertEquals("New test1", task.getName(), "Имя Task должно обновиться");
        assertEquals("New test2", task.getDescription(), "Описание Task должно обновиться");
    }
}
