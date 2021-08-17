package apple.discord.acd.parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterDefined {
    String usage();

    String splitter() default "";

    String id() default "";

    boolean optional() default false;
}
