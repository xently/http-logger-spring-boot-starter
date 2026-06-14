package co.ke.xently.request.timer.utils;

import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class HttpLogger {
    private static final Level logLevel = Level.INFO;
    private final HttpLoggerProvider loggerProvider;

    public HttpLogger(HttpLoggerProvider loggerProvider) {
        this.loggerProvider = loggerProvider;
    }

    public void logRequest(URI uri, HttpMethod method, HttpHeaders headers, String body) {
        final var logger = loggerProvider.get(new Request(uri, method));
        final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
        List<String> logOutput = new ArrayList<>();
        logOutput.add("\n======================================================START REQUEST======================================================");
        logOutput.add("[URI]: %s".formatted(uri));
        logOutput.add("[METHOD]: %s".formatted(method));
        logOutput.add("[HEADERS]:");
        headers.forEach((name, values) -> logOutput.add(getHeaderLogEntry(name, values)));
        logOutput.add("[BODY]:");
        logOutput.add(body);
        logOutput.add("=======================================================END REQUEST=======================================================");
        loggingEventBuilder.log(String.join("\n", logOutput));
    }

    public void logResponse(URI uri, HttpMethod method, HttpStatusCode httpStatus, HttpHeaders headers, String body) {
        final var logger = loggerProvider.get(new Request(uri, method));
        final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
        List<String> logOutput = new ArrayList<>();
        logOutput.add("\n======================================================START RESPONSE======================================================");
        logOutput.add("[URI]: %s".formatted(uri));
        logOutput.add("[METHOD]: %s".formatted(method));
        logOutput.add("[STATUS CODE]: %s".formatted(httpStatus));
        logOutput.add("[HEADERS]:");
        headers
                .forEach((name, values) -> logOutput.add(getHeaderLogEntry(name, values)));
        logOutput.add("[BODY]:");
        logOutput.add(body);
        logOutput.add("=======================================================END RESPONSE=======================================================");
        loggingEventBuilder.log(String.join("\n", logOutput));
    }

    private String getHeaderLogEntry(String name, List<String> values) {
        var headerValue = String.join(", ", values);
        return "\t%s: %s".formatted(name, headerValue);
    }
}
