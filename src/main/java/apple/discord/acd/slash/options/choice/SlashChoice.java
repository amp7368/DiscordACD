package apple.discord.acd.slash.options.choice;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface SlashChoice {
    boolean isUserProvided();

    OptionData getOptionData();

    Object convert(SlashCommandEvent event);
}
