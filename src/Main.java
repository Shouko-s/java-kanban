import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

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

        System.out.println(taskManager.getSubtaskByEpic(epic1.getId()));


    }
}
