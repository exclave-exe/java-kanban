package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    // -Проверьте, что наследники класса Task равны друг другу, если равен их id.
    @Test
    public void epicsShouldBeEqualIfTheyHaveTheSameId(){
        Epic epic1 = new Epic(1, "test1", "test2");
        Epic epic2 = new Epic(1, "test3", "test4");

        boolean result = epic1.hasSameId(epic2);

        assertTrue(result, "Epic с одинаковым id должны считаться равными");
    }

    // -Проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи.
    @Test
    public void shouldThrowIfEpicTriesToAddItselfAsSubtask() {
        Epic epic = new Epic(1, "test", "test");

        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(1);
        });
    }
}