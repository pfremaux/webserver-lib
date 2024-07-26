package webserver.example.hvn.web;

public class Pagination {
    private final int indexStart;
    private final int indexEnd;
    private final int currentPage;
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
