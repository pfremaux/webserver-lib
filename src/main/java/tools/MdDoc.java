package tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MdDoc {
    String description();

    /**
     * An array of references to existing source code.
     * Each reference must respect the following format :
     * <p>(package.)(ClassName):(startingLine):(endingLine):(description)</p>
     *
     * @return
     */
    String[] examples() default {};

    String url() default "";
}
