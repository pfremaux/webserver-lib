package webserver.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.LogUtils;
import webserver.ServerProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VideoStreamingHandler implements HttpHandler {

    private static final int BUFFER_SIZE = 4096;

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
        final List<String> rangeValues = exchange.getRequestHeaders().get("Range");
        int startingPointer = 0;
        int endingPointer = -1;
        if (rangeValues != null) {
            LogUtils.debug(String.format("Range: %s", rangeValues.get(0)));
            String rangeParameters = rangeValues.get(0).substring("bytes=".length());
            String strStartingPointer = rangeParameters.substring(0, rangeParameters.indexOf("-"));
            startingPointer = Integer.parseInt(strStartingPointer);
            String strEndingPointer = rangeParameters.substring(rangeParameters.indexOf("-"));
            endingPointer = parseInt(strEndingPointer)
                    .orElse(startingPointer + BUFFER_SIZE * 100);
        } else {
            for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
                LogUtils.debug(entry.getKey() + " -> " + entry.getValue());
            }
        }

        LogUtils.debug("Requested URI : " + requestURI.getPath());
        final String relativeFilePath = requestURI.toString().substring(endpointRelativePath.length());
        final String filePath = (baseDir.toFile().getAbsolutePath() + relativeFilePath).replaceAll("%20", " ");
        final File file = new File(filePath);
        LogUtils.debug("Following file requested : " + file.getAbsolutePath());
        if (file.exists()) {
            LogUtils.debug("File found !");
            if (endingPointer > 0) {
                supportFileChunk(exchange, file, startingPointer, endingPointer);
            } else {
                dontSupportFileChunk(exchange, file);
            }

            return;

        }
        LogUtils.warning("File not found :(");
        exchange.sendResponseHeaders(400, 0);
        exchange.getResponseBody().close();
    }

    private Optional<Integer> parseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void dontSupportFileChunk(HttpExchange exchange, File file) throws IOException {
        final OutputStream os = exchange.getResponseBody();
        long length = file.length();
        exchange.sendResponseHeaders(200, length);
        FileInputStream inputStream = new FileInputStream(file);

        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            os.write(buffer, 0, bytesRead);
        }

        os.close();
        inputStream.close();
    }

    private void supportFileChunk(HttpExchange exchange, File file, int startingPointer, int endingPointer) throws IOException {
        final FileInputStream in = new FileInputStream(file);
        String name = file.getName();
        int extension = name.lastIndexOf(".");
        String fileExtension = name.substring(extension + 1);
        int realEndingPointer = Math.min(endingPointer, Long.valueOf(file.length()).intValue() - 1); // content range is 0-indexed but the content length is 1-indexed.
        boolean finalChunk = realEndingPointer < endingPointer || endingPointer == (Long.valueOf(file.length()).intValue() - 1);
        exchange.getResponseHeaders().add("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().add("Content-Type", "video/" + fileExtension);
        exchange.getResponseHeaders().add("Content-Range", "bytes " + startingPointer + "-" + (realEndingPointer) + "/" + (file.length()));

        // This method must be called prior getResponseBody()
        exchange.sendResponseHeaders(finalChunk ? 200 : 206, realEndingPointer - startingPointer);

        final OutputStream os = exchange.getResponseBody();

        try {
            int bytesRead = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int remainingBytesToRead = realEndingPointer - startingPointer;
            int i = 0;
            in.skip(startingPointer);
            while ((bytesRead = in.read(buffer, 0, Math.min(BUFFER_SIZE, remainingBytesToRead))) > 0  /*&& seek < endingPointer*/) {
                os.write(buffer, 0, bytesRead);
                remainingBytesToRead = remainingBytesToRead - bytesRead;
                // TODO PFR tester en breakan direct ici
            }
            os.flush();
        } catch (Throwable t) {
            t.printStackTrace();
            LogUtils.error(VideoStreamingHandler.class.getSimpleName() + " - supportFileChunk");
        }

        os.close();
        in.close();
    }
}