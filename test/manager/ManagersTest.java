package manager;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    // Проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры
    // менеджеров;
    @Test
    public void shouldReturnInitializedManagers() throws IOException {
        // Arrange
        File tempFile = File.createTempFile("test", ".csv", new File("resources/tests"));
        tempFile.deleteOnExit();
        FileBackedTaskManager fileBackedTaskManager = Managers.getFileBacked(tempFile);
        InMemoryTaskManager inMemoryTaskManager = Managers.getDefault();

        // Act && Assert
        assertNotNull(fileBackedTaskManager);
        assertNotNull(inMemoryTaskManager);
        assertDoesNotThrow(fileBackedTaskManager::getHistory,
                "Метод inMemoryHistoryManager должен работать без исключений.");
        assertDoesNotThrow(inMemoryTaskManager::getHistory,
                "Метод inMemoryTaskManager должен работать без исключений.");
    }
}