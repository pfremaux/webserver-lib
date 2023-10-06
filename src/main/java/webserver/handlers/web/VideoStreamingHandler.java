package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.LogUtils;
import webserver.ServerProperties;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class VideoStreamingHandler implements HttpHandler {

    private final Path baseDir;
    private final String endpointRelativePath;

    public VideoStreamingHandler() {
        final String absolutePath = ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow();
        this.baseDir = Path.of(absolutePath);
        this.endpointRelativePath = ServerProperties.KEY_STREAM_VIDEO_ENDPOINT.getValue().get();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final URI requestURI = exchange.getRequestURI();
        LogUtils.debug("Requested URI : " + requestURI.getPath());
        final String relativeFilePath = requestURI.toString().substring(endpointRelativePath.length());
        final String filePath = (baseDir.toFile().getAbsolutePath() + relativeFilePath).replaceAll("%20", " ");
        final File file = new File(filePath);
        VideoStreaming.streamVideo(exchange, file);
    }
}