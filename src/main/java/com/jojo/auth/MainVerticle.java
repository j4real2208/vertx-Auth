package com.jojo.auth;

import com.jojo.auth.data.DatabaseManager;
import com.jojo.auth.handlers.UserHandlers;
import com.jojo.auth.security.BasicAuthProvider;
import com.jojo.auth.security.MyAuthenticationHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.RouterBuilder;

import io.vertx.micrometer.PrometheusScrapingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class MainVerticle extends AbstractVerticle {
    private static final Logger log = LogManager.getLogger(MainVerticle.class);
    public static final String KEY_CERT_PATH = "";
    public static final String CERT_PATH = "";

    @Override
    public void start(Promise<Void> startPromise) {

        RouterBuilder.create(vertx, "openapi-spec/vertx.yaml")
                .onSuccess(routerBuilder -> {

                    // Create a BasicAuthHandler using the BasicAuthProvider
                    MyAuthenticationHandler basicAuthHandler = new MyAuthenticationHandler(vertx, new BasicAuthProvider(vertx));

                    addRoutes(routerBuilder);

                    routerBuilder.securityHandler("basicAuth", basicAuthHandler);

                    DatabaseManager.getClient(vertx);
                    DatabaseManager.initializeDatabase(vertx);

                    Router router = routerBuilder.createRouter();

                    router.route("/metrics").handler(PrometheusScrapingHandler.create());

                    HttpServerOptions httpOptions = new HttpServerOptions();

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

                });
    }

    private void addRoutes(final RouterBuilder routerBuilder) {

        log.traceEntry(() -> routerBuilder);

        routerBuilder.operation("getUserName").handler(UserHandlers.getUserNameHandler());

        log.traceExit();
    }




}
