package com.jojo.auth.data;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseManager {
    private static volatile JDBCClient client;
    private static final Logger log = LogManager.getLogger(DatabaseManager.class.getName());

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    public static JDBCClient getClient(Vertx vertx) {
        if (client == null) {
            synchronized (DatabaseManager.class) {
                if (client == null) {
                    client = initializeClient(vertx);
                }
            }
        }
        return client;
    }

    private static JDBCClient initializeClient(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:hsqldb:mem:testdb") // JDBC connection URL for HSQLDB in-memory database
                .put("driver_class", "org.hsqldb.jdbc.JDBCDriver") // JDBC driver class for HSQLDB
                .put("max_pool_size", 30); // Maximum pool size for the database connection pool
        return JDBCClient.createShared(vertx, config);
    }

    public static void initializeDatabase(Vertx vertx) {
        JDBCClient client = getClient(vertx);
        // Create tables and insert sample data
        client.getConnection(connResult -> {
            if (connResult.succeeded()) {
                SQLConnection connection = connResult.result();
                connection.query("CREATE TABLE IF NOT EXISTS users (id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, username VARCHAR(255), password VARCHAR(255))", createTableResult -> {
                    if (createTableResult.succeeded()) {
                        log.info("User table created successfully.");
                        // Insert 10 sample users
                        for (int i = 1; i <= 10; i++) {
                            String username = "user" + i;
                            String password = "password" + i;
                            connection.updateWithParams("INSERT INTO users (username, password) VALUES (?, ?)", new JsonArray().add(username).add(password), insertResult -> {
                                if (insertResult.succeeded()) {
                                    log.info("User {} inserted successfully.", username);
                                } else {
                                    log.error("Failed to insert user {}: {}", username, insertResult.cause().getMessage());
                                }
                            });
                        }
                    } else {
                        log.error("Failed to create user table: {}", createTableResult.cause().getMessage());
                    }
                    connection.close(); // Close the connection
                });
            } else {
                log.error("Failed to obtain a database connection: {}", connResult.cause().getMessage());
            }
        });
    }

    public static void getUserByUsername(String username, Vertx vertx, Handler<AsyncResult<JsonObject>> resultHandler) {
        JDBCClient client = getClient(vertx);
        client.getConnection(connResult -> {
            if (connResult.succeeded()) {
                SQLConnection connection = connResult.result();
                connection.queryWithParams("SELECT * FROM users WHERE username = ?", new JsonArray().add(username), queryResult -> {
                    if (queryResult.succeeded()) {
                        log.info("Query result for user {}: {}", username, queryResult.result().toJson().encodePrettily());
                        if (queryResult.result().getResults().isEmpty()) {
                            resultHandler.handle(io.vertx.core.Future.succeededFuture(null));
                        } else {
                            JsonArray rows = queryResult.result().getResults().get(0);
                            if (rows != null && !rows.isEmpty()) {
                                JsonObject jsonObject = new JsonObject()
                                        .put("username", rows.getValue(1).toString())
                                        .put("password", rows.getValue(2).toString());
                                resultHandler.handle(io.vertx.core.Future.succeededFuture(jsonObject));
                            } else {
                                resultHandler.handle(io.vertx.core.Future.succeededFuture(null));
                            }
                        }

                    } else {
                        log.error("Failed to query user {}: {}", username, queryResult.cause());
                        resultHandler.handle(io.vertx.core.Future.failedFuture(queryResult.cause()));
                    }
                    connection.close();
                });
            } else {
                log.error("Failed to obtain a database connection for user {}: {}", username, connResult.cause());
                resultHandler.handle(io.vertx.core.Future.failedFuture(connResult.cause()));
            }
        });
    }
}
