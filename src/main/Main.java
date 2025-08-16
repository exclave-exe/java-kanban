package main;

import manager.InMemoryTaskManager;
import manager.Managers;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = Managers.getDefault();

        // Дополнительное задание
        Task task1 = manager.createTask("Сходить в магазин", "Купить продукты", Status.NEW);
        Task task2 = manager.createTask("Выбросить мусор", "Пакет у двери", Status.NEW);
        Epic epic3 = manager.createEpic("Переезд", "Переезд в новую квартиру");
        Epic epic4 = manager.createEpic("Переезд2", "Переезд в новую квартиру2");
        Subtask subtask5 = manager.createSubtask(epic3, "Собрать вещи", "Упаковать в коробки", Status.NEW);
        Subtask subtask6 = manager.createSubtask(epic3, "Вызвать такси", "На 18:00", Status.NEW);
        Subtask subtask7 = manager.createSubtask(epic3, "Загрузить в машину", "Помощь друга", Status.NEW);


        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic3.getId());
        manager.getEpic(epic4.getId());
        manager.getSubtask(subtask5.getId());
        manager.getSubtask(subtask6.getId());
        manager.getSubtask(subtask7.getId());

        System.out.println("\n--- История запросов ---");
        manager.getHistory();

        manager.getTask(task2.getId());
        manager.getEpic(epic3.getId());
        manager.getSubtask(subtask6.getId());

        System.out.println("\n--- История запросов ---");
        manager.getHistory();

        manager.deleteTask(1);
        manager.deleteEpic(3);

        System.out.println("\n--- История запросов ---");
        manager.getHistory();
    }
}
