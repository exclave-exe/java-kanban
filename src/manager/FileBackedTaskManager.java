package manager;

import exceptions.ManagerReadException;
import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Дополнительное задание
    public static void main(String[] args) {
        File file = new File("resources/additionalTask.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = manager.createTask("Task 1", "Описание задачи 1", Status.NEW);
        manager.setStartTimeAndDuration(task1,
                LocalDateTime.of(2025, 9, 15, 10, 0),
                Duration.ofMinutes(60));

        Task task2 = manager.createTask("Task 2", "Описание задачи 2", Status.IN_PROGRESS);
        manager.setStartTimeAndDuration(task2,
                LocalDateTime.of(2025, 9, 15, 12, 0),
                Duration.ofMinutes(90));

        Epic epic1 = manager.createEpic("Epic 1", "Описание эпика 1");

        Subtask subtask1 = manager.createSubtask(epic1, "Subtask 1", "Описание сабтаски 1", Status.NEW);
        manager.setStartTimeAndDuration(subtask1,
                LocalDateTime.of(2025, 9, 15, 14, 0),
                Duration.ofMinutes(45));

        Subtask subtask2 = manager.createSubtask(epic1, "Subtask 2", "Описание сабтаски 2", Status.DONE);


        System.out.println("=== Состояние менеджера перед сохранением ===");
        manager.getAllTasks().forEach(System.out::println);
        manager.getAllEpics().forEach(System.out::println);
        manager.getAllSubtasks().forEach(System.out::println);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("\n=== Состояние загруженного менеджера ===");
        loadedManager.getAllTasks().forEach(System.out::println);
        loadedManager.getAllEpics().forEach(System.out::println);
        loadedManager.getAllSubtasks().forEach(System.out::println);
    }

    public File getFile() {
        return file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);

        if (!fileBackedTaskManager.getFile().exists() || file.length() == 0) {
            return fileBackedTaskManager;
        }

        try {
            List<String> lines = Files.readAllLines(fileBackedTaskManager.getFile().toPath(), StandardCharsets.UTF_8);
            lines.removeFirst();
            for (String str : lines) {
                fileBackedTaskManager.taskFromString(str);
            }
        } catch (IOException e) {
            throw new ManagerReadException("Ошибка при чтении из файла", e);
        }

        return fileBackedTaskManager;
    }

    @Override
    public Task createTask(String name, String description, Status status) {
        Task task = super.createTask(name, description, status);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String name, String description) {
        Epic epic = super.createEpic(name, description);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(Epic epic, String name, String description, Status status) {
        Subtask subtask = super.createSubtask(epic, name, description, status);
        save();
        return subtask;
    }

    @Override
    public boolean deleteTask(int taskId) {
        if (allTasks.get(taskId) == null) {
            return false;
        }
        super.deleteTask(taskId);
        save();
        return true;
    }

    @Override
    public boolean deleteEpic(int epicId) {
        if (allEpics.get(epicId) == null) {
            return false;
        }
        super.deleteEpic(epicId);
        save();
        return true;
    }

    @Override
    public boolean deleteSubtask(int subtaskId) {
        if (allSubtasks.get(subtaskId) == null) {
            return false;
        }
        super.deleteSubtask(subtaskId);
        save();
        return true;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void updateStatus(Task task, Status status) {
        super.updateStatus(task, status);
        save();
    }

    @Override
    public void updateDescription(Task task, String description) {
        super.updateDescription(task, description);
        save();
    }

    @Override
    public void updateName(Task task, String name) {
        super.updateName(task, name);
        save();
    }

    @Override
    public void setStartTimeAndDuration(Task task, LocalDateTime localDateTime, Duration duration) {
        super.setStartTimeAndDuration(task, localDateTime, duration);
        save();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("id,type,name,status,description,epic,startTime,duration,endTime");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(taskToString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private String taskToString(Task task) {
        return switch (task) {
            case Subtask subtask -> {
                String startTime = subtask.getStartTime() != null
                        ? subtask.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";
                String endTime = subtask.getEndTime() != null
                        ? subtask.getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";

                yield String.format("%d,%s,%s,%s,%s,%d,%s,%d,%s",
                        subtask.getId(),
                        TaskType.SUBTASK,
                        subtask.getName(),
                        subtask.getStatus(),
                        subtask.getDescription(),
                        subtask.getParentId(),
                        startTime,
                        subtask.getDurationTime().toMinutes(),
                        endTime
                );
            }
            case Epic epic -> {
                String startTime = epic.getStartTime() != null
                        ? epic.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";
                String endTime = epic.getEndTime() != null
                        ? epic.getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";

                yield String.format("%d,%s,%s,%s,%s,%s,%s,%d,%s",
                        epic.getId(),
                        TaskType.EPIC,
                        epic.getName(),
                        epic.getStatus(),
                        epic.getDescription(),
                        "None",
                        startTime,
                        epic.getDurationTime().toMinutes(),
                        endTime
                );
            }
            default -> {
                String startTime = task.getStartTime() != null
                        ? task.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";
                String endTime = task.getEndTime() != null
                        ? task.getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                        : "None";

                yield String.format("%d,%s,%s,%s,%s,%s,%s,%d,%s",
                        task.getId(),
                        TaskType.TASK,
                        task.getName(),
                        task.getStatus(),
                        task.getDescription(),
                        "None",
                        startTime,
                        task.getDurationTime().toMinutes(),
                        endTime
                );
            }
        };
    }

    private Task taskFromString(String value) {
        String[] str = value.split(",");

        int id = Integer.parseInt(str[0]);
        TaskType type = TaskType.valueOf(str[1]);
        String name = str[2];
        Status status = Status.valueOf(str[3]);
        String description = str[4];

        LocalDateTime startTime = !str[6].equals("None")
                ? LocalDateTime.parse(str[6], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : null;
        Duration duration = !str[7].equals("None")
                ? Duration.ofMinutes(Long.parseLong(str[7]))
                : Duration.ZERO;
        LocalDateTime endTime = !str[8].equals("None")
                ? LocalDateTime.parse(str[8], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : null;

        if (id >= totalId) {
            totalId = id + 1;
        }

        return switch (type) {
            case TASK -> createTaskFromFile(id, name, description, status, startTime, duration);
            case EPIC -> createEpicFromFile(id, name, description);
            default -> createSubtaskFromFile(id, Integer.valueOf(str[5]), name, description, status, startTime,
                    duration);
        };
    }

    private Task createTaskFromFile(int id, String name, String description, Status status, LocalDateTime startTime,
                                    Duration duration) {
        Task task = new Task(id, name, description, status);
        allTasks.put(id, task);

        if (startTime != null) {
            setStartTimeAndDuration(task, startTime, duration);
        }

        return task;
    }

    private Epic createEpicFromFile(int id, String name, String description) {
        Epic epic = new Epic(id, name, description);
        allEpics.put(id, epic);
        return epic;
    }

    private Subtask createSubtaskFromFile(int id, int parentId, String name, String description, Status status,
                                          LocalDateTime startTime, Duration duration) {
        Subtask subtask = new Subtask(id, parentId, name, description, status);
        allEpics.get(parentId).addSubtask(id);
        allSubtasks.put(id, subtask);
        updateEpicStatus(allEpics.get(parentId));

        if (startTime != null) {
            setStartTimeAndDuration(subtask, startTime, duration);
        }

        return subtask;
    }
}
