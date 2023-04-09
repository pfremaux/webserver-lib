package tools;


import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.*;

public class LogUtils {
    //private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.CANADA_FRENCH);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Montreal"); // TODO PFR parameterize
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
        AppInfo appInfo = new AppInfo();
        // TODO PFR merge code in common between those 2 calls
        if (appInfo.isInIde()) {
            return initIdeLogs(appInfo);
        } else {
            return initLogsForBinaryApp(appInfo);
        }
    }

    private static Logger initIdeLogs(AppInfo appInfo) {
        if (logger == null) {
            logger = Logger.getLogger(appInfo.getAppName());
            ConsoleHandler handler = new ConsoleHandler();

            // VERY IMPORTANT otherwise we will have logs twice (with a default handler)
            logger.setUseParentHandlers(false);

            SimpleFormatter newFormatter = getNewInstanceFormatter();

            Handler[] handlers = logger.getHandlers();
            System.out.println("existing handler : " + handlers.length);
            handler.setFormatter(newFormatter);
            handler.setLevel(Level.INFO);// TODO PFR put in properties
            logger.addHandler(handler);
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

    private static Logger initLogsForBinaryApp(AppInfo appInfo) {
        if (logger == null) {
            logger = Logger.getLogger(appInfo.getAppName());
            FileHandler fh = null;
            try {
                fh = new FileHandler(appInfo.getAppName() + "-logs.log", 100, 10);
                fh.setFormatter(getNewInstanceFormatter());
                fh.setLevel(Level.FINE);
                // VERY IMPORTANT otherwise we will have logs twice (with a default handler)
                logger.setUseParentHandlers(false);
                logger.addHandler(fh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return logger;
    }

}
