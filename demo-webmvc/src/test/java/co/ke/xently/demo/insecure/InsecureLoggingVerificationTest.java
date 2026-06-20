package co.ke.xently.demo.insecure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class InsecureLoggingVerificationTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldLogSelfSignedRequest(CapturedOutput output) {
        RestClient.create().get()
                .uri("http://localhost:" + port + "/api/v1/self-signed")
                .retrieve()
                .toEntity(String.class);

        assertThat(output).contains(
                "START REQUEST",
                "GET",
                "https://self-signed.badssl.com/",
                "START RESPONSE"
        );
    }
}
