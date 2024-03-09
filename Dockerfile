FROM openjdk:17-jdk-alpine
LABEL authors="jojojohnson"
COPY target/sample_oauth2-1.0-SNAPSHOT-fat.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]