package co.ke.xently.http.logger.webflux;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@NullMarked
public record HttpLoggerFilter(
        HttpLoggerProperties properties,
        HttpLogger logger
) implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        final var canLog = properties().isEnabled();
        if (!canLog) {
            return next.exchange(request);
        }

        var bodyReference = new AtomicReference<>("");

        var interceptedRequest = ClientRequest.from(request)
                .body((outputMessage, context) -> {
                    var decorator = new ClientHttpRequestDecorator(outputMessage) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            return DataBufferUtils.join(Flux.from(body))
                                    .flatMap(dataBuffer -> {
                                        var bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        DataBufferUtils.release(dataBuffer);
                                        var bodyString = new String(bytes, StandardCharsets.UTF_8);
                                        bodyReference.set(bodyString);
                                        return super.writeWith(Mono.just(outputMessage.bufferFactory().wrap(bytes)));
                                    });
                        }
                    };
                    return request.body().insert(decorator, context);
                })
                .build();

        return next.exchange(interceptedRequest)
                .flatMap(response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(bodyString -> {
                            logger().logRequest(
                                    request.url(),
                                    request.method(),
                                    request.headers(),
                                    bodyReference.get().isEmpty() ? "(Unbuffered Reactive Stream)" : bodyReference.get()
                            );
                            logger().logResponse(
                                    request.url(),
                                    request.method(),
                                    response.statusCode(),
                                    response.headers().asHttpHeaders(),
                                    bodyString
                            );
                            return Mono.just(response.mutate()
                                    .body(bodyString)
                                    .build());
                        }))
                .doOnTerminate(() -> {
                    // Ensure some form of logging happens even if response body processing fails
                });
    }
}
