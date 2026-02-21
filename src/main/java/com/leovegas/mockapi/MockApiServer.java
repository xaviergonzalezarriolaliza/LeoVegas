package com.leovegas.mockapi;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class MockApiServer {
    public static void main(String[] args) {
        ipAddress("0.0.0.0");
        int portNumber = 4567;
        if (args != null && args.length > 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument, using default 4567");
            }
        } else {
            String prop = System.getProperty("mock.port");
            if (prop != null) {
                try {
                    portNumber = Integer.parseInt(prop);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid system property mock.port, using default 4567");
                }
            }
        }
        port(portNumber); // Default Spark port (can be overridden)

        get("/hello", (req, res) -> {
            res.type("application/json");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello, LeoVegas!");
            return new Gson().toJson(response);
        });

        post("/echo", (req, res) -> {
            res.type("application/json");
            Gson gson = new Gson();
            Map<String, Object> response = new HashMap<>();
            String body = req.body();
            if (body == null || body.isBlank()) {
                response.put("echo", "");
            } else {
                try {
                    com.google.gson.JsonElement parsed = gson.fromJson(body, com.google.gson.JsonElement.class);
                    if (parsed != null && (parsed.isJsonObject() || parsed.isJsonArray())) {
                        // Return the parsed JSON structure under "echo"
                        response.put("echo", parsed);
                    } else {
                        // Primitive JSON (number/string) or not an object/array - return raw body
                        response.put("echo", body);
                    }
                } catch (Exception e) {
                    // Not valid JSON - echo the raw body string
                    response.put("echo", body);
                }
            }
            return gson.toJson(response);
        });

        // Simulate a long response (2 seconds)
        get("/long", (req, res) -> {
            res.type("application/json");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "This was a long response...........................................................................................................................................................................................................................................................................................................................................................");
            return new Gson().toJson(response);
        });

        // Return a large JSON payload matching the manyFieldsPayload used in tests
        get("/manyFieldsPayload", (req, res) -> {
            res.type("application/json");
            Gson gson = new Gson();
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", 1001);
            payload.put("name", "Alice");
            payload.put("email", "alice@example.com");
            payload.put("age", 30);
            payload.put("country", "SE");
            payload.put("city", "Stockholm");
            payload.put("zip", "11122");
            payload.put("device", "Android");
            payload.put("os", "Android 14");
            payload.put("appVersion", "5.2.1");
            payload.put("sessionId", "sess-abc-123");
            payload.put("isPremium", true);
            payload.put("balance", 1234.56);
            payload.put("lastLogin", "2026-02-20T10:00:00Z");
            payload.put("locale", "sv-SE");
            payload.put("currency", "SEK");
            payload.put("features", "A,B,C");
            payload.put("tags", "tag1,tag2");
            payload.put("notes", "test user with many fields");
            return gson.toJson(payload);
        });
    }
}
