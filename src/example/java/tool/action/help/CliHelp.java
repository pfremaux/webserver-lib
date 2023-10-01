package tool.action.help;

import tool.config.Action;
import tool.config.internal.CliAction;
import tool.config.Parameter;

import java.util.List;
import java.util.Map;

public class CliHelp implements CliAction {

    private String shortKey;

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {
        System.out.println("Required parameters:");
        for (Action value : Action.values()) {
            System.out.printf("\t%s\t%s\n", value.getShortKey(), value.getDescription());
            for (Parameter requiredParameter : value.getActionHandler().getRequiredParameters()) {
                System.out.printf("\t\t\t%s\t%s\n", requiredParameter.getShortKey(), requiredParameter.getDescription());
            }
            if (!value.getActionHandler().getOptionalParameters().isEmpty()) {
                System.out.println("Optional parameters:");
            }
            for (Parameter optionalParameter : value.getActionHandler().getOptionalParameters()) {
                System.out.printf("\t\t\t%s\t%s\n", optionalParameter.getShortKey(), optionalParameter.getDescription());
            }
        }
        System.exit(-2);
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
        return List.of();
    }

    @Override
    public List<Parameter> getRequiredParameters() {
        return List.of();
    }

    @Override
    public List<Parameter> getOptionalParameters() {
        return List.of();
    }
}
