package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;

public class SimpleFileInfo {
    @JsonField
    private final String key;
    @JsonField
    private final String title;
    @JsonField
    private final FileMetadata metadata;

    public SimpleFileInfo(String key, String title, FileMetadata metadata) {
        this.key = key;
        this.title = title;
        this.metadata = metadata;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }
}
