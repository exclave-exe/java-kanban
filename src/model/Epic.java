package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksId;
    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW);
        subtasksId = new ArrayList<>();
    }

    // Методы добавления и удаления Subtasks.
    public void addSubtask(int subtaskId) {
        if (this.id == subtaskId) {
            throw new IllegalArgumentException("Epic не может содержать самого себя в виде подзадачи.");
        }
        subtasksId.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtasksId.remove(Integer.valueOf(subtaskId));
    }

    public void removeAllSubtask() {
        this.subtasksId.clear();
    }

    public List<Integer> getSubtasksId() {
        return new ArrayList<>(subtasksId);
    }

    public void setEndTime(LocalDateTime localDateTime) {
        this.endTime = localDateTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Переопределения.
    @Override
    public String toString() {

        String taskStartTime = !(startTime == null)
                ? startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : "None";

        String taskEndTime = !(endTime == null)
                ? getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : "None";

        return "Epic {" +
                "|id=" + id + "|" +
                ", |name='" + name + "|" +
                ", |description='" + description.length() + "|" +
                ", |status=" + status + "|" +
                ", |subtasksId=" + subtasksId + "|" +
                ", |startTime = " + taskStartTime + "|" +
                ", |duration = " + duration + "|" +
                ", |endTime = " + taskEndTime + "|" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Epic epic = (Epic) o;
        return id == epic.id &&
                name.equals(epic.name) &&
                description.equals(epic.description) &&
                status == epic.status &&
                subtasksId.equals(epic.subtasksId);
    }
}
