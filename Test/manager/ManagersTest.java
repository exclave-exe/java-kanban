package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    // -Убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры
    // менеджеров;
    @Test
    public void shouldReturnInitializedManagers(){
        Managers managers = new Managers();
        InMemoryHistoryManager inMemoryHistoryManager = managers.getDefaultHistory();
        InMemoryTaskManager inMemoryTaskManager = managers.getDefault();


        assertNotNull(inMemoryHistoryManager);
        assertNotNull(inMemoryTaskManager);
        assertDoesNotThrow(() -> inMemoryHistoryManager.getHistory(),
                "Метод inMemoryHistoryManager должен работать без исключений");
        assertDoesNotThrow(() -> inMemoryTaskManager.getHistory(),
                "Метод inMemoryTaskManager должен работать без исключений");
    }
}