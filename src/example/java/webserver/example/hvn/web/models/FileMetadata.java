package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;

import java.util.List;

public class FileMetadata {
    @JsonField
    private final String image1;
    @JsonField
    private final String image2;
    @JsonField
    private final List<String> tags;

    public FileMetadata(String image1, String image2, List<String> tags) {
        this.image1 = image1;
        this.image2 = image2;
        this.tags = tags;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }

    public List<String> getTags() {
        return tags;
    }
}
