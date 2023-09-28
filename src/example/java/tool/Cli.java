package tool;

import java.util.Map;

public interface Cli {
    void process(String[] parameters, int triggerIndex, Map<String, String> context);
    String getShortKey();
    void setShortKey(String s);
}
