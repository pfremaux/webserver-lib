package webserver.generators.js;

import java.util.function.Predicate;

public enum JsType {
    FUNCTION("function", o -> true),
    NUMBER("number", o -> true),
    TEXT("text", o -> true),
    BUTTON("button", o -> true),
    ANY_JSON("anyJson", o -> true);

    private final String name;
    private final Predicate<Object> validator;

    private JsType(String name, Predicate<Object> validator) {

        this.name = name;
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public static JsType fromJavaType(Class<?> c) {
        if (c.isAssignableFrom(Number.class)) {
            return JsType.NUMBER;
        }
        return JsType.TEXT;
    }

    public Predicate<Object> getValidator() {
        return validator;
    }
}
