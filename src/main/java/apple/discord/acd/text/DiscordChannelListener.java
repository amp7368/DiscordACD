package apple.discord.acd.text;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordChannelListener {
    String permission() default "";

    /**
     * @return lower numbers attempted first
     */
    int order() default 0;
}
