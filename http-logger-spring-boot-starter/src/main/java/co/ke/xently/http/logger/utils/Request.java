package co.ke.xently.http.logger.utils;

import org.jspecify.annotations.NullUnmarked;
import org.springframework.http.HttpMethod;

import java.net.URI;

@NullUnmarked
public record Request(URI uri, HttpMethod method) {
}
