package co.ke.xently.http.logger.ws;

import co.ke.xently.http.logger.HttpLoggerProperties;
import co.ke.xently.http.logger.utils.HttpLoggerProvider;
import co.ke.xently.http.logger.utils.Request;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NullMarked
public record LogSoapClientInterceptor(
        HttpLoggerProperties properties,
        HttpLoggerProvider loggerProvider
) implements ClientInterceptor {
    @Override
    public boolean handleRequest(MessageContext messageContext) {
        var logLevel = Level.INFO;
        var logger = loggerProvider().get(new Request(null, null));
        if (properties().isEnabled()) {
            final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
            List<String> logOutput = new ArrayList<>();
            logOutput.add("\n======================================================START SOAP REQUEST======================================================");
            logOutput.add("[SOAP ACTION]: " + getSoapAction(messageContext.getRequest()));
            logOutput.add("[PAYLOAD]:");
            logOutput.add(getMessageXml(messageContext.getRequest()));
            logOutput.add("=======================================================END SOAP REQUEST=======================================================");

            loggingEventBuilder.log(String.join("\n", logOutput));
        }
        return true; // Return true to continue the request execution
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        logIncomingMessage(messageContext.getResponse(), "START SOAP RESPONSE", "END SOAP RESPONSE");
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        // SOAP Faults (Errors) are handled in a separate lifecycle method
        logIncomingMessage(messageContext.getResponse(), "START SOAP FAULT (ERROR)", "END SOAP FAULT (ERROR)");
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, @Nullable Exception ex) {
        // No-op: Used for resource clean-up if necessary
    }

    private void logIncomingMessage(WebServiceMessage message, String startTag, String endTag) {
        var logLevel = Level.INFO;
        var logger = loggerProvider().get(new Request(null, null));
        if (properties().isEnabled()) {
            final var loggingEventBuilder = logger.makeLoggingEventBuilder(logLevel);
            List<String> logOutput = new ArrayList<>();
            logOutput.add("\n======================================================%s======================================================".formatted(startTag)); // NOSONAR: java:S3457
            logOutput.add("[PAYLOAD]:");
            logOutput.add(getMessageXml(message));
            logOutput.add("=======================================================%s=======================================================".formatted(endTag));

            loggingEventBuilder.log(String.join("\n", logOutput));
        }
    }

    private String getMessageXml(WebServiceMessage message) {
        try {
            var outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[ERROR EXTRACTING SOAP XML]: " + e.getMessage();
        }
    }

    private String getSoapAction(WebServiceMessage message) {
        if (message instanceof SoapMessage soapMessage) {
            return soapMessage.getSoapAction();
        }
        return "UNKNOWN";
    }
}