package apple.discord.acd.command;

import apple.discord.acd.ACD;
import apple.discord.acd.handler.*;
import apple.discord.acd.parameters.ACDParameterConverter;
import apple.discord.acd.permission.ACDPermissionsList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ACDCommand {
    private final List<ACDCanCatch<MessageReceivedEvent>> handlers = new ArrayList<>();
    private final List<ACDMethodCommand> commands = new ArrayList<>();
    private final ACDPermissionsList permissions;
    private final String prefix;
    private final List<CommandLogger> loggers = new ArrayList<>();
    protected ACD acd;

    public ACDCommand(ACD acd) {
        this.acd = acd;
        List<ACDMethodCommand> overrideCommands = new ArrayList<>();
        permissions = acd.getPermissions();
        prefix = acd.getPrefix();
        for (Method method : getClass().getMethods()) {
            DiscordCommandAlias annotation = method.getAnnotation(DiscordCommandAlias.class);
            if (annotation != null) {
                ACDMethodCommand command = new ACDMethodCommand(this, this, method, annotation);
                if (command.getOverrideCommandId().isBlank()) {
                    commands.add(command);
                } else {
                    overrideCommands.add(command);
                }
            }
            DiscordMiscExceptionHandler miscExceptionHandler = method.getAnnotation(DiscordMiscExceptionHandler.class);
            if (miscExceptionHandler != null) {
                handlers.add(new ACDMiscExceptionHandler<>(this, method, miscExceptionHandler));
            }
            DiscordExceptionHandler exceptionHandler = method.getAnnotation(DiscordExceptionHandler.class);
            if (exceptionHandler != null) {
                handlers.add(new ACDExceptionHandler<>(this, method, exceptionHandler));
            }
        }
        for (ACDMethodCommand command : overrideCommands) {
            acd.addOverlappingCommand(command);
        }
        acd.addCommand(this);
    }

    public void addLogger(CommandLogger logger) {
        loggers.add(logger);
    }

    public List<ACDCommandResponse> dealWithCommand(MessageReceivedEvent event) {
        List<ACDCommandResponse> responses = new ArrayList<>();
        String usageMessage = null;
        boolean wasCalled = false;
        for (ACDMethodCommand command : commands) {
            try {
                ACDCommandCalled commandCalled = command.dealWithCommand(event);
                if (commandCalled.called() == ACDCommandCalled.CallingState.CALLED) {
                    responses.add(commandCalled.response());
                    wasCalled = true;
                } else if (usageMessage == null && commandCalled.called() == ACDCommandCalled.CallingState.COULD_SEND_USAGE) {
                    usageMessage = commandCalled.usageMessage();
                }
            } catch (Exception e) {
                boolean shouldThrow = true;
                for (ACDCanCatch<MessageReceivedEvent> handle : handlers) {
                    if (handle.canCatch(e)) {
                        try {
                            handle.doCatch(e, event);
                        } catch (InvocationTargetException | IllegalAccessException ex) {
                            ex.printStackTrace();
                            continue;
                        }
                        shouldThrow = false;
                        break;
                    }
                }
                if (shouldThrow) throw e;
            }
        }
        for (CommandLogger logger : loggers) {
            for (ACDCommandResponse response : responses) {
                logger.logAll(event, response);
            }
        }
        if (!wasCalled && usageMessage != null) {
            event.getChannel().sendMessage(usageMessage).queue();
        }
        return responses;
    }

    public ACDPermissionsList getPermissions() {
        return permissions;
    }

    public String getPrefix() {
        return prefix;
    }

    public <T> ACDParameterConverter<?> getDefinedParameter(String id, Class<T> parameterType) {
        return acd.getParameterConverters().get(id, parameterType);
    }

    public Collection<? extends CommandData> getUpdatedCommnads() {
        List<CommandData> discordCommandData = new ArrayList<>();
        for (ACDMethodCommand command : commands) {
            discordCommandData.add(new CommandData(command.getName(), command.getDescription()).addOptions(command.getOptions()));
        }
        return discordCommandData;
    }
}
