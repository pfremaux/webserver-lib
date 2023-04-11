package tools;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Deprecated
public class AppInfo {

    private final ResourceBundle bundle;

    public AppInfo() {
        this(Locale.ENGLISH);
    }

    public AppInfo(Locale locale) {
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle("app-info", locale);
        } catch (MissingResourceException e) {
            // expected exception if no bundle exist
        } finally {
            this.bundle = bundle;
        }
    }

    public boolean isInitialized() {
        return bundle != null;
    }

    public String getAppName() {
        return getProperty("app.name");
    }

    public String getVersion() {
        return bundle.getString("app.version");
    }

    private String getGithubUrlPattern() {
        return getProperty("app.github.url.pattern");
    }

    public String getGithubUrl() {
        return MessageFormat.format(getGithubUrlPattern(), getProperty("app.github.owner"), getProperty("app.github.project.name"));
    }

    /**
     * Please set the property while running in your IDE.
     * @return
     */
    public boolean isInIde() {
        return Boolean.parseBoolean(System.getProperty("isInIDE", "true"));
    }

    public String getProperty(String s) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(s);
    }

    public String getProjectName() {
        return getProperty("app.github.project.name");
    }
}
