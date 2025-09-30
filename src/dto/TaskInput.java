package dto;

import model.Status;

import java.time.LocalDateTime;

public class TaskInput {
    public final String name;
    public final String description;
    public final Status status;
    public final LocalDateTime startTime;
    public final long duration;

    public TaskInput(String name, String description, Status status,
                     LocalDateTime startTime, long duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }
}