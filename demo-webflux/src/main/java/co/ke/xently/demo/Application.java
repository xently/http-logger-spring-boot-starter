package co.ke.xently.demo;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.reactive.ClientHttpConnector;
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
    @Lazy
    @ConditionalOnBooleanProperty(name = "settings.insecure.http.enabled")
    ClientHttpConnector clientHttpConnector(ResourceLoader resourceLoader,
                                            ObjectProvider<ClientHttpConnectorBuilder<?>> clientHttpConnectorBuilder,
                                            ObjectProvider<HttpClientSettings> httpClientSettings) {
        try {
            var sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            var httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            return new ReactorClientHttpConnector(httpClient);
        } catch (SSLException e) {
            log.error("Failed to create SSL context", e);
            log.info("Falling back to default (class-loader-based) connector...");
            var connector = clientHttpConnectorBuilder
                    .getIfAvailable(() -> ClientHttpConnectorBuilder.detect(resourceLoader.getClassLoader()))
                    .build(httpClientSettings.getIfAvailable(HttpClientSettings::defaults));
            log.info("Using (class-loader-based) connector: {}", connector.getClass().getName());
            return connector;
        }
    }
}
