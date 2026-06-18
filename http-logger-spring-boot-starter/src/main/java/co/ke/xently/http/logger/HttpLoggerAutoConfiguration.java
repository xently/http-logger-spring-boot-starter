package co.ke.xently.http.logger;

import co.ke.xently.http.logger.utils.HttpLogger;
import co.ke.xently.http.logger.utils.HttpLoggerProvider;
import co.ke.xently.http.logger.webflux.HttpLoggerFilter;
import co.ke.xently.http.logger.webmvc.HttpLoggerRequestInterceptor;
import co.ke.xently.http.logger.ws.LogSoapClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ws.client.core.WebServiceTemplate;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(HttpLoggerProperties.class)
public class HttpLoggerAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication
    @ConditionalOnBooleanProperty(name = "log.http.enabled", matchIfMissing = true)
    static class HttpLoggerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public HttpLoggerProvider httpLoggerProvider() {
            return request -> log;
        }

        @Bean
        public HttpLogger httpLogger(HttpLoggerProvider loggerProvider) {
            return new HttpLogger(loggerProvider);
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(WebServiceTemplate.class)
        static class SoapLoggerConfiguration {
            @Bean
            public LogSoapClientInterceptor logSoapClientInterceptor(
                    HttpLoggerProperties properties,
                    HttpLoggerProvider loggerProvider
            ) {
                return new LogSoapClientInterceptor(properties, loggerProvider);
            }

            @Bean
            @ConditionalOnMissingBean
            public WebServiceTemplateBuilder webServiceTemplateBuilder(LogSoapClientInterceptor interceptor) {
                return new WebServiceTemplateBuilder()
                        .additionalInterceptors(interceptor);
            }

            @Bean
            @ConditionalOnMissingBean
            public WebServiceTemplate webServiceTemplate(WebServiceTemplateBuilder builder) {
                return builder.build();
            }
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        @ConditionalOnClass(RestClient.class)
        static class ServletHttpLoggerConfiguration {
            @Bean
            public HttpLoggerRequestInterceptor httpLoggerRequestInterceptor(
                    HttpLoggerProperties properties,
                    HttpLogger httpLogger
            ) {
                return new HttpLoggerRequestInterceptor(properties, httpLogger);
            }

            @Bean
            @ConditionalOnMissingBean
            @ConditionalOnMissingClass(value = {"org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration"})
            public RestClient.Builder restClientBuilder(
                    HttpLoggerRequestInterceptor interceptor,
                    HttpLoggerProperties properties
            ) {
                var builder = RestClient.builder();
                if (!properties.isEnabled()) {
                    return builder;
                }
                return builder.requestInterceptor(interceptor);
            }

            @Configuration(proxyBeanMethods = false)
            @ConditionalOnClass(name = {"org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration"})
            static class RestClientCustomizerConfiguration {
                @Bean
                @ConditionalOnMissingBean
                public ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder(ResourceLoader resourceLoader) {
                    var classLoader = resourceLoader.getClassLoader();
                    var builder = ClientHttpRequestFactoryBuilder.detect(classLoader);
                    return settings -> new BufferingClientHttpRequestFactory(builder.build(settings));
                }

                @Bean
                @ConditionalOnMissingBean
                public RestClientCustomizer restClientCustomizer(HttpLoggerRequestInterceptor interceptor) {
                    return restClientBuilder -> restClientBuilder.requestInterceptor(interceptor);
                }
            }
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
        @ConditionalOnClass(WebClient.class)
        static class ReactiveHttpLoggerConfiguration {
            @Bean
            public HttpLoggerFilter filter(HttpLoggerProperties properties, HttpLogger httpLogger) {
                return new HttpLoggerFilter(properties, httpLogger);
            }

            @Bean
            @ConditionalOnMissingBean
            @ConditionalOnMissingClass(value = {"org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration"})
            public WebClient.Builder webClientBuilder(HttpLoggerProperties properties, HttpLoggerFilter loggerFilter) {
                var builder = WebClient.builder();
                if (!properties.isEnabled()) {
                    return builder;
                }
                return builder.filter(loggerFilter);
            }

            @Configuration(proxyBeanMethods = false)
            @ConditionalOnClass(name = {"org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration"})
            static class WebClientCustomizerConfiguration {
                @Bean
                @ConditionalOnMissingBean
                public WebClientCustomizer webClientCustomizer(HttpLoggerProperties properties, HttpLoggerFilter loggerFilter) {
                    return webClientBuilder -> {
                        if (properties.isEnabled()) webClientBuilder.filter(loggerFilter);
                    };
                }
            }
        }
    }
}
