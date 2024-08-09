package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import webserver.ServerProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultCssHandler implements HttpHandler {

    private final Path cssFilePath;
    private final byte[] cssFileContent;

    public DefaultCssHandler() {
        this.cssFilePath = Path.of(ServerProperties.DEFAULT_CSS_FILE_PATH.getValue().orElseThrow());
        try {
            this.cssFileContent = Files.readString(this.cssFilePath).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(200, cssFileContent.length);
            os.write(cssFileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
