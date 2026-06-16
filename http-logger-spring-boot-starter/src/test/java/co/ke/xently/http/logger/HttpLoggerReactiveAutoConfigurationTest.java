package co.ke.xently.http.logger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggerReactiveAutoConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HttpLoggerAutoConfiguration.class,
                    WebClientAutoConfiguration.class
            ));

    @Test
    void shouldRegisterWebClientBuilderWhenAutoConfigurationIsEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WebClient.Builder.class);
            assertThat(context).hasSingleBean(WebClientCustomizer.class);
        });
    }
}
