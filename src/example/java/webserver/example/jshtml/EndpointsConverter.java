package webserver.example.jshtml;

import webserver.annotations.Endpoint;
import webserver.toolstmp.JsToHtmlCreator;

import java.util.Map;

public class EndpointsConverter {
    @Endpoint(method = "POST", path = "/convert/html-script/js")
    public JsScriptResponse htmlScriptToJs(Map<String, Object> header, ScriptRequest body) {
        final String converted;
        try {
            converted = JsToHtmlCreator.convert(body.getScript());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        return new JsScriptResponse(converted);
    }
}
