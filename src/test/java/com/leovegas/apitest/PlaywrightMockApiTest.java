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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    private String extractEcho(HttpResponse<String> resp) {
        String body = resp.body();
        Gson g = new Gson();
        try {
            JsonObject obj = g.fromJson(body, JsonObject.class);
            if (obj.has("echo") && !obj.get("echo").isJsonNull()) {
                // The server stores the raw request body in the "echo" field.
                // It may be returned as a JSON primitive containing either
                // - a raw JSON string (e.g. {"id":123,...}) or
                // - an already-escaped JSON string (double-encoded). Handle both.
                if (obj.get("echo").isJsonPrimitive()) {
                    String inner = obj.get("echo").getAsString();
                    // If inner itself is JSON (object/array), return it as-is.
                    try {
                        // Parse without throwing for plain strings
                        com.google.gson.JsonElement parsed = g.fromJson(inner, com.google.gson.JsonElement.class);
                        if (parsed != null && (parsed.isJsonObject() || parsed.isJsonArray())) {
                            return g.toJson(parsed);
                        }
                    } catch (Exception ignored) {
                        // not parseable as JSON, fall-through and return raw inner
                    }
                    return inner;
                } else if (obj.get("echo").isJsonObject() || obj.get("echo").isJsonArray()) {
                    return g.toJson(obj.get("echo"));
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return "";
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
        // Use a representative payload from the mock server
        HttpResponse<String> sampleResp = get("/manyFieldsPayload");
        assertEquals(200, sampleResp.statusCode());
        Gson g = new Gson();
        JsonObject sample = g.fromJson(sampleResp.body(), JsonObject.class);
        int idVal = sample.has("id") ? sample.get("id").getAsInt() : 123;
        String deviceVal = sample.has("device") ? sample.get("device").getAsString() : "iPhone";
        String osVal = sample.has("os") ? sample.get("os").getAsString() : "iOS 17";
        String payload = String.format("{\"id\":%d,\"device\":\"%s\",\"os\":\"%s\",\"foo\":\"bar\"}", idVal, deviceVal, osVal);
        HttpResponse<String> resp = post("/echo", payload);
        assertEquals(200, resp.statusCode());
        String echoed = extractEcho(resp);
        assertTrue(echoed.contains("\"id\":" + idVal));
        assertTrue(echoed.contains("\"device\":\"" + deviceVal + "\""));
        assertTrue(echoed.contains("\"os\":\"" + osVal + "\""));
        assertTrue(echoed.contains("\"foo\":\"bar\""));

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
        String echoedEmpty = extractEcho(empty);
        assertEquals("", echoedEmpty);
        // derive invalid and partial payloads from the server-provided sample
        HttpResponse<String> sampleResp = get("/manyFieldsPayload");
        assertEquals(200, sampleResp.statusCode());
        Gson g = new Gson();
        String sampleJson = sampleResp.body();
        String invalidPayload = sampleJson.substring(0, Math.max(1, sampleJson.length() - 1));
        HttpResponse<String> invalid = post("/echo", invalidPayload);
        assertEquals(200, invalid.statusCode());
        String echoedInvalid = extractEcho(invalid);
        assertTrue(echoedInvalid.contains(invalidPayload));

        JsonObject sample = g.fromJson(sampleJson, JsonObject.class);
        int partialId = sample.has("id") ? sample.get("id").getAsInt() : 456;
        String partialDevice = sample.has("device") ? sample.get("device").getAsString() : "Android";
        String partial = String.format("{\"id\":%d,\"device\":\"%s\"}", partialId, partialDevice);
        HttpResponse<String> partialResp = post("/echo", partial);
        assertEquals(200, partialResp.statusCode());
        String echoedPartial = extractEcho(partialResp);
        assertTrue(echoedPartial.contains("\"id\":" + partialId));
        assertTrue(echoedPartial.contains("\"device\":\"" + partialDevice + "\""));

        // PUT not allowed - construct payload using server sample for realism
        HttpResponse<String> sampleForPut = get("/manyFieldsPayload");
        assertEquals(200, sampleForPut.statusCode());
        JsonObject s = g.fromJson(sampleForPut.body(), JsonObject.class);
        int putId = s.has("id") ? s.get("id").getAsInt() : 0;
        String putDevice = s.has("device") ? s.get("device").getAsString() : "unknown";
        String putBody = String.format("{\"id\":%d,\"device\":\"%s\",\"foo\":\"bar\"}", putId, putDevice);
        HttpRequest putReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/echo"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(putBody))
            .build();
        HttpResponse<String> putResp = client.send(putReq, HttpResponse.BodyHandlers.ofString());
        int code = putResp.statusCode();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(6)
    public void testManyFieldsPayload() throws Exception {
        HttpResponse<String> fetchResp = get("/manyFieldsPayload");
        assertEquals(200, fetchResp.statusCode());
        String body = fetchResp.body();
        Gson g = new Gson();
        JsonObject obj = g.fromJson(body, JsonObject.class);
        assertEquals(1001, obj.get("id").getAsInt());
        assertEquals("Alice", obj.get("name").getAsString());
        assertEquals("alice@example.com", obj.get("email").getAsString());
        assertEquals("Android", obj.get("device").getAsString());
        assertEquals("Android 14", obj.get("os").getAsString());
        assertEquals("5.2.1", obj.get("appVersion").getAsString());
        assertEquals("sess-abc-123", obj.get("sessionId").getAsString());
        assertTrue(obj.get("isPremium").getAsBoolean());
        assertEquals(1234.56, obj.get("balance").getAsDouble());
        assertEquals("2026-02-20T10:00:00Z", obj.get("lastLogin").getAsString());
        assertEquals("sv-SE", obj.get("locale").getAsString());
        assertEquals("SEK", obj.get("currency").getAsString());
        assertEquals("A,B,C", obj.get("features").getAsString());
        assertEquals("tag1,tag2", obj.get("tags").getAsString());
        assertEquals("test user with many fields", obj.get("notes").getAsString());
    }

    @Test
    @Order(7)
    public void testChiquitoEndpoint() throws Exception {
        HttpResponse<String> resp = get("/chiquito");
        assertEquals(200, resp.statusCode());
        Gson g = new Gson();
        JsonObject obj = g.fromJson(resp.body(), JsonObject.class);
        assertTrue(obj.has("condemor"));
        assertEquals("Jaaaaaaaarrll! No puedor! No puedorrrr!", obj.get("condemor").getAsString());
    }
}
