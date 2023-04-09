package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.LogUtils;
import webserver.ServerProperties;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServeFileHandler implements HttpHandler {

    private final Path baseDir;
    private final String endpointRelativePath;

    public ServeFileHandler() {
        final String absolutePath = ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow();
        final String endpointRelativePath = ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().orElseThrow();
        this.baseDir = Path.of(absolutePath);
        this.endpointRelativePath = endpointRelativePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final URI requestURI = exchange.getRequestURI();
        LogUtils.debug("Requested URI : %s", requestURI.getPath());
        LogUtils.debug("Endpoint path %s", endpointRelativePath);
        final String relativeFilePath = requestURI.toString().substring(endpointRelativePath.length());
        LogUtils.debug("Relative file path %s", relativeFilePath);
        LogUtils.debug("Base dir path %s", baseDir);
        final String filePath = baseDir.toFile().getAbsolutePath() + relativeFilePath;
        LogUtils.debug("Full file path %s", filePath);
        final File file = new File(filePath);
        LogUtils.debug(String.format("Following file requested : '%s'...", file.getAbsolutePath()));
        if (file.exists()) {
            LogUtils.debug("File found !");
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] bytes = Files.readAllBytes(Path.of(filePath));
                exchange.sendResponseHeaders(200, bytes.length);
                os.write(bytes);
            }
            return;
        }
        LogUtils.debug("File not found :(");
        exchange.sendResponseHeaders(400, 0);
        exchange.getResponseBody().close();
    }
}
