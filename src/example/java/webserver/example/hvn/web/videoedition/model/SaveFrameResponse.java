package webserver.example.hvn.web.videoedition.model;

import webserver.annotations.JsonField;

public class SaveFrameResponse {
    @JsonField
    private final String result;

    public SaveFrameResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
