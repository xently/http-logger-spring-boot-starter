package co.ke.xently.demo.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class LoggingVerificationTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldLogHttpGet(CapturedOutput output) {
        RestClient.create().get()
                .uri("http://localhost:" + port + "/api/v1/posts/1")
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "GET",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE",
                "userId"
        );
    }

    @Test
    void shouldLogHttpPost(CapturedOutput output) {
        Post post = new Post(null, 1L, "New Post", "Body");
        RestClient.create().post()
                .uri("http://localhost:" + port + "/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "POST",
                "https://jsonplaceholder.typicode.com/posts",
                "START RESPONSE",
                "New Post"
        );
    }

    @Test
    void shouldLogHttpPut(CapturedOutput output) {
        Post post = new Post(1L, 1L, "Updated Post", "Body");
        RestClient.create().put()
                .uri("http://localhost:" + port + "/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "PUT",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE",
                "Updated Post"
        );
    }

    @Test
    void shouldLogHttpPatch(CapturedOutput output) {
        Post post = new Post(1L, null, "Patched Post", null);
        RestClient.create().patch()
                .uri("http://localhost:" + port + "/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "PATCH",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE",
                "Patched Post"
        );
    }

    @Test
    void shouldLogHttpDelete(CapturedOutput output) {
        RestClient.create().delete()
                .uri("http://localhost:" + port + "/api/v1/posts/1")
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "DELETE",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogHttpOptions(CapturedOutput output) {
        RestClient.create().options()
                .uri("http://localhost:" + port + "/api/v1/posts")
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "OPTIONS",
                "https://jsonplaceholder.typicode.com/posts",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogHttpHead(CapturedOutput output) {
        RestClient.create().head()
                .uri("http://localhost:" + port + "/api/v1/posts/1")
                .retrieve()
                .toBodilessEntity();

        assertThat(output).contains(
                "START REQUEST",
                "HEAD",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogSoapRequest(CapturedOutput output) {
        RestClient.create().get()
                .uri("http://localhost:" + port + "/api/v1/soap/number-to-words?number=500")
                .retrieve()
                .body(String.class);

        assertThat(output).contains(
                "START SOAP REQUEST",
                "[SOAP ACTION]: ",
                "START SOAP RESPONSE",
                "five hundred"
        );
    }
}
