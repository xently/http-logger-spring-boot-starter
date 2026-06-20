package co.ke.xently.http.logger.ws;

import co.ke.xently.http.logger.HttpLoggerProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(OutputCaptureExtension.class)
class LogSoapClientInterceptorTest {

    private final HttpLoggerProperties properties = new HttpLoggerProperties(true);

    @Test
    void shouldLogSoapRequestWhenLoggingIsEnabled(CapturedOutput output) throws Exception {
        var soapMessage = mock(SoapMessage.class);
        when(soapMessage.getSoapAction()).thenReturn("urn:LoanRepayment");
        doAnswer(invocation -> {
            var stream = invocation.<java.io.OutputStream>getArgument(0);
            stream.write("<request>payload</request>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapMessage).writeTo(any());

        var context = mock(MessageContext.class);
        when(context.getRequest()).thenReturn(soapMessage);
        var interceptor = new LogSoapClientInterceptor(properties, request -> log);

        var handled = interceptor.handleRequest(context);

        assertAll(
                () -> assertThat(handled, is(true)),
                () -> assertThat(output.getOut(), containsString("START SOAP REQUEST")),
                () -> assertThat(output.getOut(), containsString("[SOAP ACTION]: urn:LoanRepayment")),
                () -> assertThat(output.getOut(), containsString("<request>payload</request>"))
        );
    }

    @Test
    void shouldLogSoapResponseAndFault(CapturedOutput output) throws Exception {
        var response = mock(WebServiceMessage.class);
        doAnswer(invocation -> {
            var stream = invocation.<java.io.OutputStream>getArgument(0);
            stream.write("<response>ok</response>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(response).writeTo(any());
        var context = mock(MessageContext.class);
        when(context.getResponse()).thenReturn(response);
        var interceptor = new LogSoapClientInterceptor(properties, request -> log);

        var handledResponse = interceptor.handleResponse(context);
        var handledFault = interceptor.handleFault(context);

        assertAll(
                () -> assertThat(handledResponse, is(true)),
                () -> assertThat(handledFault, is(true)),
                () -> assertThat(output.getOut(), containsString("START SOAP RESPONSE")),
                () -> assertThat(output.getOut(), containsString("START SOAP FAULT (ERROR)")),
                () -> assertThat(output.getOut(), containsString("<response>ok</response>"))
        );
    }

    @Test
    void shouldLogPayloadExtractionErrorWhenMessageWriteFails(CapturedOutput output) throws Exception {
        var response = mock(WebServiceMessage.class);
        doThrow(new IllegalStateException("broken stream"))
                .when(response).writeTo(any());
        var context = mock(MessageContext.class);
        when(context.getResponse()).thenReturn(response);
        var interceptor = new LogSoapClientInterceptor(properties, request -> log);

        interceptor.handleResponse(context);

        assertThat(output.getOut(), containsString("[ERROR EXTRACTING SOAP XML]: broken stream"));
    }

    @Test
    void shouldSkipLoggingWhenLoggerLevelIsDisabled() {
        var logger = mock(Logger.class);
        when(logger.isEnabledForLevel(Level.INFO)).thenReturn(false);
        var context = mock(MessageContext.class);
        when(context.getRequest()).thenReturn(mock(SoapMessage.class));
        var props = new HttpLoggerProperties();
        var interceptor = new LogSoapClientInterceptor(props, request -> logger);

        var handled = interceptor.handleRequest(context);

        assertThat(handled, is(true));
        verify(logger, never()).makeLoggingEventBuilder(Level.INFO);
    }
}
