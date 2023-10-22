package webserver.example.action;

import tools.Singletons;
import webserver.example.action.models.ActionRequest;
import webserver.example.action.models.ActionResponse;
import webserver.example.action.models.RunActionRequest;
import webserver.example.action.models.RunActionResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActionHandler {
    private ActionHandler() {
    }

    public static RunActionResponse runCommand(RunActionRequest request) {
        final String toolName = request.getActionName();
        final List<String> commandStrings = new ArrayList<>();
        commandStrings.add("./" + toolName);
        commandStrings.addAll(request.getParameters());

        final ProcessBuilder builder = new ProcessBuilder(commandStrings.toArray(new String[]{}));
        // TODO PFR3 handle more than 1 process
        final Process existingProcess = Singletons.get(Process.class);
        if (existingProcess.isAlive()) {
            return new RunActionResponse(toolName, 9);
        }

        try (InputStream inputStream = existingProcess.getInputStream()) {
            final String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
            return new RunActionResponse(toolName, 5);
        }

        try {
            final Process startedProcess = builder.start();
            Singletons.register(startedProcess);
            return new RunActionResponse(toolName, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new RunActionResponse(toolName, 9);
        }
    }

    public static List<ActionResponse> list() {
        final String extension = isLinux() ? ".sh" : ".bat";
        try {
            return Files.list(Path.of("."))
                    .filter(path -> path.endsWith(extension))
                    .map(path -> path.toFile().getName())
                    .map(ActionResponse::new)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static void addAction(ActionRequest request) {
        final String extension = isLinux() ? ".sh" : ".bat";
        try {
            Files.writeString(Path.of(request.getName().replaceAll(" ", "_") + extension), request.getCommand());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

    public static boolean isLinux() {
        return !isWindows();
    }

}
