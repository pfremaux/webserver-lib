package webserver;

import java.util.Arrays;
import java.util.Optional;

public enum ServerProperties {
    LISTENING_PORT(
            "server.port",
            "8080",
            "Listening port of this HTTP server."),
    MAX_THREAD(
            "server.thread.max",
            "5",
            "Threads count the server should handle at most."),
    LOG_LEVEL("log.level", "info", "Log level. Values can be: " + Arrays.toString(LogLevel.values())),
    LOG_FILE("log.file", null, "Path and name of the log file."),
    LOG_HOUR_ZONE_ID("log.hour.zone.id", "UTC", "ZoneId that correspond to the timezone where the server is executed. " +
            "If you want to use the same hour from anywhere on Earth, use 'UTC/Greenwich'. " +
            "Otherwise, you can put 'America/Montreal' or 'Europe/Paris'."),


    // SECURITY
    AUTH_ACCOUNTS(
            "server.auth.accounts",
            "admin:",
            "Not used yet. It's one solution to define accounts and their roles. " +
                    "Format should be <login>:<password>:<role1>,<role2>;<login>:<password>:<role1>"),
    KEY_STORE_PATH(
            "server.key.store.path",
            null,
            "Path where the server can find the Java Key Store. This parameter is necessary if you want to enable HTTPS."),

    // ENDPOINTS SETTINGS
    KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH(
            "server.handler.static.files.endpoint.relative.path",
            "/web",
            "Relative path on the server the client should prefix to access to static files " +
                    "(*.html, *.js, *.jpg). For example : /static"),
    KEY_STATIC_FILES_BASE_DIRECTORY(
            "server.handler.static.files.base.directory",
            "./src/main/web",
            "Local path where the server should look for returning static files."),
    KEY_STATIC_FILES_ALLOW_EXPLORATION(
            "server.handler.static.files.exploration.enabled",
            "false",
            "Allow caller to explore directories by adding slash star at the end of the URL. i.e. http://<url>/*"
    ),

    KEY_SELF_DESCRIBE_ENDPOINT(
            "server.handler.self.describe.endpoint",
            "/self-describe",
            "Endpoint developers can call to list all existing endpoints."),
    KEY_AUTH_ENDPOINT(
            "server.handler.auth.endpoint",
            "/auth",
            "Endpoint path the client should call to authenticate. For example: /authenticate"),

    KEY_STREAM_VIDEO_ENDPOINT(
            "server.handler.video.stream.endpoint",
            "/watch",
            "Endpoint path the client should call to watch long videos. Servers can't stream big files without a dedicated handler For example: /watch"),

    KEY_VIDEO_FILES_BASE_DIRECTORY(
            "server.handler.video.files.base.directory",
            null,
            "Local path where the server should look for to stream videos."),

    KEY_CONFIG_FILE_PATH(
            "server.config.file",
            "server-config.properties",
            "Properties file name. This is the file where you'd define all these settings. " +
                    "This property isn't useful unless you decide to let this app generate a properties file by itself and you want to define a specific name."),

    KEY_GENERATE_JS_LIB_ENDPOINT(
            "server.generate.js.lib.endpoint",
            "/lib.js",
            "Javascript file the client would use to call this server's endpoint. This file generated automatically."),


    ;

    private final String key;
    private final String value;
    private final String description;

    enum LogLevel {FINE, INFO, WARN, ERROR}

    ServerProperties(String k, String v, String d) {
        this.key = k;
        this.value = v;
        this.description = d;
    }

    public String getKey() {
        return key;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(System.getProperty(key, value));
    }

    public Optional<Boolean> asBoolean() {
        return getValue().map(Boolean::valueOf);
    }

    public Optional<Integer> asInteger() {
        return getValue().map(Integer::getInteger);
    }

    public String getDescription() {
        return description;
    }
}
