package co.ke.xently.demo.post;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class PostServiceTest {

    private PostService postService;
    private MockRestServiceServer server;

    @Test
    void shouldFetchAllPosts() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        postService = new PostService(builder);

        String json = """
                [
                  {
                    "userId": 1,
                    "id": 1,
                    "title": "title 1",
                    "body": "body 1"
                  }
                ]
                """;
        this.server.expect(requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Post> posts = postService.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).title()).isEqualTo("title 1");
        this.server.verify();
    }

    @Test
    void shouldFetchPostById() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        postService = new PostService(builder);

        String json = """
                {
                  "userId": 1,
                  "id": 1,
                  "title": "title 1",
                  "body": "body 1"
                }
                """;
        this.server.expect(requestTo("https://jsonplaceholder.typicode.com/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Post post = postService.findById(1L);
        assertThat(post.id()).isEqualTo(1L);
        assertThat(post.title()).isEqualTo("title 1");
        this.server.verify();
    }
}
