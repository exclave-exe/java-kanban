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

    // Методы просмотра HashMap.
    void printAllTasks();

    void printAllEpics();

    void printAllSubtasks();

    // Методы возвращения копии списка всех Task / Epics / Subtask.
    List<Task> returnAllTasks();

    List<Epic> returnAllEpics();

    List<Subtask> returnAllSubtasks();

    // Методы получения по ID из HashMap.
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // Методы удаления по ID из HashMap.
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // Методы очищения HashMap.
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // Методы обновления полей.
    void updateStatus(Task task, Status status);

    void updateName(Task task, String name);

    void updateDescription(Task task, String description);

    // Метод возвращающий историю.
    void getHistory();
}