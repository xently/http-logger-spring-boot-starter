package co.ke.xently.demo;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLogger;
import co.ke.xently.http.logger.webflux.HttpLoggerFilter;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Slf4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "settings.insecure.http.enabled")
    public WebClientCustomizer insecureSslWebClientCustomizer(HttpLoggerProperties properties) {
        return webClientBuilder -> {
            try {
                var sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                var httpClient = HttpClient.create()
                        .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

                webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
                if (properties.isEnabled()) {
                    webClientBuilder
                            .filter(new HttpLoggerFilter(properties, new HttpLogger(request -> log)));
                }
            } catch (SSLException e) {
                throw new IllegalStateException("Failed to configure insecure SSL for WebClient", e);
            }
        };
    }
}
