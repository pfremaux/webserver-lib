package webserver.example.hvn.persistence;

import webserver.example.hvn.web.models.SimpleFileInfo;

import java.util.List;

public interface HvnDataAccess {

    List<SimpleFileInfo> search(int page);

}
