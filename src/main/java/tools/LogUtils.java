package tools;


import java.io.IOException;
import java.time.Instant;
import java.util.logging.*;

public class LogUtils {
    private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
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

    public static void error(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    public static Logger initLogs() {
        AppInfo appInfo = new AppInfo();
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

            SimpleFormatter newFormatter = new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format(DEFAULT_FORMAT,
                            Instant.now().toEpochMilli(),
                            record.getLevel().getLocalizedName(),
                            record.getMessage()
                    );
                }
            };

            handler.setFormatter(newFormatter);
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);
        }

        return logger;
    }

    private static Logger initLogsForBinaryApp(AppInfo appInfo) {
        if (logger == null) {
            logger = Logger.getLogger(appInfo.getAppName());
            FileHandler fh = null;
            try {
                fh = new FileHandler(appInfo.getAppName() + "-logs.log", 100, 10);
                fh.setFormatter(new SimpleFormatter());
                fh.setLevel(Level.FINE);
                logger.setUseParentHandlers(false);
                logger.addHandler(fh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return logger;
    }

}
