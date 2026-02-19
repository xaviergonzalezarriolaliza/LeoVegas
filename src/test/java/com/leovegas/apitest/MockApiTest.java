package com.leovegas.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import com.leovegas.mockapi.MockApiServer;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;
import java.net.ServerSocket;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MockApiTest {
            @Test
    @Order(5)
    public void testEchoEndpointWithMissingOrInvalidPayload() {
        // POST /echo with missing payload (empty body)
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", equalTo(""));

        // POST /echo with invalid (non-JSON) payload
        String invalidPayload = "not-a-json";
        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", equalTo(invalidPayload));

        // POST /echo with partial fields
        String partialPayload = "{\"id\":456,\"device\":\"Android\"}";
        given()
            .contentType(ContentType.JSON)
            .body(partialPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", containsString("\"id\":456"))
            .body("echo", containsString("\"device\":\"Android\""));

        // PUT /echo (method not allowed)
        given()
            .contentType(ContentType.JSON)
            .body("{\"foo\":\"bar\"}")
        .when()
            .put("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }
        @Test
        @Order(4)
        public void testInvalidEndpoint() {
            // GET invalid endpoint
            given()
            .when()
                .get("/notfound")
            .then()
                .statusCode(404);

            // POST invalid endpoint
            given()
            .when()
                .post("/notfound")
            .then()
                .statusCode(404);
        }
    @BeforeAll
    public static void setup() throws Exception {
        // pick a free port and start the server on it to avoid CI port collisions
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        // Start the embedded Mock API server in-process for tests using the selected port
        MockApiServer.main(new String[]{String.valueOf(port)});
        // Wait for Spark to initialize
        awaitInitialization();
    }

    @AfterAll
    public static void teardown() {
        try {
            stop();
            // give Spark a moment to shutdown
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(1)
    public void testHelloEndpoint() {
        // GET /hello
        given()
        .when()
            .get("/hello")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Hello, LeoVegas!"));

        // POST /hello (should return 404 or 405 since not implemented)
        given()
        .when()
            .post("/hello")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(2)
    public void testEchoEndpoint() {
        String payload = "{\"id\":123,\"device\":\"iPhone\",\"os\":\"iOS 17\",\"foo\":\"bar\"}";
        // POST /echo
        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo", containsString("\"id\":123"))
            .body("echo", containsString("\"device\":\"iPhone\""))
            .body("echo", containsString("\"os\":\"iOS 17\""))
            .body("echo", containsString("\"foo\":\"bar\""));

        // GET /echo (should return 404 or 405 since not implemented)
        given()
        .when()
            .get("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(3)
    public void testLongResponseTime() {
        // GET /long
        long responseTime =
            given()
            .when()
                .get("/long")
            .then()
                .statusCode(200)
                .extract()
                .time();
        System.out.println("Response time for GET /long endpoint: " + responseTime + " ms");

        // POST /long (should return 404 or 405 since not implemented)
        given()
        .when()
            .post("/long")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }
}
