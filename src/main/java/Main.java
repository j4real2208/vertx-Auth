
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class.getName());

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
        final MainVerticle adminRestServer = new MainVerticle();

        vertx.deployVerticle(adminRestServer,
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

///Users/jojojohnson/Library/Java/JavaVirtualMachines/openjdk-19/Contents/Home/bin/java -javaagent:/Applications/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=65386:/Applications/IntelliJ IDEA CE.app/Contents/bin -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/jojojohnson/personal/vert.x_sample/sample_mvn/sample_oauth2/target/classes:/Users/jojojohnson/.m2/repository/org/apache/logging/log4j/log4j-api/2.23.0/log4j-api-2.23.0.jar:/Users/jojojohnson/.m2/repository/io/vertx/vertx-core/4.5.4/vertx-core-4.5.4.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-common/4.1.107.Final/netty-common-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-buffer/4.1.107.Final/netty-buffer-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-transport/4.1.107.Final/netty-transport-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-handler/4.1.107.Final/netty-handler-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-transport-native-unix-common/4.1.107.Final/netty-transport-native-unix-common-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-codec/4.1.107.Final/netty-codec-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-handler-proxy/4.1.107.Final/netty-handler-proxy-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-codec-socks/4.1.107.Final/netty-codec-socks-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-codec-http/4.1.107.Final/netty-codec-http-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-codec-http2/4.1.107.Final/netty-codec-http2-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-resolver/4.1.107.Final/netty-resolver-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-resolver-dns/4.1.107.Final/netty-resolver-dns-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/io/netty/netty-codec-dns/4.1.107.Final/netty-codec-dns-4.1.107.Final.jar:/Users/jojojohnson/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.16.1/jackson-core-2.16.1.jar Main
