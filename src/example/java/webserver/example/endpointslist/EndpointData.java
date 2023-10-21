package webserver.example.endpointslist;

import webserver.annotations.JsonField;

public class EndpointData {
    @JsonField
    private final String path;
    @JsonField
    private final String jsName;

    public EndpointData(String path, String jsName) {
        this.path = path;
        this.jsName = jsName;
    }

    public String getPath() {
        return path;
    }

    public String getJsName() {
        return jsName;
    }
}
