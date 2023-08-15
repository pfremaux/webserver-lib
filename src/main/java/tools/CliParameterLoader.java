package tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CliParameterLoader {
    public static final String DEFAULT_HELP_KEY = "-h";

    private final Map<String, String> mandatoryParameters;
    private final Map<String, String> optionalParameters;
    private final Map<String, String> dependantParameters;
    private final String usage;

    public CliParameterLoader(Map<String, String> mandatoryParameters, Map<String, String> optionalParameters, Map<String, String> dependantParameters) {
        if (mandatoryParameters.containsKey(DEFAULT_HELP_KEY) || optionalParameters.containsKey(DEFAULT_HELP_KEY)) {
            throw new IllegalArgumentException(DEFAULT_HELP_KEY + " parameter is not customized. It's reserved to show a summary of all parameters ot the user.");
        }
        this.mandatoryParameters = mandatoryParameters;
        this.optionalParameters = optionalParameters;
        this.dependantParameters = dependantParameters;
        final StringBuilder builder = new StringBuilder();
        if (!mandatoryParameters.isEmpty()) {
            builder.append("Mandatory parameters :\n");
            feedWithParameters(mandatoryParameters, builder);
            builder.append("\n");
        }
        if (!optionalParameters.isEmpty()) {
            builder.append("Optional parameters :\n");
            feedWithParameters(optionalParameters, builder);
            builder.append("\n");
        }
        this.usage = builder.toString();
    }

    private static void feedWithParameters(Map<String, String> parameters, StringBuilder builder) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append("\t");
            builder.append(entry.getKey());
            builder.append(" : ");
            builder.append(entry.getValue());
            builder.append("\n");
        }
    }

    public Map<String, String> load(String[] args) {
        final Map<String, String> result = new HashMap<>();
        boolean expectsParameterName = true;
        String key = null;
        String value;

        for (String parameterKey : mandatoryParameters.values()) {
            final String environmentVariable = cliParameterToEnvironmentVariable(parameterKey);
            Optional.ofNullable(System.getenv(environmentVariable)).ifPresent(envVarValue -> {
                LogUtils.info("Found env variable %s", environmentVariable);
                result.put(parameterKey, envVarValue);
            });
        }

        for (String parameterKey : optionalParameters.values()) {
            final String environmentVariable = cliParameterToEnvironmentVariable(parameterKey);
            Optional.ofNullable(System.getenv(environmentVariable)).ifPresent(envVarValue -> {
                LogUtils.info("Found env variable %s", environmentVariable);
                result.put(parameterKey, envVarValue);
            });
        }

        for (String parameter : args) {
            if (expectsParameterName) {
                key = parameter;
                // Was key not found anywhere ?
                if (!DEFAULT_HELP_KEY.equals(key) && !mandatoryParameters.containsKey(key) && !optionalParameters.containsKey(key)) {
                    SystemUtils.failUser("Unrecognized parameter : " + key);
                }
            } else {
                value = parameter;
                result.put(key, value);
            }
            expectsParameterName = !expectsParameterName;
        }

        for (String mandatoryParameter : mandatoryParameters.keySet()) {
            if (!result.containsKey(mandatoryParameter)) {
                SystemUtils.failUser("Mandatory key not found : " + mandatoryParameter);
            }
        }

        for (Map.Entry<String, String> entry : dependantParameters.entrySet()) {
            if (result.containsKey(entry.getKey()) ^ result.containsKey(entry.getValue())) {
                if (!result.containsKey(entry.getKey())) {
                    SystemUtils.failUser("If " + entry.getValue() + " is passed in parameter then " + entry.getKey() + " is also expected.");
                } else if (!result.containsKey(entry.getValue())) {
                    SystemUtils.failUser("If " + entry.getKey() + " is passed in parameter then " + entry.getValue() + " is also expected.");
                } else {
                    SystemUtils.failProgrammer();
                }
            }
        }

        result.put(DEFAULT_HELP_KEY, usage);
        return result;
    }

    private static String cliParameterToEnvironmentVariable(String cliParameter) {
        if (cliParameter.startsWith("--")) {
            return cliParameter.substring(2).replaceAll("-", "_").toUpperCase();
        }
        return "unsupported";
    }

}
