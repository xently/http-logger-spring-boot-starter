package co.ke.xently.http.logger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggerPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        HttpLoggerProperties properties = new HttpLoggerProperties(true);
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void shouldAllowModifyingValues() {
        HttpLoggerProperties properties = new HttpLoggerProperties(true);
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldSupportBuilder() {
        HttpLoggerProperties properties = new HttpLoggerProperties();
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldSupportToString() {
        HttpLoggerProperties properties = new HttpLoggerProperties(true);
        assertThat(properties.toString()).contains("enabled=true");
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        HttpLoggerProperties properties1 = new HttpLoggerProperties(true);
        HttpLoggerProperties properties2 = new HttpLoggerProperties(true);
        HttpLoggerProperties properties3 = new HttpLoggerProperties();

        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        assertThat(properties1).isNotEqualTo(properties3);
    }
}
