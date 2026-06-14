package co.ke.xently.demo.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class LoggingVerificationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldLogHttpGet(CapturedOutput output) {
        webTestClient().get()
                .uri("/api/v1/posts/1")
                .exchange()
                .expectStatus().isOk();

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
        webTestClient().post()
                .uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus().isOk();

        assertThat(output).contains(
                "START REQUEST",
                "POST",
                "https://jsonplaceholder.typicode.com/posts",
                "\"title\":\"New Post\"",
                "START RESPONSE",
                "New Post"
        );
    }

    @Test
    void shouldLogHttpPut(CapturedOutput output) {
        Post post = new Post(1L, 1L, "Updated Post", "Body");
        webTestClient().put()
                .uri("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus().isOk();

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
        webTestClient().patch()
                .uri("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus().isOk();

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
        webTestClient().delete()
                .uri("/api/v1/posts/1")
                .exchange()
                .expectStatus().isOk();

        assertThat(output).contains(
                "START REQUEST",
                "DELETE",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogHttpOptions(CapturedOutput output) {
        webTestClient().options()
                .uri("/api/v1/posts")
                .exchange()
                .expectStatus().isOk();

        assertThat(output).contains(
                "START REQUEST",
                "OPTIONS",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogHttpHead(CapturedOutput output) {
        webTestClient().head()
                .uri("/api/v1/posts/1")
                .exchange()
                .expectStatus().isOk();

        assertThat(output).contains(
                "START REQUEST",
                "HEAD",
                "https://jsonplaceholder.typicode.com/posts/1",
                "START RESPONSE"
        );
    }

    @Test
    void shouldLogSoapRequest(CapturedOutput output) {
        var actual = webTestClient().get()
                .uri("/api/v1/soap/number-to-words?number=500")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);

        assertThat(actual.returnResult().getResponseBody()).contains("five hundred");

        assertThat(output).contains(
                "START SOAP REQUEST",
                "[SOAP ACTION]: ",
                "START SOAP RESPONSE",
                "five hundred"
        );
    }
}
