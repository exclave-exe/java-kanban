package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    // Проверка, что Subtask равны друг другу, если равен их id.
    @Test
    public void epicsShouldBeEqualIfTheyHaveTheSameId() {
        // Arrange
        Epic epic1 = new Epic(1, "test1", "test2");
        Epic epic2 = new Epic(2, "test3", "test4");
        Subtask subtask1 = new Subtask(3, epic1.getId(), "test5", "test6", Status.NEW);
        Subtask subtask2 = new Subtask(3, epic2.getId(), "test7", "test8", Status.IN_PROGRESS);

        // Act & Assert
        assertTrue(subtask1.hasSameId(subtask2), "Subtasks с одинаковым id должны считаться равными.");
    }

    // Проверка, что объекты Epic равны друг другу, если равны их поля.
    @Test
    public void subtasksShouldBeEqualIfAllFieldsMatch() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");
        Subtask subtask1 = new Subtask(5, epic.getId(), "test", "test", Status.NEW);
        Subtask subtask2 = new Subtask(5, epic.getId(), "test", "test", Status.NEW);

        // Act & Assert
        assertEquals(subtask1, subtask2, "Subtasks с одинаковыми полями должны быть равны.");
    }

    // Проверка, что Subtask возвращает корректный ParentId.
    @Test
    public void shouldReturnCorrectParentId() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");
        Subtask subtask = new Subtask(2, epic.getId(), "test", "test", Status.NEW);

        //Act & Assert
        assertEquals(epic.getId(), subtask.getParentId(), "Должен возвращать корректный parentId.");
    }

    // Проверка, что объект Subtask нельзя сделать своим же эпиком.
    @Test
    public void shouldThrowIfSubtaskIsItsOwnEpic() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask(1, epic.getId(), "test", "test", Status.NEW);
        });
    }
}