package tools.security;

import java.util.*;

public class SyncedConfig {
    public interface Config {
        boolean isNumerical();
    }

    public record StringConfig(String key, String value) implements Config {
        @Override
        public boolean isNumerical() {
            return false;
        }
    }

    ;

    public record NumericConfig(String key, Integer value) implements Config {
        @Override
        public boolean isNumerical() {
            return true;
        }
    }


    private static final Map<String, Config> settings = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Class<?>, Object> singletons = Collections.synchronizedMap(new HashMap<>());

    public static <T> T register(Object value) {
        return (T) singletons.put(value.getClass(), value);
    }

    public static <T> T get(Class<T> c) {
        return (T) singletons.get(c);
    }


    public static Config register(String key, int value) {
        return settings.put(key, new NumericConfig(key, value));
    }

    public static Config register(String key, String value) {
        return settings.put(key, new StringConfig(key, value));
    }

    public static Optional<Config> getConfig(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    public static Config requireConfig(String key) {
        return Objects.requireNonNull(settings.get(key));
    }

}
