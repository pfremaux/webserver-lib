package webserver.example.savetext.web;

import webserver.annotations.JsonField;

public class SaveTextResponse {
    @JsonField
    private final String state;

    public SaveTextResponse(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
