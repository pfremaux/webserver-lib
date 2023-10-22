package webserver.example.action.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

import java.util.List;

public class ActionsResponse {
    @JsonField
    private final List<ActionResponse> actions;

    public ActionsResponse(@JsonParameter(name = "actions") List<ActionResponse> actions) {
        this.actions = actions;
    }

    public List<ActionResponse> getActions() {
        return actions;
    }
}
