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
public class MockApiTest {

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
    public void testHelloEndpoint() {
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
    public void testEchoEndpoint() {
        String payload = "{\"id\":123,\"device\":\"iPhone\",\"os\":\"iOS 17\",\"foo\":\"bar\"}";

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo.id", equalTo(123))
            .body("echo.device", equalTo("iPhone"))
            .body("echo.os", equalTo("iOS 17"))
            .body("echo.foo", equalTo("bar"));

        given()
        .when()
            .get("/echo")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }

    @Test
    @Order(3)
    public void testLongResponseTime() {
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
    public void testInvalidEndpoint() {
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
    public void testEchoEndpointWithMissingOrInvalidPayload() {
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
    public void testEchoEndpointWithManyFields() {
        String manyFieldsPayload = "{" +
                "\"id\":1001,\"name\":\"Alice\",\"email\":\"alice@example.com\",\"age\":30,\"country\":\"SE\",\"city\":\"Stockholm\",\"zip\":\"11122\",\"device\":\"Android\",\"os\":\"Android 14\",\"appVersion\":\"5.2.1\",\"sessionId\":\"sess-abc-123\",\"isPremium\":true,\"balance\":1234.56,\"lastLogin\":\"2026-02-20T10:00:00Z\",\"locale\":\"sv-SE\",\"currency\":\"SEK\",\"features\":\"A,B,C\",\"tags\":\"tag1,tag2\",\"notes\":\"test user with many fields\"" +
                "}";

        given()
            .contentType(ContentType.JSON)
            .body(manyFieldsPayload)
        .when()
            .post("/echo")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("echo.id", equalTo(1001))
            .body("echo.name", equalTo("Alice"))
            .body("echo.email", equalTo("alice@example.com"))
            .body("echo.age", equalTo(30))
            .body("echo.country", equalTo("SE"))
            .body("echo.city", equalTo("Stockholm"))
            .body("echo.device", equalTo("Android"))
            .body("echo.os", equalTo("Android 14"))
            .body("echo.appVersion", equalTo("5.2.1"))
            .body("echo.sessionId", equalTo("sess-abc-123"))
            .body("echo.isPremium", equalTo(true))
            .body("echo.balance", equalTo(1234.56f))
            .body("echo.lastLogin", equalTo("2026-02-20T10:00:00Z"))
            .body("echo.locale", equalTo("sv-SE"))
            .body("echo.currency", equalTo("SEK"))
            .body("echo.features", equalTo("A,B,C"))
            .body("echo.tags", equalTo("tag1,tag2"))
            .body("echo.notes", equalTo("test user with many fields"));
    }
}
