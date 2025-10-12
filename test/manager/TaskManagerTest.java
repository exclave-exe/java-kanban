package manager;

import exceptions.NotFoundException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    public void setup() {
        manager = createManager();
    }

    @Test
    void shouldReturnTaskById() {
        Task task = manager.createTask("task1", "desc1", Status.NEW);
        Epic epic = manager.createEpic("epic1", "descEpic");
        Subtask subtask = manager.createSubtask(epic, "sub1", "descSub", Status.NEW);

        Task savedTask = manager.getTask(task.getId());
        Epic savedEpic = manager.getEpic(epic.getId());
        Subtask savedSubtask = manager.getSubtask(subtask.getId());

        assertNotNull(savedTask, "Задача должна возвращаться по id");
        assertNotNull(savedEpic, "Задача должна возвращаться по id");
        assertNotNull(savedSubtask, "Задача должна возвращаться по id");
        assertEquals(task, savedTask, "Возвращаемая задача должна совпадать с сохранённой");
        assertEquals(epic, savedEpic, "Возвращаемая задача должна совпадать с сохранённой");
        assertEquals(subtask, savedSubtask, "Возвращаемая задача должна совпадать с сохранённой");
    }

    @Test
    void shouldThrowNotFoundExceptionIfTasksDoesNotExist() {
        assertThrows(NotFoundException.class, () -> manager.getTask(999),
                "Если задачи нет, метод должен бросить NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getEpic(999),
                "Если эпика нет, метод должен бросить NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(999),
                "Если подзадачи нет, метод должен бросить NotFoundException");
    }

    @Test
    void shouldReturnAllTasksEpicsSubtasks() {
        Task task1 = manager.createTask("task1", "desc1", Status.NEW);
        Task task2 = manager.createTask("task2", "desc2", Status.IN_PROGRESS);
        Epic epic1 = manager.createEpic("epic1", "descEpic1");
        Epic epic2 = manager.createEpic("epic2", "descEpic2");
        Subtask subtask1 = manager.createSubtask(epic1, "sub1", "descSub1", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic2, "sub2", "descSub2", Status.DONE);

        List<Task> allTasks = manager.getAllTasks();
        List<Epic> allEpics = manager.getAllEpics();
        List<Subtask> allSubtasks = manager.getAllSubtasks();

        assertEquals(2, allTasks.size(), "Должны вернуться все созданные задачи");
        assertTrue(allTasks.contains(task1), "Список задач должен содержать task1");
        assertTrue(allTasks.contains(task2), "Список задач должен содержать task2");
        assertEquals(2, allEpics.size(), "Должны вернуться все созданные эпики");
        assertTrue(allEpics.contains(epic1), "Список эпиков должен содержать epic1");
        assertTrue(allEpics.contains(epic2), "Список эпиков должен содержать epic2");
        assertEquals(2, allSubtasks.size(), "Должны вернуться все созданные подзадачи");
        assertTrue(allSubtasks.contains(subtask1), "Список подзадач должен содержать subtask1");
        assertTrue(allSubtasks.contains(subtask2), "Список подзадач должен содержать subtask2");
    }

    @Test
    void shouldReturnCopyOfTaskLists() {
        Task task1 = manager.createTask("task1", "desc1", Status.NEW);
        Task task2 = manager.createTask("task2", "desc2", Status.NEW);
        Epic epic1 = manager.createEpic("epic1", "descEpic1");
        Subtask subtask1 = manager.createSubtask(epic1, "sub1", "descSub1", Status.NEW);

        List<Task> tasksCopy = manager.getAllTasks();
        List<Epic> epicsCopy = manager.getAllEpics();
        List<Subtask> subtasksCopy = manager.getAllSubtasks();
        tasksCopy.clear();
        epicsCopy.clear();
        subtasksCopy.clear();

        assertEquals(2, manager.getAllTasks().size(), "Внутренний список Task не должен изменяться");
        assertEquals(1, manager.getAllEpics().size(), "Внутренний список Epic не должен изменяться");
        assertEquals(1, manager.getAllSubtasks().size(), "Внутренний список Subtask не должен изменяться");
    }

    @Test
    void shouldUpdateNameForAllTaskTypes() {
        Task task = manager.createTask("Old Task", "Description", Status.NEW);
        Epic epic = manager.createEpic("Old Epic", "Epic Description");
        Subtask subtask = manager.createSubtask(epic, "Old Subtask", "Subtask Description", Status.NEW);
        String newTaskName = "New Task";
        String newEpicName = "New Epic";
        String newSubtaskName = "New Subtask";

        manager.updateName(task, newTaskName);
        manager.updateName(epic, newEpicName);
        manager.updateName(subtask, newSubtaskName);

        assertEquals(newTaskName, task.getName(), "Имя Task должно обновиться");
        assertEquals(newEpicName, epic.getName(), "Имя Epic должно обновиться");
        assertEquals(newSubtaskName, subtask.getName(), "Имя Subtask должно обновиться");
    }

    @Test
    void shouldUpdateDescriptionForAllTaskTypes() {
        Task task = manager.createTask("Task", "Old Description", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Old Epic Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Old Subtask Description", Status.NEW);
        String newTaskDescription = "New Task Description";
        String newEpicDescription = "New Epic Description";
        String newSubtaskDescription = "New Subtask Description";

        manager.updateDescription(task, newTaskDescription);
        manager.updateDescription(epic, newEpicDescription);
        manager.updateDescription(subtask, newSubtaskDescription);

        assertEquals(newTaskDescription, task.getDescription(), "Описание Task должно обновиться");
        assertEquals(newEpicDescription, epic.getDescription(), "Описание Epic должно обновиться");
        assertEquals(newSubtaskDescription, subtask.getDescription(), "Описание Subtask должно обновиться");
    }

    @Test
    void taskStatusShouldUpdateCorrectly() {
        Task task = manager.createTask("Задача", "Описание", Status.NEW);

        manager.updateStatus(task, Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, task.getStatus(), "Статус задачи должен обновляться на IN_PROGRESS");
        manager.updateStatus(task, Status.DONE);
        assertEquals(Status.DONE, task.getStatus(), "Статус задачи должен обновляться на DONE");
        manager.updateStatus(task, Status.NEW);
        assertEquals(Status.NEW, task.getStatus(), "Статус задачи должен обновляться на NEW");
    }

    @Test
    void epicStatusShouldUpdateCorrectly() {
        Epic epic = manager.createEpic("Эпик", "Описание");

        Subtask subtask1 = manager.createSubtask(epic, "Подзадача 1", "Описание", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Подзадача 2", "Описание", Status.NEW);

        // Обновление
        //Все подзадачи NEW
        assertEquals(Status.NEW, epic.getStatus(), "Если все подзадачи NEW, статус эпика должен быть NEW");
        //Все подзадачи DONE
        manager.updateStatus(subtask1, Status.DONE);
        manager.updateStatus(subtask2, Status.DONE);
        assertEquals(Status.DONE, epic.getStatus(), "Если все подзадачи DONE, статус эпика должен быть DONE");
        //Подзадачи NEW и DONE
        manager.updateStatus(subtask1, Status.NEW);
        manager.updateStatus(subtask2, Status.DONE);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Если подзадачи NEW и DONE, статус эпика должен быть IN_PROGRESS");
        //Подзадачи IN_PROGRESS
        manager.updateStatus(subtask1, Status.IN_PROGRESS);
        manager.updateStatus(subtask2, Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Если подзадачи IN_PROGRESS, статус эпика должен быть IN_PROGRESS");

        //Удаление
        manager.updateStatus(subtask1, Status.NEW);
        manager.updateStatus(subtask2, Status.DONE);
        manager.deleteSubtask(subtask2.getId());
        assertEquals(Status.NEW, epic.getStatus(), "После удаления Subtask, статус Epic должен обновляться");

        //Создание
        Subtask subtask3 = manager.createSubtask(epic, "Подзадача 2", "Описание", Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "После создания Subtask, статус Epic должен обновляться");
    }

    @Test
    void subtaskStatusShouldUpdateCorrectly() {
        Epic epic = manager.createEpic("Эпик", "Описание");
        Subtask subtask = manager.createSubtask(epic, "Подзадача", "Описание", Status.NEW);

        manager.updateStatus(subtask, Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, subtask.getStatus(), "Статус Subtask должен обновляться на IN_PROGRESS");
        manager.updateStatus(subtask, Status.DONE);
        assertEquals(Status.DONE, subtask.getStatus(), "Статус Subtask должен обновляться на DONE");
        manager.updateStatus(subtask, Status.NEW);
        assertEquals(Status.NEW, subtask.getStatus(), "Статус Subtask должен обновляться на NEW");
    }

    @Test
    void shouldCreateEntitiesWithCorrectFields() {
        Task task = manager.createTask("Task1", "Desc Task", Status.NEW);
        Epic epic = manager.createEpic("Epic1", "Desc Epic");
        Subtask subtask = manager.createSubtask(epic, "Sub1", "Desc Sub", Status.NEW);

        assertNotNull(task, "Task не должен быть null");
        assertEquals("Task1", task.getName());
        assertEquals("Desc Task", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
        assertNotNull(epic, "Epic не должен быть null");
        assertEquals("Epic1", epic.getName());
        assertEquals("Desc Epic", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtasksId().contains(subtask.getId()), "Epic должен содержать SubtaskId");
        assertNotNull(subtask, "Subtask не должен быть null");
        assertEquals("Sub1", subtask.getName());
        assertEquals("Desc Sub", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(epic.getId(), subtask.getParentId(), "Subtask должен принадлежать правильному Epic");
    }

    @Test
    void shouldGenerateUniqueIdsForAllEntities() {
        Task task1 = manager.createTask("Task1", "Desc1", Status.NEW);
        Task task2 = manager.createTask("Task2", "Desc2", Status.NEW);
        Epic epic1 = manager.createEpic("Epic1", "Desc Epic1");
        Epic epic2 = manager.createEpic("Epic2", "Desc Epic2");
        Subtask sub1 = manager.createSubtask(epic1, "Sub1", "Desc Sub1", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic2, "Sub2", "Desc Sub2", Status.NEW);

        assertNotEquals(task1.getId(), task2.getId(), "Id должны быть уникальными");
        assertNotEquals(epic1.getId(), epic2.getId(), "Id должны быть уникальными");
        assertNotEquals(sub1.getId(), sub2.getId(), "Id должны быть уникальными");
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = manager.createTask("Задача", "Описание", Status.NEW);

        boolean result = manager.deleteTask(task.getId());

        assertTrue(result, "Метод должен вернуть true при успешном удалении");
        assertThrows(NotFoundException.class, () -> manager.getTask(task.getId()),
                "Удалённая задача должна бросать NotFoundException");
        assertFalse(manager.getAllTasks().contains(task), "Возвращаемый список не должен содержать Task");
        assertFalse(manager.deleteTask(task.getId()), "Повторное удаление должно вернуть false");
    }

    @Test
    void shouldDeleteEpicByIdAndAlsoRemoveItsSubtasks() {
        Epic epic = manager.createEpic("Эпик", "Описание");
        Subtask sub1 = manager.createSubtask(epic, "Саб 1", "Описание", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic, "Саб 2", "Описание", Status.NEW);

        boolean result = manager.deleteEpic(epic.getId());

        assertTrue(result, "Метод должен вернуть true при успешном удалении эпика");
        assertThrows(NotFoundException.class, () -> manager.getEpic(epic.getId()),
                "Удалённый эпик должен бросать NotFoundException");
        assertFalse(manager.getAllEpics().contains(epic), "Возвращаемый список не должен содержать Epic");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub1.getId()),
                "При удалении Epic его Subtask должен бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub2.getId()),
                "При удалении Epic его Subtask должен бросать NotFoundException");
        assertFalse(manager.getAllSubtasks().contains(sub1), "Возвращаемый список не должен содержать sub1");
        assertFalse(manager.getAllSubtasks().contains(sub2), "Возвращаемый список не должен содержать sub2");
        assertFalse(manager.deleteEpic(epic.getId()), "Повторное удаление должно вернуть false");
    }

    @Test
    void shouldDeleteSubtaskById() {
        Epic epic = manager.createEpic("Эпик", "Описание");
        Subtask sub = manager.createSubtask(epic, "Саб", "Описание", Status.NEW);

        boolean result = manager.deleteSubtask(sub.getId());

        assertTrue(result, "Метод должен вернуть true при успешном удалении Subtask");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub.getId()),
                "Удалённый Subtask должен бросать NotFoundException");
        assertFalse(manager.getAllSubtasks().contains(sub), "Возвращаемый список не должен содержать удалённый Subtask");
        assertFalse(epic.getSubtasksId().contains(sub.getId()), "Epic не должен хранить id удалённого Subtask");
        assertFalse(manager.deleteSubtask(sub.getId()), "Повторное удаление должно вернуть false");
    }

    @Test
    void shouldDeleteAllTasks() {
        Task task1 = manager.createTask("Задача 1", "Описание 1", Status.NEW);
        Task task2 = manager.createTask("Задача 2", "Описание 2", Status.NEW);

        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty(), "После удаления всех задач список должен быть пустым");
        assertThrows(NotFoundException.class, () -> manager.getTask(task1.getId()),
                "Удалённая задача должна бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getTask(task2.getId()),
                "Удалённая задача должна бросать NotFoundException");
    }

    @Test
    void shouldDeleteAllEpicsAndTheirSubtasks() {
        Epic epic1 = manager.createEpic("Эпик 1", "Описание 1");
        Epic epic2 = manager.createEpic("Эпик 2", "Описание 2");
        Subtask sub1 = manager.createSubtask(epic1, "Саб 1", "Описание", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic2, "Саб 2", "Описание", Status.NEW);

        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty(), "После удаления всех эпиков список должен быть пустым");
        assertTrue(manager.getAllSubtasks().isEmpty(), "При удалении всех эпиков должны удалиться и все подзадачи");
        assertThrows(NotFoundException.class, () -> manager.getEpic(epic1.getId()),
                "Удалённый Epic должен бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getEpic(epic2.getId()),
                "Удалённый Epic должен бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub1.getId()),
                "Subtask, связанный с Epic, должен бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub2.getId()),
                "Subtask, связанный с Epic, должен бросать NotFoundException");
    }

    @Test
    void shouldDeleteAllSubtasksButKeepEpics() {
        Epic epic = manager.createEpic("Эпик", "Описание");
        Subtask sub1 = manager.createSubtask(epic, "Саб 1", "Описание", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic, "Саб 2", "Описание", Status.NEW);

        manager.deleteAllSubtasks();

        assertTrue(manager.getAllSubtasks().isEmpty(), "После удаления всех подзадач список Subtask должен быть пустым");
        assertDoesNotThrow(() -> manager.getEpic(epic.getId()), "Epic должен остаться после удаления всех Subtask");
        assertTrue(epic.getSubtasksId().isEmpty(), "Список id подзадач в Epic должен быть пустым после удаления всех Subtask");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub1.getId()),
                "Удалённый Subtask должен бросать NotFoundException");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub2.getId()),
                "Удалённый Subtask должен бросать NotFoundException");
    }
}