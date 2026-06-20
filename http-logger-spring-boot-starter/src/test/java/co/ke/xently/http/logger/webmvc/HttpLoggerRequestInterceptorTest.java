package co.ke.xently.http.logger.webmvc;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class HttpLoggerRequestInterceptorTest {
    private final Logger logger = LoggerFactory.getLogger(HttpLoggerRequestInterceptorTest.class);

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse response;

    @Test
    void shouldLogRequestDetailsWhenLogLevelIsEnabled(CapturedOutput output) throws IOException {
        var properties = new HttpLoggerProperties(true);
        var interceptor = new HttpLoggerRequestInterceptor(properties, new HttpLogger(request -> logger));

        byte[] body = "test body".getBytes();
        var headers = new HttpHeaders(
                MultiValueMap.fromSingleValue(
                        Map.of(
                                "Authorization", "Bearer very long token value base64 encoded",
                                "Cookie", "Value1",
                                "Test-Header", "Value1"
                        )
                )
        );

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("http://localhost"));
        when(request.getHeaders()).thenReturn(headers);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("test response body".getBytes(StandardCharsets.UTF_8)));
        when(execution.execute(request, body)).thenReturn(response);

        var result = interceptor.intercept(request, body, execution);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(execution).execute(request, body);
        assertThat(output).contains(
                "Authorization: Bearer very long token value base64 encoded",
                "Cookie: Value1",
                "Test-Header: Value1"
        );
    }

    @Test
    void shouldNotLogRequestDetailsWhenLogLevelIsDisabled(CapturedOutput output) throws IOException {
        var properties = new HttpLoggerProperties();
        var interceptor = new HttpLoggerRequestInterceptor(properties, new HttpLogger(request -> logger));

        byte[] body = "test body".getBytes();
        when(execution.execute(request, body)).thenReturn(response);

        var result = interceptor.intercept(request, body, execution);

        assertThat(result).isNotNull();
        verify(execution).execute(request, body);
        assertThat(output).doesNotContain("[URI]");
    }
}