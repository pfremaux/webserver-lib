package webserver.example.videoedition.model;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class SaveFrameRequest {
    @JsonField
    private final int index;
    @JsonField
    private final String framePath;
    @JsonField
    private final String key;

    public SaveFrameRequest(
            @JsonParameter(name = "index") int index,
            @JsonParameter(name = "videoPath") String framePath,
            @JsonParameter(name = "key") String key) {
        this.index = index;
        this.framePath = framePath;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getIndex() {
        return index;
    }

    public String getFramePath() {
        return framePath;
    }
}
