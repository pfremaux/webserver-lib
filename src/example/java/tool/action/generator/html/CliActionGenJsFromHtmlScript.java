package tool.action.generator.html;

import tool.utils.Assert;
import tool.config.internal.CliAction;
import tool.config.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CliActionGenJsFromHtmlScript implements CliAction {
    private final List<Parameter> requiredParameters = List.of(Parameter.INPUT_FILE, Parameter.OUTPUT_FILE);
    private final List<Parameter> optionalParameters = List.of();
    private final List<Parameter> allParameters = Stream.concat(requiredParameters.stream(), optionalParameters.stream()).toList();
    private String shortKey;

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {
        context.put(parameters[triggerIndex], "action");
        Optional<String> input = Optional.empty();
        Optional<String> output = Optional.empty();
        for (int i = triggerIndex + 1; i < parameters.length; i++) {
            final String parameter = parameters[i];
            if (Parameter.INPUT_FILE.getShortKey().equals(parameter)) {
                input = Optional.of(parameters[++i]);
            } else if (Parameter.OUTPUT_FILE.getShortKey().equals(parameter)) {
                output = Optional.of(parameters[++i]);
            }
        }
        Assert.require(input, "You must provide an input path with flag: " + Parameter.INPUT_FILE.getShortKey());
        Assert.require(output, "You must provide an output path with flag: " + Parameter.OUTPUT_FILE.getShortKey());
        final GenerateJsCodeToGenerateHtml generateJsCodeToGenerateHtml = new GenerateJsCodeToGenerateHtml(input.get(), output.get());
        generateJsCodeToGenerateHtml.run();
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
        return allParameters;
    }

    @Override
    public List<Parameter> getRequiredParameters() {
        return requiredParameters;
    }

    @Override
    public List<Parameter> getOptionalParameters() {
        return optionalParameters;
    }
}
