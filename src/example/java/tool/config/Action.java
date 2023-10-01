package tool.config;

import tool.config.internal.CliAction;
import tool.action.generator.html.CliActionGenJsFromHtmlScript;
import tool.action.help.CliHelp;
import tool.action.profile.CliProfile;

public enum Action {
    GENERATE_JSHTML_FROM_HTMLSCRIPT("gen-htjs", "generate js generator (to HTML) with html script.", new CliActionGenJsFromHtmlScript()),
    HELP("-h", "Display help.", new CliHelp()),
    SET_PROFILE("profile", "Update server-config.properties to fit with the given profile.", new CliProfile()),
    ;

    private String shortKey;
    private String description;
    private CliAction actionHandler;

    Action(String shortKey, String description, CliAction actionHandler) {
        this.shortKey = shortKey;
        this.description = description;
        this.actionHandler = actionHandler;
        this.actionHandler.setShortKey(shortKey);
        if (!shortKey.equals(this.actionHandler.getShortKey())) {
            throw new IllegalStateException("" + shortKey);
        }
    }

    public String getShortKey() {
        return shortKey;
    }

    public String getDescription() {
        return description;
    }

    public CliAction getActionHandler() {
        return actionHandler;
    }

    public static Action fromActionParameter(String actionKey) {
        for (Action action : values()) {
            if (action.shortKey.equals(actionKey)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unrecognized actionKey: " + actionKey);
    }
}
