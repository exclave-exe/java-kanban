package dto;

import model.Status;

import java.time.LocalDateTime;

public class SubtaskInput {
    public final String name;
    public final String description;
    public final Status status;
    public final int parentId;
    public final LocalDateTime startTime;
    public final long duration;

    public SubtaskInput(String name, String description, Status status, int parentId,
                        LocalDateTime startTime, long duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.parentId = parentId;
        this.startTime = startTime;
        this.duration = duration;
    }
}