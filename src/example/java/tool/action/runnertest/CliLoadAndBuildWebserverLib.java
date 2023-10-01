package tool.action.runnertest;

import tool.config.Parameter;
import tool.config.internal.CliAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CliLoadAndBuildWebserverLib implements CliAction {
    private String shortKey;

    public static void main(String[] args) {

    }

    private static void runLinuxCommandLines() {
        // TODO PFR need to confirm
        try {
            runCommandLine("mkdir", "working_dir");
            runCommandLine("git", "clone", "git@github.com:pfremaux/webserver-lib.git", "./working_dir");
            runCommandLine("cd", "working_dir", "&", "build.bat");
            runCommandLine("cp", "working_dir\\server-lib.jar", "..", "&&", "cp", "working_dir\\server-config.properties", "..");
            runCommandLine("rmdir", "-Rf", "working_dir");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runWindowsCommandLines() {
        try {
            runCommandLine("cmd.exe", "/c", "mkdir", "working_dir");
            runCommandLine("git", "clone", "git@github.com:pfremaux/webserver-lib.git", "./working_dir");
            runCommandLine("cmd.exe", "/c", "cd", "working_dir", "&", "build.bat");
            runCommandLine("cmd.exe", "/c", "copy", "working_dir\\server-lib.jar", "..", "&", "copy", "working_dir\\server-config.properties", "..");
            runCommandLine("cmd.exe", "/c", "rmdir", "/s/q", "working_dir");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runCommandLine(String... commandLine) throws IOException, InterruptedException {
        final Process makingWorkDir = new ProcessBuilder()
                .command(commandLine)
                .start();
        executeProcess(makingWorkDir);
    }

    private static int executeProcess(Process makingWorkDir) throws InterruptedException {
        int result = makingWorkDir.waitFor();
        if (result != 0) {
            System.out.println(output(makingWorkDir.getInputStream()));
            System.out.println(output(makingWorkDir.getErrorStream()));
            System.exit(result);
        }
        return result;
    }

    private static String output(InputStream inputStream) {
        String s = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            s = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
        } catch (IOException e) {
            System.err.println("Caught IOException");
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void process(String[] parameters, int triggerIndex, Map<String, String> context) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            runWindowsCommandLines();
        } else {
            runLinuxCommandLines();
        }
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
        return Stream.concat(getRequiredParameters().stream(), getOptionalParameters().stream()).toList();
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
