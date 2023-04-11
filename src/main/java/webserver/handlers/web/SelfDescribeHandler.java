package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.HttpContext;
import tools.LogUtils;
import webserver.generators.DocumentedEndpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * The goal of this handler is to generate a HTML page that describes all endpoints.
 */
public class SelfDescribeHandler implements HttpHandler {

    private final String strContexts;

    public SelfDescribeHandler(HttpContext[] contexts) {
        final StringBuilder builder = new StringBuilder();
        for (HttpContext context : contexts) {
            builder.append("<h1>");
            builder.append(context.getPath());
            builder.append("</h1>");
            builder.append("<p>");
            builder.append(context.getDescription());
            builder.append("</p>");
            builder.append("<p>");
            builder.append(context.getHandler().getClass().getSimpleName());
            builder.append("</p>");
            builder.append("<br/>");
        }
        this.strContexts = builder.toString();
    }

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
        // TODO PFR replace with call to buildResponseAndClose()
        exchange.sendResponseHeaders(200, strContexts.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(strContexts.getBytes());
        os.close();
    }
}
