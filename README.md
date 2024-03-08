# Vert.x Authentication 

This repository contains a sample Vert.x application demonstrating the integration of authentication feature. The code includes a basic HTTP server with authentication using a custom provider (`BasicAuthProvider`) .

## Prerequisites

- Java 8 or later
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


### Note:

- If authentication or authorization fails, the server will respond with an HTTP status code 401 (Unauthorized) or 403 (Forbidden), respectively.
- Make sure to include the correct path and endpoint in the URL based on your application structure.

Feel free to adjust the examples based on your specific use case or structure.

---

## License

This sample application is released under the MIT License. See the [LICENSE](LICENSE) file for details.
