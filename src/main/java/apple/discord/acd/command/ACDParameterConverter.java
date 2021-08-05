package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface ACDParameterConverter<T> {
    ACDParameterMessageEaten<T> eatInput(MessageReceivedEvent event, String input) throws IllegalArgumentException;
}
