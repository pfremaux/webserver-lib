package webserver.handlers.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import tools.LogUtils;

import java.io.*;
import java.util.List;

/**
 * Trying to follow RFC: https://datatracker.ietf.org/doc/html/rfc9110#name-content-range
 */
class VideoStreaming {
    private static final int MAX_BYTES_TO_RETURN = 512_000;

    public static void streamVideo(HttpExchange exchange, File file) throws IOException {
        try (final FileInputStream videoStream = new FileInputStream(file)) {
            // Determine the range requested by the client
            final Range streamRange = getRange(exchange, file.length());

            // Jumps to the chunk requested by the client.
            videoStream.skip(streamRange.start);

            final int requestedChunkSize = (int) streamRange.requestedChunkLength();
            LogUtils.debug("[Backend] chunk size requested: %d", requestedChunkSize);

            // We're defining a ceiling just to make sure we're not handling a huge file.
            final int effectiveByteCountsToRead = Math.min(requestedChunkSize, MAX_BYTES_TO_RETURN);
            // Set the response headers based on the requested range
            prepareResponseHeader(exchange, streamRange, effectiveByteCountsToRead);
            sendVideoStream(exchange, videoStream, streamRange, effectiveByteCountsToRead);
        }
    }

    private static void sendVideoStream(HttpExchange exchange, FileInputStream videoStream, Range streamRange, int effectiveByteCountsToRead) {
        try (OutputStream outputStream = exchange.getResponseBody()) {
            // HTTP 206: partial content
            // HTTP 200: final part.
            exchange.sendResponseHeaders(streamRange.isFinalRange() ? 200 : 206, effectiveByteCountsToRead);

            final byte[] buffer = new byte[effectiveByteCountsToRead];
            int remainingBytesToRead = effectiveByteCountsToRead;
            int totalBytesRead = 0;
            while (remainingBytesToRead > 0) {
                totalBytesRead = transferFileChunkToOutputStream(videoStream, effectiveByteCountsToRead, outputStream, totalBytesRead, remainingBytesToRead, buffer);
                remainingBytesToRead = remainingBytesToRead - totalBytesRead;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int transferFileChunkToOutputStream(FileInputStream mediaFileInputStream, int requestedChunkSize, OutputStream outputStream, int totalBytesRead, int remainingBytesToRead, byte[] buffer) throws IOException {
        final int bytesRead = mediaFileInputStream.read(buffer, 0, Math.min(remainingBytesToRead, buffer.length));
        LogUtils.debug("[Backend] Bytes read in file: %d", bytesRead);
        outputStream.write(buffer, 0, bytesRead);
        totalBytesRead += bytesRead;
        if (totalBytesRead > requestedChunkSize) {
            LogUtils.error("[ERROR] More bytes have been read then requested: totalBytesRead=" + totalBytesRead + " ; requestedChunkSize=" + requestedChunkSize);
        }
        return totalBytesRead;
    }

    private static void prepareResponseHeader(HttpExchange exchange, Range range, int effectiveByteCountsToRead) {
        final Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Accept-Ranges", "bytes");
        responseHeaders.add("Content-Type", "video/mp4");
        responseHeaders.add("Content-Length", String.valueOf(effectiveByteCountsToRead));
        final String headerValue = range.getHeaderValue();
        responseHeaders.add("Content-Range", headerValue);
        LogUtils.debug("[Response header] Content-Range: " + headerValue);
    }

    private static Range getRange(HttpExchange exchange, long videoLength) {
        final List<String> rangeHeader = exchange.getRequestHeaders().get("Range");
        // Expects format: bytes=<firstPos>-<lastPos> OR bytes=<firstPos>-
        // Other case like bytes=-<lastPos> OR bytes=<firstPos>-<lastPos>,<firstPos>-<lastPos> aren't supported
        final String range = rangeHeader.get(0);
        LogUtils.debug("[Request header] Range: %s", range);
        if (range != null && range.startsWith("bytes=")) {
            final String[] rangeValues = range.substring("bytes=".length()).split("-");
            final long firstPos = Long.parseLong(rangeValues[0]);
            long lastPos;
            // If client sent a last-pos
            if (rangeValues.length > 1 && rangeValues[1].length() > 0) {
                lastPos = Math.min(videoLength - 1, Long.parseLong(rangeValues[1]))/* + 1*/;
            } else {
                lastPos = videoLength - 1;
            }
            if (lastPos - firstPos > MAX_BYTES_TO_RETURN) {
                lastPos = Math.min(videoLength - 1, firstPos + MAX_BYTES_TO_RETURN);
            }
            return new Range(firstPos, lastPos, videoLength);
        } else {
            return new Range(0, videoLength - 1, videoLength);
        }
    }

    private record Range(long start, long end, long fileLength) {
        String getHeaderValue() {
            return "bytes " + start + "-" + (end) + "/" + (fileLength);
        }

        boolean isFinalRange() {
            return (end) == fileLength;
        }

        long requestedChunkLength() {
            return (end + 1) - start;
        }
    }
}

