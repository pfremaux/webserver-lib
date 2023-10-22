package webserver.example.action.models;

import webserver.annotations.JsonField;

public class RunActionResponse {
    @JsonField
    private final String name;
    @JsonField
    private final int status;

    public RunActionResponse(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }
}
