package tool;

import tool.config.Action;
import tool.config.Parameter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.*;
import static tool.utils.Assert.requireGreaterOrEqual;

public class Tool {
    public static void main(String[] args) {
        try {
            args = kindWelcome(args);
            execute(args);
        } catch (Exception e) {
            err.println(e.getLocalizedMessage());
            exit(-1);
        }
    }

    private static String[] kindWelcome(String[] args) {
        if (args.length == 0) {
            final Object[] options = Arrays.stream(Action.values()).map(Action::getShortKey).toArray();
            final Object choice = JOptionPane.showInputDialog(null, "Which action do you want to do?", "Missing parameter", JOptionPane.QUESTION_MESSAGE,
                    null, options, null);
            if (choice == null) {
                System.exit(-1);
            }
            final String actionSelected = choice.toString();
            final Action action = Action.fromActionParameter(actionSelected);
            final List<Parameter> allParametersAllowed = action.getActionHandler().getAllParametersAllowed();
            if (allParametersAllowed.isEmpty()) {
                return new String[]{actionSelected};
            }
            final String message = allParametersAllowed.stream().map(parameter -> parameter.getShortKey() + " " + parameter.getDescription()).collect(Collectors.joining("\n"));

            final String parametersStr = JOptionPane.showInputDialog(null, message);
            out.println(parametersStr);
            if (parametersStr == null) {
                return new String[]{actionSelected};
            }
            final List<String> parameters = new ArrayList<>();
            boolean inQuote = false;
            final StringBuilder parametersChunks = new StringBuilder();
            for (String chunk : parametersStr.split(" ")) {
                if (chunk.startsWith("\"")) {
                    inQuote = !inQuote;
                    parametersChunks.append(chunk.substring(1));
                } else if (chunk.endsWith("\"")) {
                    inQuote = !inQuote;
                    parametersChunks.append(" ").append(chunk, 0, chunk.length() - 2);
                    parameters.add(parametersChunks.toString());
                    parametersChunks.setLength(0);
                } else {
                    if (inQuote) {
                        parametersChunks.append(" ");
                    }
                    parameters.add(chunk);
                }
            }

            return parameters.stream()
                    .map(s -> s + "\n")
                    .toArray(String[]::new);
        }
        return args;
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
