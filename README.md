# Vert.x Authentication 

- This repository contains a sample Vert.x application demonstrating the integration of authentication feature. The code includes a basic HTTP server with authentication using a custom provider (`BasicAuthProvider`) . 
- It includes instructions for setting up monitoring using Prometheus and Grafana.
  
## Prerequisites

- Java 17 or later
- Maven
- Vert.x 4.5.4

## Setup

1. Clone the repository:

    ```bash
    git clone https://github.com/jojo/auth.git
    ```

2. Build the project:

    ```bash
    cd auth
    mvn clean package
    ```

3. Run the Vert.x application:

    ```bash
    java -jar target/your-jar-name.jar
    ```

## Features

### 1. Authentication

The application uses a custom authentication provider (`BasicAuthProvider`) to authenticate users. The `MyUser` class implements the `User` interface, providing user information.

```java
// Implement your custom authentication logic in MyUser class
public class MyUser implements User {
    // ...
}
```

```java
public class BasicAuthProvider implements AuthenticationProvider {
    // Implement your custom logic to check user roles
    // ...
}
```

### 2. HTTP Server

The `MainVerticle` class starts a Vert.x HTTP server and integrates authentication handlers into the router.

```java
// Integration of authentication and authorization handlers
AuthenticationProvider authProvider = new BasicAuthProvider();
BasicAuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
router.route().handler(basicAuthHandler);
```

### 3. Payload Information

The application fetches payload information from a remote server based on user authentication.

```java
// Handle payload request with authentication and authorization checks
private void handlePayloadRequest(RoutingContext routingContext) {
    getPayloadInformation(routingContext)
        .onSuccess(name -> {
            // Handle payload response
        })
        .onFailure(err -> {
            // Handle payload request failure
        });
}
```

---

Examples 


### 1. Authenticate (Basic Auth)

```bash
curl -X GET -u user1:password1 http://localhost:8888/api/payload
```

Replace `user1:password1` with valid credentials from the `BasicAuthProvider` user database.





---
### Testing on Docker 

## Setting Up Monitoring {JMX and micrometer data sources } 

---

## Monitoring Configuration

The following code snippet demonstrates how to configure metrics collection and monitoring using Micrometer with Vert.x:

```java
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
```

This code can be used to enable and configure metrics collection within your Vert.x application. It utilizes Micrometer to gather metrics data and provides options for integration with monitoring systems like Prometheus. Additionally, it demonstrates how to apply a filter to configure distribution statistics, such as percentile histograms, for more granular monitoring insights.

--- 


### Update Prometheus Configuration

Update the `prometheus.yml` file to replace the placeholder `YourHostIP` with the host IP address. Run the following command:

```bash
HOST_IP=$(ipconfig getifaddr en0 | awk '{print $NF}')
sed -i '' "s/YourHostIP/$HOST_IP/g" ./utilities/prometheus.yml
```

### Build Vert.x Application Image

Build the Vert.x application Docker image with the desired tag:

```bash
TAG="latest"  # or specify a custom tag
docker image build -t java-vertx-sample:"$TAG" .
```

### Run Vert.x Application Container

Run the Vert.x application container:

```bash
docker run -d -p 8888:8888 -p 12345:12345 java-vertx-sample:"$TAG"
```

### Run Prometheus Container

Run the Prometheus container with the mounted YAML file:

```bash
docker run -d -p 9090:9090 -v ./utilities/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```

### Run Grafana Container

Optionally, you can run the Grafana container for visualization:

```bash
docker run -d -p 3000:3000 --name=grafana grafana/grafana-oss
```

Access Grafana at http://localhost:3000 (default credentials: admin/admin).

---


### Note:

- If authentication or authorization fails, the server will respond with an HTTP status code 401 (Unauthorized) or 403 (Forbidden), respectively.
- Make sure to include the correct path and endpoint in the URL based on your application structure.

---

## License

This sample application is released under the MIT License. See the [LICENSE](LICENSE) file for details.
