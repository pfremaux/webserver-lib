package tool.action.runnertest;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class CliLoadAndBuildWebserverLib {

    private static final List<String> MAKE_WORKING_DIR =  List.of("cmd.exe", "/c", "mkdir", "working_dir");
    private static final List<String> GIT_LOAD_WEBSERVER_LIB =  List.of("git", "clone", "git@github.com:pfremaux/webserver-lib.git", "./working_dir");
    private static final List<String> BUILD_WEBSERVER_LIB =  List.of("cmd.exe", "/c",".\\working_dir\\build.bat");
    public static void main(String[] args) {
        try {
            final Process makingWorkDir = new ProcessBuilder()
                    .command(MAKE_WORKING_DIR)
                    .start();
            execute(makingWorkDir);
            final Process loadingGitProject = new ProcessBuilder()
                    .command(GIT_LOAD_WEBSERVER_LIB)
                    .start();
            execute(loadingGitProject);
            final Process building = new ProcessBuilder()
                    .command(BUILD_WEBSERVER_LIB)
                    .start();
            execute(building);

           /* start = new ProcessBuilder()
                    .command("cmd.exe", "/c", "dir", "C:\\textures", "/p")
                    .start();*/
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(start.info());
        //System.out.println(output(start.getInputStream()));

    }

    private static int execute(Process makingWorkDir) throws InterruptedException {
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


}
