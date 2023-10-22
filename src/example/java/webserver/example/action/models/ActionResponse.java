package webserver.example.action.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class ActionResponse {
    @JsonField
    private final String name;

    public ActionResponse(@JsonParameter(name = "name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
