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


