package co.ke.xently.request.timer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggerPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        HttpLoggerProperties properties = HttpLoggerProperties.builder().build();
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void shouldAllowModifyingValues() {
        HttpLoggerProperties properties = HttpLoggerProperties.builder().build();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldSupportBuilder() {
        HttpLoggerProperties properties = HttpLoggerProperties.builder()
                .enabled(false)
                .build();
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldSupportToString() {
        HttpLoggerProperties properties = HttpLoggerProperties.builder().build();
        assertThat(properties.toString()).contains("enabled=true");
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        HttpLoggerProperties properties1 = HttpLoggerProperties.builder().enabled(true).build();
        HttpLoggerProperties properties2 = HttpLoggerProperties.builder().enabled(true).build();
        HttpLoggerProperties properties3 = HttpLoggerProperties.builder().enabled(false).build();

        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        assertThat(properties1).isNotEqualTo(properties3);
    }
}
