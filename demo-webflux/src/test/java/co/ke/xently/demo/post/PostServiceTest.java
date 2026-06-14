package co.ke.xently.demo.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostServiceTest {

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(WebClient.builder());
    }

    @Test
    void shouldFetchAllPosts() {
        Flux<Post> posts = postService.findAll();
        // Since we are hitting the real API in this demo context, we just verify it's not empty
        StepVerifier.create(posts.take(1))
                .assertNext(post -> {
                    assertThat(post.id()).isNotNull();
                    assertThat(post.title()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void shouldFetchPostById() {
        Mono<Post> postMono = postService.findById(1L);
        StepVerifier.create(postMono)
                .assertNext(post -> {
                    assertThat(post.id()).isEqualTo(1L);
                    assertThat(post.title()).isNotBlank();
                })
                .verifyComplete();
    }
}
