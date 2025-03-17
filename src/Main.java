import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Описание эпика с тремя подзадачами");
        taskManager.addEpic(epicWithSubtasks);
        System.out.println(epicWithSubtasks.getId());

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epicWithSubtasks.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.IN_PROGRESS, epicWithSubtasks.getId());
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", Status.DONE, epicWithSubtasks.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        System.out.println(subtask3.getId());

        Epic epicWithoutSubtasks = new Epic("Эпик без подзадач", "Описание эпика без подзадач");
        taskManager.addEpic(epicWithoutSubtasks);


        System.out.println("Первый цикл запросов:");
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epicWithSubtasks.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getTask(task2.getId());
        System.out.println("История: " + taskManager.getHistory());

        System.out.println("\nВторой цикл запросов:");
        taskManager.getEpic(epicWithoutSubtasks.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getTask(task1.getId());
        System.out.println("История: " + taskManager.getHistory());


        taskManager.deleteTask(task1.getId());
        System.out.println("\nПосле удаления task1, история: " + taskManager.getHistory());


        taskManager.deleteEpic(epicWithSubtasks.getId());
        System.out.println("\nПосле удаления эпика с подзадачами, история: " + taskManager.getHistory());

        System.out.println(taskManager.getAllEpics());
    }
}
