package apple.discord.acd.slash.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ACDBaseCommand {
    String alias();

    String description();

    boolean isPermissionRequired() default false;
}
