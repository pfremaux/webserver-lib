package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

import java.util.List;

public class VideoInfoResponse {
    @JsonField
    private final String path;
    @JsonField
    private final List<String> tags;

    public VideoInfoResponse(
            @JsonParameter(name = "path") String path,
            @JsonParameter(name = "tags") List<String> tags) {
        this.path = path;
        this.tags = tags;
    }

    public String getPath() {
        return path;
    }

    public List<String> getTags() {
        return tags;
    }
}
