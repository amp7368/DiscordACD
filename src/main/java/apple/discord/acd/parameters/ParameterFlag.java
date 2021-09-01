package apple.discord.acd.parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterFlag {
    String usage();

    String[] flags();

    boolean ignoreCase() default true;

    String splitter() default " ";
}
