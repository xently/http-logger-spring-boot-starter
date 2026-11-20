# HTTP Logger Spring Boot Starter

![Maven Central Version](https://img.shields.io/maven-central/v/ke.co.xently/http-logger-spring-boot-starter)
[![Java CI with Maven](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven.yml)
[![Maven Central Publish](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/xently/http-logger-spring-boot-starter/actions/workflows/maven-publish.yml)

A Spring Boot starter that logs outgoing HTTP requests and responses for `RestClient` (WebMvc), `WebClient` (WebFlux),
and `WebServiceTemplate` (SOAP).

- **Spring Boot**: 4.x
- **Java**: 21 (LTS)
- **Web Support**: WebMvc (Servlet) & WebFlux (Reactive)
- **SOAP Support**: Spring Web Services (`WebServiceTemplate`)

## Features

- **Automatic Logging**: Log URI, method, headers, and body for outgoing HTTP calls.
- **SOAP Logging**: Log SOAP Action and XML payload for outgoing SOAP calls.
- **Readable Payloads**: JSON and XML bodies can be pretty-printed in logs; plain text, empty, malformed, or unsafe XML
  bodies are kept unchanged.
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
    pretty-print: true # Whether to enable pretty-printing of the body of the request or response. Default is true.
```

In a web application, `RestClient` and `WebClient` logging are configured independently based on whether each client is
on the classpath, rather than on whether the application uses WebMvc or WebFlux. For example, a WebMvc application can
use a logged `WebClient`, and a WebFlux application can use a logged `RestClient`, provided the corresponding client
dependencies are available. Both can be configured in the same application. Existing beans may cause individual
autoconfigured builders or customisers to back off; non-web applications are not auto-configured by this starter.

## Persisting Requests and Responses for Audit

The starter exposes `HttpLoggerInterceptor` and `SoapLoggerInterceptor` callback interfaces for applications that need
to persist outgoing-call data, for example in an audit database. Register a Spring bean implementing the relevant
interface instead of adding another HTTP or SOAP client interceptor just to capture the same request and response
bodies. When `log.http.enabled` is `true`, the starter invokes your bean as part of its existing logging flow; if you do
not provide one, the starter uses a no-op implementation. Database schema, persistence dependencies, and retention are
the application's responsibility.

For `RestClient`-backed HTTP service groups, the starter places its logging interceptor after the group configurers so
that headers added by `OAuth2RestClientHttpServiceGroupConfigurer` are included in logs and audit callbacks. If a custom
group configurer adds an authentication interceptor after the starter's configurer, ensure the logging interceptor is
moved to the end of that group's interceptor list as well. Custom clients must also include the starter interceptor and
place it after any interceptor that adds headers they need logged.

For example, the following PostgreSQL table can store one row for each request or response callback:

```sql
CREATE TABLE outbound_audit_event
(
    id          bigserial PRIMARY KEY,
    protocol    varchar(8)  NOT NULL,
    phase       varchar(8)  NOT NULL,
    request_uri text,
    http_method varchar(16),
    http_status integer,
    soap_action text,
    headers     text,
    body        text,
    created_at  timestamptz NOT NULL DEFAULT current_timestamp
);
```

This example bean implements both callback interfaces and writes the supplied values using Spring's `JdbcTemplate`:

```java
import co.ke.xently.http.logger.utils.HttpLoggerInterceptor;
import co.ke.xently.http.logger.utils.SoapLoggerInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class OutboundAuditCallbacks implements HttpLoggerInterceptor, SoapLoggerInterceptor {
    private final JdbcTemplate jdbcTemplate;

    public OutboundAuditCallbacks(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void request(URI uri, HttpMethod method, HttpHeaders headers, String body) {
        save("HTTP", "REQUEST", uri.toString(), method.name(), null, null, headers.toString(), body);
    }

    @Override
    public void response(URI uri, HttpMethod method, HttpStatusCode status, HttpHeaders headers, String body) {
        save("HTTP", "RESPONSE", uri.toString(), method.name(), status.value(), null, headers.toString(), body);
    }

    @Override
    public void request(String action, String body) {
        save("SOAP", "REQUEST", null, null, null, action, null, body);
    }

    @Override
    public void response(String body) {
        save("SOAP", "RESPONSE", null, null, null, null, null, body);
    }

    private void save(String protocol, String phase, String uri, String method, Integer status,
                      String action, String headers, String body) {
        jdbcTemplate.update("""
                INSERT INTO outbound_audit_event
                    (protocol, phase, request_uri, http_method, http_status, soap_action, headers, body)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, protocol, phase, uri, method, status, action, headers, body);
    }
}
```

The callback body is the string passed to the logger, so it is pretty-printed when `log.http.pretty-print` is enabled.
HTTP callbacks receive URI, method, headers, and (for responses) status; SOAP request callbacks receive the SOAP action
and payload, while SOAP response callbacks receive only the payload. SOAP faults are also delivered through the SOAP
`response` callback. The callback signatures do not include a shared request/response correlation ID, so add your own
correlation mechanism if the audit store needs to pair events.

Callbacks run synchronously in the outgoing-call flow. A persistence exception can therefore fail the call, and a failed
HTTP exchange may not have a response callback. In particular, do not perform blocking `JdbcTemplate` work directly in a
`WebClient` callback, which may run on a reactive event-loop thread; enqueue the audit event or offload persistence to
an appropriate scheduler. For `WebClient`, the request body may be reported as `(Unbuffered Reactive Stream)` when no
non-empty body was captured. The starter applies its hooks to its Boot-managed client builders; if you construct custom
clients or builders, or replace the relevant autoconfigured beans, ensure they use the starter's logging
filter/interceptor. Redact sensitive values before storing headers or payloads and apply suitable database access and
retention controls.

## Security and Performance Considerations (Production Settings)

While this library is useful for debugging, logging full request and response bodies in production environments carries
several risks:

### 1. Security and Privacy Risks

- **Sensitive Data Exposure**: Request and response bodies often contain Personally Identifiable Information (PII),
  credentials, API keys, or session tokens. Logging these in plain text can lead to data breaches if logs are not
  properly secured or if they are sent to external log aggregators.
- **Authorization Headers**: Request headers are logged and delivered to `HttpLoggerInterceptor` callbacks without
  redaction, including OAuth2 `Authorization: Bearer` tokens. Treat logs and audit callback storage as secret-bearing
  data, and redact or omit credentials before persisting them.
- **Compliance**: Many regulations (e.g., GDPR, PCI-DSS) strictly prohibit the logging of sensitive data.

### 2. Performance Impacts

- **Memory Consumption**: To log the body of a request or response, the library must buffer the entire payload into
  memory. For large files or high-concurrency environments, this can significantly increase memory usage and potentially
  lead to `OutOfMemoryError`.
- **Latency**: Buffering and converting large payloads to strings adds processing time to every outgoing request.

### 3. Storage and Cost

- **Log Volume**: Logging every request and response body can quickly consume large amounts of disk space or exceed log
  ingestion quotas in cloud monitoring services, leading to increased operational costs.

### Recommendation

It is recommended to **disable** this library in production environments unless strictly necessary for short-term
troubleshooting. To disable it, set the following property:

```yaml
log:
  http:
    enabled: false
    pretty-print: false
```
