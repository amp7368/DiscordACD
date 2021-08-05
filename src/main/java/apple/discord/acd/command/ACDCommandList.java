package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class ACDCommandList {
    private final List<ACDCommand> commands = new ArrayList<>();

    public void dealWithCommands(MessageReceivedEvent event) {
        for (ACDCommand command : commands) {
            command.dealWithCommand(event);
        }
    }

    public void addCommand(ACDCommand command) {
        commands.add(command);
    }
}
