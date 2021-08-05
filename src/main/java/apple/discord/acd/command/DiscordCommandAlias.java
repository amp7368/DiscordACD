package apple.discord.acd.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordCommandAlias {
    String[] alias();

    String overrideUsage() default "";

    String usageFormat() default "%s";

    String permission() default "";

    boolean ignoreCase() default true;
}
