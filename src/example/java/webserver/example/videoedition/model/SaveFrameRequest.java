package webserver.example.videoedition.model;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class SaveFrameRequest {
    @JsonField
    private final int index;
    @JsonField
    private final String key;

    public SaveFrameRequest(
            @JsonParameter(name = "index") int index,
            @JsonParameter(name = "key") String key) {
        this.index = index;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getIndex() {
        return index;
    }

}
