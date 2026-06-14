package co.ke.xently.request.timer.utils;


import org.slf4j.Logger;

@FunctionalInterface
public interface HttpLoggerProvider {
    Logger get(Request request);
}
