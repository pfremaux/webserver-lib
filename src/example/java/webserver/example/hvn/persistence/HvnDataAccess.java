package webserver.example.hvn.persistence;

import webserver.example.hvn.web.LocalFilesEndpoints;
import webserver.example.hvn.web.models.SimpleFileInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HvnDataAccess {

    LocalFilesEndpoints.FileIndexedForManifest getFileManifest(String key);

    Map<String, LocalFilesEndpoints.FileIndexedForManifest> scanAndGetAllKeyToFilesManifest() throws IOException;

    List<SimpleFileInfo> search(int page);


}
