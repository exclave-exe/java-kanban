package todoManager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    public static int totalId = 1;
    private HashMap<Integer, Task> allTasks;
    private HashMap<Integer, Epic> allEpics;
    private HashMap<Integer, Subtask> allSubtasks;

    public TaskManager() {
        allTasks = new HashMap<>();
        allEpics = new HashMap<>();
        allSubtasks = new HashMap<>();
    }

    // Методы создания.
    public Task createTask(String name, String description, Status status) {
        Task task = new Task(name, description, status);
        allTasks.put(task.getId(), task);
        System.out.println("Task успешно создан!");
        return task;
    }

    public Epic createEpic(String name, String description) {
        Epic epic = new Epic(name, description);
        allEpics.put(epic.getId(), epic);
        System.out.println("Epic успешно создан!");
        return epic;
    }

    public Subtask createSubtask(Epic parent, String name, String description, Status status) {
        Subtask subtask = new Subtask(parent, name, description, status);
        allSubtasks.put(subtask.getId(), subtask);
        parent.addSubtask(subtask);
        System.out.println("Subtask успешно добавлен!");
        return subtask;
    }

    // Методы получения по ID.
    public Task getTask(int id) {
        if (allTasks.get(id) == null) {
            System.out.println("Task с таким id не существует.");
            return null;
        } else {
            return allTasks.get(id);
        }
    }

    public Epic getEpic(int id) {
        if (allEpics.get(id) == null) {
            System.out.println("Epic с таким id не существует.");
            return null;
        } else {
            return allEpics.get(id);
        }
    }

    public Subtask getSubtask(int id) {
        if (allSubtasks.get(id) == null) {
            System.out.println("Subtask с таким id не существует.");
            return null;
        } else {
            return allSubtasks.get(id);
        }
    }

    // Методы удаления по ID.
    public void deleteTask(int id) {
        if (allTasks.get(id) == null) {
            System.out.println("Ошибка удаления. Task с таким id не существует.");
        } else {
            allTasks.remove(id);
            System.out.println("Task успешно удалён!");
        }
    }

    public void deleteEpic(int id) { // Перед удалением Epic стирает все его Subtasks.
        Epic epic = allEpics.get(id);
        if (epic == null) {
            System.out.println("Ошибка удаления. Epic с таким id не существует.");
            return;
        }

        ArrayList<Integer> toRemoveId = new ArrayList<>();
        for (Subtask subtask : allSubtasks.values()) {
            if (subtask.getParent().equals(epic)) {
                toRemoveId.add(subtask.getId());
            }
        }

        for (Integer subtaskId : toRemoveId) {
            allSubtasks.remove(subtaskId);
        }

        allEpics.remove(id);
        System.out.println("Epic и его Subtask(s) успешно удалены!");
    }

    public void deleteSubtask(int id) {
        if (allSubtasks.get(id) == null) {
            System.out.println("Ошибка удаления. Subtask с таким id не существует.");
        }

        allSubtasks.get(id).getParent().removeSubtaskById(id);
        allSubtasks.remove(id);
        System.out.println("Subtask успешно удален!");
    }

    // Методы очищения Tasks / Epics / Subtasks.
    public void deleteAllTasks() {
        allTasks.clear();
    }

    public void deleteAllEpics() { // Вместе с Epics очищает Subtasks
        allEpics.clear();
        allSubtasks.clear();
    }

    public void deleteEpicSubtasks(Epic epic) { // Очищает все Subtask переданного Epic
        ArrayList<Integer> toRemoveSubtasks = new ArrayList<>();
        for (Subtask subtask : allSubtasks.values()) {
            if (subtask.getParent().equals(epic)) {
                toRemoveSubtasks.add(subtask.getId());
            }
        }

        for (Integer subtaskId : toRemoveSubtasks) {
            allSubtasks.remove(subtaskId);
        }

        epic.removeAllSubtask();
    }

    // Методы просмотра Task / Epic / Subtasks
    public void printAllTasks() {
        System.out.println(allTasks.values());
    }

    public void printAllEpics() {
        System.out.println(allEpics.values());
    }

    public void printAllSubtasks() {
        System.out.println(allSubtasks.values());
    }
}

