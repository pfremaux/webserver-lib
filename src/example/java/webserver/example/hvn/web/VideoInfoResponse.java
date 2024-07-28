package webserver.example.hvn.web;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class VideoInfoResponse {
    @JsonField
    private final String path;

    public VideoInfoResponse(@JsonParameter(name = "path") String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
