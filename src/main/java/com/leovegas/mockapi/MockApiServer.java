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
            Map<String, Object> response = new HashMap<>();
            response.put("echo", req.body());
            return new Gson().toJson(response);
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
