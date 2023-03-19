package tools;

public final class SystemUtils {

    public static final int EXIT_SUCCESSFUL = 0;
    public static final int EXIT_USER_MISTAKE = -4;
    public static final int EXIT_PROGRAMMER_ERROR = -5;
    public static final int EXIT_SYSTEM_ERROR = -6;

    private SystemUtils() {

    }

    public static void failUser(String message) {
        System.err.println(message);
        failUser();
    }
    public static void failUser() {
        System.err.println("User mistake, exiting...");
        System.exit(EXIT_USER_MISTAKE);
    }

    public static void failProgrammer(String message) {
        System.err.println(message);
        System.exit(EXIT_PROGRAMMER_ERROR);
    }

    public static void failProgrammer() {
        System.err.println("Entering in a case programmer didn't expect, exiting...");
        System.exit(EXIT_PROGRAMMER_ERROR);
    }

    public static void failSystem() {
        System.err.println("System error, exiting...");
        System.exit(EXIT_SYSTEM_ERROR);
    }

    public static void endOfApp() {
        System.exit(EXIT_SUCCESSFUL);
    }
}
