package com.leovegas.mockapi;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class MockApiServer {
    public static void main(String[] args) {
        port(4567); // Default Spark port

        get("/hello", (req, res) -> {
            res.type("application/json");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello, LeoVegas!");
            return new Gson().toJson(response);
        });

        post("/echo", (req, res) -> {
            res.type("application/json");
            Map<String, Object> response = new HashMap<>();
            response.put("echo", req.body());
            return new Gson().toJson(response);
        });

        // Simulate a long response (2 seconds)
        get("/long", (req, res) -> {
            res.type("application/json");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "This was a long response...........................................................................................................................................................................................................................................................................................................................................................");
            return new Gson().toJson(response);
        });
    }
}
