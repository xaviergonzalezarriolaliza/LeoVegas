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

        // Obtain a representative payload from the mock server and derive invalid/partial payloads from it
        var sampleResp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        String sampleJson = sampleResp.asString();
        // invalid payload: take the sample and corrupt it by truncating the final char
        String invalidPayload = sampleJson.substring(0, Math.max(1, sampleJson.length() - 1));
        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo", equalTo(invalidPayload));

        // partial json: use two fields from the sample payload dynamically
        int partialId = sampleResp.jsonPath().getInt("id");
        String partialDevice = sampleResp.jsonPath().getString("device");
        String partialPayload = String.format("{\"id\":%d,\"device\":\"%s\"}", partialId, partialDevice);
        given()
            .contentType(ContentType.JSON)
            .body(partialPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .body("echo.id", equalTo(partialId))
            .body("echo.device", equalTo(partialDevice));

        // PUT not allowed
        // PUT not allowed - build payload from server sample to keep tests realistic
        var sampleForPut =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int putId = sampleForPut.jsonPath().getInt("id");
        String putDevice = sampleForPut.jsonPath().getString("device");
        String putPayload = String.format("{\"id\":%d,\"device\":\"%s\",\"foo\":\"bar\"}", putId, putDevice);

        given()
            .contentType(ContentType.JSON)
            .body(putPayload)
        .when()
            .put("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(6)
    public void testManyFieldsPayload_jUnit() {
        // Extract response and perform stricter type and boundary checks
        var resp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        // Basic equality checks
        Assertions.assertEquals(1001, resp.jsonPath().getInt("id"));
        Assertions.assertEquals("Alice", resp.jsonPath().getString("name"));
        Assertions.assertEquals("alice@example.com", resp.jsonPath().getString("email"));
        Assertions.assertEquals(30, resp.jsonPath().getInt("age"));
        Assertions.assertEquals("SE", resp.jsonPath().getString("country"));
        Assertions.assertEquals("Stockholm", resp.jsonPath().getString("city"));
        Assertions.assertEquals("Android", resp.jsonPath().getString("device"));
        Assertions.assertEquals("Android 14", resp.jsonPath().getString("os"));
        Assertions.assertEquals("5.2.1", resp.jsonPath().getString("appVersion"));
        Assertions.assertEquals("sess-abc-123", resp.jsonPath().getString("sessionId"));
        Assertions.assertEquals("2026-02-20T10:00:00Z", resp.jsonPath().getString("lastLogin"));
        Assertions.assertEquals("sv-SE", resp.jsonPath().getString("locale"));
        Assertions.assertEquals("SEK", resp.jsonPath().getString("currency"));
        Assertions.assertEquals("A,B,C", resp.jsonPath().getString("features"));
        Assertions.assertEquals("tag1,tag2", resp.jsonPath().getString("tags"));
        Assertions.assertEquals("test user with many fields", resp.jsonPath().getString("notes"));

        // Type checks
        Object idObj = resp.jsonPath().get("id");
        Assertions.assertTrue(idObj instanceof Integer || idObj instanceof Long,
            "id should be an integer type");

        Object isPremiumObj = resp.jsonPath().get("isPremium");
        Assertions.assertTrue(isPremiumObj instanceof Boolean,
            "isPremium should be a boolean");

        Object balanceObj = resp.jsonPath().get("balance");
        Assertions.assertTrue(balanceObj instanceof Number,
            "balance should be numeric");

        // Numeric boundaries (example): id positive, balance reasonable
        int idVal = resp.jsonPath().getInt("id");
        double balanceVal = resp.jsonPath().getDouble("balance");
        Assertions.assertTrue(idVal >= 1 && idVal <= 10_000_000,
            "id out of expected range");
        Assertions.assertTrue(balanceVal >= -1_000_000.0 && balanceVal <= 1_000_000.0,
            "balance out of expected range");
    }

    @Test
    @Order(8)
    public void testManyFieldsPayloadBoundary_low_jUnit() {
        var resp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int idVal = resp.jsonPath().getInt("id");
        double balanceVal = resp.jsonPath().getDouble("balance");

        // Lower-bound expectations
        Assertions.assertTrue(idVal >= 1, "id must be >= 1");
        Assertions.assertTrue(balanceVal >= -1_000_000.0, "balance lower bound violated");
    }

    @Test
    @Order(9)
    public void testManyFieldsPayloadBoundary_high_jUnit() {
        var resp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int idVal = resp.jsonPath().getInt("id");
        double balanceVal = resp.jsonPath().getDouble("balance");

        // Upper-bound expectations
        Assertions.assertTrue(idVal <= 10_000_000, "id must be <= 10,000,000");
        Assertions.assertTrue(balanceVal <= 1_000_000.0, "balance upper bound violated");
    }

    @Test
    @Order(10)
    public void testExampleCobardeDeLaPradera() {
        given()
        .when()
            .get("/chiquito")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("condemor", equalTo("Jaaaaaaaarrll! No puedor! No puedorrrr!"));
    }

    @Test
    @Order(7)
    public void testEchoEndpointWithLargePayload_jUnit() {
        // Build a large payload by repeating a string value obtained from the mock server
        var sampleResp =
            given()
            .when()
                .get("/manyFieldsPayload")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        String base = sampleResp.jsonPath().getString("notes");
        if (base == null) base = "x";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 10000) {
            sb.append(base);
        }
        String largeData = sb.substring(0, 10000);
        String largePayload = String.format("{\"data\":\"%s\"}", largeData);

        given()
            .contentType(ContentType.JSON)
            .body(largePayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo.data", equalTo(largeData));
    }

}
