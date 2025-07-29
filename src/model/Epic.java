package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId;

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW);
        subtasksId = new ArrayList<>();
    }

    // Методы добавления и удаления Subtasks.
    public void addSubtask(int subtaskId){
        if (this.id == subtaskId) {
            throw new IllegalArgumentException("Epic не может содержать самого себя в виде подзадачи");
        }
        subtasksId.add(subtaskId);
    }

    public void removeSubtask(int subtaskId){
        subtasksId.remove(Integer.valueOf(subtaskId));
    }

    // Getter для метода updateStatus.
    public ArrayList<Integer> getSubtasksId() {
        return subtasksId;
    }

    // Переопределения.
    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description.length() + '\'' +
                ", status=" + status +
                ",\nsubtasksId=" + subtasksId +
                '}';
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
