package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import webserver.generators.DocumentedEndpoint;
import webserver.handlers.WebHandlerUtils;

import java.io.IOException;
import java.lang.reflect.Field;
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

    private final static String color_dark_blue = "#aaaaff";
    private final static String color_blue = "#ccccff";
    private final static String color_light_blue = "#eeeeff";

    private final String strContexts;


    public SelfDescribeHandler(List<DocumentedEndpoint> endpointsDoc) {
        final StringBuilder builder = new StringBuilder();
        int counter = 1;
        for (DocumentedEndpoint doc : endpointsDoc) {
            prepareHtmlDisplay(builder, doc, counter++);
        }
        this.strContexts = pattern.formatted(builder.toString());
    }

    private void prepareHtmlDisplay(StringBuilder builder, DocumentedEndpoint doc, int counter) {
        builder.append("<div style=\"border-style:solid\">");
        if (doc.getHttpMethod().equalsIgnoreCase("GET")) {
            //builder.append("<h1  onclick=\"toggle(this)\">");// TODO PFR improve sinon ca cache le titre...
            builder.append("<h1 style='margin:0px;background-color:%s'>".formatted(color_dark_blue));
            // TODO PFR toggle
            addToggleComponent(builder, counter);
            builder.append("<a href=\"");
            builder.append(doc.getPath());
            builder.append("\">");

            builder.append(doc.getHttpMethod());
            builder.append(" ");
            builder.append(doc.getPath());
            builder.append("</a>");
            builder.append("</h1>");
        } else {
            builder.append("<h1 style='margin:0px;background-color:%s'>".formatted(color_dark_blue));
            addToggleComponent(builder, counter);
            // TODO PFR toggle
            //builder.append(" onclick=\"toggle(this)\" >");
            builder.append(doc.getHttpMethod());
            builder.append(" ");
            builder.append(doc.getPath());
            builder.append("</h1>");
        }

        if (doc.getJavaMethodName() != null) {
            String callBackSeparator = doc.getParameters().isEmpty() ? "" : ", ";
            builder.append("<div>")
                    .append(doc.getJavaMethodName()).append("(").append(String.join(", ", doc.getParameters().values()));
            builder.append(callBackSeparator+"e => {<br>");
            if (doc.getReturnType() != null) {
                for (Field declaredField : doc.getReturnType().getType().getDeclaredFields()) {
                    builder.append("const obj = JSON.parse(e);<br>");
                    builder.append("// obj.");
                    builder.append(declaredField.getName());
                    builder.append("<br>");
                }
            } else {
                builder.append("// ");
                builder.append(doc.getResponseExample());
                builder.append("<br>");
                builder.append("const obj = JSON.parse(e);<br>");
            }
            builder.append("});<br>");
            builder.append("</div>");
        }

        // TODO PFR grouping tag with background color
        builder.append("<div id='details%d' style='background-color:%s; display:none'>".formatted(counter, color_blue));
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
            builder.append("<h4>Body example:</h4>");// TODO PFR background
            builder.append("<p style='background-color:%s'>".formatted(color_light_blue));
            builder.append(formatJson(doc.getBodyExample()));
            builder.append("</p>");
        }
        if (doc.getResponseExample() != null && !"".equals(doc.getResponseExample())) {
            builder.append("<h4>Response example:</h4>");// TODO PFR background
            builder.append("<p style='background-color:%s'>".formatted(color_light_blue));
            builder.append(formatJson(doc.getResponseExample()));
            builder.append("</p>");
        }
        builder.append("</div>"); // End of details block
        builder.append("</div>"); // End of global border styled block
        builder.append("<br/>");
    }

    private void addToggleComponent(StringBuilder builder, int counter) {
        builder.append("<div style=\"cursor:hand;display:inline\" onclick='toggle(\"details\"+%d);".formatted(counter));
        builder.append("console.log(this.innerHTML);");
        builder.append("this.innerHTML = this.innerHTML === \"+ \"? \"- \":\"+ \"'>+ ");
        builder.append("</div>");
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
        builder.append("&nbsp;&nbsp;&nbsp;&nbsp;".repeat(Math.max(0, count)));
    }
}
