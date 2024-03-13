package com.jojo.auth.security;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CustomAuthenticationHandler implements AuthenticationHandler {

    private final Vertx vertx; // Reference to the Vertx instance
    private final AuthenticationProvider authenticationProvider; // Your custom authentication provider

    public CustomAuthenticationHandler(Vertx vertx, AuthenticationProvider authenticationProvider) {
        this.vertx = vertx;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        // Retrieve credentials from the request
        Credentials credentials = extractCredentials(routingContext);
        // Authenticate using your custom authentication provider
        authenticationProvider.authenticate(credentials, ar -> {
            if (ar.succeeded()) {
                // Authentication succeeded
                User authenticatedUser = ar.result();

                // Set the authenticated user in the security context
                routingContext.setUser(authenticatedUser);

                // Continue processing the request
                routingContext.next();
            } else {
                // Authentication failed
                routingContext.fail(401); // Unauthorized
            }
        });
    }

    private Credentials extractCredentials(RoutingContext routingContext) {
        String authorizationHeader = routingContext.request().getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
            byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
            String credentialsString = new String(decodedCredentials, StandardCharsets.UTF_8);
            String[] parts = credentialsString.split(":");
            // Construct the JSON object with username and password fields
            JsonObject jsonObject = new JsonObject()
                    .put("username", parts[0])
                    .put("password", parts[1]);
            return new UsernamePasswordCredentials(jsonObject);
        }
        return null;
    }
}
