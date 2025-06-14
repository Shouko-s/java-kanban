import com.sun.net.httpserver.HttpServer;
import handlers.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        TaskManager taskManager = Managers.getDefault();
        server.createContext("/tasks", new TaskHandler(taskManager));
//        server.createContext("/subtasks", new SubtaskHandler(taskManager));
//        server.createContext("/epics", new EpicHandler(taskManager));
//        server.createContext("/history", new HistoryHandler(taskManager));
//        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
        server.start();
    }

    /* Постараюсь завтра закончить */
}
