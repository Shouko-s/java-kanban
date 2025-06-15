package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;
import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager) {
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
            String[] parts = path.split("/");

            switch (method) {
                case "GET":
                    if (parts.length > 2) {
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
            e.printStackTrace();
            sendError(exchange);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = manager.getAllSubtasks();
        String response = gson.toJson(subtasks);
        sendText(exchange, response);
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        Subtask subtask = manager.getSubtask(id);
        if (subtask == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(subtask));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        try {
            if (subtask.getId() == 0) {
                manager.addSubtask(subtask);
                sendCreated(exchange, "Subtask added, id - " + subtask.getId());
            } else {
                manager.updateSubtask(subtask);
                sendText(exchange, "Subtask updated, id - " + subtask.getId());
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        Subtask existing = manager.getSubtask(id);
        if (existing == null) {
            sendNotFound(exchange);
            return;
        }
        manager.deleteSubtask(id);
        sendText(exchange, "Subtask was deleted, id - " + id);
    }
}
