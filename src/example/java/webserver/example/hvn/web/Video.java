package webserver.example.hvn.web;

import webserver.annotations.JsonField;

import java.util.List;

public class Video {
    @JsonField
    private final String title;
    @JsonField
    private final String checksum;
    @JsonField
    private final List<String> images;

    public Video(String title, String checksum, List<String> images) {
        this.title = title;
        this.checksum = checksum;
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public String getChecksum() {
        return checksum;
    }

    public List<String> getImages() {
        return images;
    }
}
