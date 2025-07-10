package model;

public class Subtask extends Task {
    private int parentId;

    public Subtask(int id, Epic parent, String name, String description, Status status) {
        super(id, name, description, status);
        this.parentId = parent.getId();
    }

    public int getParentId() {
        return parentId;
    }

    // Переопределение.
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description.length() + '\'' +
                ", status=" + status +
                ",\nparentId=" + parentId +
                '}';
    }
}
