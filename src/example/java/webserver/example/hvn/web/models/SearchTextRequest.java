package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class SearchTextRequest {
    @JsonField
    private final String text;
    @JsonField
    private final int page;

    public SearchTextRequest(
            @JsonParameter(name = "text") String text,
            @JsonParameter(name = "page") int page) {
        this.text = text;
        this.page = page;
    }

    public String getText() {
        return text;
    }

    public int getPage() {
        return page;
    }
}
