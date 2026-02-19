package com.leovegas.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base configuration class for all API tests.
 * Sets up RestAssured with common request specification.
 */
public class BaseApiTest {

    protected static RequestSpecification requestSpec;

    @BeforeAll
    public static void setup() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(io.restassured.config.RestAssuredConfig.config()
                        .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", ApiConfig.DEFAULT_TIMEOUT_MS)
                                .setParam("http.socket.timeout", ApiConfig.DEFAULT_TIMEOUT_MS)))
                .build();
    }
}
