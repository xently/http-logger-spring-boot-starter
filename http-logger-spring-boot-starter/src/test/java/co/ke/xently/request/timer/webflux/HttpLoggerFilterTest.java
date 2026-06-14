package co.ke.xently.request.timer.webflux;

import co.ke.xently.request.timer.HttpLoggerProperties;
import co.ke.xently.request.timer.utils.HttpLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class HttpLoggerFilterTest {
    private final Logger logger = LoggerFactory.getLogger(HttpLoggerFilterTest.class);

    @Mock
    private ClientRequest request;

    @Mock
    private ExchangeFunction next;

    @Mock
    private ClientResponse response;

    @Test
    void shouldLogRequestAndResponseDetailsWhenEnabled(CapturedOutput output) {
        var properties = HttpLoggerProperties.builder().enabled(true).build();
        var filter = new HttpLoggerFilter(properties, new HttpLogger(request -> logger));

        var httpHeaders = new HttpHeaders(
                MultiValueMap.fromSingleValue(
                        Map.of(
                                "Authorization", "Bearer very long token value base64 encoded",
                                "Cookie", "Value1",
                                "Test-Header", "Value1"
                        )
                )
        );

        WebClient webClient = WebClient.builder()
                .filter(filter)
                .exchangeFunction(req -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK)
                        .headers(h -> h.addAll(httpHeaders))
                        .body("test response body")
                        .build()))
                .build();

        webClient.post()
                .uri("http://localhost/test")
                .header("Test-Request-Header", "Value1")
                .bodyValue("test request body")
                .exchangeToMono(res -> res.bodyToMono(String.class))
                .block();

        assertThat(output).contains(
                "START REQUEST",
                "http://localhost/test",
                "POST",
                "Test-Request-Header: Value1",
                "END REQUEST",
                "START RESPONSE",
                "test response body",
                "END RESPONSE",
                "Authorization: Bearer very long token value base64 encoded",
                "Cookie: Value1",
                "Test-Header: Value1"
        );
    }

    @Test
    void shouldNotLogWhenDisabled(CapturedOutput output) {
        var properties = HttpLoggerProperties.builder().enabled(false).build();
        var filter = new HttpLoggerFilter(properties, new HttpLogger(request -> logger));

        when(next.exchange(request)).thenReturn(Mono.just(response));

        filter.filter(request, next).block();

        assertThat(output).doesNotContain("START REQUEST");
        assertThat(output).doesNotContain("START RESPONSE");
    }
}
