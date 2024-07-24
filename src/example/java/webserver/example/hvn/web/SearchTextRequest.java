package webserver.example.hvn.web;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class SearchTextRequest {
    @JsonField
    private final String text;

    public SearchTextRequest(
            @JsonParameter(name = "text") String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
