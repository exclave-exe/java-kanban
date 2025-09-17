package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager inMemoryHistoryManager;
    private Task task1;
    private Task task2;
    private Task task3;
    private Task task4;

    @BeforeEach
    void setUp() {
        inMemoryHistoryManager = new InMemoryHistoryManager();
        task1 = new Task(1, "test1", "test1", Status.NEW);
        task2 = new Task(2, "test2", "test2", Status.DONE);
        task3 = new Task(3, "test3", "test3", Status.IN_PROGRESS);
        task4 = new Task(4, "test4", "test4", Status.IN_PROGRESS);
    }

    // Проверка, что в HistoryManager не дублируются задачи.
    @Test
    public void shouldNotDuplicateTasksInHistory() {
        // Arrange
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task4);
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task4);

        // Act
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();

        // Assert
        assertEquals(4, history.size(), "Задачи не должны повторяться");
    }

    // Проверка, что в HistoryManager задачи хранятся в порядке вызова.
    @Test
    public void shouldStoreTasksInCallOrder() {
        // Arrange
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task4);
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task4);

        // Act
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();
        Task taskFromHistory1 = history.get(0);
        Task taskFromHistory2 = history.get(1);
        Task taskFromHistory3 = history.get(2);
        Task taskFromHistory4 = history.get(3);


        // Assert
        assertEquals(taskFromHistory1, task1, "Задачи должны храниться в порядке вызова.");
        assertEquals(taskFromHistory2, task3, "Задачи должны храниться в порядке вызова.");
        assertEquals(taskFromHistory3, task2, "Задачи должны храниться в порядке вызова.");
        assertEquals(taskFromHistory4, task4, "Задачи должны храниться в порядке вызова.");
    }

    // Проверка, что в HistoryManager Tasks корректно удаляются не нарушая порядок хранения.
    @Test
    public void shouldRemoveTaskWithoutBreakingOrder() {
        // Arrange
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task4);
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task4);

        // Act
        inMemoryHistoryManager.remove(task2.getId());
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();
        Task taskFromHistory1 = history.get(0);
        Task taskFromHistory2 = history.get(1);
        Task taskFromHistory3 = history.get(2);

        // Assert
        assertEquals(3, history.size(), "При удалении размер истории должен уменьшиться");
        assertEquals(taskFromHistory1, task1, "При удалении порядок не должен нарушаться.");
        assertEquals(taskFromHistory2, task3, "При удалении порядок не должен нарушаться.");
        assertEquals(taskFromHistory3, task4, "При удалении порядок не должен нарушаться.");
    }

    // Проверка, что в HistoryManager при пустой истории getHistory() возвращает пустой список.
    @Test
    public void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        assertTrue(inMemoryHistoryManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    // Проверка, что в HistoryManager после удаления последнего элемента возвращается пустой список.
    @Test
    public void shouldHandleRemovingSingleTask() {
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.remove(task1.getId());
        assertTrue(inMemoryHistoryManager.getHistory().isEmpty(), "После удаления последней задачи история должна быть пустой");
    }

    // Проверка, что в HistoryManager удаления головы происходит успешно.
    @Test
    public void shouldRemoveFirstTaskAndShiftHead() {
        // Arrange
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);

        // Act
        inMemoryHistoryManager.remove(task1.getId());
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();

        // Assert
        assertEquals(2, history.size(), "После удаления первой задачи размер должен уменьшиться");
        assertEquals(task2, history.get(0), "Новая голова должна быть task2");
        assertEquals(task3, history.get(1), "Второй элемент должен быть task3");
    }

    // Проверка, что в HistoryManager удаления хвоста происходит успешно.
    @Test
    public void shouldRemoveLastTaskAndUpdateTail() {
        // Arrange
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);

        // Act
        inMemoryHistoryManager.remove(task3.getId());
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();

        // Assert
        assertEquals(2, history.size(), "После удаления последней задачи размер должен уменьшиться");
        assertEquals(task1, history.get(0), "Первый элемент должен остаться task1");
        assertEquals(task2, history.get(1), "Новый хвост должен быть task2");
    }
}