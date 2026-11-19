package co.ke.xently.http.logger.utils;

import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class HttpLogger {
    private static final Level logLevel = Level.INFO;
    private final boolean prettyPrint;
    private final HttpLoggerProvider provider;
    private final HttpLoggerInterceptor interceptor;

    public HttpLogger(boolean prettyPrint, HttpLoggerProvider provider, HttpLoggerInterceptor interceptor) {
        this.prettyPrint = prettyPrint;
        this.provider = provider;
        this.interceptor = interceptor;
    }

    public HttpLogger(boolean prettyPrint, HttpLoggerProvider provider) {
        this(prettyPrint, provider, HttpLoggerInterceptor.DEFAULT);
    }

    public void logRequest(URI uri, HttpMethod method, HttpHeaders headers, String body) {
        final var logger = provider.get(new Request(uri, method));
        final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
        List<String> logOutput = new ArrayList<>();
        logOutput.add("\n======================================================START REQUEST======================================================");
        logOutput.add("[URI]: %s".formatted(uri));
        logOutput.add("[METHOD]: %s".formatted(method));
        logOutput.add("[HEADERS]:");
        headers.forEach((name, values) -> logOutput.add(getHeaderLogEntry(name, values)));
        logOutput.add("[BODY]:");
        var payload = prettyPrint ? BodyFormatter.format(body) : body;
        logOutput.add(payload);
        logOutput.add("=======================================================END REQUEST=======================================================");
        interceptor.request(uri, method, headers, payload);
        loggingEventBuilder.log(String.join("\n", logOutput));
    }

    public void logResponse(URI uri, HttpMethod method, HttpStatusCode httpStatus, HttpHeaders headers, String body) {
        final var logger = provider.get(new Request(uri, method));
        final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
        List<String> logOutput = new ArrayList<>();
        logOutput.add("\n======================================================START RESPONSE======================================================");
        logOutput.add("[URI]: %s".formatted(uri));
        logOutput.add("[METHOD]: %s".formatted(method));
        logOutput.add("[STATUS CODE]: %s".formatted(httpStatus));
        logOutput.add("[HEADERS]:");
        headers.forEach((name, values) -> logOutput.add(getHeaderLogEntry(name, values)));
        logOutput.add("[BODY]:");
        var payload = prettyPrint ? BodyFormatter.format(body) : body;
        logOutput.add(payload);
        logOutput.add("=======================================================END RESPONSE=======================================================");
        interceptor.response(uri, method, httpStatus, headers, payload);
        loggingEventBuilder.log(String.join("\n", logOutput));
    }

    private String getHeaderLogEntry(String name, List<String> values) {
        var headerValue = String.join(", ", values);
        return "\t%s: %s".formatted(name, headerValue);
    }
}
