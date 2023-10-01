package tool.config.internal;

import java.util.Map;

public class CliNothing implements Cli {
    private String shortKey;

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {

    }

    @Override
    public String getShortKey() {
        return shortKey;
    }

    @Override
    public void setShortKey(String s) {
        shortKey = s;
    }
}
