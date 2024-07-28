package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;

public class FileMetadata {
    @JsonField
    private final String image1;
    @JsonField
    private final String image2;

    public FileMetadata(String image1, String image2) {
        this.image1 = image1;
        this.image2 = image2;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }
}
