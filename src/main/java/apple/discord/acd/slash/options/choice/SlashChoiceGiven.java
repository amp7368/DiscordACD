package apple.discord.acd.slash.options.choice;

import apple.discord.acd.slash.options.converter.SlashChoiceConverter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public record SlashChoiceGiven(SlashChoiceConverter<?> choiceConverter) implements SlashChoice {
    @Override
    public boolean isUserProvided() {
        return false;
    }

    @Override
    public OptionData getOptionData() {
        return null;
    }

    @Override
    public Object convert(SlashCommandEvent event) {
        return choiceConverter.convert(event, null);
    }
}
