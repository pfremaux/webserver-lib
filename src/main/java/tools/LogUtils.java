package tools;


import webserver.ServerProperties;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.*;

@MdDoc(description = "Log class used by the web server to write logs. Feel free to also use this class in your app.")
public class LogUtils {
    //private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.CANADA_FRENCH);
    private static final ZoneId ZONE_ID = ZoneId.of(ServerProperties.LOG_HOUR_ZONE_ID.getValue().orElseThrow());
    public static final String WEBSERVER_LIB = "webserver-lib";
    private static Logger logger = initLogs();

    private LogUtils() {

    }

    public static void debug(String msg, Object... params) {
        logger.log(Level.FINE, msg, params);
    }

    public static void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    public static void warning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    public static void warning(String msg, Throwable t) {
        logger.log(Level.WARNING, msg, t);
    }

    public static void error(String msg, Throwable t) {
        logger.log(Level.SEVERE, msg, t);
    }

    public static void error(String msg, Object... params) {
        logger.log(Level.SEVERE, msg, params);
    }

    public static Logger initLogs() {
        return initLogs(WEBSERVER_LIB);
    }

    private static Logger initLogs(String appName) {
        if (logger == null) {

            logger = Logger.getLogger(appName);
            if (ServerProperties.LOG_FILE.getValue().isEmpty()) {
                ConsoleHandler handler = new ConsoleHandler();
                // VERY IMPORTANT otherwise we will have logs twice (with a default handler)
                logger.setUseParentHandlers(false);

                SimpleFormatter newFormatter = getNewInstanceFormatter();

                Handler[] handlers = logger.getHandlers();
                System.out.println("existing handler : " + handlers.length);
                handler.setFormatter(newFormatter);
                handler.setLevel(Level.parse(ServerProperties.LOG_LEVEL.getValue().orElseThrow().toUpperCase()));
                logger.addHandler(handler);
            } else {
                FileHandler fh;
                try {
                    fh = new FileHandler(ServerProperties.LOG_FILE.getValue().get(), 100, 10);
                    fh.setFormatter(getNewInstanceFormatter());
                    fh.setLevel(Level.FINE);
                    // VERY IMPORTANT otherwise we will have logs twice (with a default handler)
                    logger.setUseParentHandlers(false);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return logger;
    }

    private static SimpleFormatter getNewInstanceFormatter() {
        return new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return "[%s][%s] %s\n".formatted(record.getLevel().getLocalizedName(),
                        Instant.now().atZone(ZONE_ID).format(DATE_TIME_FORMATTER),
                        record.getMessage().formatted(record.getParameters())
                );
            }
        };
    }

}
