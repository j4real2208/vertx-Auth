package com.jojo.auth;

import com.jojo.auth.data.DatabaseManager;
import com.jojo.auth.handlers.UserHandlers;
import com.jojo.auth.security.BasicAuthProvider;
import com.jojo.auth.security.CustomAuthenticationHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.LocalSessionStore;
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
                    BasicAuthProvider basicAuthProvider = new BasicAuthProvider(vertx);
                    CustomAuthenticationHandler customAuthenticationHandler = new CustomAuthenticationHandler(vertx, basicAuthProvider);

                    addRoutes(routerBuilder);

                    routerBuilder.securityHandler("basicAuth", customAuthenticationHandler);

                    DatabaseManager.getClient(vertx);
                    DatabaseManager.initializeDatabase(vertx);

                    Router router = routerBuilder.createRouter();
                    router.route("/metrics").handler(PrometheusScrapingHandler.create());

                    // Set up middleware
                    router.route().handler(BodyHandler.create());
                    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

                    // Protected routes
                    router.route("/private/*").handler(RedirectAuthHandler.create(basicAuthProvider, "/loginpage.html"));
                    router.route("/private/*").handler(StaticHandler.create("private").setCachingEnabled(false));

                    // Form login handler
                    router.route("/loginhandler").handler(FormLoginHandler.create(basicAuthProvider));

                    // Logout handler
                    router.route("/logout").handler(context -> {
                        log.info("Current context: {}", context.user().principal().encodePrettily());
                        context.clearUser();
                        context.response().putHeader("location", "/").setStatusCode(302).end();
                    });

                    // Static files handler
                    router.route().handler(StaticHandler.create("webroot"));

                    // Start the HTTP server
                    vertx.createHttpServer(new HttpServerOptions())
                            .requestHandler(router)
                            .listen(8888)
                            .onSuccess(http -> {
                                startPromise.complete();
                                log.info("HTTP server started on port 8888");
                            })
                            .onFailure(err -> {
                                startPromise.fail(err.getCause());
                                log.error("Failed to start HTTP server: {}", err.getMessage());
                            });
                })
                .onFailure(startPromise::fail);
    }

    private void addRoutes(RouterBuilder routerBuilder) {

        log.traceEntry(() -> routerBuilder);

        routerBuilder.operation("getUserName").handler(UserHandlers.getUserNameHandler());

        log.traceExit();
    }
}
