package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private static int totalId = 1;
    private final HashMap<Integer, Task> allTasks;
    private final HashMap<Integer, Epic> allEpics;
    private final HashMap<Integer, Subtask> allSubtasks;
    private final InMemoryHistoryManager inMemoryHistoryManager;

    public InMemoryTaskManager() {
        allTasks = new HashMap<>();
        allEpics = new HashMap<>();
        allSubtasks = new HashMap<>();
        inMemoryHistoryManager = new InMemoryHistoryManager();
    }

    // Метод генерации неповторяющегося ID.
    @Override
    public int generateTotalId() {
        return totalId++;
    }

    // Методы создания объектов.
    @Override
    public Task createTask(String name, String description, Status status) {
        int id = generateTotalId();
        Task task = new Task(id, name, description, status);
        allTasks.put(task.getId(), task);
        System.out.println("Task успешно создан!");
        return task;
    }

    @Override
    public Epic createEpic(String name, String description) {
        int id = generateTotalId();
        Epic epic = new Epic(id, name, description);
        allEpics.put(epic.getId(), epic);
        System.out.println("Epic успешно создан!");
        return epic;
    }

    @Override
    public Subtask createSubtask(Epic parent, String name, String description, Status status) {
        int id = generateTotalId();
        Subtask subtask = new Subtask(id, parent, name, description, status);
        parent.addSubtask(subtask.getId());
        allSubtasks.put(subtask.getId(), subtask);
        System.out.println("Subtask успешно добавлен!");
        return subtask;
    }

    // Методы просмотра HashMap.
    @Override
    public void printAllTasks() {
        System.out.println(allTasks.values());
    }

    @Override
    public void printAllEpics() {
        System.out.println(allEpics.values());
    }

    @Override
    public void printAllSubtasks() {
        System.out.println(allSubtasks.values());
    }

    // Методы получения по ID из HashMap.
    @Override
    public Task getTask(int id) {
        if (allTasks.get(id) == null) {
            System.out.println("Task с таким id не существует.");
            return null;
        } else {
            inMemoryHistoryManager.add(allTasks.get(id));
            return allTasks.get(id);
        }
    }

    @Override
    public Epic getEpic(int id) {
        if (allEpics.get(id) == null) {
            System.out.println("Epic с таким id не существует.");
            return null;
        } else {
            inMemoryHistoryManager.add(allEpics.get(id));
            return allEpics.get(id);
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        if (allSubtasks.get(id) == null) {
            System.out.println("Subtask с таким id не существует.");
            return null;
        } else {
            inMemoryHistoryManager.add(allSubtasks.get(id));
            return allSubtasks.get(id);
        }
    }

    // Методы удаления по ID из HashMap.
    @Override
    public void deleteTask(int id) {
        if (allTasks.get(id) == null) {
            System.out.println("Ошибка удаления. Task с таким id не существует.");
            return;
        }
        allTasks.remove(id);
        System.out.println("Task успешно удалён!");
        inMemoryHistoryManager.remove(id);
    }

    @Override
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
                inMemoryHistoryManager.remove(subtask.getId());
            }
        }
        for (Integer subtaskId : toRemoveId) {
            allSubtasks.remove(subtaskId);
        }
        allEpics.remove(id);
        inMemoryHistoryManager.remove(id);
        System.out.println("Epic и его Subtask(s) успешно удалены!");
    }

    @Override
    public void deleteSubtask(int id) {
        if (allSubtasks.get(id) == null) {
            System.out.println("Ошибка удаления. Subtask с таким id не существует.");
            return;
        }
        allEpics.get(allSubtasks.get(id).getParentId()).removeSubtask(id);
        updateEpicStatus(allEpics.get(allSubtasks.get(id).getParentId()));
        allSubtasks.remove(id);
        inMemoryHistoryManager.remove(id);

        System.out.println("Subtask успешно удален!");
    }

    // Методы очищения HashMap.
    @Override
    public void deleteAllTasks() {
        for (int id : allTasks.keySet()) {
            inMemoryHistoryManager.remove(id);
        }
        allTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (int id : allSubtasks.keySet()) {
            inMemoryHistoryManager.remove(id);
        }
        for (int id : allEpics.keySet()) {
            inMemoryHistoryManager.remove(id);
        }
        allEpics.clear();
        allSubtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (int id : allSubtasks.keySet()) {
            inMemoryHistoryManager.remove(id);
        }
        allSubtasks.clear();
    }

    // Методы обновления статуса.
    @Override
    public void updateStatus(Task task, Status status) {
        task.setStatus(status);
        if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            updateEpicStatus(allEpics.get(subtask.getParentId()));
        }
    }

    // Методы обновления name и description.
    @Override
    public void updateName(Task task, String name) {
        task.setName(name);
    }

    @Override
    public void updateDescription(Task task, String description) {
        task.setDescription(description);
    }

    public void getHistory() {
        System.out.println(inMemoryHistoryManager.getHistory());
    }

    // Отдельный приватный метод для автоматического обновления статуса Epic в течении кода.
    private void updateEpicStatus(Epic epic) {
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

