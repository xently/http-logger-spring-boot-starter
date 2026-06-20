package co.ke.xently.demo;

import co.ke.xently.http.logger.HttpLoggerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "settings.insecure.http.enabled")
    public ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder(
            ResourceLoader resourceLoader,
            HttpLoggerProperties properties
    ) {
        return settings -> {
            ClientHttpRequestFactory requestFactory;
            try {
                var sslContext = SSLContextBuilder.create()
                        .loadTrustMaterial((chain, authType) -> true)
                        .build();

                var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
                        .build();

                var httpClientBuilder = HttpClients.custom()
                        .setConnectionManager(connectionManager);
                if (properties.isEnabled()) {
                    httpClientBuilder.disableContentCompression(); // Added to avoid issues with some servers during testing
                }
                var httpClient = httpClientBuilder.build();

                requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                log.error("Failed to create SSL context", e);
                log.info("Falling back to default (class-loader-based) request factory...");
                var classLoader = resourceLoader.getClassLoader();
                var builder = ClientHttpRequestFactoryBuilder.detect(classLoader);
                requestFactory = builder.build(settings);
                log.info("Using (class-loader-based) request factory: {}", requestFactory.getClass().getName());
            }
            if (!properties.isEnabled()) {
                return requestFactory;
            }
            return new BufferingClientHttpRequestFactory(requestFactory);
        };
    }
}
