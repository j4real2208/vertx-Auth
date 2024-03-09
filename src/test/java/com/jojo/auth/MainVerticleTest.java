package com.jojo.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainVerticleTest {

    private Vertx vertx;
    private MockWebServer mockWebServer;

    @BeforeAll
    void setup() throws IOException {
        vertx = Vertx.vertx();
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);

        System.setProperty("RANDOM_USER_API_BASE_URL", mockWebServer.url("/api/").toString());

        vertx.deployVerticle(MainVerticle.class.getName());
    }

    @AfterAll
    void tearDown() throws IOException {
        vertx.close();
        mockWebServer.shutdown();
    }

    @Test
    void testGetUserNameHandler() {
        // Enqueue a mock response with non-null values for the first name and last name
        mockWebServer.enqueue(new MockResponse().setBody("{\"firstName\":\"Gökhan\",\"lastName\":\"Elmastaşoğlu\"}"));

        WebClient client = WebClient.create(vertx);
        client.get(8888, "localhost", "/payload")
                .send()
                .onSuccess(response -> {
                    // Assert that the response body is not null
                    assertNotNull(response.bodyAsJsonObject());
                    System.out.println(response.bodyAsJsonObject());
                    // Assert that the first name and last name are not null
                    assertNotNull(response.bodyAsJsonObject().getString("firstName"));
                    assertNotNull(response.bodyAsJsonObject().getString("lastName"));
                })
                .onFailure(Throwable::printStackTrace);
    }
}
