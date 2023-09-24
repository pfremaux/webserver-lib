package webserver.example.savetext.web;

import webserver.annotations.JsonField;

public class SaveTextRequest {
    @JsonField
    private final String text;

    public SaveTextRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
