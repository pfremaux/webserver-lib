package webserver.example.action.models;

import webserver.annotations.JsonField;

import java.util.List;

public class RunActionRequest {
    @JsonField
    private final String actionName;
    @JsonField
    private final List<String> parameters;

    public RunActionRequest(String actionName, List<String> parameters) {
        this.actionName = actionName;
        this.parameters = parameters;
    }

    public String getActionName() {
        return actionName;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
