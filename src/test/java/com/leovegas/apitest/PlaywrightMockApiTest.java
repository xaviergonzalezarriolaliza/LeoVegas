package com.leovegas.apitest;

import com.leovegas.mockapi.MockApiServer;
import org.junit.jupiter.api.*;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@org.junit.jupiter.api.extension.ExtendWith(TestLogger.class)
public class PlaywrightMockApiTest {
    private static String baseUrl;
    private static HttpClient client;

    @BeforeAll
    public static void setup() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        baseUrl = "http://localhost:" + port;
        // Start the embedded Mock API server on chosen port
        MockApiServer.main(new String[]{String.valueOf(port)});
        awaitInitialization();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
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

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @Order(1)
    public void testHelloEndpoint() throws Exception {
        HttpResponse<String> response = get("/hello");
        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("\"message\":\"Hello, LeoVegas!\""));

        HttpResponse<String> postResp = post("/hello", null);
        int code = postResp.statusCode();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(2)
    public void testEchoEndpoint() throws Exception {
        String payload = "{\"id\":123,\"device\":\"iPhone\",\"os\":\"iOS 17\",\"foo\":\"bar\"}";
        HttpResponse<String> resp = post("/echo", payload);
        assertEquals(200, resp.statusCode());
        String text = resp.body();
        assertTrue(text.contains("\"id\":123"));
        assertTrue(text.contains("\"device\":\"iPhone\""));
        assertTrue(text.contains("\"os\":\"iOS 17\""));
        assertTrue(text.contains("\"foo\":\"bar\""));

        HttpResponse<String> getResp = get("/echo");
        int getCode = getResp.statusCode();
        assertTrue(getCode == 404 || getCode == 405);
    }

    @Test
    @Order(3)
    public void testLongResponseTime() throws Exception {
        long start = System.nanoTime();
        HttpResponse<String> resp = get("/long");
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        assertEquals(200, resp.statusCode());
        System.out.println("Response time for GET /long endpoint: " + durationMs + " ms");

        HttpResponse<String> postResp = post("/long", null);
        int code = postResp.statusCode();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(4)
    public void testInvalidEndpoint() throws Exception {
        HttpResponse<String> r1 = get("/notfound");
        assertEquals(404, r1.statusCode());
        HttpResponse<String> r2 = post("/notfound", null);
        assertEquals(404, r2.statusCode());
    }

    @Test
    @Order(5)
    public void testEchoEndpointWithMissingOrInvalidPayload() throws Exception {
        HttpResponse<String> empty = post("/echo", null);
        assertEquals(200, empty.statusCode());
        assertTrue(empty.body().contains("\"echo\":\"\""));

        HttpResponse<String> invalid = post("/echo", "not-a-json");
        assertEquals(200, invalid.statusCode());
        assertTrue(invalid.body().contains("not-a-json"));

        String partial = "{\"id\":456,\"device\":\"Android\"}";
        HttpResponse<String> partialResp = post("/echo", partial);
        assertEquals(200, partialResp.statusCode());
        assertTrue(partialResp.body().contains("\"id\":456"));
        assertTrue(partialResp.body().contains("\"device\":\"Android\""));

        HttpRequest putReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/echo"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"foo\":\"bar\"}"))
                .build();
        HttpResponse<String> putResp = client.send(putReq, HttpResponse.BodyHandlers.ofString());
        int code = putResp.statusCode();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(6)
    public void testEchoEndpointWithManyFields() throws Exception {
        String manyFieldsPayload = "{" +
                "\"id\":1001,\"name\":\"Alice\",\"email\":\"alice@example.com\",\"age\":30,\"country\":\"SE\",\"city\":\"Stockholm\",\"zip\":\"11122\",\"device\":\"Android\",\"os\":\"Android 14\",\"appVersion\":\"5.2.1\",\"sessionId\":\"sess-abc-123\",\"isPremium\":true,\"balance\":1234.56,\"lastLogin\":\"2026-02-20T10:00:00Z\",\"locale\":\"sv-SE\",\"currency\":\"SEK\",\"features\":\"A,B,C\",\"tags\":\"tag1,tag2\",\"notes\":\"test user with many fields\"" +
                "}";

        HttpResponse<String> resp = post("/echo", manyFieldsPayload);
        assertEquals(200, resp.statusCode());
        String text = resp.body();
        assertTrue(text.contains("\"id\":1001"));
        assertTrue(text.contains("\"name\":\"Alice\""));
        assertTrue(text.contains("\"email\":\"alice@example.com\""));
        assertTrue(text.contains("\"device\":\"Android\""));
        assertTrue(text.contains("\"os\":\"Android 14\""));
        assertTrue(text.contains("\"appVersion\":\"5.2.1\""));
        assertTrue(text.contains("\"sessionId\":\"sess-abc-123\""));
        assertTrue(text.contains("\"isPremium\":true"));
        assertTrue(text.contains("\"balance\":1234.56"));
        assertTrue(text.contains("\"lastLogin\":\"2026-02-20T10:00:00Z\""));
        assertTrue(text.contains("\"locale\":\"sv-SE\""));
        assertTrue(text.contains("\"currency\":\"SEK\""));
        assertTrue(text.contains("\"features\":\"A,B,C\""));
        assertTrue(text.contains("\"tags\":\"tag1,tag2\""));
        assertTrue(text.contains("\"notes\":\"test user with many fields\""));
    }
}
