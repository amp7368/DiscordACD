package apple.discord.acd.slash.runner;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.options.SlashOptionDefault;
import apple.discord.acd.slash.options.choice.SlashChoice;
import apple.discord.acd.slash.options.choice.SlashChoiceGiven;
import apple.discord.acd.slash.options.choice.SlashChoiceUserProvided;
import apple.discord.acd.slash.options.converter.SlashChoiceConverter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

public class SlashRunner {
    private final SlashChoice[] choices;
    private final String path;
    private final Object thingOfSuper;
    private final Method method;

    public SlashRunner(Object thingOfSuper, Method method, ACDSlashMethodCommand methodAnnotation, ACD acd, String path) {
        this.thingOfSuper = thingOfSuper;
        this.method = method;
        this.path = path;
        Parameter[] parameters = method.getParameters();
        choices = new SlashChoice[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            SlashOptionDefault annotation = parameter.getAnnotation(SlashOptionDefault.class);
            if (annotation != null) {
                // this is a userProvided
                choices[i] = new SlashChoiceUserProvided(parameter, annotation, acd);
            } else {
                // see if there is a default SlashChoiceConverter available to just give the parameter without using a user entered value
                SlashChoiceConverter<?> choiceConverter = acd.getChoiceProviders().getChoiceConverter("", parameter.getType());
                if (choiceConverter != null) {
                    choices[i] = new SlashChoiceGiven(choiceConverter);
                } else {
                    // I won't know how to deal with this parameter when the user does the command
                    throw new NullPointerException("There is no annotation for a parameter along the ACD command path '" + this.path + "'");
                }
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void onSlashCommand(SlashCommandEvent event) throws InvocationTargetException, IllegalAccessException {
        Object[] args = new Object[choices.length];
        for (int i = 0; i < choices.length; i++) {
            SlashChoice choice = choices[i];
            args[i] = choice.convert(event);
        }
        method.invoke(thingOfSuper, args);
    }

    public Collection<? extends OptionData> getOptionsData() {
        Collection<OptionData> optionsData = new ArrayList<>();
        for (SlashChoice choice : choices) {
            OptionData optionData = choice.getOptionData();
            if (optionData != null)
                optionsData.add(optionData);
        }
        return optionsData;
    }
}
