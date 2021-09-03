package apple.discord.acd.slash.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashOptionDefault {
    OptionType optionType();

    String name();

    String description();

    boolean isRequired() default true;

    String[] choicesNames() default {};

    String[] choicesValues() default {};

    String choiceProvider() default "";
}
