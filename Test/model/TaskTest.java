package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    // -Проверьте, что экземпляры класса Task равны друг другу, если равен их id.
    @Test
    public void tasksShouldBeEqualIfTheyHaveTheSameId(){
        Task task1 = new Task(1, "test1", "test2", Status.NEW);
        Task task2 = new Task(1, "test3", "test4", Status.IN_PROGRESS);

        boolean result = task1.hasSameId(task2);

        assertTrue(result, "Task с одинаковым id должны считаться равными");
    }
}