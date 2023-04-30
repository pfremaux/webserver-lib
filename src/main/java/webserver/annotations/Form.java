package webserver.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Form {
    String jsMethodName();

    Class<?> validator();

    String validationMethod();

    Class<?>[] parametersToValidate();

}
