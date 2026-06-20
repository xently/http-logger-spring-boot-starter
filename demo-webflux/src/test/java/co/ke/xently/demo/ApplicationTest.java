package co.ke.xently.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void shouldHaveWebClientCustomizerBean() {
        assertThat(context.containsBean("insecureSslWebClientCustomizer")).isTrue();
        assertThat(context.getBeansOfType(WebClientCustomizer.class)).isNotEmpty();
    }
}
