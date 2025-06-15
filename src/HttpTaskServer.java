import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import handlers.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final TaskManager manager;
    private HttpServer server;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(java.time.Duration.class, new adapters.DurationAdapter())
            .registerTypeAdapter(java.time.LocalDateTime.class, new adapters.LocalDateTimeAdapter())
            .serializeNulls()
            .create();

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    // Для main()
    public static void main(String[] args) throws IOException {
        TaskManager defaultManager = Managers.getDefault();
        new HttpTaskServer(defaultManager).start();
        System.out.println("Server started on port " + PORT);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static Gson getGson() {
        return GSON;
    }
}
