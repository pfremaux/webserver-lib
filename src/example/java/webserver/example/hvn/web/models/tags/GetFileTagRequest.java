package webserver.example.hvn.web.models.tags;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class GetFileTagRequest {
    @JsonField
    private String key;

    public GetFileTagRequest(@JsonParameter(name = "key") String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
