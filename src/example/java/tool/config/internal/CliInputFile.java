package tool.config.internal;

import java.util.Map;

public class CliInputFile implements Cli {

    private String shortKey;

    @Override
    public String getShortKey() {
        return shortKey;
    }

    @Override
    public void setShortKey(String s) {
        this.shortKey = s;
    }

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {
        final String inputFile = parameters[triggerIndex + 1];
        context.put("InputFile", inputFile);
    }
}
