package webserver.handlers;

import tools.CliParameterLoader;
import tools.LogUtils;
import tools.SystemUtils;
import webserver.ServerProperties;
import webserver.handlers.web.auth.AccountsHandler;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*
 * TODO parse ALL classes to find out which one have @Endpoint. This is to help to generate valid properties.
 * TODO handle classpath of packages to list only classes inside of it and check which are annotated @Endpoint
 * TODO load properly accounts with roles. And also find a more secure solution to protect user passwords (SHA ?)
 * */
public class ConfigHandler {
    public static final String CONFIG_FILE = "--config-file";
    public static final String GENERATE_PROPERTIES_PARAM = "gen-prop";


    private ConfigHandler() {
    }

    public static void processCommandLineParameters(Set<String> classesPath, Set<String> providedParameters, Map<String, String> parameters) throws IOException {
        // If user passed the help key, just display help and leave.
        if (providedParameters.contains(CliParameterLoader.DEFAULT_HELP_KEY)) {
            System.out.println(parameters.get(CliParameterLoader.DEFAULT_HELP_KEY));
            SystemUtils.endOfApp();
        } else if (providedParameters.contains(GENERATE_PROPERTIES_PARAM)) {
            StringBuilder builder = new StringBuilder();
            for (ServerProperties serverProperty : ServerProperties.values()) {
                if (serverProperty.getDescription() != null) {
                    builder.append("# ").append(serverProperty.getDescription()).append('\n');
                }
                if (serverProperty.getValue().isPresent()) {
                    builder.append(serverProperty.getKey());
                    builder.append("=");
                    builder.append(serverProperty.getValue().get());
                    builder.append("\n");
                } else {
                    builder.append("# ");
                    builder.append(serverProperty.getKey());
                    builder.append("=<not set by default>");
                    builder.append("\n");
                }
            }
            int counter = 0;
            for (String classWithEndpoint : classesPath) {// TODO PFR implement the loading of classesPath from the properties keys
                builder.append("server.handlers.").append(counter).append(".endpoint.class").append("=").append(classWithEndpoint).append("\n");
                counter++;
            }
            final Path outputProperties = Path.of(ServerProperties.KEY_CONFIG_FILE_PATH.getValue().get());
            Files.writeString(outputProperties, builder.toString());
            SystemUtils.endOfApp();
        } else if (providedParameters.contains("add-account")) {
            final String login;
            byte[] hash;
            final String roles;
            final Console console = System.console();
            if (console == null) {
                LogUtils.warning("No console instance found. Assuming we're in an IDE and setup admin/admin");
                login = "admin";
                hash = AccountsHandler.hash("admin".getBytes(StandardCharsets.UTF_8), AccountsHandler.SHA_256);
                roles = "admin";
            } else {
                System.out.println("Login:");
                login = console.readLine();
                System.out.println("Password:");
                // Read password
                byte[] byteArray = new String(console.readPassword()).getBytes(StandardCharsets.UTF_8);
                hash = AccountsHandler.hash(byteArray, AccountsHandler.SHA_256);
                System.out.println("Roles (separated with commas):");
                roles = console.readLine();
            }
            final int accountsCount = AccountsHandler.accountsCount();
            System.out.println(getLoginKey(accountsCount) + "=" + login);
            System.out.println(getPasswordKey(accountsCount) + "=" + new String(hash, StandardCharsets.UTF_8));
            System.out.println(getRolesKey(accountsCount) + "=" + roles);
            SystemUtils.endOfApp();
        }
    }


    public static void processConfigFile(Set<String> providedParameters, Map<String, String> parameters) {
        LogUtils.info("Looking for a config file...");
        if (providedParameters.contains(CONFIG_FILE)) {
            final String configFile = parameters.get(CONFIG_FILE);
            ConfigHandler.loadConfigFile(configFile);
        } else if (ServerProperties.KEY_CONFIG_FILE_PATH.getValue().map(path -> Files.exists(Path.of(path))).orElse(false)) {
            final String configFile = ServerProperties.KEY_CONFIG_FILE_PATH.getValue().get();
            LogUtils.info("Default config file found [%s]", configFile);
            ConfigHandler.loadConfigFile(configFile);
        } else {
            LogUtils.warning("No config file provided, trying to run anyway...");
        }
    }


    public static void loadConfigFile(String configFile) {
        final Path configFilePath = Path.of(configFile);
        if (!Files.exists(configFilePath)) {
            LogUtils.error("Config file parameter provided but the properties file doesn't exist providedParameter=[%s], fullPath=[%s]", configFile, configFilePath.toFile().getAbsolutePath());
            SystemUtils.failUser();
        }
        LogUtils.info("Config file %s found. Loading settings...", configFile);
        final Properties p = new Properties();
        try {
            p.load(new FileInputStream(configFilePath.toFile())); // Load the properties from a file in your jar
        } catch (IOException e) {
            LogUtils.error("Failed to load the config file: " + configFilePath.toFile().getAbsolutePath(), e);
            SystemUtils.failSystem();
        }
        for (String name : p.stringPropertyNames()) {
            String value = p.getProperty(name);
            System.setProperty(name, value);
        }

        int accountCounter = 0;
        String login = null;
        while ((login = p.getProperty(getLoginKey(accountCounter))) != null) {
            accountCounter++;
            String pwd = p.getProperty(getPasswordKey(accountCounter));
            String roles = p.getProperty(getRolesKey(accountCounter));
            AccountsHandler.register(
                    login,
                    pwd,
                    Stream.of(roles.split(",")).map(String::trim).collect(Collectors.toSet())
            );

        }
    }

    private static String getRolesKey(int accountCounter) {
        return "server.account." + accountCounter + ".roles";
    }

    private static String getPasswordKey(int accountCounter) {
        return "server.account." + accountCounter + ".password";
    }

    private static String getLoginKey(int accountCounter) {
        return "server.account." + accountCounter + ".login";
    }

}
