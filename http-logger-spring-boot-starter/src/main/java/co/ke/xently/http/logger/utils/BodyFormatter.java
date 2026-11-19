package co.ke.xently.http.logger.utils;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.PrettyPrinter;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.util.DefaultPrettyPrinter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

@UtilityClass
public final class BodyFormatter {
    private static final DefaultHandler DEFAULT_ERROR_HANDLER = new DefaultHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectWriteContext PRETTY_PRINT_CONTEXT = new ObjectWriteContext.Base() {
        @Override
        public PrettyPrinter getPrettyPrinter() {
            return new DefaultPrettyPrinter();
        }

        @Override
        public TokenStreamFactory tokenStreamFactory() {
            return JSON_FACTORY;
        }
    };

    public static String format(String body) {
        var trimmedBody = body.stripLeading();
        if (trimmedBody.isEmpty()) return body;

        try {
            if (trimmedBody.startsWith("{") || trimmedBody.startsWith("[")) {
                return formatJson(body);
            }
            if (trimmedBody.startsWith("<")) {
                return formatXml(body);
            }
        } catch (Exception ignored) {
            // Return the original body when it is malformed or cannot be safely formatted.
        }
        return body;
    }

    private static String formatJson(String body) {
        try (var parser = JSON_FACTORY.createParser(ObjectReadContext.empty(), body)) {
            if (parser.nextToken() == null) return body;

            var writer = new StringWriter();
            try (var generator = JSON_FACTORY.createGenerator(PRETTY_PRINT_CONTEXT, writer)) {
                generator.copyCurrentStructure(parser);
                if (parser.nextToken() != null) return body;
            }
            return writer.toString();
        }
    }

    private static String formatXml(String body) throws Exception {
        var documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        Document document;
        try (var reader = new StringReader(body)) {
            var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(DEFAULT_ERROR_HANDLER);
            document = documentBuilder.parse(new InputSource(reader));
        }

        var transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        var transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        var writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}