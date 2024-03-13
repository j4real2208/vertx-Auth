package com.jojo.auth.security;

import com.jojo.auth.data.DatabaseManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.impl.UserImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BasicAuthProvider implements AuthenticationProvider {

    private static final Logger log = LogManager.getLogger(BasicAuthProvider.class.getName());
    private final Vertx vertx;

    public BasicAuthProvider(Vertx vertx) {

        this.vertx = vertx;
        // Add more users as needed
    }


    @Override
    public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    }

    @Override
    public void authenticate(Credentials authInfo, Handler<AsyncResult<User>> resultHandler) {
        if (authInfo instanceof UsernamePasswordCredentials) {
            String username = ((UsernamePasswordCredentials) authInfo).getUsername();
            String password = ((UsernamePasswordCredentials) authInfo).getPassword();
            log.info("Entered to authenticate in BasicAuth for Principal: {}", username);

            DatabaseManager.getUserByUsername(username, vertx, queryResult -> {
                if (queryResult.succeeded()) {
                    if(queryResult.result() == null) {
                        resultHandler.handle(Future.failedFuture("USER not in DB"));
                    }
                    else if (isValidCredentials(username, password, queryResult.result())) {
                        // Create a simple User instance
                        User authenticatedUser = new MyUser(username);
                        resultHandler.handle(Future.succeededFuture(authenticatedUser));
                    } else {
                        resultHandler.handle(Future.failedFuture("Authentication failed"));
                    }
                } else {
                    resultHandler.handle(Future.failedFuture("Unable to query the User Details for UserDB"));
                }
            });
        } else {
            resultHandler.handle(Future.failedFuture("Unsupported credentials type"));
        }
    }

    private boolean isValidCredentials(String username, String password, JsonObject queryDb) {
        // Check if the username exists and the password matches
//        return users.containsKey(username) && users.get(username).equals(password);
        return queryDb.getString("username").equals(username) && queryDb.getString("password").equals(password);
    }

    // A simple User class implementation
    public static class MyUser extends UserImpl {

        final private String username;

        public MyUser(String username) {
            this.username = username;
        }

        @Override
        public JsonObject attributes() {
            return new JsonObject();
        }


        @Override
        public JsonObject principal() {
            return new JsonObject().put("username", username);
        }

        @Override
        public User merge(User user) {
            // Implement merging logic if needed
            return this;
        }
    }
}
