package tool;

import tool.config.Action;

import java.util.HashMap;

import static java.lang.System.*;
import static tool.utils.Assert.requireGreaterOrEqual;

public class Tool {
    public static void main(String[] args) {
        try {
            execute(args);
        } catch (Exception e) {
            err.println(e.getLocalizedMessage());
            exit(-1);
        }
    }

    private static void execute(String[] args) {
        requireGreaterOrEqual(args.length, 1, "You must provide at least 1 parameter to say which action you want to do.");
        final String userAction = args[0];
        Action.fromActionParameter(userAction)
                .getActionHandler()
                .process(args, 0, new HashMap<>());
        out.println(userAction + " DONE");
    }
}
