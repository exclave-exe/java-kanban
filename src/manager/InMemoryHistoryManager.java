package manager;

import model.*;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private ArrayList<Task> history;
    private static final int MAX_HISTORY_SIZE = 10;


    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    @Override
    public void add(Task anyTask) {
        history.add(anyTask);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
