package webserver.example.savetext.web;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class SaveTextRequest {
    @JsonField
    private final String text;

    public SaveTextRequest(
            @JsonParameter(name = "text") String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
