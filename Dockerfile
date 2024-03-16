FROM openjdk:17-jdk-alpine
LABEL authors="jojojohnson"
RUN apk add --no-cache openssl
COPY target/sample_oauth2-1.0-SNAPSHOT-fat.jar app.jar
COPY utilities/jmx_prometheus/jmx_prometheus_javaagent-0.20.0.jar jmx.jar
COPY utilities/jmx_config.yaml config.yaml
COPY src/main/resources/cert cert
ENTRYPOINT ["java","-javaagent:jmx.jar=12345:config.yaml","-jar","/app.jar"]