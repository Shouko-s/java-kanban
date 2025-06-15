package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager) {
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
            String path   = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            switch (method) {
                case "GET":
                    if (parts.length == 4 && "subtasks".equals(parts[3])) {
                        handleGetSubtasks(exchange);
                    } else if (parts.length == 3) {
                        handleGetById(exchange);
                    } else {
                        handleGetAll(exchange);
                    }
                    break;

                case "POST":
                    handlePost(exchange);
                    break;

                case "DELETE":
                    if (parts.length == 3) {
                        handleDelete(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
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
        List<Epic> epics = manager.getAllEpics();
        String response = gson.toJson(epics);
        sendText(exchange, response);
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        Epic epic = manager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(epic));
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        Epic epic = manager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }
        List<Subtask> subtasks = manager.getSubtaskByEpic(id);
        sendText(exchange, gson.toJson(subtasks));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() == 0) {
            manager.addEpic(epic);
            sendCreated(exchange, "Epic added, id - " + epic.getId());
        } else {
            manager.updateEpic(epic);
            sendText(exchange, "Epic updated, id - " + epic.getId());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(parts[2]);
        Epic existing = manager.getEpic(id);
        if (existing == null) {
            sendNotFound(exchange);
            return;
        }
        manager.deleteEpic(id);
        sendText(exchange, "Epic was deleted, id - " + id);
    }
}
