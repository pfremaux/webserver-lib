package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;

import java.util.List;

public class SearchFileResponse {
    @JsonField
    private final Pagination pagination;
    @JsonField
    private final List<SimpleFileInfo> files;


    public SearchFileResponse(Pagination pagination, List<SimpleFileInfo> files) {
        this.pagination = pagination;
        this.files = files;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<SimpleFileInfo> getFiles() {
        return files;
    }
}
