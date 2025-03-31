import manager.FileBackedTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {

//        try {
////            File file = File.createTempFile("tasks", ".csv");
//            File file = new File("tasks.csv");
//            System.out.println("Файл для автосохранения: " + file.getAbsolutePath());
//
////            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
//
////            Task task1 = new Task("Task 1", "Описание 1", Status.NEW);
////            fileBackedTaskManager.addTask(task1);
////            System.out.println(task1.getId());
////
////            Epic epic1 = new Epic("Epic 1", "Описание 2");
////            fileBackedTaskManager.addEpic(epic1);
////            System.out.println(epic1.getId());
////
////            Subtask subtask1 = new Subtask("Subtask 1", "Описание 3", Status.IN_PROGRESS, epic1.getId());
////            fileBackedTaskManager.addSubtask(subtask1);
////            System.out.println(subtask1.getEpicId());
////            System.out.println(subtask1.getId());
//
//            String content = Files.readString(file.toPath());
//            System.out.println("Содержимое файла:");
//            System.out.println(content);
//
//            FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);
//
//            System.out.println("--All tasks after loading--");
//            System.out.println(fileBackedTaskManager.getAllTasks());
//            System.out.println(fileBackedTaskManager.getAllSubtasks());
//            System.out.println(fileBackedTaskManager.getAllEpics());
//            System.out.println("-------------");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            File file = new File("tasks.csv");
            System.out.println("Файл для автосохранения: " + file.getAbsolutePath());
            FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

            System.out.println("Содержимое файла:");
            System.out.println(Files.readString(file.toPath()));

            System.out.println("-- Все задачи --");
            for (Task task : manager.getAllTasks()) {
                System.out.println(task);
            }
            System.out.println("-- Все эпики --");
            for (Epic epic : manager.getAllEpics()) {
                System.out.println(epic);
            }
            System.out.println("-- Все подзадачи --");
            for (Subtask subtask : manager.getAllSubtasks()) {
                System.out.println(subtask);
            }

            System.out.println(manager.getIdCounter());

            System.out.println("-- Все задачи --");
            for (Task task : manager.getAllTasks()) {
                System.out.println(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
