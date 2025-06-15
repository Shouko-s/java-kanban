
import com.google.gson.Gson;
import model.Epic;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest extends BaseHttpServerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = HttpTaskServer.getGson();
    private final String url = "http://localhost:" + HttpTaskServer.PORT + "/epics";

    @Test
    public void testCreateAndGetEpic() throws Exception {
        Epic epic = new Epic("Launch", "Campaign", Duration.ofMinutes(0), LocalDateTime.now());
        String json = gson.toJson(epic);

        HttpResponse<String> createResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createResp.statusCode());

        HttpResponse<String> listResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, listResp.statusCode());
        List<Epic> list = List.of(gson.fromJson(listResp.body(), Epic[].class));
        assertEquals(1, list.size());
        assertEquals("Launch", list.get(0).getTitle());
    }

    @Test
    public void testUpdateEpic() throws Exception {
        Epic epic = new Epic("E1", "D1", Duration.ofMinutes(0), LocalDateTime.now());
        manager.addEpic(epic);
        int id = epic.getId();

        epic.setTitle("E2");
        epic.setId(id);
        String json = gson.toJson(epic);

        HttpResponse<String> updateResp = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, updateResp.statusCode());

        Epic fromManager = manager.getEpic(id);
        assertEquals("E2", fromManager.getTitle());
    }
}
