import com.google.gson.Gson;
import model.Subtask;
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

public class HttpTaskServerSubtasksTest extends BaseHttpServerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.getGson();
    private final String url = "http://localhost:" + HttpTaskServer.PORT + "/subtasks";

    private int createEpic() {
        model.Epic epic = new model.Epic("Parent", "Task");
        manager.addEpic(epic);
        return epic.getId();
    }

    @Test
    public void testCreateSubtask() throws Exception {
        int epicId = createEpic();
        Subtask st = new Subtask("Sub", "Task", Status.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.of(2025, 6, 15, 9, 0));
        String json = gson.toJson(st);

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        List<Subtask> all = manager.getAllSubtasks();
        assertEquals(1, all.size());
        assertEquals("Sub", all.get(0).getTitle());
    }

    @Test
    public void testUpdateSubtask_TimeIntersection() throws Exception {
        int epicId = createEpic();
        Subtask a = new Subtask("A", "D", Status.NEW, epicId,
                Duration.ofMinutes(10), LocalDateTime.of(2025, 6, 15, 10, 0));
        Subtask b = new Subtask("B", "D", Status.NEW, epicId,
                Duration.ofMinutes(10), LocalDateTime.of(2025, 6, 15, 10, 20));
        manager.addSubtask(a);
        manager.addSubtask(b);

        b.setStartTime(LocalDateTime.of(2025, 6, 15, 10, 5));
        b.setId(b.getId());
        String json = gson.toJson(b);

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(406, resp.statusCode());
    }
}
