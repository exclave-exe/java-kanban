package Main;

import model.*;
import manager.*;

public class Main {
    public static void main(String[] args) {
        Managers managers = new Managers();
        InMemoryTaskManager manager = managers.getDefault();

        // --- Создание Task ---
        Task task1 = manager.createTask("Сходить в магазин", "Купить продукты", Status.NEW);
        Task task2 = manager.createTask("Выбросить мусор", "Пакет у двери", Status.NEW);

        // --- Создание Epic и Subtasks ---
        Epic epic1 = manager.createEpic("Переезд", "Переезд в новую квартиру");

        Subtask subtask1 = manager.createSubtask(epic1, "Собрать вещи", "Упаковать в коробки", Status.NEW);
        Subtask subtask2 = manager.createSubtask(epic1, "Вызвать такси", "На 18:00", Status.NEW);
        Subtask subtask3 = manager.createSubtask(epic1, "Загрузить в машину", "Помощь друга", Status.NEW);

        // --- Просмотр всех объектов ---
        System.out.println("\n--- Все задачи ---");
        manager.printAllTasks();

        System.out.println("\n--- Все эпики ---");
        manager.printAllEpics();

        System.out.println("\n--- Все подзадачи ---");
        manager.printAllSubtasks();

        // --- Получение по ID ---
        System.out.println("\n--- Получение Tasks по ID ---");
        System.out.println(manager.getTask(task1.getId()));
        System.out.println(manager.getTask(task2.getId()));

        System.out.println("\n--- Получение Epic по ID ---");
        System.out.println(manager.getEpic(epic1.getId()));

        System.out.println("\n--- Получение Subtasks по ID ---");
        System.out.println(manager.getSubtask(subtask1.getId()));
        System.out.println(manager.getSubtask(subtask2.getId()));
        System.out.println(manager.getSubtask(subtask3.getId()));

        // --- Обновление статуса ---
        manager.updateStatus(task1, Status.DONE);
        manager.updateStatus(subtask1, Status.IN_PROGRESS);
        manager.updateStatus(subtask2, Status.DONE);
        manager.updateStatus(subtask3, Status.DONE);

        System.out.println("\n--- Просмотр после обновления статусов ---");
        System.out.println(manager.getTask(task1.getId()));
        System.out.println(manager.getEpic(epic1.getId()));
        System.out.println(manager.getSubtask(subtask1.getId()));
        System.out.println(manager.getSubtask(subtask2.getId()));
        System.out.println(manager.getSubtask(subtask3.getId()));

        // --- Обновление имени и описания ---
        manager.update(task2, "Выбросить мусор", "Сделать это после магазина");
        manager.update(epic1, "Переезд в новый дом", "Другая квартира");
        manager.update(subtask1, "Сложить одежду", "Положить в чемодан");

        System.out.println("\n--- Просмотр после обновления описания ---");
        System.out.println(manager.getTask(task2.getId()));
        System.out.println(manager.getEpic(epic1.getId()));
        System.out.println(manager.getSubtask(subtask1.getId()));

        // --- Удаление объектов по ID ---
        manager.deleteTask(task2.getId());
        manager.deleteSubtask(subtask1.getId());

        System.out.println("\n--- После удаления ---");
        manager.printAllTasks();
        manager.printAllEpics();
        manager.printAllSubtasks();

        // --- Очистка всех задач ---
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        System.out.println("\n--- После полной очистки ---");
        manager.printAllTasks();
        manager.printAllEpics();
        manager.printAllSubtasks();

        // --- История запросов ---
        System.out.println("\n--- История последних 10-и запросов ---");
        manager.getHistory();
    }
}
