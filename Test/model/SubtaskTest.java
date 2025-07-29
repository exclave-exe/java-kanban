package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    // -Проверьте, что наследники класса Task равны друг другу, если равен их id.
    @Test
    public void epicsShouldBeEqualIfTheyHaveTheSameId(){
        Epic epic1 = new Epic(1, "test1", "test2");
        Epic epic2 = new Epic(2, "test3", "test4");

        Subtask subtask1 = new Subtask(3, epic1, "test5", "test6", Status.NEW);
        Subtask subtask2 = new Subtask(3, epic2, "test7", "test8", Status.IN_PROGRESS);

        boolean result = subtask1.hasSameId(subtask2);

        assertTrue(result, "Subtasks с одинаковым id должны считаться равными");
    }

    // -Проверьте, что объект Subtask нельзя сделать своим же эпиком;
    @Test
    public void shouldThrowIfSubtaskIsItsOwnEpic() {
        Epic epic = new Epic(1, "test", "test");

        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask(1, epic, "test", "test", Status.NEW);
        });
    }
}