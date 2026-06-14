package co.ke.xently.http.logger.webmvc;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@NullMarked
public record HttpLoggerRequestInterceptor(
        HttpLoggerProperties properties,
        HttpLogger logger
) implements ClientHttpRequestInterceptor {

    /**
     * Intercepts and logs outgoing HTTP requests.
     *
     * @param request   The request to intercept.
     * @param body      The request body.
     * @param execution The request execution context.
     * @return The client response.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var canLog = properties().isEnabled();
        if (canLog) {
            logger().logRequest(request.getURI(), request.getMethod(), request.getHeaders(), new String(body));
        }
        var response = execution.execute(request, body);
        if (canLog) {
            byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
            logger().logResponse(
                    request.getURI(),
                    request.getMethod(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    new String(responseBody)
            );
            return new BufferingClientHttpResponseWrapper(response, responseBody);
        }
        return response;
    }

    private record BufferingClientHttpResponseWrapper(
            ClientHttpResponse response,
            byte[] body
    ) implements ClientHttpResponse {
        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }
}