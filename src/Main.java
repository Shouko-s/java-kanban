public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Создание двух обычных задач
        Task task1 = new Task("Купить продукты", "Сходить в магазин и купить еду", Status.NEW);
        Task task2 = new Task("Заплатить за интернет", "Оплатить счет за интернет", Status.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // Создание эпика с двумя подзадачами
        Epic epic1 = new Epic("Подготовка к поездке", "Собрать вещи, купить билеты");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Купить билеты", "Забронировать и оплатить билеты", Status.IN_PROGRESS, epic1.getId());
        Subtask subtask2 = new Subtask("Собрать чемодан", "Упаковать вещи", Status.NEW, epic1.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        // Создание эпика с одной подзадачей
        Epic epic2 = new Epic("Организация вечеринки", "Подготовка к вечеринке");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask("Купить напитки", "Закупить напитки для гостей", Status.IN_PROGRESS, epic2.getId());
        taskManager.addSubtask(subtask3);

        // Вывод всех задач
        System.out.println("После создания:");
        taskManager.printAllTasks();

        // Изменение статусов задач
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);

        // Повторный вывод после изменения статусов
        System.out.println("\nПосле изменения статусов:");
        taskManager.printAllTasks();

        // Удаление одной задачи и одного эпика
        taskManager.deleteTask(task1.getId());
        taskManager.deleteEpic(epic2.getId());

        // Итоговый вывод
        System.out.println("\nПосле удаления задачи и эпика:");
        taskManager.printAllTasks();
    }
}
