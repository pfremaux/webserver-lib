package tool;

public enum Parameter {
    INPUT_FILE("-i", "Input file", new CliInputFile()),
    OUTPUT_FILE("-o", "Output file", new CliOutputFile()),

    ;

    private String shortKey;
    private String description;
    private Cli parameterHandler;

    Parameter(String shortKey, String description, Cli parameterHandler) {
        this.shortKey = shortKey;
        this.description = description;
        this.parameterHandler = parameterHandler;
        this.parameterHandler.setShortKey(shortKey);
        if (!shortKey.equals(this.parameterHandler.getShortKey()) {
            throw new IllegalStateException("" + shortKey);
        }
    }

    public String getShortKey() {
        return shortKey;
    }

    public String getDescription() {
        return description;
    }

    public Cli getParameterHandler() {
        return parameterHandler;
    }
}
