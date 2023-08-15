package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import webserver.generators.DocumentedEndpoint;
import webserver.handlers.WebHandlerUtils;

import java.io.IOException;
import java.util.List;

/**
 * The goal of this handler is to generate a HTML page that describes all endpoints.
 */
public class SelfDescribeHandler implements HttpHandler {
private static String pattern = """
<html>
    <head>
        <script src="web/js/helpers.js"></script>
    </head>
    <body>
    %s
    </body>
</html>
        """;

    private final String strContexts;


    public SelfDescribeHandler(List<DocumentedEndpoint> endpointsDoc) {
        final StringBuilder builder = new StringBuilder();
        for (DocumentedEndpoint doc : endpointsDoc) {
            prepareHtmlDisplay(builder, doc);
        }
        this.strContexts = pattern.formatted(builder.toString());
    }

    private void prepareHtmlDisplay(StringBuilder builder, DocumentedEndpoint doc) {
        if (doc.getHttpMethod().equalsIgnoreCase("GET")) {
            //builder.append("<h1  onclick=\"toggle(this)\">");// TODO PFR improve sinon ca cache le titre...
            builder.append("<h1>");
            builder.append("<a href=\"");
            builder.append(doc.getPath());
            builder.append("\">");

            builder.append(doc.getHttpMethod());
            builder.append(" ");
            builder.append(doc.getPath());
            builder.append("</a>");
            builder.append("</h1>");
        } else {
            builder.append("<h1 ");
            builder.append(" onclick=\"toggle(this)\" >");
            builder.append(doc.getHttpMethod());
            builder.append(" ");
            builder.append(doc.getPath());
            builder.append("</h1>");
        }

        if (doc.getRole() != null) {
            builder.append("<h3>Requires role: ");
            builder.append(doc.getRole());
            builder.append("</h3>");
        }
        if (doc.getDescription() != null && !doc.getDescription().equals("")) {
            builder.append("<p>");
            builder.append(doc.getDescription());
            builder.append("</p>");
        }
        if (doc.getBodyExample() != null && !"".equals(doc.getBodyExample())) {
            builder.append("<h4>Body example:</h4>");
            builder.append("<p>");
            builder.append(formatJson(doc.getBodyExample()));
            builder.append("</p>");
        }
        if (doc.getResponseExample() != null && !"".equals(doc.getResponseExample())) {
            builder.append("<h4>Response example:</h4>");
            builder.append("<p>");
            builder.append(formatJson(doc.getResponseExample()));
            builder.append("</p>");
        }
        builder.append("<br/>");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        WebHandlerUtils.buildValidResponseAndClose(exchange, strContexts);
    }

    private String formatJson(String rawJson) {
        if (rawJson == null || rawJson.equals("")) {
            return "";
        }
        if (!rawJson.startsWith("{")) {
            return rawJson;
        }
        final StringBuilder formattedJson = new StringBuilder();
        int indentationCount = 0;
        boolean inQuote = false;
        char previousChar = 0;
        for (char c : rawJson.toCharArray()) {
            if (c == ',') {
                formattedJson.append(c);
                formattedJson.append("<br>");
                indent(formattedJson, indentationCount);
            } else if (c == '\'' && !inQuote && previousChar != '\\') {
                inQuote = true;
                formattedJson.append(c);
            } else if (c == '\'' && inQuote && previousChar != '\\') {
                inQuote = false;
                formattedJson.append(c);
            } else if (c == '{') {
                indentationCount++;
                formattedJson.append(c);
                formattedJson.append("<br>");
                indent(formattedJson, indentationCount);
            } else if (c == '}') {
                indentationCount--;
                formattedJson.append("<br>");
                indent(formattedJson, indentationCount);
                formattedJson.append(c);
            } else if (c == '[') {
                formattedJson.append(c);
                if (previousChar != ':') {
                    indent(formattedJson, indentationCount);
                }
            } else if (c == ']') {
                // indentationCount--;
                formattedJson.append(c);
            } else {
                formattedJson.append(c);
            }
            previousChar = c;
        }
        return formattedJson.toString();
    }

    private void indent(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    }
}
