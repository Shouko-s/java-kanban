package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.split("/").length > 2) {
                        handleGetById(exchange);
                    } else {
                        handleGet(exchange);
                    }
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    sendNotFound(exchange);

            }
        } catch (Exception e) {
            sendError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getAllTasks();
        String response = gson.toJson(tasks);
        sendText(exchange, response);
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        String[] str = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(str[2]);
        Task task = manager.getTask(id);
        String jsonTask = gson.toJson(task);
        sendText(exchange, jsonTask);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Task task = gson.fromJson(body, Task.class);
        manager.addTask(task);
        sendCreated(exchange, "Task added");
    }
}
