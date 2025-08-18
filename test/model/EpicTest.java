package model;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    // Проверка, что объекты Epic равны друг другу, если равен их id.
    @Test
    public void epicsShouldBeEqualIfTheyHaveTheSameId(){
        // Arrange
        Epic epic1 = new Epic(1, "test1", "test2");
        Epic epic2 = new Epic(1, "test3", "test4");

        // Act & Assert
        assertTrue(epic1.hasSameId(epic2), "Epic с одинаковым id должны считаться равными.");
    }

    // Проверка, что объекты Epic равны друг другу, если равны их поля.
    @Test
    public void epicsShouldBeEqualIfTheyHaveTheSameFieldsAndId(){
        // Arrange
        Epic epic1 = new Epic(1, "test", "test");
        Epic epic2 = new Epic(1, "test", "test");

        // Act & Assert
        assertTrue(epic1.equals(epic2), "Epic с одинаковыми id и полями должны считаться равными.");
    }

    // Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи.
    @Test
    public void shouldThrowIfEpicTriesToAddItselfAsSubtask() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(1);
        });
    }

    // Проверка, что в Epic добавляется Subtask ID.
    @Test
    public void shouldAddSubtaskIdToEpic() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");

        // Act
        epic.addSubtask(2);
        epic.addSubtask(3);
        epic.addSubtask(4);

        // Assert
        assertEquals(3, epic.getSubtasksId().size(), "Должно быть три subtask ID.");
        assertTrue(epic.getSubtasksId().contains(2), "Список должен содержать ID равное 2.");
        assertTrue(epic.getSubtasksId().contains(3), "Список должен содержать ID равное 3.");
        assertTrue(epic.getSubtasksId().contains(4), "Список должен содержать ID равное 4.");
    }

    // Проверка, что из Epic удаляется Subtask ID.
    @Test
    public void shouldRemoveSubtaskIdFromEpic() {
        // Arrange
        Epic epic = new Epic(1, "test", "test");

        // Act
        epic.addSubtask(2);
        epic.addSubtask(3);
        epic.removeSubtask(2);

        // Assert
        assertEquals(1, epic.getSubtasksId().size(), "После удаления должен остаться один subtask.");
        assertFalse(epic.getSubtasksId().contains(2), "ID 2 должен быть удалён.");
        assertTrue(epic.getSubtasksId().contains(3), "ID 3 должен остаться.");
    }

    // Проверка, что getSubtasksId() возвращает копию списка.
   @Test
    public void shouldReturnListCopy() {
        // Arrange
       Epic epic = new Epic(1, "test", "test");
       epic.addSubtask(2);
       epic.addSubtask(3);

       // Act
       List<Integer> subtasksId = epic.getSubtasksId();
       subtasksId.clear();

       // Act & Assert
       assertNotEquals(epic.getSubtasksId(), subtasksId, "Должна возвращаться копия списка.");
   }
}