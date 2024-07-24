package webserver.example.hvn.dao;

import webserver.example.hvn.web.Video;

import java.util.ArrayList;
import java.util.List;

public class FileTagsDao {


    public Paginated<Video> list(List<String> criteria, int start, int end, int sizeRequested) {
        final List<Video> videos = new ArrayList<>();
        return new Paginated<>(
                videos,
                start,
                end,
                sizeRequested
        );
    }


    private List<Video> query(List<String> criteria, int start, int end) {
        final String req = "SELECT * FROM file f inner join tags t on f.tag_id = t.id";
        return List.of();
    }

}
