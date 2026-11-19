package co.ke.xently.http.logger.webflux;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@Slf4j
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class HttpLoggerFilterTest {
    @Mock
    private ClientRequest request;

    @Mock
    private ExchangeFunction next;

    @Mock
    private ClientResponse response;

    @Test
    void shouldLogRequestAndResponseDetailsWhenEnabled(CapturedOutput output) {
        var properties = new HttpLoggerProperties(true, true);
        var filter = new HttpLoggerFilter(properties, new HttpLogger(properties.isPrettyPrint(), request -> log));

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
    void shouldPrettyPrintJsonBodiesWithoutChangingTheResponse(CapturedOutput output) {
        var properties = new HttpLoggerProperties(true, true);
        var filter = new HttpLoggerFilter(properties, new HttpLogger(properties.isPrettyPrint(), request -> log));
        var responseBody = "{\"result\":true}";
        WebClient webClient = WebClient.builder()
                .filter(filter)
                .exchangeFunction(request -> Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK)
                        .body(responseBody)
                        .build()))
                .build();

        var returnedBody = webClient.post()
                .uri("http://localhost/test")
                .header("Content-Type", "application/json")
                .bodyValue("{\"outer\":{\"count\":2}}")
                .exchangeToMono(response -> response.bodyToMono(String.class))
                .block();

        assertThat(returnedBody).isEqualTo(responseBody);
        assertThat(output).contains("\n  \"result\"")
                .doesNotContain(responseBody);
    }

    @Test
    void shouldNotLogWhenDisabled(CapturedOutput output) {
        var properties = new HttpLoggerProperties();
        var filter = new HttpLoggerFilter(properties, new HttpLogger(properties.isPrettyPrint(), request -> log));

        when(next.exchange(request)).thenReturn(Mono.just(response));

        filter.filter(request, next).block();

        assertThat(output).doesNotContain("START REQUEST");
        assertThat(output).doesNotContain("START RESPONSE");
    }
}
