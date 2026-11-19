package co.ke.xently.http.logger.utils;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SoapLoggerInterceptor {
    SoapLoggerInterceptor DEFAULT = new SoapLoggerInterceptor() {
        @Override
        public void request(String action, String body) {

        }

        @Override
        public void response(String body) {

        }
    };

    void request(String action, String body);

    void response(String body);
}
