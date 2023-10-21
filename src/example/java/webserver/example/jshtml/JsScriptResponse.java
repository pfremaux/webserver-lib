package webserver.example.jshtml;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class JsScriptResponse {
    @JsonField
    private final String script;

    public JsScriptResponse(@JsonParameter(name = "script") String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }
}
