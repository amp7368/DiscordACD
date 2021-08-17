package apple.discord.acd.command;

import apple.discord.acd.handler.ACDBadArgumentsException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

import static apple.discord.acd.command.ACDCommandCalled.*;

public class ACDCommandList {
    private final List<ACDCommand> commands = new ArrayList<>();
    private final Map<String, List<ACDMethodCommand>> overrideCommands = new HashMap<>();

    public void dealWithCommands(MessageReceivedEvent event) {
        for (ACDCommand command : commands) {
            command.dealWithCommand(event);
        }
        for (List<ACDMethodCommand> byName : overrideCommands.values()) {
            String usageMessage = null;
            for (ACDMethodCommand command : byName) {
                ACDCommandCalled acdCommandCalled = command.dealWithCommand(event);
                if (acdCommandCalled.called() == CallingState.CALLED) {
                    usageMessage = null;
                    break;
                }
                if (usageMessage == null && acdCommandCalled.called() == CallingState.COULD_SEND_USAGE) {
                    usageMessage = acdCommandCalled.usageMessage();
                }
            }
            if (usageMessage != null) {
                event.getChannel().sendMessage(usageMessage).queue();
            }
        }
    }

    public void addCommand(ACDCommand command) {
        commands.add(command);
    }

    public void addOverlappingCommand(ACDMethodCommand command) {
        List<ACDMethodCommand> overridden = overrideCommands.computeIfAbsent(command.getOverrideCommandId(), (k) -> new ArrayList<>());
        overridden.add(command);
        overridden.sort(Comparator.comparingInt(ACDMethodCommand::getOrder).thenComparingInt(o -> o.getPermission().getStrictness()));
    }
}
