import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BasicAuthHandler;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import security.BasicAuthProvider;

import java.util.Optional;

public class MainVerticle extends AbstractVerticle {
    private static final Logger log = LogManager.getLogger(MainVerticle.class);
    public static final String KEY_CERT_PATH = "";
    public static final String CERT_PATH = "";

    @Override
    public void start(Promise<Void> startPromise) {
        // Create a BasicAuthProvider instance
        AuthenticationProvider authProvider = new BasicAuthProvider();

        // Create a BasicAuthHandler using the BasicAuthProvider
        BasicAuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);

        Router router = Router.router(vertx);

        // Add Basic Authentication handler to the router
        router.route().handler(basicAuthHandler);

        // Define the request handler for processing the payload
        router.route("/api/payload").handler(this::handlePayloadRequest);

        HttpServerOptions httpOptions = new HttpServerOptions();

//        if (new File(KEY_CERT_PATH).exists() && new File(CERT_PATH).exists()) {
//             httpOptions
//                     .setSsl(true)
//                    .setKeyCertOptions(new PemKeyCertOptions()
//                            .setKeyPath(KEY_CERT_PATH)
//                            .setCertPath(CERT_PATH));
//        }


        // Start the HTTP server
        vertx.createHttpServer(httpOptions)
                .requestHandler(router)
                .listen(8888)
                .onSuccess(http -> {
                    startPromise.complete();
                    System.out.println("HTTP server started on port 8888");
                })
                .onFailure(err -> {
                    startPromise.fail(err.getCause());
                    System.err.println("Failed to start HTTP server: " + err.getMessage());
                });
    }

    private void handlePayloadRequest(RoutingContext routingContext) {
        getPayloadInformation()
                .onSuccess(name -> {
                    log.info("The user we had processed is: " + name.firstName + " and last name: " + name.lastName);
                    try {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(new ObjectMapper().writeValueAsString(name));
                    } catch (JsonProcessingException e) {
                        routingContext.fail(500, e);
                    }
                })
                .onFailure(err -> {
                    log.error("Failed to fetch payload information", err);
                    routingContext.fail(500, err);
                });
    }

    private Future<Name> getPayloadInformation() {
        final Promise<Name> promise = Promise.promise();

        WebClientOptions options = new WebClientOptions()
                .setDefaultHost("randomuser.me")
                .setDefaultPort(8080);

        WebClient client = WebClient.create(vertx, options);

        client.get("/api/")
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        log.info("Received the payload from server");
                        Optional.of(extractNameFromResponse(response.bodyAsString()))
                                .ifPresentOrElse(
                                        promise::complete,
                                        () -> promise.fail("Failed to fetch the data")
                                );
                    }
                })
                .onFailure(err -> {
                    log.error("Something went wrong", err);
                    promise.fail("Failed to fetch the data");
                });

        return promise.future();
    }

    private static Name extractNameFromResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode resultsArray = rootNode.path("results");

            if (resultsArray.isArray() && !resultsArray.isEmpty()) {
                JsonNode firstUser = resultsArray.get(0);

                // Accessing values dynamically
                String firstName = firstUser.path("name").path("first").asText();
                String lastName = firstUser.path("name").path("last").asText();

                log.info("Successfully extracted user details - First Name: {}, Last Name: {}", firstName, lastName);
                return new Name(firstName, lastName);
            } else {
                log.warn("No user details found in the 'results' array.");
            }
        } catch (Exception e) {
            log.error("Error extracting user details from the response", e);
        }

        // Return an empty Name object in case of failure
        return new Name("", "");
    }

    @Data
    public static class Name {
        String firstName;
        String lastName;

        @JsonCreator
        public Name(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
