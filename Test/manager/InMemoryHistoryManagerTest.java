package manager;

import model.Task;
import java.util.ArrayList;
import model.Status;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    // Проверка, что добавляемые в HistoryManager задачи корректно сохраняются.
    @Test
    public void shouldStoreExactSameTaskInstanceInHistory() {
        // Arrange
        InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
        Task task1 = new Task(1, "test", "test", Status.NEW);
        Task task2 = new Task(1, "test", "test", Status.NEW);
        inMemoryHistoryManager.add(task1);
        inMemoryHistoryManager.add(task2);

        // Act
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();
        Task taskFromHistory1 = history.get(0);
        Task taskFromHistory2 = history.get(1);

        // Assert
        assertEquals(2, history.size(), "История должна содержать две задачи");
        assertSame(task1, taskFromHistory1, "HistoryManager должен сохранить ту же ссылку на задачу");
        assertEquals(task1, taskFromHistory1, "HistoryManager должен сохранять те же данные");
        assertSame(task2, taskFromHistory2, "HistoryManager должен сохранить ту же ссылку на задачу");
        assertEquals(task2, taskFromHistory2, "HistoryManager должен сохранять те же данные");
    }

    // Проверка, что история не может превышать больше 10 запросов.
    @Test
    public void shouldNotStoreMoreThan10TasksInHistory() {
        // Arrange
        InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
        inMemoryHistoryManager.add(new Task(1, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(2, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(3, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(4, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(5, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(6, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(7, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(8, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(9, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(10, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(11, "test", "test", Status.NEW));
        inMemoryHistoryManager.add(new Task(12, "test", "test", Status.NEW));

        // Act
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();

        // Assert
        assertEquals(10, history.size(), "История должна содержать десять задач");
    }
}