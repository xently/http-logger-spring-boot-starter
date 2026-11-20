package co.ke.xently.http.logger;

import co.ke.xently.http.logger.utils.HttpLoggerInterceptor;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.service.HttpServiceClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class HttpLoggerOAuth2RestClientHttpServiceGroupTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HttpLoggerAutoConfiguration.class,
                    HttpServiceClientPropertiesAutoConfiguration.class,
                    RestClientAutoConfiguration.class,
                    HttpServiceClientAutoConfiguration.class
            ));

    @Test
    void shouldLogOAuth2BearerHeaderAddedByServiceGroupConfigurer(CapturedOutput output) throws IOException {
        var receivedAuthorization = new AtomicReference<String>();
        var server = startServer(receivedAuthorization);

        try {
            contextRunner
                    .withUserConfiguration(OAuthServiceConfiguration.class)
                    .withPropertyValues(
                            "log.http.enabled=true",
                            "test.service.base-url=http://127.0.0.1:" + server.getAddress().getPort()
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean(SecureHttpService.class).get()).isEqualTo("ok");

                        var authorization = "Bearer test-access-token";
                        assertThat(context.getBean(AuditCapture.class).authorizedClientManagerCalls).isEqualTo(1);
                        assertThat(receivedAuthorization).hasValue(authorization);
                        assertThat(context.getBean(AuditCapture.class).requestHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                                .isEqualTo(authorization);
                        assertThat(output).contains("Authorization: " + authorization);
                        assertThat(output.getOut().split("START REQUEST", -1)).hasSize(2);
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldLogNonOAuthServiceGroupRequests(CapturedOutput output) throws IOException {
        var receivedAuthorization = new AtomicReference<String>();
        var server = startServer(receivedAuthorization);

        try {
            contextRunner
                    .withUserConfiguration(PlainServiceConfiguration.class)
                    .withPropertyValues(
                            "log.http.enabled=true",
                            "test.service.base-url=http://127.0.0.1:" + server.getAddress().getPort()
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean(PlainHttpService.class).get()).isEqualTo("ok");
                        assertThat(receivedAuthorization).hasValue(null);
                        assertThat(context.getBean(AuditCapture.class).requestHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                                .isNull();
                        assertThat(output.getOut()).contains("START REQUEST").doesNotContain("Authorization: Bearer");
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldNotAddLoggingToServiceGroupWhenLoggingIsDisabled(CapturedOutput output) throws IOException {
        var receivedAuthorization = new AtomicReference<String>();
        var server = startServer(receivedAuthorization);

        try {
            contextRunner
                    .withUserConfiguration(PlainServiceConfiguration.class)
                    .withPropertyValues("log.http.enabled=false", "test.service.base-url=http://127.0.0.1:" + server.getAddress().getPort())
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean(PlainHttpService.class).get()).isEqualTo("ok");
                        assertThat(output.getOut()).doesNotContain("START REQUEST");
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldNotAddLoggingToGroupBuilderWithoutLoggerInterceptor(CapturedOutput output) throws IOException {
        var receivedAuthorization = new AtomicReference<String>();
        var server = startServer(receivedAuthorization);

        try {
            contextRunner
                    .withUserConfiguration(ServiceGroupWithoutLoggerConfiguration.class)
                    .withPropertyValues(
                            "log.http.enabled=true",
                            "test.service.base-url=http://127.0.0.1:" + server.getAddress().getPort()
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBean(PlainHttpService.class).get()).isEqualTo("ok");
                        assertThat(output.getOut()).doesNotContain("START REQUEST");
                    });
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startServer(AtomicReference<String> receivedAuthorization) throws IOException {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/resource", exchange -> {
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            var response = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (var responseBody = exchange.getResponseBody()) {
                responseBody.write(response);
            }
        });
        server.start();
        return server;
    }

    interface SecureHttpService {
        @GetExchange("/resource")
        @ClientRegistrationId("test")
        String get();
    }

    interface PlainHttpService {
        @GetExchange("/resource")
        String get();
    }

    @Configuration(proxyBeanMethods = false)
    @ImportHttpServices(group = "oauth", types = SecureHttpService.class)
    static class OAuthServiceConfiguration {
        @Bean
        OAuth2AuthorizedClientManager authorizedClientManager(AuditCapture capture) {
            var registration = ClientRegistration.withRegistrationId("test")
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .tokenUri("https://example.invalid/token")
                    .build();
            var accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "test-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(300)
            );
            var authorizedClient = new OAuth2AuthorizedClient(registration, "test-principal", accessToken);
            return authorizeRequest -> {
                capture.authorizedClientManagerCalls++;
                return authorizedClient;
            };
        }

        @Bean
        OAuth2RestClientHttpServiceGroupConfigurer oauth2Configurer(OAuth2AuthorizedClientManager manager) {
            return OAuth2RestClientHttpServiceGroupConfigurer.from(manager);
        }

        @Bean
        RestClientHttpServiceGroupConfigurer baseUrlConfigurer(
                @Value("${test.service.base-url}") String baseUrl
        ) {
            return groups -> groups.forEachClient((group, builder) -> builder.baseUrl(baseUrl));
        }

        @Bean
        AuditCapture auditCapture() {
            return new AuditCapture();
        }

        @Bean
        HttpLoggerInterceptor httpLoggerInterceptor(AuditCapture capture) {
            return new HttpLoggerInterceptor() {
                @Override
                public void request(URI uri, HttpMethod method, HttpHeaders headers, String body) {
                    capture.requestHeaders = new HttpHeaders();
                    capture.requestHeaders.putAll(headers);
                }

                @Override
                public void response(URI uri, HttpMethod method, HttpStatusCode status, HttpHeaders headers, String body) {
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportHttpServices(group = "plain", types = PlainHttpService.class)
    static class PlainServiceConfiguration {
        @Bean
        RestClientHttpServiceGroupConfigurer baseUrlConfigurer(
                @Value("${test.service.base-url}") String baseUrl
        ) {
            return groups -> groups.forEachClient((group, builder) -> builder.baseUrl(baseUrl));
        }

        @Bean
        AuditCapture auditCapture() {
            return new AuditCapture();
        }

        @Bean
        HttpLoggerInterceptor httpLoggerInterceptor(AuditCapture capture) {
            return new HttpLoggerInterceptor() {
                @Override
                public void request(URI uri, HttpMethod method, HttpHeaders headers, String body) {
                    capture.requestHeaders = new HttpHeaders();
                    capture.requestHeaders.putAll(headers);
                }

                @Override
                public void response(URI uri, HttpMethod method, HttpStatusCode status, HttpHeaders headers, String body) {
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportHttpServices(group = "plain", types = PlainHttpService.class)
    static class ServiceGroupWithoutLoggerConfiguration {
        @Bean
        RestClientCustomizer restClientCustomizer() {
            return builder -> {
            };
        }

        @Bean
        RestClientHttpServiceGroupConfigurer baseUrlConfigurer(
                @Value("${test.service.base-url}") String baseUrl
        ) {
            return groups -> groups.forEachClient((group, builder) -> builder.baseUrl(baseUrl));
        }
    }

    static class AuditCapture {
        private HttpHeaders requestHeaders = new HttpHeaders();
        private int authorizedClientManagerCalls;
    }
}