package com.jojo.auth;


import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyMain {
    private static final Logger log = LogManager.getLogger(MyMain.class.getName());

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();

        log.info("Starting up the server ");
        run(vertx).onComplete(res -> {
            if (res.failed()) {
                log.atFatal().withThrowable(res.cause()).log( "Failed during startup and error: {}",res.cause());
                System.exit(1);
            }
        });
    }



    static Future<String> run(final Vertx vertx) {
        log.traceEntry(() -> vertx);

        final Promise<String> fut = Promise.promise();
        final MainVerticle userGeneratorServer = new MainVerticle();

        vertx.deployVerticle(userGeneratorServer,
                res -> {
                    if (res.failed()) {
                        log.fatal("The install failed: %s",res.cause());
                    }
                    fut.handle(res);
                }
        );

        return log.traceExit(fut.future());
    }
}


