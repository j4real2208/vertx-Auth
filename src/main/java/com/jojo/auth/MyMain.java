package com.jojo.auth;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyMain {
    private static final Logger log = LogManager.getLogger(MyMain.class.getName());

    public static void main(String[] args) {
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
                .setEnabled(true)
                .setJmxMetricsOptions(new VertxJmxMetricsOptions().setEnabled(true))
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true));
        VertxOptions vertxOptions = new VertxOptions()
                .setMetricsOptions(metricsOptions);

        final Vertx vertx = Vertx.vertx(vertxOptions);

        setupCertificates(vertx);


        PrometheusMeterRegistry registry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();


        registry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                return DistributionStatisticConfig.builder()
                        .percentilesHistogram(true)
                        .build()
                        .merge(config);
            }
        });

        log.info("Starting up the server ");
        run(vertx).onComplete(res -> {
            if (res.failed()) {
                log.atFatal().withThrowable(res.cause()).log("Failed during startup and error: {}", res.cause());
                System.exit(1);
            }
        });
    }

    private static void setupCertificates(Vertx vertx) {
        vertx.executeBlocking(blockingCodeHandler -> {
            try {
                // Run your bash script here
                ProcessBuilder processBuilder = new ProcessBuilder("sh", "src/main/resources/cert/executeCert.sh");
                processBuilder.redirectErrorStream(true); // Merge stdout and stderr
                Process process = processBuilder.start();

                // Print the output of the script
                printScriptOutput(process.getInputStream());

                // Wait for the script to finish
                int exitCode = process.waitFor();

                // Log the exit code
                System.out.println("Script execution finished with exit code: " + exitCode);

                blockingCodeHandler.complete();
            } catch (Exception e) {
                blockingCodeHandler.fail(e);
            }
        }, resultHandler -> {
                if (resultHandler.succeeded()) {
                    System.out.println("Script execution completed successfully");
                } else {
                    System.err.println("Script execution failed: " + resultHandler.cause().getMessage());
                }
            });
    }

    private static void printScriptOutput(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Script output: " + line);
            }
        } catch (IOException e) {
            System.err.println("Error reading script output: " + e.getMessage());
        }
    }

    static Future<String> run(final Vertx vertx) {
        log.traceEntry(() -> vertx);

        final Promise<String> fut = Promise.promise();
        final MainVerticle userGeneratorServer = new MainVerticle();

        vertx.deployVerticle(userGeneratorServer, res -> {
            if (res.failed()) {
                log.fatal("The install failed: %s", res.cause());
            } fut.handle(res);});
        return log.traceExit(fut.future());
    }
}


