package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    // Проверка, что экземпляры класса Task равны друг другу, если равен их id.
    @Test
    public void tasksShouldBeEqualIfTheyHaveTheSameId(){
        // Arrange
        Task task1 = new Task(1, "test1", "test2", Status.NEW);
        Task task2 = new Task(1, "test3", "test4", Status.IN_PROGRESS);

        // Act & Assert
        assertTrue(task1.hasSameId(task2), "Task с одинаковым id должны считаться равными");
    }

    // Проверка, что экземпляры класса Task равны друг другу, если равны их поля.
    @Test
    public void tasksShouldBeEqualIfTheyHaveTheSameFields() {
        // Arrange
        Task t1 = new Task(9, "Same", "Fields", Status.NEW);
        Task t2 = new Task(9, "Same", "Fields", Status.NEW);

        // Act & Assert
        assertEquals(t1, t2);
    }

    // Проверка, что геттеры возвращают правильные значения.
    @Test
    public void constructorShouldInitializeFieldsCorrectly() {
        // Arrange
        Task task = new Task(1, "test1", "test2", Status.NEW);

        // Act & Assert
        assertEquals(1, task.getId(), "Должен возвращать корректное ID.");
        assertEquals("test1", task.getName(),"Должен возвращать корректное имя.");
        assertEquals("test2", task.getDescription(),"Должен возвращать корректное описание.");
        assertEquals(Status.NEW, task.getStatus(), "Должен возвращать корректный статус.");
    }

    // Проверка, что сеттеры корректно меняют значения.
    @Test
    public void setDetailsShouldChangeFields() {
        // Arrange
        Task task = new Task(1, "test", "test", Status.NEW);

        // Act
        task.setName("New test1");
        task.setDescription("New test2");
        task.setStatus(Status.DONE);

        // Assert
        assertEquals("New test1", task.getName(), "Должен возвращать новое имя.");
        assertEquals("New test2", task.getDescription(), "Должен возвращать новое описание.");
        assertEquals(Status.DONE, task.getStatus(), "Должен возвращать новый статус.");
    }
}