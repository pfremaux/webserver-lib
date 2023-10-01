package tool.action.profile;

import tool.utils.Assert;
import tool.utils.RuntimeExceptionWithContext;
import tool.config.internal.CliAction;
import tool.config.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CliProfile implements CliAction {
    public static final List<Parameter> REQUIRED_PARAMETERS = List.of();
    public static final List<Parameter> OPTIONAL_PARAMETERS = List.of(Parameter.PROFILE_FILE_SERVER, Parameter.PROFILE_BACKEND_SERVER, Parameter.PROFILE_WEB_SERVER);
    public static final List<Parameter> ALL_PARAMETERS = Stream.concat(REQUIRED_PARAMETERS.stream(), OPTIONAL_PARAMETERS.stream()).toList();
    private String shortKey;

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {
        Assert.requireGreaterOrEqual(parameters.length, triggerIndex + 2, "Please say which profile you want when running this command. Possible profiles:" + OPTIONAL_PARAMETERS.stream().map(Parameter::getShortKey).collect(Collectors.joining(", ")));
        final String profileChoice = parameters[triggerIndex + 1];


        final Path configFile = Path.of("./server-config.properties");
        final String configFileAbsolutePath = configFile.toFile().getAbsolutePath();
        Assert.requireExists(configFile, "Config file not found in the current directory. Path=" + configFileAbsolutePath);
        final List<String> lines;
        try {
            lines = Files.readAllLines(configFile);
        } catch (IOException e) {
            throw new RuntimeExceptionWithContext(e, "Error while reading config file: " + configFileAbsolutePath);
        }
        final List<String> updatedLine = new ArrayList<>();
        if (profileChoice.equals(Parameter.PROFILE_FILE_SERVER.getShortKey())) {
            Assert.requireGreaterOrEqual(parameters.length, triggerIndex + 3, "You need to give the path that will be shared.");
            final String pathConfig = parameters[triggerIndex + 2];
            final Map<String, String> keysToDisable = Map.of();
            final Map<String, String> keysToSet = Map.of(
                    "server.handler.static.files.exploration.enabled", "true",
                    "server.handler.video.files.base.directory", pathConfig,
                    "server.handler.static.files.base.directory", pathConfig
            );
            for (String line : lines) {
                updatedLine.add(transformIfNeeded(line, keysToDisable, keysToSet));
            }
        } else if (profileChoice.equals(Parameter.PROFILE_WEB_SERVER.getShortKey())) {
            final String pathConfig = parameters[triggerIndex + 2];
            final Map<String, String> keysToDisable = Map.of("server.handler.static.files.exploration.enabled", "false");
            final Map<String, String> keysToSet = Map.of(
                    "server.handler.video.files.base.directory", pathConfig,
                    "server.handler.static.files.base.directory", pathConfig
            );
            for (String line : lines) {
                updatedLine.add(transformIfNeeded(line, keysToDisable, keysToSet));
            }
        } else if (profileChoice.equals(Parameter.PROFILE_BACKEND_SERVER.getShortKey())) {
            final Map<String, String> keysToDisable = Map.of(
                    "server.handler.static.files.exploration.enabled", "false",
                    "server.handler.video.files.base.directory", "",
                    "server.handler.static.files.base.directory", "");

            final Map<String, String> keysToSet = Map.of();
            for (String line : lines) {
                updatedLine.add(transformIfNeeded(line, keysToDisable, keysToSet));
            }
        } else {
            throw new RuntimeExceptionWithContext("Unsupported profile:" + profileChoice);
        }

        try {
            Files.write(configFile, updatedLine);
        } catch (IOException e) {
            throw new RuntimeExceptionWithContext(e, "Error while writing updates in config file: " + configFileAbsolutePath);
        }
    }

    private String transformIfNeeded(String line, Map<String, String> keysToDisable, Map<String, String> keysToSet) {
        for (Map.Entry<String, String> entry : keysToDisable.entrySet()) {
            if (line.startsWith("#" + entry.getKey()) || line.startsWith("# " + entry.getKey())) {
                return line;
            }
            if (line.startsWith(entry.getKey())) {
                return "# " + line;
            }
        }
        for (Map.Entry<String, String> entry : keysToSet.entrySet()) {
            if (line.startsWith("#" + entry.getKey()) || line.startsWith("# " + entry.getKey())) {
                return entry.getKey() + "=" + entry.getValue() + "\n";
            }
            if (line.startsWith(entry.getKey())) {
                return "# " + entry.getKey() + "=" + entry.getValue() + "\n";
            }
        }
        return line;
    }

    @Override
    public String getShortKey() {
        return shortKey;
    }

    @Override
    public void setShortKey(String s) {
        this.shortKey = s;
    }

    @Override
    public List<Parameter> getAllParametersAllowed() {
        return ALL_PARAMETERS;
    }

    @Override
    public List<Parameter> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    public List<Parameter> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }
}
