package webserver.example.endpointslist;

import webserver.annotations.JsonField;

public class EndpointData {
    @JsonField
    private final String path;
    @JsonField
    private final String jsName;
    @JsonField
    private final String responseExample;

    public EndpointData(String path, String jsName, String responseExample) {
        this.path = path;
        this.jsName = jsName;
        this.responseExample = responseExample;
    }

    public String getPath() {
        return path;
    }

    public String getJsName() {
        return jsName;
    }

    public String getResponseExample() {
        return responseExample;
    }
}
