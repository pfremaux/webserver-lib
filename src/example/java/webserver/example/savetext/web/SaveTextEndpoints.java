package webserver.example.savetext.web;

import webserver.annotations.Endpoint;
import webserver.example.savetext.persistence.FileSystemAccess;

import java.util.Map;

public class SaveTextEndpoints {

    private final FileSystemAccess fileSystemAccess;

    public SaveTextEndpoints() {
        this.fileSystemAccess = new FileSystemAccess(".");
    }

    @Endpoint(method = "POST", path = "/text/save")
    public SaveTextResponse save(Map<String, Object> headers, SaveTextRequest saveTextRequest) {
        final String result = fileSystemAccess.horodateText(saveTextRequest.getText());
        return new SaveTextResponse(result);
    }
}
