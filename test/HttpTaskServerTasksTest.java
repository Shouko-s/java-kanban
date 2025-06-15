import com.google.gson.Gson;
import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest extends BaseHttpServerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.getGson();
    private final String url = "http://localhost:" + HttpTaskServer.PORT + "/tasks";

    @Test
    public void testCreateTask() throws Exception {
        Task task = new Task("Title A", "Desc A", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> list = manager.getAllTasks();
        assertEquals(1, list.size());
        assertEquals("Title A", list.get(0).getTitle());
    }

    @Test
    public void testGetTaskById_Success() throws Exception {
        // сначала создаём задачу
        Task task = new Task("T1", "D1", Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addTask(task);
        int id = task.getId();

        // теперь GET /tasks/{id}
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task fromServer = gson.fromJson(response.body(), Task.class);
        assertEquals(id, fromServer.getId());
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask_Success() throws Exception {
        // создаём и сохраняем
        Task task = new Task("Old", "OldDesc", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        manager.addTask(task);
        int id = task.getId();

        // готовим обновлённый JSON
        Task updated = new Task("New", "NewDesc", Status.IN_PROGRESS, Duration.ofMinutes(20), LocalDateTime.now());
        updated.setId(id);
        String json = gson.toJson(updated);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task fromManager = manager.getTask(id);
        assertEquals("New", fromManager.getTitle());
        assertEquals(Status.IN_PROGRESS, fromManager.getStatus());
    }

    @Test
    public void testUpdateTask_TimeIntersection() throws Exception {
        // 1) создаём первую задачу 10:00–10:10
        Task t1 = new Task("A", "D", Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2025, 6, 15, 10, 0));
        manager.addTask(t1);

        // 2) создаём вторую задачу 10:20–10:30 (не пересекается)
        Task t2 = new Task("B", "D", Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2025, 6, 15, 10, 20));
        manager.addTask(t2);

        // 3) теперь пробуем обновить t2 на 10:05–10:15 — это пересечение с t1
        t2.setStartTime(LocalDateTime.of(2025, 6, 15, 10, 5));
        t2.setId(t2.getId());
        String json = gson.toJson(t2);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(406, response.statusCode());
    }


    @Test
    public void testDeleteTask() throws Exception {
        Task t = new Task("X", "Y", Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addTask(t);
        int id = t.getId();

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url + "/" + id))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertNull(manager.getTask(id));
    }

    @Test
    public void testDeleteTask_NotFound() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url + "/999"))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, response.statusCode());
    }
}
