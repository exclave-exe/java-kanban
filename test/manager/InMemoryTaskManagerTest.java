package manager;

import exceptions.TimeInterectionException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    // После вызова .get_() объект должен добавляться в историю.
    @Test
    void shouldAddTaskEpicSubtaskToHistoryWhenGetById() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);

        manager.getTask(task.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask.getId());

        List<Task> history = manager.getHistory();
        assertTrue(history.contains(task), "История должна содержать Task после вызова getTask()");
        assertTrue(history.contains(epic), "История должна содержать Epic после вызова getEpic()");
        assertTrue(history.contains(subtask), "История должна содержать Subtask после вызова getSubtask()");
    }

    // После удаления Task он также должен удаляться из истории.
    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        manager.getTask(task.getId());

        manager.deleteTask(task.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.contains(task), "После удаления задачи её не должно быть в истории");
    }

    // После удаления Epic, он и все его Subtask должны удаляться из истории.
    @Test
    void shouldRemoveEpicAndItsSubtasksFromHistoryWhenDeleted() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());

        manager.deleteEpic(epic.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.contains(epic), "После удаления Epic, он должен быть удален из истории");
        assertFalse(history.contains(subtask1), "После удаления Epic все его Subtask должны быть удалены из истории");
        assertFalse(history.contains(subtask2), "После удаления Epic все его Subtask должны быть удалены из истории");
    }

    //  После удаления Subtask он также должен удаляться из истории, а его Epic должен остаться.
    @Test
    void shouldRemoveSubtaskFromHistoryWhenDeleted() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        manager.getSubtask(subtask.getId());
        manager.getEpic(epic.getId());

        manager.deleteSubtask(subtask.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.contains(subtask), "После удаления Subtask он должно быть удалён из истории");
        assertTrue(history.contains(epic), "После удаления Subtask его Epic должен остаться в истории");
    }

    // После удаления всех Task с помощью .deleteAllTasks() история не должна содержать их.
    @Test
    void shouldClearHistoryWhenAllTasksDeleted() {
        Task task1 = manager.createTask("Task 1", "Description", Status.NEW);
        Task task2 = manager.createTask("Task 2", "Description", Status.NEW);
        Task task3 = manager.createTask("Task 3", "Description", Status.NEW);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());

        manager.deleteAllTasks();

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "После удаления всех Task история должна быть пуста");
    }

    // После удаления всех Epic с помощью .deleteAllEpics() история не должна содержать Epic и Subtask.
    @Test
    void shouldClearHistoryWhenAllEpicsDeleted() {
        Epic epic1 = manager.createEpic("Epic 1", "Description");
        Epic epic2 = manager.createEpic("Epic 2", "Description");
        Subtask subtask1 = manager.createSubtask(epic1, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic1, "Subtask 2", "Description", Status.NEW);
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());

        manager.deleteAllEpics();

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "После удаления всех Epic и Subtask история должна быть пуста");
    }

    // После удаления всех Subtask с помощью .deleteAllSubtasks() они должны быть удалены из истории, а их Epic должны остаться.
    @Test
    void shouldClearSubtaskHistoryWhenAllSubtasksDeleted() {
        Epic epic1 = manager.createEpic("Epic 1", "Description");
        Epic epic2 = manager.createEpic("Epic 2", "Description");
        Subtask sub1 = manager.createSubtask(epic1, "Subtask 1", "Description", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic2, "Subtask 2", "Description", Status.NEW);
        manager.getSubtask(sub1.getId());
        manager.getSubtask(sub2.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());

        manager.deleteAllSubtasks();

        List<Task> history = manager.getHistory();
        assertFalse(history.contains(sub1), "После удаления всех Subtask история не должна содержать Subtask");
        assertFalse(history.contains(sub2), "После удаления всех Subtask история не должна содержать Subtask");
        assertTrue(history.contains(epic1), "После удаления всех Subtask его Epic должен остаться в истории");
        assertTrue(history.contains(epic2), "После удаления всех Subtask его Epic должен остаться в истории");
    }

    // Время у Task должно корректно настраиваться через .setStartTimeAndDuration().
    @Test
    void shouldSetTaskTimeCorrectlyUsingSetStartTimeAndDuration() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        LocalDateTime starTimeTask = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationTask = Duration.ofMinutes(30);
        LocalDateTime endTimeTask = starTimeTask.plus(durationTask);

        manager.setStartTimeAndDuration(task, starTimeTask, durationTask);

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertTrue(consistent.contains(task), "tasksByPriority должен содержать Task");
        assertTrue(reversible.contains(task), "tasksByPriority должен содержать Task");
        assertEquals(task.getStartTime(), starTimeTask, "startTime должен совпадать с task.getStartTime()");
        assertEquals(task.getDurationTime(), durationTask, "duration должен совпадать с task.getDurationTime()");
        assertEquals(task.getEndTime(), endTimeTask, "endTime должен правильно высчитываться внутри Task");
    }

    // Время у Subtask должно корректно настраиваться через .setStartTimeAndDuration().
    @Test
    void shouldSetSubtaskTimeCorrectlyUsingSetStartTimeAndDuration() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        LocalDateTime starTimeSubtask = LocalDateTime.of(2025, 9, 14, 12, 0);
        Duration durationSubtask = Duration.ofMinutes(45);
        LocalDateTime endTimeSubtask = starTimeSubtask.plus(durationSubtask);

        manager.setStartTimeAndDuration(subtask, starTimeSubtask, durationSubtask);

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertTrue(consistent.contains(subtask), "tasksByPriority должен содержать Subtask");
        assertTrue(reversible.contains(subtask), "tasksByPriority должен содержать Subtask");
        assertEquals(subtask.getStartTime(), starTimeSubtask, "startTime должен совпадать с subtask.getStartTime()");
        assertEquals(subtask.getDurationTime(), durationSubtask, "duration должен совпадать с subtask.getDurationTime()");
        assertEquals(subtask.getEndTime(), endTimeSubtask, "endTime должен правильно высчитываться внутри Subtask");
    }

    // Время у Epic НЕ должно настраиваться напрямую.
    @Test
    void epicTimeShouldNotBeSetDirectly() {
        Epic epic = manager.createEpic("Epic", "Description");

        manager.setStartTimeAndDuration(epic, LocalDateTime.of(2025, 9, 14, 10, 0),
                Duration.ofMinutes(30));

        assertFalse(manager.getTasksByPriority(true).contains(epic), "TasksByPriority не должен содержать Epic");
        assertFalse(manager.getTasksByPriority(false).contains(epic), "TasksByPriority не должен содержать Epic");
        assertNull(epic.getStartTime(), "Epic не должен содержать starTime");
        assertEquals(Duration.ZERO, epic.getDurationTime(), "У Epic DurationTime должен быть равен нулю");
        assertNull(epic.getEndTime(), "Epic не должен содержать endTime");
    }

    // Epic НЕ должен содержаться в TasksByPriority.
    @Test
    void shouldNotAddEpicToTasksByPriority() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        LocalDateTime startSubtask = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(subtask, startSubtask, durationSubtask);

        assertFalse(manager.getTasksByPriority(true).contains(epic),
                "TasksByPriority не должен содержать Epic");
        assertFalse(manager.getTasksByPriority(false).contains(epic),
                "TasksByPriority не должен содержать Epic");
    }

    // Время у Epic должно обновляться при настройке Subtask.
    @Test
    void shouldUpdateEpicTimeWhenSettingSubtask() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        LocalDateTime startSubtask = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask = Duration.ofMinutes(30);
        LocalDateTime endSubtask = startSubtask.plus(durationSubtask);

        manager.setStartTimeAndDuration(subtask, startSubtask, durationSubtask);

        assertEquals(epic.getStartTime(), startSubtask, "StartTime Epic должен совпадать с StarTime Subtask");
        assertEquals(epic.getDurationTime(), durationSubtask, "Duration Epic должен совпадать с Duration Subtask");
        assertEquals(epic.getEndTime(), endSubtask, "EndTime Epic должен совпадать с EndTime Subtask");
    }

    // Время у Epic должно обновляться на Null / 0 при удалении Subtask.
    @Test
    void shouldResetEpicTimeAfterAllSubtasksDeleted() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        LocalDateTime startSubtask = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(subtask, startSubtask, durationSubtask);
        manager.deleteSubtask(subtask.getId());

        assertNull(epic.getStartTime(), "StartTime Epic должен быть Null");
        assertEquals(Duration.ZERO, epic.getDurationTime(), "Duration Epic должен быть 0");
        assertNull(epic.getEndTime(), "EndTime Epic должен должен быть Null>");
    }

    // Время у Epic должно обновляться относительно оставшихся Subtask при удалении Subtask.
    @Test
    void shouldUpdateEpicTimeAfterSubtaskDeletion() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);
        LocalDateTime startSubtask1 = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask1 = Duration.ofMinutes(30);
        LocalDateTime endSubtask1 = startSubtask1.plus(durationSubtask1);
        LocalDateTime startSubtask2 = LocalDateTime.of(2025, 9, 14, 15, 30);
        Duration durationSubtask2 = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(subtask1, startSubtask1, durationSubtask1);
        manager.setStartTimeAndDuration(subtask2, startSubtask2, durationSubtask2);
        manager.deleteSubtask(subtask2.getId());

        assertEquals(epic.getStartTime(), startSubtask1, "StartTime Epic должен совпадать с StarTime Subtask1");
        assertEquals(epic.getDurationTime(), durationSubtask1, "duration Epic должен совпадать с duration Subtask1");
        assertEquals(epic.getEndTime(), endSubtask1, "EndTime Epic должен совпадать с EndTime Subtask1");
    }

    // Продолжительностью Epic должно являться суммой продолжительностей всех его подзадач.
    @Test
    void shouldCalculateEpicDurationAsSumOfSubtasks() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);
        LocalDateTime startSubtask1 = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask1 = Duration.ofMinutes(30);
        LocalDateTime startSubtask2 = LocalDateTime.of(2025, 9, 14, 15, 30);
        Duration durationSubtask2 = Duration.ofMinutes(30);
        LocalDateTime endSubtask2 = startSubtask2.plus(durationSubtask1);
        Duration durationBetweenTasks = Duration.between(startSubtask1, endSubtask2);

        manager.setStartTimeAndDuration(subtask1, startSubtask1, durationSubtask1);
        manager.setStartTimeAndDuration(subtask2, startSubtask2, durationSubtask2);

        assertEquals(epic.getStartTime(), startSubtask1, "StartTime Epic должен совпадать с StarTime Subtask1");
        assertEquals(epic.getDurationTime(), durationBetweenTasks,
                "Должно являться суммой продолжительностей всех его подзадач");
        assertEquals(epic.getEndTime(), endSubtask2, "EndTime Epic должен совпадать с EndTime Subtask2");
    }

    // В tasksByPriority должен быть порядок относительно переданного аргумента.
    @Test
    void shouldReturnTasksInAscendingOrder() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task, LocalDateTime.of(2025, 9, 14, 10, 0),
                Duration.ofMinutes(30));
        manager.setStartTimeAndDuration(subtask, LocalDateTime.of(2025, 9, 14, 12, 0),
                Duration.ofMinutes(45));

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));

        assertEquals(consistent.get(0), task, "TasksByPriority должен возвращать от меньшего к большему");
        assertEquals(consistent.get(1), subtask, "TasksByPriority должен возвращать от меньшего к большему");
    }

    @Test
    void shouldReturnTasksInDescendingOrder() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask = manager.createSubtask(epic, "Subtask", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task, LocalDateTime.of(2025, 9, 14, 10, 0),
                Duration.ofMinutes(30));
        manager.setStartTimeAndDuration(subtask, LocalDateTime.of(2025, 9, 14, 12, 0),
                Duration.ofMinutes(45));

        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));

        assertEquals(reversible.get(0), subtask, "TasksByPriority должен возвращать от большего к меньшему");
        assertEquals(reversible.get(1), task, "TasksByPriority должен возвращать от большего к меньшему");
    }

    // После удаления Task он также должен удаляться из tasksByPriority.
    @Test
    void shouldRemoveTaskFromTasksByPriorityWhenDeleted() {
        Task task = manager.createTask("Task", "Description", Status.NEW);
        LocalDateTime starTimeTask = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationTask = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(task, starTimeTask, durationTask);
        manager.deleteTask(task.getId());

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertFalse(consistent.contains(task), "После удаления задачи её не должно быть в tasksByPriority");
        assertFalse(reversible.contains(task), "После удаления задачи её не должно быть в tasksByPriority");

    }

    // После удаления Epic все его Subtask должны удаляться из tasksByPriority.
    @Test
    void shouldRemoveEpicAndItsSubtasksFromTasksByPriorityWhenDeleted() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic, "Subtask 2", "Description", Status.NEW);
        LocalDateTime startSubtask1 = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask1 = Duration.ofMinutes(30);
        LocalDateTime startSubtask2 = LocalDateTime.of(2025, 9, 14, 15, 30);
        Duration durationSubtask2 = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(subtask1, startSubtask1, durationSubtask1);
        manager.setStartTimeAndDuration(subtask2, startSubtask2, durationSubtask2);
        manager.deleteEpic(epic.getId());

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertFalse(consistent.contains(subtask1),
                "После удаления Epic все его Subtask должны быть удалены из tasksByPriority");
        assertFalse(reversible.contains(subtask2),
                "После удаления Epic все его Subtask должны быть удалены из tasksByPriority");
    }

    //  После удаления Subtask он также должен удаляться из tasksByPriority.
    @Test
    void shouldRemoveSubtaskFromTasksByPriorityWhenDeleted() {
        Epic epic = manager.createEpic("Epic", "Description");
        Subtask subtask1 = manager.createSubtask(epic, "Subtask 1", "Description", Status.NEW);
        LocalDateTime startSubtask1 = LocalDateTime.of(2025, 9, 14, 10, 0);
        Duration durationSubtask1 = Duration.ofMinutes(30);

        manager.setStartTimeAndDuration(subtask1, startSubtask1, durationSubtask1);
        manager.deleteSubtask(subtask1.getId());

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertFalse(consistent.contains(subtask1), "После удаления Subtask он должно быть удалён из tasksByPriority");
        assertFalse(reversible.contains(subtask1), "После удаления Subtask он должно быть удалён из tasksByPriority");
    }

    // После удаления всех Task с помощью .deleteAllTasks() TasksByPriority не должна содержать их.
    @Test
    void shouldClearTasksByPriorityWhenAllTasksDeleted() {
        Task task1 = manager.createTask("Task 1", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task1, LocalDateTime.of(2025, 9, 17, 9, 0),
                Duration.ofHours(2));
        Task task2 = manager.createTask("Task 2", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task2, LocalDateTime.of(2025, 9, 17, 12, 0),
                Duration.ofHours(1));
        Task task3 = manager.createTask("Task 3", "Description", Status.NEW);
        manager.setStartTimeAndDuration(task3, LocalDateTime.of(2025, 9, 17, 15, 30),
                Duration.ofMinutes(90));

        manager.deleteAllTasks();

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertTrue(consistent.isEmpty(), "После удаления всех Task tasksByPriority должна быть пуста");
        assertTrue(reversible.isEmpty(), "После удаления всех Task tasksByPriority должна быть пуста");
    }

    // После удаления всех Epic с помощью .deleteAllEpics() TasksByPriority не должна содержать Subtask.
    @Test
    void shouldClearTasksByPriorityWhenAllEpicsDeleted() {
        Epic epic1 = manager.createEpic("Epic 1", "Description");
        Subtask subtask1 = manager.createSubtask(epic1, "Subtask 1", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask1, LocalDateTime.of(2025, 9, 17, 10, 0),
                Duration.ofHours(1));
        Subtask subtask2 = manager.createSubtask(epic1, "Subtask 2", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask2, LocalDateTime.of(2025, 9, 17, 11, 30),
                Duration.ofMinutes(90));

        manager.deleteAllEpics();

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertTrue(consistent.isEmpty(), "После удаления всех Epic tasksByPriority должна быть пуста");
        assertTrue(reversible.isEmpty(), "После удаления всех Epic tasksByPriority должна быть пуста");
    }

    // После удаления всех Subtask с помощью .deleteAllSubtasks() TasksByPriority не должна содержать Subtask.
    @Test
    void shouldClearSubtaskTasksByPriorityWhenAllSubtasksDeleted() {
        Epic epic1 = manager.createEpic("Epic 1", "Description");
        Subtask subtask1 = manager.createSubtask(epic1, "Subtask 1", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask1, LocalDateTime.of(2025, 9, 17, 10, 0),
                Duration.ofHours(1));
        Subtask subtask2 = manager.createSubtask(epic1, "Subtask 2", "Description", Status.NEW);
        manager.setStartTimeAndDuration(subtask2, LocalDateTime.of(2025, 9, 17, 11, 30),
                Duration.ofMinutes(90));

        manager.deleteAllSubtasks();

        ArrayList<Task> consistent = new ArrayList<>(manager.getTasksByPriority(true));
        ArrayList<Task> reversible = new ArrayList<>(manager.getTasksByPriority(false));
        assertTrue(consistent.isEmpty(), "После удаления всех Subtask tasksByPriority должна быть пуста");
        assertTrue(reversible.isEmpty(), "После удаления всех Subtask tasksByPriority должна быть пуста");
    }

    // При установке пересекающего времени оно не должно устанавливаться.
    @Test
    void testSetStartTimeAndDuration_withIntersection_taskNotAdded() {
        Task task1 = manager.createTask("Task 1", "Description 1", Status.NEW);
        LocalDateTime start1 = LocalDateTime.of(2025, 9, 17, 10, 0);
        Duration duration1 = Duration.ofHours(2);
        Task task2 = manager.createTask("Task 2", "Description 2", Status.NEW);
        LocalDateTime start2 = LocalDateTime.of(2025, 9, 17, 11, 0);
        Duration duration2 = Duration.ofHours(1);

        manager.setStartTimeAndDuration(task1, start1, duration1);

        assertThrows(TimeInterectionException.class, () -> manager.setStartTimeAndDuration(task2, start2, duration2));
        assertFalse(manager.getTasksByPriority(true).contains(task2));
        assertFalse(manager.getTasksByPriority(false).contains(task2));
        assertNull(task2.getStartTime());
        assertEquals(Duration.ZERO, task2.getDurationTime());
        assertNull(task2.getEndTime());
    }

    // При установке не пересекающего времени оно должно устанавливаться.
    @Test
    void testSetStartTimeAndDuration_withoutIntersection_taskAdded() {
        Task task1 = manager.createTask("Task 1", "Description 1", Status.NEW);
        LocalDateTime start1 = LocalDateTime.of(2025, 9, 17, 10, 0);
        Duration duration1 = Duration.ofHours(2);
        Task task2 = manager.createTask("Task 2", "Description 2", Status.NEW);
        LocalDateTime start2 = LocalDateTime.of(2025, 9, 17, 12, 1);
        Duration duration2 = Duration.ofHours(1);

        manager.setStartTimeAndDuration(task1, start1, duration1);
        manager.setStartTimeAndDuration(task2, start2, duration2);

        assertTrue(manager.getTasksByPriority(true).contains(task2));
        assertTrue(manager.getTasksByPriority(false).contains(task2));
        assertEquals(task2.getStartTime(), start2);
        assertEquals(task2.getDurationTime(), duration2);
    }
}
