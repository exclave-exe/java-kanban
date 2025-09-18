package model;

import java.time.format.DateTimeFormatter;

public class Subtask extends Task {
    private final int parentId;

    public Subtask(int id, int parentId, String name, String description, Status status) {
        super(id, name, description, status);
        if (parentId == id) {
            throw new IllegalArgumentException("Subtask не может быть своим же Epic'ом");
        }
        this.parentId = parentId;
    }

    public int getParentId() {
        return parentId;
    }

    // Переопределение.
    @Override
    public String toString() {

        String taskStartTime = !(startTime == null)
                ? startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : "None";

        String taskEndTime = !(getEndTime() == null)
                ? getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                : "None";

        return "Subtask {" +
                "|id = " + id +
                ", |name = " + name + "|" +
                ", |description = " + description.length() + "|" +
                ", |status = " + status + "|" +
                ", |parentId = " + parentId + "|" +
                ", |startTime = " + taskStartTime + "|" +
                ", |duration = " + duration + "|" +
                ", |endTime = " + taskEndTime + "|" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subtask subtask = (Subtask) o;
        return id == subtask.id &&
                name.equals(subtask.name) &&
                description.equals(subtask.description) &&
                status == subtask.status &&
                parentId == subtask.parentId;
    }
}
