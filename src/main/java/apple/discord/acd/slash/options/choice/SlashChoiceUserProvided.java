package apple.discord.acd.slash.options.choice;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.options.SlashOptionDefault;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;

public class SlashChoiceUserProvided extends SlashChoiceComputed {
    public SlashChoiceUserProvided(Parameter parameter, SlashOptionDefault annotation, ACD acd) {
        super(parameter, annotation, acd);
    }

    @Override
    public boolean isUserProvided() {
        return true;
    }

    @Override
    public Collection<Command.Choice> getExtraChoices() {
        return Collections.emptyList();
    }
}
