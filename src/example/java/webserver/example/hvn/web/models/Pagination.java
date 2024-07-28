package webserver.example.hvn.web.models;

import webserver.annotations.JsonField;

public class Pagination {
    @JsonField
    private final int indexStart;
    @JsonField
    private final int indexEnd;
    @JsonField
    private final int currentPage;
    @JsonField
    private final int maxPage;

    public Pagination(int indexStart, int indexEnd, int currentPage, int maxPage) {
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.currentPage = currentPage;
        this.maxPage = maxPage;
    }

    public int getIndexStart() {
        return indexStart;
    }

    public int getIndexEnd() {
        return indexEnd;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        return maxPage;
    }
}
