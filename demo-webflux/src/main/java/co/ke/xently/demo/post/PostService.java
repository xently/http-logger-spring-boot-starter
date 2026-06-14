package co.ke.xently.demo.post;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class PostService {
    private final WebClient webClient;

    public PostService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://jsonplaceholder.typicode.com").build();
    }

    public Flux<Post> findAll() {
        return webClient.get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(Post.class);
    }

    public Mono<Post> findById(Long id) {
        return webClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(Post.class);
    }

    public Mono<Post> create(Post post) {
        return webClient.post()
                .uri("/posts")
                .bodyValue(post)
                .retrieve()
                .bodyToMono(Post.class);
    }

    public Mono<Post> update(Long id, Post post) {
        return webClient.put()
                .uri("/posts/{id}", id)
                .bodyValue(post)
                .retrieve()
                .bodyToMono(Post.class);
    }

    public Mono<Post> patch(Long id, Post post) {
        return webClient.patch()
                .uri("/posts/{id}", id)
                .bodyValue(post)
                .retrieve()
                .bodyToMono(Post.class);
    }

    public Mono<Void> delete(Long id) {
        return webClient.delete()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Set<HttpMethod>> options() {
        return webClient.options()
                .uri("/posts")
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getHeaders().getAllow());
    }

    public Mono<HttpHeaders> head() {
        return webClient.head()
                .uri("/posts/1")
                .retrieve()
                .toBodilessEntity()
                .map(HttpEntity::getHeaders);
    }
}
