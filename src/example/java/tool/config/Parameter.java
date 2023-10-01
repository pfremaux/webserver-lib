package tool.config;

import tool.config.internal.Cli;
import tool.config.internal.CliInputFile;
import tool.config.internal.CliOutputFile;

import static tool.utils.Assert.requireEqual;

/**
 * Contains ALL possible parameters, whichever action the user wants to do.
 */
public enum Parameter {
    // I/O
    INPUT_FILE("-i", "Input file", new CliInputFile()),
    OUTPUT_FILE("-o", "Output file", new CliOutputFile()),

    // With action 'profile': Allows to setup server-config.properties in specific ways depending on the usage.
    PROFILE_WEB_SERVER("web-server", "Webserver profile: expose static files and endpoints", Cli.NOTHING),
    PROFILE_BACKEND_SERVER("backend-server", "Webserver profile: expose endpoints", Cli.NOTHING),
    PROFILE_FILE_SERVER("files-n-vstream", "Webserver profile: expose static files and stream videos", Cli.NOTHING),
    ;

    private String shortKey;
    private String description;
    private Cli parameterHandler;

    Parameter(String shortKey, String description, Cli parameterHandler) {
        this.shortKey = shortKey;
        this.description = description;
        this.parameterHandler = parameterHandler;
        this.parameterHandler.setShortKey(shortKey);
        requireEqual(shortKey, this.parameterHandler.getShortKey(),
                "Programmatic error: the parameter's short key(%s) should be equal to the parameter handler's short key (%s).".formatted(shortKey, this.parameterHandler.getShortKey()));
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
