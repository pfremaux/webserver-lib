package webserver.example.action;

import tools.MdDoc;
import webserver.annotations.Endpoint;
import webserver.example.action.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionEndpoints {
    @Endpoint(path = "/actions", method = "GET")
    @MdDoc(description = "List existing actions.")
    public ActionsResponse listAction(Map<String, Object> headers) {
        return new ActionsResponse(ActionHandler.list());
    }

    @Endpoint(path = "/actions/add", method = "POST")
    @MdDoc(description = "Add existing action.")
    //@Role(value = "admin")
    public ActionsResponse addAction(Map<String, Object> headers, ActionRequest request) {
        ActionHandler.addAction(request);
        return new ActionsResponse(ActionHandler.list());
    }

    @Endpoint(path = "/actions/run", method = "POST")
    @MdDoc(description = "Add existing action.")
    //@Role(value = "admin")
    public RunActionResponse execute(Map<String, Object> headers, RunActionRequest request) {
        return ActionHandler.runCommand(request);
    }

}
