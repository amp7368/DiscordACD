package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ACDCommandLoggerList {
    private final List<CommandLogger> loggers = new ArrayList<>();

    public void addLogger(CommandLogger onCommand) {
        this.loggers.add(onCommand);
    }

    public void log(@NotNull MessageReceivedEvent event, List<ACDCommandResponse> responses) {
        for (CommandLogger logger : loggers) {
            for (ACDCommandResponse response : responses) {
                logger.logAll(event, response);
            }
        }
    }
}
