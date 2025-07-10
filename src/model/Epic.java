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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        Epic epic = (Epic) obj;
        return Objects.equals(id, epic.id);
    }

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
}
