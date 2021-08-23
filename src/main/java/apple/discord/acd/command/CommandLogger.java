package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandLogger {
    void log(@NotNull MessageReceivedEvent event, ACDCommandResponse response);

    default void logAll(@NotNull MessageReceivedEvent event, ACDCommandResponse response) {
        log(event, response);
    }
}
