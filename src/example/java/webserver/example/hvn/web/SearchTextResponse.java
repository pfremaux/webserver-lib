package webserver.example.hvn.web;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

import java.util.List;

public class SearchTextResponse {
    @JsonField
    private final Pagination pagination;

    @JsonField
    private final List<Video> files;

    public SearchTextResponse(Pagination pagination, List<Video> files) {
        this.pagination = pagination;
        this.files = files;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<Video> getFiles() {
        return files;
    }
}
