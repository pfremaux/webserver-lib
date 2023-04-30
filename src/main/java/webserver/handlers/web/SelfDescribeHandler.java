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

    private final String strContexts;


    public SelfDescribeHandler(List<DocumentedEndpoint> endpointsDoc) {
        final StringBuilder builder = new StringBuilder();
        for (DocumentedEndpoint doc : endpointsDoc) {
            prepareHtmlDisplay(builder, doc);

        }
        this.strContexts = builder.toString();
    }

    private void prepareHtmlDisplay(StringBuilder builder, DocumentedEndpoint doc) {
        if (doc.getHttpMethod().equalsIgnoreCase("GET")) {
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
            builder.append("<h1>");
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
        builder.append("<p>");
        builder.append(doc.getDescription());
        builder.append("</p>");
        builder.append("<p>");
        builder.append(doc.getBodyExample());
        builder.append("</p>");
        builder.append("<p>");
        builder.append(doc.getResponseExample());
        builder.append("</p>");
        builder.append("<br/>");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        WebHandlerUtils.buildValidResponseAndClose(exchange, strContexts);
    }
}
