package main;

import exceptions.TimeInterectionException;
import manager.InMemoryTaskManager;
import model.Status;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task1 = manager.createTask("Task 1", "Description 1", Status.NEW);
        LocalDateTime start1 = LocalDateTime.of(2025, 9, 17, 10, 0);
        Duration duration1 = Duration.ofHours(2);
        Task task2 = manager.createTask("Task 2", "Description 2", Status.NEW);
        LocalDateTime start2 = LocalDateTime.of(2025, 9, 17, 11, 0);
        Duration duration2 = Duration.ofHours(1);

        manager.setStartTimeAndDuration(task1, start1, duration1);
        try {
            manager.setStartTimeAndDuration(task2, start2, duration2);
        } catch (TimeInterectionException e) {
            System.out.println(manager.getTasksByPriority(true));
            System.out.println(manager.getTasksByPriority(false));
        }

    }
}
