package com.leovegas.apitest;

import com.leovegas.mockapi.MockApiServer;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.*;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@org.junit.jupiter.api.extension.ExtendWith(TestLogger.class)
public class PlaywrightMockApiTest {
    private static Playwright playwright;
    private static APIRequestContext request;
    private static String baseUrl;

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

        playwright = Playwright.create();
        request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(baseUrl));
    }

    @AfterAll
    public static void teardown() {
        try {
            if (request != null) request.dispose();
            if (playwright != null) playwright.close();
            stop();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(1)
    public void testHelloEndpoint() {
        APIResponse response = request.get("/hello");
        assertEquals(200, response.status());
        String body = response.text();
        assertTrue(body.contains("\"message\":\"Hello, LeoVegas!\""));

        APIResponse postResp = request.post("/hello");
        int code = postResp.status();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(2)
    public void testEchoEndpoint() {
        String payload = "{\"id\":123,\"device\":\"iPhone\",\"os\":\"iOS 17\",\"foo\":\"bar\"}";
        APIResponse resp = request.post("/echo", APIRequest.NewContextOptions.create().setData(payload).setExtraHTTPHeaders(null));
        // Fallback: if the above builder usage isn't supported in some Playwright versions, call with plain body map
        int status = resp.status();
        assertEquals(200, status);
        String text = resp.text();
        assertTrue(text.contains("\"id\":123"));
        assertTrue(text.contains("\"device\":\"iPhone\""));
        assertTrue(text.contains("\"os\":\"iOS 17\""));
        assertTrue(text.contains("\"foo\":\"bar\""));

        APIResponse getResp = request.get("/echo");
        int getCode = getResp.status();
        assertTrue(getCode == 404 || getCode == 405);
    }

    @Test
    @Order(3)
    public void testLongResponseTime() {
        APIResponse resp = request.get("/long");
        assertEquals(200, resp.status());
        long time = resp.timing().duration();
        System.out.println("Response time for GET /long endpoint: " + time + " ms");

        APIResponse postResp = request.post("/long");
        int code = postResp.status();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(4)
    public void testInvalidEndpoint() {
        APIResponse r1 = request.get("/notfound");
        assertEquals(404, r1.status());
        APIResponse r2 = request.post("/notfound");
        assertEquals(404, r2.status());
    }

    @Test
    @Order(5)
    public void testEchoEndpointWithMissingOrInvalidPayload() {
        APIResponse empty = request.post("/echo");
        assertEquals(200, empty.status());
        assertTrue(empty.text().contains("\"echo\":\"\""));

        APIResponse invalid = request.post("/echo", APIRequest.NewContextOptions.create().setData("not-a-json"));
        assertEquals(200, invalid.status());
        assertTrue(invalid.text().contains("not-a-json"));

        String partial = "{\"id\":456,\"device\":\"Android\"}";
        APIResponse partialResp = request.post("/echo", APIRequest.NewContextOptions.create().setData(partial));
        assertEquals(200, partialResp.status());
        assertTrue(partialResp.text().contains("\"id\":456"));
        assertTrue(partialResp.text().contains("\"device\":\"Android\""));

        APIResponse putResp = request.put("/echo", APIRequest.NewContextOptions.create().setData("{\"foo\":\"bar\"}"));
        int code = putResp.status();
        assertTrue(code == 404 || code == 405);
    }

    @Test
    @Order(6)
    public void testEchoEndpointWithManyFields() {
        String manyFieldsPayload = "{" +
                "\"id\":1001,\"name\":\"Alice\",\"email\":\"alice@example.com\",\"age\":30,\"country\":\"SE\",\"city\":\"Stockholm\",\"zip\":\"11122\",\"device\":\"Android\",\"os\":\"Android 14\",\"appVersion\":\"5.2.1\",\"sessionId\":\"sess-abc-123\",\"isPremium\":true,\"balance\":1234.56,\"lastLogin\":\"2026-02-20T10:00:00Z\",\"locale\":\"sv-SE\",\"currency\":\"SEK\",\"features\":\"A,B,C\",\"tags\":\"tag1,tag2\",\"notes\":\"test user with many fields\"" +
                "}";

        APIResponse resp = request.post("/echo", APIRequest.NewContextOptions.create().setData(manyFieldsPayload));
        assertEquals(200, resp.status());
        String text = resp.text();
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
