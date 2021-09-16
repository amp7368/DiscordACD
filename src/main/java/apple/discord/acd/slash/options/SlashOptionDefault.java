package apple.discord.acd.slash.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashOptionDefault {
    OptionType optionType();

    String name();

    String description();

    /**
     * used only for special cases like member vs user
     */
    SlashOptionType slashOptionType() default SlashOptionType.NORMAL;

    boolean isRequired() default true;

    String[] choicesNames() default {};

    String[] choicesValues() default {};

    String choiceProvider() default "";
}
