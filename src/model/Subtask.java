package model;

public class Subtask extends Task {

    private Epic parent;

    public Subtask(Epic parent, String name, String description, Status status) {
        super(name, description, status);
        this.parent = parent;
    }

    public Epic getParent() {
        return parent;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
        parent.updateStatus();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description.length() + '\'' +
                ", status=" + status +
                ",\nparent=" + parent.getId() +
                '}';
    }
}
