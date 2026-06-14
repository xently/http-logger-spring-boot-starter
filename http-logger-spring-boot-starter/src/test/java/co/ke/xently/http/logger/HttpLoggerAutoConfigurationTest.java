package co.ke.xently.http.logger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggerAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpLoggerAutoConfiguration.class));

    @Test
    void shouldRegisterRestClientBuilderWhenAutoConfigurationIsEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestClient.Builder.class);
        });
    }

    @Test
    void shouldNotRegisterRestClientBuilderWhenAutoConfigurationIsDisabled() {
        contextRunner.withPropertyValues("log.http.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RestClient.Builder.class);
                });
    }
}
