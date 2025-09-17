package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {

    protected int id;
    protected String name;
    protected String description;
    protected Status status;
    protected LocalDateTime startTime;
    protected Long duration = 0L;

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String name) {
        this.description = name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDurationTime() {
        return Duration.ofMinutes(duration);
    }

    public void setDurationTime(Duration durationTime) {
        this.duration = durationTime.toMinutes();
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime.plus(Duration.ofMinutes(duration));
    }

    // Метод для проверки по id.
    public boolean hasSameId(Task other) {
        if (other == null) return false;
        return this.id == other.id;
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

        return "Task {" +
                "|id = " + id + "|" +
                ", |name = " + name + "|" +
                ", |description = " + description.length() + "|" +
                ", |status = " + status + "|" +
                ", |startTime = " + taskStartTime + "|" +
                ", |duration = " + duration + "|" +
                ", |endTime = " + taskEndTime + "|" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;
        return id == task.id &&
                name.equals(task.name) &&
                description.equals(task.description) &&
                status == task.status;
    }
}
