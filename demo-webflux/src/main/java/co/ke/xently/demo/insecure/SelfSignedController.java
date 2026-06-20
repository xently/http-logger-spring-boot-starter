package co.ke.xently.demo.insecure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
class SelfSignedController {
    private final WebClient httpClient;

    SelfSignedController(WebClient.Builder builder) {
        this.httpClient = builder.baseUrl("https://self-signed.badssl.com/").build();
    }

    @GetMapping("/self-signed")
    Mono<ResponseEntity<String>> selfSigned() {
        return httpClient.get().retrieve().toEntity(String.class);
    }
}
