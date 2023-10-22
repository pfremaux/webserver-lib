package webserver.example.action.models;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class ActionRequest {
    @JsonField
    private final String name;
    @JsonField
    private final String command;

    public ActionRequest(@JsonParameter(name = "name") String name, @JsonParameter(name = "command") String command) {
        this.name = name;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }
}
