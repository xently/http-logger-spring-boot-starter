package co.ke.xently.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.test.context.SpringBootTest;
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
    void shouldHaveClientHttpRequestFactoryBuilderBean() {
        assertThat(context.containsBean("clientHttpRequestFactoryBuilder")).isTrue();
        assertThat(context.getBean(ClientHttpRequestFactoryBuilder.class)).isNotNull();
    }
}
