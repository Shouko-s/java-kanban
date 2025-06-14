package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected static final Gson gson = new Gson();

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(404, 0);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        h.sendResponseHeaders(406, 0);
        h.close();
    }

    protected void sendError(HttpExchange h) throws IOException {
        h.sendResponseHeaders(500, 0);
        h.close();
    }

    protected String readText(HttpExchange h) throws IOException {
        InputStream inputStream = h.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
