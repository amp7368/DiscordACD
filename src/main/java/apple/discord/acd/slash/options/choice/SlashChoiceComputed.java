package apple.discord.acd.slash.options.choice;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.options.SlashOptionDefault;
import apple.discord.acd.slash.options.SlashOptionType;
import apple.discord.acd.slash.options.converter.SlashChoiceConverter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

public abstract class SlashChoiceComputed implements SlashChoice {
    protected final OptionType optionType;
    protected final String name;
    protected final String description;
    protected final boolean isRequired;
    protected final String[] choicesNames;
    protected final String[] choicesValues;
    protected final SlashOptionType slashOptionType;
    private final SlashChoiceConverter<?> choiceConverter;

    public SlashChoiceComputed(Parameter parameter, SlashOptionDefault annotation, ACD acd) {
        this.optionType = annotation.optionType();
        this.name = annotation.name();
        this.description = annotation.description();
        this.isRequired = annotation.isRequired();
        this.choicesNames = annotation.choicesNames();
        this.choicesValues = annotation.choicesValues();
        this.slashOptionType = annotation.slashOptionType();
        String choiceProvider = annotation.choiceProvider();
        if (!choiceProvider.isEmpty())
            this.choiceConverter = acd.getChoiceProviders().getChoiceConverter(choiceProvider, parameter.getType());
        else
            this.choiceConverter = null;
    }

    @Override
    public OptionData getOptionData() {
        OptionData optionData = new OptionData(optionType, name, description, isRequired);
        for (int i = 0; i < choicesNames.length; i++) {
            optionData.addChoice(choicesNames[i], choicesValues[i]);
        }

        Collection<Command.Choice> extraChoices = getExtraChoices();
        if (!extraChoices.isEmpty())
            optionData.addChoices(extraChoices);
        return optionData;
    }

    public abstract Collection<Command.Choice> getExtraChoices();

    @Override
    public Object convert(SlashCommandEvent event) {
        if (isUserProvided()) {
            List<OptionMapping> optionsProvided = event.getOptionsByName(name);
            for (OptionMapping optionProvided : optionsProvided) {
                if (optionProvided.getType() == optionType) {
                    if (this.choiceConverter == null) {
                        return switch (optionType) {
                            case USER -> {
                                if (slashOptionType == SlashOptionType.MEMBER) yield optionProvided.getAsMember();
                                yield optionProvided.getAsUser();
                            }
                            case ROLE -> optionProvided.getAsRole();
                            case STRING -> optionProvided.getAsString();
                            case BOOLEAN -> optionProvided.getAsBoolean();
                            case INTEGER -> optionProvided.getAsLong();
                            default -> null;
                        };
                    } else {
                        choiceConverter.convert(event, optionProvided);
                    }
                }
            }
        }
        return choiceConverter.convert(event, null);
    }

}
