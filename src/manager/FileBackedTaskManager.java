package manager;

import exceptions.ManagerReadException;
import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        Task task2 = manager.createTask("Task 2", "Описание задачи 2", Status.IN_PROGRESS);

        Epic epic1 = manager.createEpic("Epic 1", "Описание эпика 1");
        Subtask subtask1 = manager.createSubtask(epic1, "Subtask 1", "Описание сабтаски 1", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic1, "Subtask 2", "Описание сабтаски 2", Status.DONE);

        System.out.println("=== Состояние менеджера перед сохранением ===");
        manager.printAllTasks();
        manager.printAllEpics();
        manager.printAllSubtasks();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("\n=== Состояние загруженного менеджера ===");
        loadedManager.printAllTasks();
        loadedManager.printAllEpics();
        loadedManager.printAllSubtasks();
    }

    public File getFile() {
        return file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fbtm = new FileBackedTaskManager(file);

        if (!fbtm.getFile().exists() || file.length() == 0) {
            return fbtm;
        }

        try {
            List<String> lines = Files.readAllLines(fbtm.getFile().toPath(), StandardCharsets.UTF_8);
            lines.removeFirst();
            for (String str : lines) {
                fbtm.taskFromString(str);
            }
        } catch (IOException e) {
            throw new ManagerReadException("Ошибка при чтении из файла", e);
        }
        return fbtm;
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
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
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

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : returnAllTasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Epic epic : returnAllEpics()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }
            for (Subtask subtask : returnAllSubtasks()) {
                writer.write(taskToString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private String taskToString(Task task) {
        return switch (task) {
            case Subtask subtask -> String.format("%d,%s,%s,%s,%s,%d",
                    subtask.getId(),
                    TaskType.SUBTASK,
                    subtask.getName(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getParentId()
            );
            case Epic epic -> String.format("%d,%s,%s,%s,%s",
                    epic.getId(),
                    TaskType.EPIC,
                    epic.getName(),
                    epic.getStatus(),
                    epic.getDescription()
            );
            default -> String.format("%d,%s,%s,%s,%s",
                    task.getId(),
                    TaskType.TASK,
                    task.getName(),
                    task.getStatus(),
                    task.getDescription()
            );
        };
    }

    private Task taskFromString(String value) {
        String[] str = value.split(",");

        int id = Integer.parseInt(str[0]);
        TaskType type = TaskType.valueOf(str[1]);
        String name = str[2];
        Status status = Status.valueOf(str[3]);
        String description = str[4];

        if (id >= totalId) {
            totalId = id + 1;
        }

        return switch (type) {
            case TASK -> createTaskFromFile(id, name, description, status);
            case EPIC -> createEpicFromFile(id, name, description);
            case SUBTASK -> createSubtaskFromFile(id, Integer.valueOf(str[5]), name, description, status);
        };
    }

    private Task createTaskFromFile(int id, String name, String description, Status status) {
        Task task = new Task(id, name, description, status);
        allTasks.put(id, task);
        return task;
    }

    private Epic createEpicFromFile(int id, String name, String description) {
        Epic epic = new Epic(id, name, description);
        allEpics.put(id, epic);
        return epic;
    }

    private Subtask createSubtaskFromFile(int id, int parentId, String name, String description, Status status) {
        Subtask subtask = new Subtask(id, parentId, name, description, status);
        allSubtasks.put(id, subtask);
        allEpics.get(parentId).addSubtask(id);
        return subtask;
    }
}
