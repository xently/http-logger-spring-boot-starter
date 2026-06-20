# HTTP Logger Spring Boot Starter

![Maven Central Version](https://img.shields.io/maven-central/v/ke.co.xently/http-logger-spring-boot-starter)
[![Java CI with Maven](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml)
[![Maven Central Publish](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml)

A Spring Boot starter that logs outgoing HTTP requests and responses for `RestClient` (WebMvc), `WebClient` (WebFlux), and `WebServiceTemplate` (SOAP).

- **Spring Boot**: 4.x
- **Java**: 21 (LTS)
- **Web Support**: WebMvc (Servlet) & WebFlux (Reactive)
- **SOAP Support**: Spring Web Services (`WebServiceTemplate`)

## Features

- **Automatic Logging**: Log URI, method, headers, and body for outgoing HTTP calls.
- **SOAP Logging**: Log SOAP Action and XML payload for outgoing SOAP calls.
- **Dual Support**: Works with both `RestClient`, `WebClient` and `WebServiceTemplate`.
- **Configurable**: Easily enable or disable logging via properties.

## Modules

- `http-logger-spring-boot-starter`: The autoconfiguration starter providing the HTTP logging interceptors and filters.
- `demo-webmvc`: Demo application using Spring WebMvc and RestClient.
- `demo-webflux`: Demo application using Spring WebFlux and WebClient.

## Quick Start

### Requirements
- Java 21
- Maven 3.9+

### Run all tests
```bash
./mvnw test
```

### Run the Demo Applications
To run the WebMvc demo:
```bash
./mvnw -pl demo-webmvc spring-boot:run
```

To run the WebFlux demo:
```bash
./mvnw -pl demo-webflux spring-boot:run
```

## Configuration

The starter can be configured using the `log.http` prefix in your `application.yml` or `application.properties`.

```yaml
log:
  http:
    enabled: true # Whether to enable HTTP logging. Default is false.
```

## Security and Performance Considerations (Production Settings)

While this library is useful for debugging, logging full request and response bodies in production environments carries several risks:

### 1. Security and Privacy Risks
- **Sensitive Data Exposure**: Request and response bodies often contain Personally Identifiable Information (PII), credentials, API keys, or session tokens. Logging these in plain text can lead to data breaches if logs are not properly secured or if they are sent to external log aggregators.
- **Compliance**: Many regulations (e.g., GDPR, PCI-DSS) strictly prohibit the logging of sensitive data.

### 2. Performance Impacts
- **Memory Consumption**: To log the body of a request or response, the library must buffer the entire payload into memory. For large files or high-concurrency environments, this can significantly increase memory usage and potentially lead to `OutOfMemoryError`.
- **Latency**: Buffering and converting large payloads to strings adds processing time to every outgoing request.

### 3. Storage and Cost
- **Log Volume**: Logging every request and response body can quickly consume large amounts of disk space or exceed log ingestion quotas in cloud monitoring services, leading to increased operational costs.

### Recommendation
It is recommended to **disable** this library in production environments unless strictly necessary for short-term troubleshooting. To disable it, set the following property:

```yaml
log:
  http:
    enabled: false
```
