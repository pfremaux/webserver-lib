package webserver.example.hvn.dao;

import java.util.List;

public class Paginated<T> {
    private final List<T> data;
    private final int indexStart;
    private final int indexEnd;
    private final int resultSizeRequested;

    public Paginated(List<T> data, int indexStart, int indexEnd, int resultSizeRequested) {
        this.data = data;
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.resultSizeRequested = resultSizeRequested;
    }

    public List<T> getData() {
        return data;
    }

    public int getIndexStart() {
        return indexStart;
    }

    public int getIndexEnd() {
        return indexEnd;
    }

    public int getResultSizeRequested() {
        return resultSizeRequested;
    }
}
