package model;

import todoManager.TaskManager;

public class Task {

    protected int id;
    protected String name;
    protected String description;
    protected Status status;

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // Общие Getters.
    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    // Общие Setters.
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Переопределение.
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }
}
