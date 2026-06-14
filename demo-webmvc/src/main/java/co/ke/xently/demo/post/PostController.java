package co.ke.xently.demo.post;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<Post> findAll() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public Post findById(@PathVariable Long id) {
        return postService.findById(id);
    }

    @PostMapping
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable Long id, @RequestBody Post post) {
        return postService.update(id, post);
    }

    @PatchMapping("/{id}")
    public Post patch(@PathVariable Long id, @RequestBody Post post) {
        return postService.patch(id, post);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public Set<HttpMethod> options() {
        return postService.options();
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public ResponseEntity<Void> head() {
        return ResponseEntity.ok().headers(postService.head()).build();
    }
}
