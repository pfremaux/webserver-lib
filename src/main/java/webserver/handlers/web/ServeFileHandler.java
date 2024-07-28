package webserver.handlers.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.LogUtils;
import webserver.ServerProperties;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServeFileHandler implements HttpHandler {

    private final Path baseDir;
    private final String endpointRelativePath;
    private final boolean allowFilesExploration;

    public ServeFileHandler() {
        final String absolutePath = ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow();
        final String endpointRelativePath = ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().orElseThrow();
        this.allowFilesExploration = ServerProperties.KEY_STATIC_FILES_ALLOW_EXPLORATION.asBoolean().orElse(Boolean.FALSE);
        this.baseDir = Path.of(absolutePath);
        this.endpointRelativePath = endpointRelativePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final URI requestURI = exchange.getRequestURI();
        LogUtils.debug("Requested URI : %s", requestURI.getPath());
        LogUtils.debug("Endpoint path %s", endpointRelativePath);
        final String relativeFilePathWithQueryParameters = requestURI.toString().substring(endpointRelativePath.length());
        int queryParameterSeparator = relativeFilePathWithQueryParameters.lastIndexOf("?");
        final String relativeFilePath = queryParameterSeparator == -1 ? relativeFilePathWithQueryParameters : relativeFilePathWithQueryParameters.substring(0, queryParameterSeparator);
        LogUtils.debug("Relative file path %s", relativeFilePath);
        LogUtils.debug("Base dir path %s", baseDir);
        final String filePath = (baseDir.toFile().getAbsolutePath() + relativeFilePath).replaceAll("%20", " ");
        if (filePath.endsWith("*") && allowFilesExploration) {
            String path = filePath.substring(0, filePath.length() - 1);
            File f = new File(path);
            if (!f.isDirectory()) {
                // TODO throw
            }
            final String response = formatFilesHtml(relativeFilePath.substring(0, relativeFilePath.length() - 2), f.listFiles());
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                os.write(bytes);
                exchange.getResponseBody().close();
            }
            return;
        }
        LogUtils.debug("Full file path %s", filePath);
        final File file = new File(filePath);
        LogUtils.debug(String.format("Following file requested : '%s'...", file.getAbsolutePath()));
        if (file.exists()) {
            LogUtils.debug("File found !");
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] bytes = Files.readAllBytes(Path.of(filePath));
                if (bytes.length > 3 * 1024 * 1024) {
                    // File too large.
                    exchange.sendResponseHeaders(400, 0);
                    exchange.getResponseBody().close();
                    return;
                }
                exchange.sendResponseHeaders(200, bytes.length);
                os.write(bytes);
            } catch (Exception e) {
                System.out.println(filePath);
                e.printStackTrace();
                //LogUtils.error("failed reading a file + %s",  e.s());
            }
            return;
        }
        LogUtils.debug("File not found :(");
        exchange.sendResponseHeaders(400, 0);
        exchange.getResponseBody().close();
    }

    private String formatFilesHtml(String relativeFilePath, File[] files) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<meta http-equiv=\"Content-Security-Policy\" content=\"upgrade-insecure-requests\">");
        builder.append("<link rel=\"stylesheet\" href=\"/web/css/baseMobile.css\">");
        builder.append("</head>");

        builder.append("<body>");
        addUtilityFunctions(builder);
        builder.append("<ul>");
        builder.append("<li><a href=\"");
        builder.append(endpointRelativePath);
        builder.append(relativeFilePath.substring(0, endpointRelativePath.lastIndexOf("/")));
        builder.append("/*\">..</a></li>");
        for (File file : (files == null ? new File[]{} : files)) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            final String fileURL = (endpointRelativePath + relativeFilePath + "/" + file.getName()).replaceAll("\s", "%20");
            builder.append("<li><a href=\"");
            builder.append(file.getName().endsWith(".mp4") ?fileURL.replace("/web/", "/watch/"): fileURL); // TODO PFR duplicate action. Factoriser
            if (file.isDirectory()) {
                builder.append("/*");
            }
            builder.append("\">");
            //builder.append(endpointRelativePath);
            //builder.append(relativeFilePath);
            //builder.append("/");
            builder.append(file.getName());
            builder.append("</a>");

            // handleVideo(builder, file, fileURL);
            handleVideoV2(builder, file, fileURL);

            builder.append("</li>");
        }
        builder.append("</ul>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    private void handleVideo(StringBuilder builder, File file, String fileURL) {
        if (file.getName().endsWith(".mp4")) {
            final String id = file.getName().trim() + ".id";
            builder.append("<a href=\"#\" onclick=\"document.getElementById('" + id + "').style.display === 'block' ? document.getElementById('" + id + "').style.display = 'none' : document.getElementById('" + id + "').style.display = 'block'");
            builder.append("\"> show");
            builder.append("</a>");

            builder.append("<video id=\"");
            builder.append(file.getName());
            builder.append(".id");
            builder.append("\" displaysinline ");
            builder.append("src=\"");
            // builder.append(fileURL);
            builder.append(fileURL.replace("/web/", "/watch/"));
            builder.append("\" style=\"display:none\" type=\"video/mp4\"");
            builder.append("></video>");
        }
    }

    private void addUtilityFunctions(StringBuilder builder) {
        builder.append("<script>");
        builder.append("""
                function insertVideo(id, url) {
                    let videoTag = document.createElement('video');
                    videoTag.id = id+".id";
                    videoTag.playsinline = "";
                    videoTag.autoplay="autoplay";
                     videoTag.muted="muted";
                      videoTag.loop="loop";
                      videoTag.controls="controls";
                      videoTag.style.width="640px";
                      videoTag.style.height="480px";
                       videoTag.type="video/mp4";
                    videoTag.src = url;
                    document.getElementById(id).appendChild(videoTag);
                    
                     let buttonTag = document.createElement('button');
                     buttonTag.onclick = e => removeAllChildren(id);
                     buttonTag.innerHTML = 'X';
                     document.getElementById(id).appendChild(buttonTag);
                }

                function removeAllChildren(i) {
                	const myNode = document.getElementById(i);
                	while (myNode.lastElementChild) {
                		myNode.removeChild(myNode.lastElementChild);
                	}
                }
                """);
        builder.append("</script>");
    }

    private void handleVideoV2(StringBuilder builder, File file, String fileURL) {
        if (file.getName().endsWith(".mp4")) {
            final String id = file.getName().trim() + ".id";
            builder.append("<div id=\"%s\">".formatted(id));
            builder.append("<a href=\"#\" onclick=\"");
            final String urlToWatch = fileURL.replace("/web/", "/watch/");
            builder.append("insertVideo('%s', '%s')".formatted(id, urlToWatch));
            builder.append("\"> show");
            builder.append("</a>");
            builder.append("</div>");
        }
    }
}
