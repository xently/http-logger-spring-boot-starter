package co.ke.xently.demo.insecure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1")
class SelfSignedController {
    private final RestClient httpClient;

    SelfSignedController(RestClient.Builder builder) {
        this.httpClient = builder.baseUrl("https://self-signed.badssl.com/").build();
    }

    @GetMapping("/self-signed")
    ResponseEntity<String> selfSigned() {
        return httpClient.get().retrieve().toEntity(String.class);
    }
}
