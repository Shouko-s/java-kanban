package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic");
            bufferedWriter.newLine();

            for (Task task : tasks.values()) {
                bufferedWriter.write(toString(task));
                bufferedWriter.newLine();
            }

            for (Epic epic : epics.values()) {
                bufferedWriter.write(toString(epic));
                bufferedWriter.newLine();
            }

            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(toString(subtask));
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения менеджера в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\\R");

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                Task task = fromString(line);

                if (task instanceof Epic) {
                    fileBackedTaskManager.addEpic((Epic) task);
                    System.out.println("added epic");
                } else if (task instanceof Subtask){
                    fileBackedTaskManager.addSubtask((Subtask) task);
                    System.out.println("added subtask");
                } else {
                    fileBackedTaskManager.addTask(task);
                    System.out.println("added task");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки менеджера из файла", e);
        }

        return fileBackedTaskManager;
    }

    public String toString(Task task) {
        String id = String.valueOf(task.getId());
        String type;
        if (task instanceof Subtask) {
            type = TasksType.SUBTASK.name();
        } else if (task instanceof Epic) {
            type = TasksType.EPIC.name();
        } else {
            type = TasksType.TASK.name();
        }
        String title = task.getTitle();
        String status = task.getStatus().name();
        String description = task.getDescription();
        String epicId = "";


        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.format("%s,%s,%s,%s,%s,%s", id, type, title, status, description, epicId);
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        String epicPart = parts[5];
        int epicId;
        if (!epicPart.isEmpty()) {
            epicId = Integer.parseInt(epicPart);
        } else {
            epicId = -1;
        }

        switch (type) {
            case "TASK":
                Task task = new Task(title, description, status);
                task.setId(id);
                task.setStatus(status);
                return task;
            case "SUBTASK":
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            case "EPIC":
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public void printAllTasks() {
        super.printAllTasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        return super.getTask(id);
    }

    @Override
    public Epic getEpic(int id) {
        return super.getEpic(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        return super.getSubtask(id);
    }

    @Override
    public List<Subtask> getSubtaskByEpic(int epicId) {
        return super.getSubtaskByEpic(epicId);
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }
}
