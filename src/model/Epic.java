package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        updateStatus();
    }

    public void removeSubtaskById(int id) {
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).getId() == id) {
                subtasks.remove(i);
                break;
            }
        }
        updateStatus();
    }

    public void removeAllSubtask() {
        subtasks.clear();
        updateStatus();
    }

    public Subtask getSubtask(int id) {
        for (Subtask subtask : subtasks) {
            if (subtask.id == id) {
                return subtask;
            }
        }
        return null;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            this.status = Status.NEW;
            return;
        }

        boolean isAllNew = true;
        boolean isAllDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.NEW) {
                isAllNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                isAllDone = false;
            }
        }

        if (isAllNew) {
            this.status = Status.NEW;
        } else if (isAllDone) {
            this.status = Status.DONE;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }

    public void updateEpic(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updateSubtask(int idSubtask, String name, String description, Status status) {
        Subtask subtask = getSubtask(idSubtask);
        if (subtask == null) {
            System.out.println("Subtask не найден");
            return;
        }
        subtask.name = name;
        subtask.description = description;
        subtask.status = status;
        updateStatus();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description.length() + '\'' +
                ", status=" + status +
                ",\nsubtasks=" + subtasks +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        Epic epic = (Epic) obj;
        return Objects.equals(id, epic.id);
    }
}
