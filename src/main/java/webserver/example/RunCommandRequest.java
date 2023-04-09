package webserver.example;

import webserver.annotations.JsonField;

public class RunCommandRequest {
    @JsonField
    private final String toolName;
    @JsonField
    private final String parameters;

    public RunCommandRequest(String toolName, String parameters) {
        this.toolName = toolName;
        this.parameters = parameters;
    }


    public String getToolName() {
        return toolName;
    }

    public String getParameters() {
        return parameters;
    }
}
