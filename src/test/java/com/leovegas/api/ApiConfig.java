package com.leovegas.api;

/**
 * Configuration constants for the LeoVegas API tests.
 */
public class ApiConfig {

    public static final String BASE_URL = System.getProperty("api.base.url",
            "https://jsonplaceholder.typicode.com");

    public static final int DEFAULT_TIMEOUT_MS = 10_000;

    private ApiConfig() {
        // utility class
    }
}
