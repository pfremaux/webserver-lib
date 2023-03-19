package tools;

import com.sun.net.httpserver.HttpHandler;

public class HttpContext {
    private final String path;
    private final HttpHandler handler;
    private final String description;

    public HttpContext(String path, HttpHandler handler, String description) {
        this.path = path;
        this.handler = handler;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public HttpHandler getHandler() {
        return handler;
    }

    public String getDescription() {
        return description;
    }
}
