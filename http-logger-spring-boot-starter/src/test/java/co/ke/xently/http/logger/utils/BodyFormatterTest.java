package co.ke.xently.http.logger.utils;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BodyFormatterTest {
    @Test
    void shouldPrettyPrintNestedJson() {
        var formatted = BodyFormatter.format("{\"outer\":{\"count\":2}}");

        assertThat(formatted).contains("\n  \"outer\"", "\n    \"count\"");
    }

    @Test
    void shouldPrettyPrintNestedXml() {
        var formatted = BodyFormatter.format("<root><item>value</item></root>");

        assertThat(formatted).contains("\n  <item>value</item>\n");
    }

    @Test
    void shouldReturnPlainEmptyAndMalformedBodiesUnchanged() {
        assertThat(BodyFormatter.format("plain text")).isEqualTo("plain text");
        assertThat(BodyFormatter.format(" ")).isEqualTo(" ");
        assertThat(BodyFormatter.format("{malformed}")).isEqualTo("{malformed}");
        assertThat(BodyFormatter.format("<root>")).isEqualTo("<root>");
    }

    @Test
    void shouldNotFormatXmlWithExternalEntities() {
        var body = "<!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///etc/hostname\">]>"
                + "<root>&xxe;</root>";

        assertThat(BodyFormatter.format(body)).isEqualTo(body);
    }

    @Test
    void shouldFormatConcurrentlyWithoutSharingMutableParserState() {
        IntStream.range(0, 20).parallel().forEach(index -> {
            var formatted = BodyFormatter.format("{\"value\":" + index + "}");
            assertThat(formatted).contains("\n  \"value\"");
            assertThat(formatted).contains(Integer.toString(index));
        });
    }
}