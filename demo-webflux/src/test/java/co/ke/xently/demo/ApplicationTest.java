package co.ke.xently.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

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
    void shouldHaveClientHttpConnectorBean() {
        assertThat(context.containsBean("clientHttpConnector")).isTrue();
        assertThat(context.getBeansOfType(ReactorClientHttpConnector.class)).isNotEmpty();
    }
}
