package security;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authorization.Authorization;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class BasicAuthProvider implements AuthenticationProvider {

    // In-memory user store (replace with a proper database in a real-world scenario)
    private final Map<String, String> users = new HashMap<>();

    public BasicAuthProvider() {
        // Add some sample users (username, password)
        users.put("user1", "password1");
        users.put("user2", "password2");
        // Add more users as needed
    }

    @Override
    public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    }

    @Override
    public void authenticate(Credentials authInfo, Handler<AsyncResult<User>> resultHandler) {
        String authHeader = authInfo.toHttpAuthorization();
        System.out.println(authHeader);
        System.out.println(resultHandler);
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            // No or invalid Authorization header
            resultHandler.handle(Future.failedFuture("Invalid or missing Authorization header"));
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedCredentials, StandardCharsets.UTF_8);

        String[] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            // Invalid credentials format
            resultHandler.handle(Future.failedFuture("Invalid credentials format"));
            return;
        }

        String username = parts[0];
        String password = parts[1];

        // Check if the provided credentials are valid
        if (isValidCredentials(username, password)) {
            // Create a simple User instance
            User authenticatedUser = new MyUser(username);
            resultHandler.handle(Future.succeededFuture(authenticatedUser));
        } else {
            resultHandler.handle(Future.failedFuture("Authentication failed"));
        }
    }

    private boolean isValidCredentials(String username, String password) {
        // Check if the username exists and the password matches
        return users.containsKey(username) && users.get(username).equals(password);
    }

    // A simple User class implementation
    private class MyUser implements User {
        private final String username;

        MyUser(String username) {
            this.username = username;
        }

        @Override
        public JsonObject attributes() {
            return null;
        }

        @Override
        public User isAuthorized(Authorization authorization, Handler<AsyncResult<Boolean>> handler) {
            return null;
        }

        @Override
        public JsonObject principal() {
            return new JsonObject().put("username", username);
        }

        @Override
        public void setAuthProvider(AuthProvider authProvider) {

        }

        @Override
        public User merge(User user) {
            return null;
        }
    }
}
