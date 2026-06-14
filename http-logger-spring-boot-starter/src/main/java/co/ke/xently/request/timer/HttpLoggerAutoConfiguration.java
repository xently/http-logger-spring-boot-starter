package co.ke.xently.request.timer;

import co.ke.xently.request.timer.utils.HttpLogger;
import co.ke.xently.request.timer.utils.HttpLoggerProvider;
import co.ke.xently.request.timer.webflux.HttpLoggerFilter;
import co.ke.xently.request.timer.webmvc.HttpLoggerRequestInterceptor;
import co.ke.xently.request.timer.ws.LogSoapClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ws.client.core.WebServiceTemplate;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(HttpLoggerProperties.class)
@ConditionalOnBooleanProperty(name = "log.http.enabled", matchIfMissing = true)
public class HttpLoggerAutoConfiguration {
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
        public ClientHttpRequestFactory clientHttpRequestFactory() {
            return new JdkClientHttpRequestFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public RestClient.Builder restClientBuilder(
                ClientHttpRequestFactory factory,
                HttpLoggerRequestInterceptor interceptor
        ) {
            var builder = RestClient.builder();
            try {
                builder.requestFactory(new BufferingClientHttpRequestFactory(factory));
            } catch (Exception e) {
                log.error("Failed to create HTTP request factory", e);
            }
            return builder.requestInterceptor(interceptor);
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
        public WebClient.Builder webClientBuilder(HttpLoggerFilter loggerFilter) {
            return WebClient.builder()
                    .filter(loggerFilter);
        }
    }
}
