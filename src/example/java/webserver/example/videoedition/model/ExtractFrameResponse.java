package webserver.example.videoedition.model;

import webserver.annotations.JsonField;

public class ExtractFrameResponse {
    @JsonField
    private final String base64Image;

    public ExtractFrameResponse(String base64Image) {
        this.base64Image = base64Image;
    }

    public String getBase64Image() {
        return base64Image;
    }
}
