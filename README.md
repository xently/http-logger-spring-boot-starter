# HTTP Logger Spring Boot Starter

![Maven Central Version](https://img.shields.io/maven-central/v/ke.co.xently/http-logger-spring-boot-starter)
[![Java CI with Maven](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml)
[![Maven Central Publish](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml)

A Spring Boot starter that logs outgoing HTTP requests and responses for `RestClient` (WebMvc), `WebClient` (WebFlux), and `WebServiceTemplate` (SOAP).

- **Spring Boot**: 3.x
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
log.http:
  # Whether to enable HTTP logging. Default is true.
  enabled: true
```
