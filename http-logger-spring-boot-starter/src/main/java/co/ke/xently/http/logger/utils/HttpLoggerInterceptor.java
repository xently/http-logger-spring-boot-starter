package co.ke.xently.http.logger.utils;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;

@NullMarked
public interface HttpLoggerInterceptor {
    HttpLoggerInterceptor DEFAULT = new HttpLoggerInterceptor() {
        @Override
        public void request(URI uri, HttpMethod method, HttpHeaders headers, String body) {
        }

        @Override
        public void response(URI uri, HttpMethod method, HttpStatusCode httpStatus, HttpHeaders headers, String body) {
        }
    };

    void request(URI uri, HttpMethod method, HttpHeaders headers, String body);

    void response(URI uri, HttpMethod method, HttpStatusCode httpStatus, HttpHeaders headers, String body);
}
