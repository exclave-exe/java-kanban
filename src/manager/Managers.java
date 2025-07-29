package manager;

// Не стал делать класс утилитарным, поскольку навебинаре сказали о том, что это антипаттерн.
public class Managers {
    public InMemoryTaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}