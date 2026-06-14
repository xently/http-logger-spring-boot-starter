package co.ke.xently.http.logger.utils;


import org.slf4j.Logger;

@FunctionalInterface
public interface HttpLoggerProvider {
    Logger get(Request request);
}
