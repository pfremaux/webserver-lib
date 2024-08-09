package webserver.example.hvn.web.models.tags;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

import java.util.List;

public class SetFileTagRequest {
    @JsonField
    private final String key;
    @JsonField
    private final List<String> tags;

    public SetFileTagRequest(
            @JsonParameter(name = "key") String key,
            @JsonParameter(name = "tags") List<String> tags
    ) {
        this.key = key;
        this.tags = tags;
    }

    public String getKey() {
        return key;
    }

    public List<String> getTags() {
        return tags;
    }
}
