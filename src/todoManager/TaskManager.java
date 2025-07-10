package todoManager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int totalId = 1;
    private HashMap<Integer, Task> allTasks;
    private HashMap<Integer, Epic> allEpics;
    private HashMap<Integer, Subtask> allSubtasks;

    public TaskManager() {
        allTasks = new HashMap<>();
        allEpics = new HashMap<>();
        allSubtasks = new HashMap<>();
    }

    // Метод генерации неповторяющегося ID.
    private int generateTotalId() {
        return totalId++;
    }

    // Методы создания объектов.
    public Task createTask(String name, String description, Status status) {
        int id = generateTotalId();
        Task task = new Task(id, name, description, status);
        allTasks.put(task.getId(), task);
        System.out.println("Task успешно создан!");
        return task;
    }

    public Epic createEpic(String name, String description) {
        int id = generateTotalId();
        Epic epic = new Epic(id, name, description);
        allEpics.put(epic.getId(), epic);
        System.out.println("Epic успешно создан!");
        return epic;
    }

    public Subtask createSubtask(Epic parent, String name, String description, Status status) {
        int id = generateTotalId();
        Subtask subtask = new Subtask(id, parent, name, description, status);
        parent.addSubtask(subtask.getId());
        allSubtasks.put(subtask.getId(), subtask);
        System.out.println("Subtask успешно добавлен!");
        return subtask;
    }

    // Методы просмотра HashMap.
    public void printAllTasks() {
        System.out.println(allTasks.values());
    }

    public void printAllEpics() {
        System.out.println(allEpics.values());
    }

    public void printAllSubtasks() {
        System.out.println(allSubtasks.values());
    }

    // Методы получения по ID из HashMap.
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

    // Методы удаления по ID из HashMap.
    public void deleteTask(int id) {
        if (allTasks.get(id) == null) {
            System.out.println("Ошибка удаления. Task с таким id не существует.");
            return;
        }
        allTasks.remove(id);
        System.out.println("Task успешно удалён!");
    }

    public void deleteEpic(int id) {
        if (allEpics.get(id) == null) {
            System.out.println("Ошибка удаления. Epic с таким id не существует.");
            return;
        }
        Epic epic = allEpics.get(id);
        ArrayList<Integer> toRemoveId = new ArrayList<>();
        for (Subtask subtask : allSubtasks.values()) {
            if (subtask.getParentId() == epic.getId()) {
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
            return;
        }
        allEpics.get(allSubtasks.get(id).getParentId()).removeSubtask(id);
        updateStatus(allEpics.get(allSubtasks.get(id).getParentId()));
        allSubtasks.remove(id);

        System.out.println("Subtask успешно удален!");
    }

    // Методы очищения HashMap.
    public void deleteAllTasks() {
        allTasks.clear();
    }

    public void deleteAllEpics() {
        allEpics.clear();
        allSubtasks.clear();
    }

    public void deleteAllSubtasks() {
        allSubtasks.clear();
    }

    // Методы обновления статуса.
    public void updateStatus(Task task, Status status) {
        task.setStatus(status);
    }

    public void updateStatus(Subtask subtask, Status status) {
        subtask.setStatus(status);
        updateStatus(allEpics.get(subtask.getParentId()));
    }

    // Методы обновления name и description.
    public void update(Task task, String name, String description) {
        task.setDetails(name,description);
    }

    public void update(Epic epic, String name, String description) {
        epic.setDetails(name,description);
    }

    public void update(Subtask subtask, String name, String description) {
        subtask.setDetails(name,description);
    }

    // Отдельный приватный метод для автоматического обновления статуса Epic в течении кода.
    private void updateStatus(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        ArrayList<Subtask> subtaskOfAnEpic = new ArrayList<>();
        for (Subtask subtask : allSubtasks.values()) {
            if (subtask.getParentId() == epic.getId()) {
                subtaskOfAnEpic.add(subtask);
            }
        }
        boolean isAllNew = true;
        boolean isAllDone = true;
        for (Subtask subtask : subtaskOfAnEpic) {
            if (subtask.getStatus() != Status.NEW) {
                isAllNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                isAllDone = false;
            }
        }
        if (isAllNew) {
            epic.setStatus(Status.NEW);
        } else if (isAllDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}

