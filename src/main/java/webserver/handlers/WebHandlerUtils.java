package webserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import tools.JsonMapper;
import tools.LogUtils;
import tools.MdDoc;
import webserver.handlers.web.BaseError;
import webserver.handlers.web.ErrorReport;

import java.io.IOException;
import java.io.OutputStream;

public class WebHandlerUtils {

    private WebHandlerUtils() {
    }

    public static boolean validateHttpRequest(HttpExchange exchange, String expectedMethod) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(expectedMethod)) {
            LogUtils.error("Expected %s but got %s", expectedMethod, exchange.getRequestMethod());
            prepareErrorResponse(exchange, 409, BaseError.METHOD_NOT_ALLOWED);
            return false;
        }
        return true;
    }


    public static void prepareErrorResponse(HttpExchange exchange, int httpCode, ErrorReport errorReport) throws IOException {
        final String msg = JsonMapper.objectToJson(errorReport).toString();
        exchange.sendResponseHeaders(httpCode, msg.length());
        exchange.getResponseHeaders().add("Content-Type", "text/json");
        final OutputStream os = exchange.getResponseBody();
        os.write(msg.getBytes());
        os.close();
        exchange.getResponseBody().close();
    }


    @MdDoc(description = "Returns the given body response. If any extra header need to be returned you have to deal it by yourself in exchange.")
    public static void buildValidResponseAndClose(
            @MdDoc(description = "HttpExchange instance provided directly the interface HttpHandler")
            HttpExchange exchange,
            @MdDoc(description = "The full body response, commonly we're returning a JSON format.")
            String responseText) throws IOException {
        exchange.sendResponseHeaders(200, responseText.length());
        exchange.getResponseHeaders().add("Content-Type", "text/json");
        final OutputStream os = exchange.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
        exchange.getResponseBody().close();
    }

}
