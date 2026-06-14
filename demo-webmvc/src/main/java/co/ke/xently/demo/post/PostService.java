package co.ke.xently.demo.post;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

@Service
public class PostService {
    private final RestClient restClient;

    public PostService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://jsonplaceholder.typicode.com").build();
    }

    public List<Post> findAll() {
        return restClient.get()
                .uri("/posts")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Post findById(Long id) {
        return restClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .body(Post.class);
    }

    public Post create(Post post) {
        return restClient.post()
                .uri("/posts")
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    public Post update(Long id, Post post) {
        return restClient.put()
                .uri("/posts/{id}", id)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    public Post patch(Long id, Post post) {
        return restClient.patch()
                .uri("/posts/{id}", id)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    public void delete(Long id) {
        restClient.delete()
                .uri("/posts/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public Set<HttpMethod> options() {
        return restClient.options()
                .uri("/posts")
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getAllow();
    }

    public HttpHeaders head() {
        return restClient.head()
                .uri("/posts/1")
                .retrieve()
                .toBodilessEntity()
                .getHeaders();
    }
}
