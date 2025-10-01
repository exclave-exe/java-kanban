package manager;

import exceptions.NotFoundException;
import exceptions.TimeArgumentException;
import exceptions.TimeInterectionException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected static int totalId = 1;
    protected final HashMap<Integer, Task> allTasks;
    protected final HashMap<Integer, Epic> allEpics;
    protected final HashMap<Integer, Subtask> allSubtasks;
    protected final InMemoryHistoryManager inMemoryHistoryManager;
    protected final TreeSet<Task> tasksByPriority;

    public InMemoryTaskManager() {
        allTasks = new HashMap<>();
        allEpics = new HashMap<>();
        allSubtasks = new HashMap<>();
        inMemoryHistoryManager = new InMemoryHistoryManager();
        tasksByPriority = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));
    }

    @Override
    public int generateTotalId() {
        return totalId++;
    }

    @Override
    public Task createTask(String name, String description, Status status) {
        int id = generateTotalId();
        Task task = new Task(id, name, description, status);
        allTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(String name, String description) {
        int id = generateTotalId();
        Epic epic = new Epic(id, name, description);
        allEpics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Epic parent, String name, String description, Status status) {
        int id = generateTotalId();
        Subtask subtask = new Subtask(id, parent.getId(), name, description, status);
        parent.addSubtask(subtask.getId());
        allSubtasks.put(subtask.getId(), subtask);
        updateEpicStatus(parent);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(allSubtasks.values());
    }

    @Override
    public Task getTask(int taskId) {
        if (allTasks.get(taskId) == null) {
            throw new NotFoundException("Task c ID:" + taskId + " не найден");
        }
        inMemoryHistoryManager.add(allTasks.get(taskId));
        return allTasks.get(taskId);
    }

    @Override
    public Epic getEpic(int epicId) {
        if (allEpics.get(epicId) == null) {
            throw new NotFoundException("Task c ID:" + epicId + " не найден");
        }
        inMemoryHistoryManager.add(allEpics.get(epicId));
        return allEpics.get(epicId);
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        if (allSubtasks.get(subtaskId) == null) {
            throw new NotFoundException("Task c ID:" + subtaskId + " не найден");
        }
        inMemoryHistoryManager.add(allSubtasks.get(subtaskId));
        return allSubtasks.get(subtaskId);
    }

    @Override
    public boolean deleteTask(int taskId) {
        if (allTasks.get(taskId) == null) {
            return false;
        }
        tasksByPriority.remove(allTasks.get(taskId));
        inMemoryHistoryManager.remove(taskId);
        allTasks.remove(taskId);
        return true;
    }

    @Override
    public boolean deleteEpic(int epicId) {
        if (allEpics.get(epicId) == null) {
            return false;
        }
        allEpics.get(epicId).getSubtasksId().forEach(subtaskId -> {
            tasksByPriority.remove(allSubtasks.get(subtaskId));
            inMemoryHistoryManager.remove(subtaskId);
            allSubtasks.remove(subtaskId);
        });
        tasksByPriority.remove(allEpics.get(epicId));
        inMemoryHistoryManager.remove(epicId);
        allEpics.remove(epicId);
        return true;
    }

    @Override
    public boolean deleteSubtask(int subtaskId) {
        if (allSubtasks.get(subtaskId) == null) {
            return false;
        }
        Epic parent = allEpics.get(allSubtasks.get(subtaskId).getParentId());
        parent.removeSubtask(subtaskId);
        tasksByPriority.remove(allSubtasks.get(subtaskId));
        inMemoryHistoryManager.remove(subtaskId);
        allSubtasks.remove(subtaskId);
        updateEpicStatus(parent);
        updateEpicStartTimeAndDuration(parent);
        return true;
    }

    @Override
    public void deleteAllTasks() {
        allTasks.keySet().forEach(taskId -> {
            tasksByPriority.remove(allTasks.get(taskId));
            inMemoryHistoryManager.remove(taskId);
        });
        allTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        allSubtasks.keySet().forEach(subtaskId -> {
            tasksByPriority.remove(allSubtasks.get(subtaskId));
            inMemoryHistoryManager.remove(subtaskId);
        });
        allEpics.keySet().forEach(taskId -> {
            tasksByPriority.remove(allEpics.get(taskId));
            inMemoryHistoryManager.remove(taskId);
        });
        allEpics.clear();
        allSubtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        allSubtasks.keySet().forEach(subtaskId -> {
            tasksByPriority.remove(allSubtasks.get(subtaskId));
            inMemoryHistoryManager.remove(subtaskId);
        });
        allEpics.values().forEach(epic -> {
            epic.removeAllSubtask();
            updateEpicStatus(epic);
            updateEpicStartTimeAndDuration(epic);
        });
        allSubtasks.clear();
    }

    @Override
    public void updateStatus(Task task, Status status) {
        switch (task) {
            case Subtask subtask -> {
                subtask.setStatus(status);
                updateEpicStatus(allEpics.get(subtask.getParentId()));
            }
            case Epic ignored -> {
                // Обновление Epic происходит автоматически
            }
            default -> {
                task.setStatus(status);
            }
        }
    }

    @Override
    public void updateName(Task task, String name) {
        task.setName(name);
    }

    @Override
    public void updateDescription(Task task, String description) {
        task.setDescription(description);
    }

    public List<Task> getHistory() {
        return new ArrayList<>(inMemoryHistoryManager.getHistory());
    }

    public Set<Task> getTasksByPriority(boolean ascending) {
        if (ascending) {
            return new TreeSet<>(tasksByPriority);
        } else {
            return tasksByPriority.descendingSet();
        }
    }

    public boolean isIntersection(LocalDateTime startTime, Duration duration) {
        if (startTime == null || duration.isZero()) {
            return false;
        }
        if (tasksByPriority.isEmpty()) {
            return false;
        }
        return tasksByPriority.stream()
                .anyMatch((task) -> !startTime.isAfter(task.getEndTime()) &&
                        !startTime.plus(duration).isBefore(task.getStartTime()));
    }

    public void setStartTimeAndDuration(Task task, LocalDateTime localDateTime, Duration duration) {
        // Если переданы null и 0 - сбрасываем время
        if (localDateTime == null && duration.isZero()) {
            if (task.getStartTime() != null && !task.getDurationTime().isZero()) {
                tasksByPriority.remove(task);

                switch (task) {
                    case Subtask subtask -> {
                        subtask.setDurationTime(Duration.ZERO);
                        subtask.setStartTime(null);
                        updateEpicStartTimeAndDuration(allEpics.get(subtask.getParentId()));
                    }
                    case Epic ignored -> {
                        // Обновление Epic происходит автоматически
                    }
                    default -> {
                        task.setDurationTime(Duration.ZERO);
                        task.setStartTime(null);
                    }
                }
            }
            return;
        }

        // Если один параметр null/zero, а другой нет - ошибка
        if (localDateTime == null || duration.isZero()) {
            throw new TimeArgumentException("Both startTime and duration must be provided together or both must be null/zero");
        }

        tasksByPriority.remove(task);

        if (isIntersection(localDateTime, duration)) {
            throw new TimeInterectionException("Time intersection");
        }

        switch (task) {
            case Subtask subtask -> {
                subtask.setDurationTime(duration);
                subtask.setStartTime(localDateTime);
                updateEpicStartTimeAndDuration(allEpics.get(subtask.getParentId()));
                tasksByPriority.add(subtask);
            }
            case Epic ignored -> {
                // Обновление Epic происходит автоматически
            }
            default -> {
                task.setDurationTime(duration);
                task.setStartTime(localDateTime);
                tasksByPriority.add(task);
            }
        }
    }

    // Метод для автоматического обновления статуса Epic в течении кода.
    protected void updateEpicStatus(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = epic.getSubtasksId().stream()
                .map(allSubtasks::get)
                .allMatch(s -> s.getStatus() == Status.NEW);

        boolean allDone = epic.getSubtasksId().stream()
                .map(allSubtasks::get)
                .allMatch(s -> s.getStatus() == Status.DONE);

        epic.setStatus(allNew ? Status.NEW : (allDone ? Status.DONE : Status.IN_PROGRESS));
    }

    // Приватный метод для автоматической работы со временем Epic
    private void updateEpicStartTimeAndDuration(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDurationTime(Duration.ZERO);
            return;
        }

        epic.setStartTime(epic.getSubtasksId().stream()
                .map(allSubtasks::get)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null)
        );

        epic.setEndTime(epic.getSubtasksId().stream()
                .map(allSubtasks::get)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null)
        );

        if (epic.getStartTime() != null && epic.getEndTime() != null) {
            epic.setDurationTime(Duration.between(epic.getStartTime(), epic.getEndTime()));
        } else {
            epic.setDurationTime(Duration.ZERO);
        }
    }
}

