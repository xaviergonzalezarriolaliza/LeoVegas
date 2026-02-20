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
    }
}
