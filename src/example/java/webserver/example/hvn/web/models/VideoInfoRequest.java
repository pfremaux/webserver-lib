package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class VideoInfoRequest {
    @JsonField
    private final String key;

    public VideoInfoRequest(@JsonParameter(name = "key") String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
