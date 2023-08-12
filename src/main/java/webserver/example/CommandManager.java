package webserver.example;

import tools.MdDoc;
import tools.Singletons;
import webserver.annotations.Endpoint;
import webserver.annotations.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    @Endpoint(path = "/run", method = "POST")
    @MdDoc(description = "Run a command line in the server.")
    @Role(value = "admin")
    public RunCommandResponse runCommand(RunCommandRequest runCommandRequest) {
        final String toolName = runCommandRequest.getToolName();
        final List<String> commandStrings = new ArrayList<>();
        commandStrings.add(toolName);
        commandStrings.addAll(Arrays.asList(runCommandRequest.getParameters().split(" ")));

        final ProcessBuilder builder = new ProcessBuilder(commandStrings.toArray(new String[]{}));
        // TODO PFR3 handle more than 1 process
        final Process existingProcess = Singletons.get(Process.class);
        if (existingProcess.isAlive()) {
            return new RunCommandResponse(9, true);
        }

        try {
            final Process startedProcess = builder.start();
            Singletons.register(startedProcess);
            //int status  = start.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ;
        return new RunCommandResponse(9, false);
    }
}
