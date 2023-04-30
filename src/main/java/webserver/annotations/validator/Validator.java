package webserver.annotations.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {

    Class<?> validator();

    String validationMethod();

    Class<?>[] inputs();
}
