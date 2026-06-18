# HTTP Logger Spring Boot Starter
## Effortless HTTP Client Logging

---

## The Problem
- **Visibility**: What exactly are my `RestClient`, `WebClient`, or `WebServiceTemplate` instances sending and receiving?
- **Debugging**: Identifying failed or slow external API calls without complicated logging configurations.
- **Traceability**: Seeing the full request/response cycle for outgoing calls in the console.
- **Inconsistency**: Different logging formats and strategies across multiple microservices.

---

## The Solution
### **HTTP Logger Spring Boot Starter**

- **Automatic**: Automatically registers interceptors/filters for `RestClient.Builder` and `WebClient.Builder`.
- **Transparent**: Logs request URI, Method, Headers, and Body.
- **Complete**: Logs response Status Code, Headers, and Body.
- **Universal**: Supports **Spring WebMvc** (`RestClient`), **Spring WebFlux** (`WebClient`), and **Spring Web Services** (`WebServiceTemplate`).

---

## Key Features

- ✅ **Zero Code Changes**: Auto-configuration kicks in as soon as the starter is added.
- ✅ **Comprehensive Logging**: Captures all HTTP methods (GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD).
- ✅ **Configurable**: Easily enable/disable via properties.
- ✅ **Reactive Ready**: Fully supports non-blocking `WebClient` logging.
- ✅ **SOAP Ready**: Supports `WebServiceTemplate` with automatic XML payload capture.

---

## Quick Start: Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ke.co.xently</groupId>
    <artifactId>http-logger-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

*Requirements: Java 21+ and Spring Boot 3.x*

---

## How It Works (The Result)

A simple `GET` request using `RestClient`:
```java
restClient.get().uri("/posts/1").retrieve().body(Post.class);
```

The Console Output:
```text
======================================================START REQUEST======================================================
[URI]: https://jsonplaceholder.typicode.com/posts/1
[METHOD]: GET
[HEADERS]:
	Accept: application/json, application/*+json
[BODY]:

=======================================================END REQUEST=======================================================

======================================================START RESPONSE======================================================
[URI]: https://jsonplaceholder.typicode.com/posts/1
[METHOD]: GET
[STATUS CODE]: 200 OK
[HEADERS]:
	Content-Type: application/json; charset=utf-8
	...
[BODY]:
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum..."
}
=======================================================END RESPONSE=======================================================
```

---

## Configuration

Customize behavior in `application.yml`:

```yaml
log:
  http:
    enabled: true  # Default: true
```

---

## Why Use This?

1. **Standardization**: Use the same HTTP logging format across all your services.
2. **Developer Experience**: Instant feedback on external API interactions during development.
3. **No Boilerplate**: No need to manually add interceptors to every client builder.
4. **Lightweight**: Minimal overhead, focuses strictly on logging.

---

## Questions?
### Get started today!
Check out the `demo-webmvc` and `demo-webflux` modules in the repo for full CRUD examples using JSONPlaceholder and SOAP conversion examples.
