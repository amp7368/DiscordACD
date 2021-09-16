package apple.discord.acd.slash.options.converter;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface SlashChoiceConverter<T> {
    T convert(SlashCommandEvent event, @Nullable OptionMapping optionProvided);
}
