package tool.config.internal;

import java.util.Map;

public interface Cli {
    CliNothing NOTHING = new CliNothing();
    void process(String[] parameters, int triggerIndex, Map<String, String> context);
    String getShortKey();
    void setShortKey(String s);
}
