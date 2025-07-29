package manager;

import model.Status;
import model.Task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import static org.junit.jupiter.api.Assertions.*;
class InMemoryTaskManagerTest {

    // -Cоздайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task newTask = inMemoryTaskManager.createTask("test", "test", Status.NEW);
        Task storedTask = inMemoryTaskManager.getTask(1);

        assertEquals(newTask, storedTask, "Task должен остаться прежним");
    }
}
