package apple.discord.acd.parameters;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public record ACDParameter<T>(String usage, String splitter,
                              ACDParameterConverter<T> converter,
                              OptionType optionType) {
    @NotNull
    public static String verifyName(String s) {
        return s.replace(" ", "-");
    }

    public OptionData asOption() {
        if (optionType == null) return null;
        return new OptionData(optionType, verifyName(usage), usage);
    }
}
