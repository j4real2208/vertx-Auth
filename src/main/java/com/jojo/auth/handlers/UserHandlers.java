package com.jojo.auth.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojo.auth.models.Name;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class UserHandlers {

    public static Handler<RoutingContext> getUserNameHandler() {
        final UserHandlers userHandlers = new UserHandlers();
        return userHandlers::handlePayloadRequest;
    }
    private static final Logger log = LogManager.getLogger(UserHandlers.class.getName());
    private void handlePayloadRequest(RoutingContext routingContext) {
        log.info("Information of the current incoming user: {}", routingContext.user().principal().getString("username"));
        getPayloadInformation(routingContext)
                .onSuccess(name -> {
                    log.info("The user we had processed is: " + name.getFirstName() + " and last name: " + name.getLastName());
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

    private Future<Name> getPayloadInformation(RoutingContext routingContext) {
        final Promise<Name> promise = Promise.promise();

        WebClientOptions options = new WebClientOptions()
                .setDefaultHost("randomuser.me")
                .setDefaultPort(8080);

        WebClient client = WebClient.create(routingContext.vertx(), options);

        client.get("/api/")
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        log.info("Received the payload from server");
                        Optional.of(extractNameFromResponse(response.bodyAsString()))
                                .ifPresentOrElse(promise::complete, () -> promise.fail("Failed to fetch the data"));}
                }).onFailure(err -> {
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
}
