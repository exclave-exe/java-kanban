package manager;

import model.Task;
import java.util.ArrayList;
import model.Status;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    // - Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    public void shouldStoreExactSameTaskInstanceInHistory() {
        InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
        Task originalTask = new Task(1, "Test Task", "Description", Status.NEW);

        inMemoryHistoryManager.add(originalTask);
        ArrayList<Task> history = inMemoryHistoryManager.getHistory();
        Task taskFromHistory = history.get(0);

        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertSame(originalTask, taskFromHistory, "HistoryManager должен сохранить ту же ссылку на задачу");
        assertEquals(originalTask, taskFromHistory, "HistoryManager должен сохранять те же данные");
    }
}