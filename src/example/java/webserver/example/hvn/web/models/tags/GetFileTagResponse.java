package webserver.example.hvn.web.models.tags;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

import java.util.List;

public class GetFileTagResponse {
    @JsonField
    private List<String> tags;

    public GetFileTagResponse(@JsonParameter(name = "tags") List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }
}
