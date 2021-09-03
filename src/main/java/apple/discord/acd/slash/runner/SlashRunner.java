package apple.discord.acd.slash.runner;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.ACDSlashCommandHandler;
import apple.discord.acd.slash.options.SlashOptionDefault;
import apple.discord.acd.slash.options.choice.SlashChoice;
import apple.discord.acd.slash.options.choice.SlashChoiceGiven;
import apple.discord.acd.slash.options.choice.SlashChoiceUserProvided;
import apple.discord.acd.slash.options.converter.SlashChoiceConverter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class SlashRunner implements ACDSlashCommandHandler {
    private final SlashChoice[] choices;
    private final String path;
    private final ACDSlashCommandHandler parent;
    private final Method method;
    private BiFunction<SlashCommandEvent, Object, ReplyAction> handleAfterCommand;

    public SlashRunner(ACDSlashCommandHandler parent, Method method, ACDSlashMethodCommand methodAnnotation, ACD acd, String path) {
        this.parent = parent;
        this.method = method;
        Class<?> returnType = this.method.getReturnType();
        List<MessageEmbed> embedsListClass = Collections.emptyList();
        if (returnType == Void.class) {
            handleAfterCommand = (e, o) -> null;
        } else if (returnType == String.class) {
            handleAfterCommand = (e, o) -> e.reply(o.toString());
        } else if (returnType == MessageEmbed[].class) {
            handleAfterCommand = (e, o) -> e.replyEmbeds(List.of((MessageEmbed[]) o));
        } else if (returnType == MessageEmbed.class) {
            handleAfterCommand = (e, o) -> e.replyEmbeds((MessageEmbed) o);
        } else if (returnType == embedsListClass.getClass()) {
            handleAfterCommand = (e, o) -> {
                @SuppressWarnings("unchecked") Collection<MessageEmbed> o1 = (Collection<MessageEmbed>) o;
                return e.replyEmbeds(o1);
            };
        } else if (returnType == Message.class) {
            handleAfterCommand = (e, o) -> e.reply((Message) o);
        } else if (returnType == SlashRunnerReturn.class) {
            handleAfterCommand = (e, o) -> ((SlashRunnerReturn) o).handleAfterCommand(e);
        }

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

    @Override
    public ACDSlashCommandHandler getParent() {
        return parent;
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
        ReplyAction actionToDo = handleAfterCommand.apply(event, method.invoke(parent, args));
        this.onReturnFromRunnerInit();
        if (actionToDo != null) {
            actionToDo.queue(this::onSentReplyInit, this::onSentFailureInit);
        }
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
