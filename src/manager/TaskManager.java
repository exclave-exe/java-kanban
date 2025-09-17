package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    // Метод генерации неповторяющегося ID.
    int generateTotalId();

    // Методы создания объектов.
    Task createTask(String name, String description, Status status);

    Epic createEpic(String name, String description);

    Subtask createSubtask(Epic parent, String name, String description, Status status);

    // Методы возвращения копии списка всех Task / Epics / Subtask.
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    // Методы получения по ID из HashMap.
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // Методы удаления по ID из HashMap.
    boolean deleteTask(int id);

    boolean deleteEpic(int id);

    boolean deleteSubtask(int id);

    // Методы очищения HashMap.
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // Методы обновления полей.
    void updateStatus(Task task, Status status);

    void updateName(Task task, String name);

    void updateDescription(Task task, String description);

}