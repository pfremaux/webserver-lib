package webserver.example.hvn.logic;

import webserver.example.hvn.dao.FileTagsDao;

public class SearchLogic {
    private final FileTagsDao fileTagsDao;

    public SearchLogic(FileTagsDao fileTagsDao) {
        this.fileTagsDao = fileTagsDao;
    }

    public void search(String texts, int page) {
        final String[] tags = texts.split(" ");
        //fileTagsDao.list(Arrays.asList(tags, ));

    }

}
