package co.ke.xently.http.logger.webflux;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

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

        return Mono.defer(() -> {
            var bodyCapture = new RequestBodyCapture();
            var requestLogged = new AtomicBoolean();
            Runnable logRequest = () -> {
                if (requestLogged.compareAndSet(false, true)) {
                    logger().logRequest(request.url(), request.method(), request.headers(), bodyCapture.body());
                }
            };

            var interceptedRequest = ClientRequest.from(request)
                    .body((outputMessage, context) -> {
                        var decorator = new ClientHttpRequestDecorator(outputMessage) {
                            @Override
                            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                return super.writeWith(Flux.from(body)
                                                .doOnNext(bodyCapture::append))
                                        .doOnSuccess(ignored -> logRequest.run());
                            }

                            @Override
                            public Mono<Void> writeAndFlushWith(
                                    Publisher<? extends Publisher<? extends DataBuffer>> body
                            ) {
                                var observedBody = Flux.from(body)
                                        .map(group -> Flux.from(group).doOnNext(bodyCapture::append));

                                return super.writeAndFlushWith(observedBody)
                                        .doOnSuccess(ignored -> logRequest.run());
                            }
                        };
                        return request.body().insert(decorator, context);
                    })
                    .build();

            return next.exchange(interceptedRequest)
                    .flatMap(response -> response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(bodyString -> {
                                logRequest.run();
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
                            }));
        });
    }

    private static final class RequestBodyCapture {
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        synchronized void append(DataBuffer dataBuffer) {
            var bytes = dataBuffer.toByteBuffer(dataBuffer.readPosition(), dataBuffer.readableByteCount());
            var capturedBytes = new byte[bytes.remaining()];
            bytes.get(capturedBytes);
            body.write(capturedBytes, 0, capturedBytes.length);
        }

        synchronized String body() {
            return body.toString(StandardCharsets.UTF_8);
        }
    }
}
