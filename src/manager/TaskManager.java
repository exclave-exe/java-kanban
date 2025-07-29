package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

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

    // Методы обновления статуса.
    void updateStatus(Task task, Status status);
    void updateStatus(Subtask subtask, Status status);

    // Методы обновления name и description.
    void update(Task task, String name, String description);
    void update(Epic epic, String name, String description);
    void update(Subtask subtask, String name, String description);

    // Метод возвращающий историю.
    void getHistory();
}