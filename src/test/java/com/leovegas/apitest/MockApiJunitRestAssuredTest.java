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
@org.junit.jupiter.api.extension.ExtendWith(TestLogger.class)
public class MockApiJunitRestAssuredTest {

    @BeforeAll
    public static void setup() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        MockApiServer.main(new String[]{String.valueOf(port)});
        awaitInitialization();
    }

    @AfterAll
    public static void teardown() {
        try {
            stop();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(1)
    public void testHelloEndpoint_jUnit() {
        given()
        .when()
            .get("/hello")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Hello, LeoVegas!"));

        given()
        .when()
            .post("/hello")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(2)
    public void testEchoEndpoint_jUnit() {
        // Obtain a representative payload from the mock server and reuse it for the echo test
        var sampleResp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        String payload = sampleResp.asString();
        int expectedId = sampleResp.jsonPath().getInt("id");
        String expectedDevice = sampleResp.jsonPath().getString("device");
        String expectedOs = sampleResp.jsonPath().getString("os");

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo.id", equalTo(expectedId))
            .body("echo.device", equalTo(expectedDevice))
            .body("echo.os", equalTo(expectedOs));

        // GET /echo is not implemented; ensure it returns 404/405
        given()
        .when()
            .get("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(3)
    public void testLongResponseTime_jUnit() {
        long responseTime = given()
            .when()
                .get("/long")
            .then()
                .statusCode(200)
                .extract()
                .time();
        System.out.println("Response time for GET /long endpoint: " + responseTime + " ms");

        given()
        .when()
            .post("/long")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(4)
    public void testInvalidEndpoint_jUnit() {
        given()
        .when()
            .get("/notfound")
        .then()
            .statusCode(404);

        given()
        .when()
            .post("/notfound")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(5)
    public void testEchoEndpointWithMissingOrInvalidPayload_jUnit() {
        // empty body
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", equalTo(""));

        // invalid payload
        String invalidPayload = "not-a-json";
        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", equalTo(invalidPayload));

        // partial json
        String partialPayload = "{\"id\":456,\"device\":\"Android\"}";
        given()
            .contentType(ContentType.JSON)
            .body(partialPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo.id", equalTo(456))
            .body("echo.device", equalTo("Android"));

        // PUT not allowed
        given()
            .contentType(ContentType.JSON)
            .body("{\"foo\":\"bar\"}")
        .when()
            .put("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(6)
    public void testManyFieldsPayload_jUnit() {
        given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1001))
                .body("name", equalTo("Alice"))
                .body("email", equalTo("alice@example.com"))
                .body("age", equalTo(30))
                .body("country", equalTo("SE"))
                .body("city", equalTo("Stockholm"))
                .body("device", equalTo("Android"))
                .body("os", equalTo("Android 14"))
                .body("appVersion", equalTo("5.2.1"))
                .body("sessionId", equalTo("sess-abc-123"))
                .body("isPremium", equalTo(true))
                .body("balance", equalTo(1234.56f))
                .body("lastLogin", equalTo("2026-02-20T10:00:00Z"))
                .body("locale", equalTo("sv-SE"))
                .body("currency", equalTo("SEK"))
                .body("features", equalTo("A,B,C"))
                .body("tags", equalTo("tag1,tag2"))
                .body("notes", equalTo("test user with many fields"));
    }

    @Test
    @Order(7)
    public void testEchoEndpointWithLargePayload_jUnit() {
        StringBuilder largePayload = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 10000; i++) {
            largePayload.append("x");
        }
        largePayload.append("\"}");

        given()
            .contentType(ContentType.JSON)
            .body(largePayload.toString())
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo.data", equalTo(largePayload.substring(9, largePayload.length() - 2)));
    }

}
