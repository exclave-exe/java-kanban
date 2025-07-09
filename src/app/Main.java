package app;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import todoManager.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание обычных Tasks
        Task task1 = manager.createTask("Купить продукты", "Купить хлеб, молоко", Status.NEW);
        Task task2 = manager.createTask("Погладить кота", "Обязательно!", Status.NEW);

        // Создание Epic и Subtasks
        Epic epic1 = manager.createEpic("Сделать ремонт", "Ремонт в ванной комнате");
        Subtask sub1 = manager.createSubtask(epic1, "Купить плитку", "Керама Марацци", Status.NEW);
        Subtask sub2 = manager.createSubtask(epic1, "Вызвать мастера", "На субботу", Status.NEW);

        // Печать всех задач
        System.out.println("\n== ВСЕ TASKS ==");
        manager.printAllTasks();

        System.out.println("\n== ВСЕ EPICS ==");
        manager.printAllEpics();

        System.out.println("\n== ВСЕ SUBTASKS ==");
        manager.printAllSubtasks();

        // Проверка методов обновления:
        System.out.println("\n== ОБНОВЛЕНИЕ SUBTASK ==");
        epic1.updateSubtask(sub1.getId(), "Купить плитку и клей", "Добавить клей", Status.IN_PROGRESS);
        System.out.println("Epic после обновления subtask:\n" + epic1);

        System.out.println("\n== ОБНОВЛЕНИЕ TASK ==");
        task1.updateTask("Купить продукты и напитки", "Добавить сок", Status.DONE);
        System.out.println("Task после обновления:\n" + task1);

        System.out.println("\n== ОБНОВЛЕНИЕ EPIC ==");
        epic1.updateEpic("Ремонт всей квартиры", "Пока только ванна и кухня");
        System.out.println("Epic после обновления:\n" + epic1);

        // Проверка методов Удаления
        System.out.println("\n== УДАЛЕНИЕ TASK ==");
        manager.deleteTask(task2.getId());
        manager.printAllTasks();

        System.out.println("\n== УДАЛЕНИЕ SUBTASK ==");
        manager.deleteSubtask(sub2.getId());
        manager.printAllSubtasks();
        System.out.println("Epic после удаления Subtask:\n" + epic1);

        // Удаление Epic (и всех его Subtasks)
        System.out.println("\n== УДАЛЕНИЕ EPIC ==");
        manager.deleteEpic(epic1.getId());
        manager.printAllEpics();
        manager.printAllSubtasks();

        // Очистка всех Task
        System.out.println("\n== ПОЛНАЯ ОЧИСТКА ВСЕХ TASK ==");
        manager.deleteAllTasks();
        manager.printAllTasks();
    }
}
