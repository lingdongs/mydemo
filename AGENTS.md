# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Architecture

*   **Microservices**: This is a Spring Cloud-based microservices project.
*   **API Gateway**: `gateway-demo` is the API gateway. All external traffic must go through it.
*   **Service Discovery & Configuration**: The project uses Nacos for service discovery and dynamic configuration. Routing rules for the gateway are managed in Nacos, not in local files.
*   **Prerequisites**: A running Nacos server is required to run the services locally.

## Build & Run

*   **Build All Modules**: From the project root, run `mvn clean install`. This is mandatory after pulling changes or switching branches.
*   **Run a Service**: Navigate to the service's directory (e.g., `cd general-demo`) and run `mvn spring-boot:run`.
*   **Run a Single Test**:
    *   To run a test class: `mvn test -Dtest=ClassName`
    *   To run a single test method: `mvn test -Dtest=ClassName#methodName`

## Code Conventions (Non-Obvious)

*   **Unified API Response**: All controller methods MUST return a `com.xxx.common.vo.Result<T>` object. Use the static methods `Result.ok()` and `Result.fail()`.
*   **Unified Exception Handling**:
    *   For business logic errors, you MUST throw a `com.xxx.common.exception.CommonException`.
    *   This exception takes a `com.xxx.common.enums.CodesEnum` to specify the error code and message.
    *   A global handler (`GlobalExceptionHandler`) will format it into a standard `Result` response.
*   **Anti-Replay Attack Mechanism**:
    *   Some endpoints are protected against replay attacks using the `@RepeatSubmit` annotation.
    *   **Flow**:
        1.  The client must first call an endpoint like `/app/authed/token` to get a one-time token.
        2.  The client then sends the actual request with the token in the `X-Repeat-Token` HTTP header.
    *   When implementing a new feature that needs this protection, you must implement both the token generation and the protected endpoint.
