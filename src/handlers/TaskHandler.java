package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;
import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.split("/").length == 3) {
                        handleGetById(exchange);
                    } else {
                        handleGetAll(exchange);
                    }
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();      // <- покажет реальную причину в логе
            sendError(exchange);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getAllTasks();
        String response = gson.toJson(tasks);
        sendText(exchange, response);
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        String[] str = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(str[2]);
        Task task = manager.getTask(id);
        if (task == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(task));

    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Task task = gson.fromJson(body, Task.class);
        try {
            if (task.getId() == 0) {
                manager.addTask(task);
                sendCreated(exchange, "Task added, id - " + task.getId());
            } else {
                manager.updateTask(task);
                sendText(exchange, "Task updated, id - " + task.getId());
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] str = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(str[2]);

        Task existing = manager.getTask(id);
        if (existing == null) {
            sendNotFound(exchange);
            return;
        }
        manager.deleteTask(id);
        sendText(exchange, "Task was deleted, id - " + id);
    }
}
