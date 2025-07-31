package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    // Проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры
    // менеджеров;
    @Test
    public void shouldReturnInitializedManagers(){
        // Arrange
        InMemoryHistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();
        InMemoryTaskManager inMemoryTaskManager = Managers.getDefault();

        // Act && Assert
        assertNotNull(inMemoryHistoryManager);
        assertNotNull(inMemoryTaskManager);
        assertDoesNotThrow(() -> inMemoryHistoryManager.getHistory(),
                "Метод inMemoryHistoryManager должен работать без исключений.");
        assertDoesNotThrow(() -> inMemoryTaskManager.getHistory(),
                "Метод inMemoryTaskManager должен работать без исключений.");
    }
}