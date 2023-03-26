package webserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServeFileHandler implements HttpHandler {


    private final Path baseDir;
    private final String relativePath;

    public ServeFileHandler(List<String> parameters) {
        this.baseDir = Path.of("C:\\Users\\Pierre\\IdeaProjects\\webserver-lib\\src\\main\\web");// TODO PFR add to config
        relativePath = "/static";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final URI requestURI = exchange.getRequestURI();
        LogUtils.info("OK");
        LogUtils.info(String.format("Requested URI : %s", requestURI.getPath()));
        LogUtils.info(String.format("Looking for matching pattern : '%s'...", relativePath));
        int i = requestURI.getPath().indexOf(relativePath);
        if (i >= 0) {
            LogUtils.debug("Pattern found !");
            final String substring = requestURI.getPath().substring(i+relativePath.length()+1);
            final Path filePath = baseDir.resolve(substring);
            final File file = filePath.toFile();
            LogUtils.info(String.format("Following file requested : '%s'...", file.getAbsolutePath()));
            if (file.exists()) {
                LogUtils.info("File found !");
                try (OutputStream os = exchange.getResponseBody()) {
                    byte[] bytes = Files.readAllBytes(filePath);
                    exchange.sendResponseHeaders(200, bytes.length);
                    os.write(bytes);
                }
                return;
            }
            LogUtils.info("File not found :(");
        }
        exchange.sendResponseHeaders(400, 0);
        exchange.getResponseBody().close();
    }
}
