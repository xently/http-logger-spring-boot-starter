package co.ke.xently.demo.post;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Flux<Post> findAll() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> findById(@PathVariable Long id) {
        return postService.findById(id);
    }

    @PostMapping
    public Mono<Post> create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable Long id, @RequestBody Post post) {
        return postService.update(id, post);
    }

    @PatchMapping("/{id}")
    public Mono<Post> patch(@PathVariable Long id, @RequestBody Post post) {
        return postService.patch(id, post);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id) {
        return postService.delete(id);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public Mono<Set<HttpMethod>> options() {
        return postService.options();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public Mono<ResponseEntity<Void>> head() {
        return postService.head().map(headers -> ResponseEntity.ok().headers(headers).build());
    }
}
