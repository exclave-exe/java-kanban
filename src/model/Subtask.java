package model;

public class Subtask extends Task {
    private int parentId;

    public Subtask(int id, Epic parent, String name, String description, Status status) {
        super(id, name, description, status);
        if (parent.getId() == id) {
            throw new IllegalArgumentException("Subtask не может быть своим же Epic'ом");
        }
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
