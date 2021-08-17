package apple.discord.acd.command;

import apple.discord.acd.ACD;
import apple.discord.acd.parameters.ACDParameterConverter;
import apple.discord.acd.handler.*;
import apple.discord.acd.permission.ACDPermissionsList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class ACDCommand {
    private final List<ACDCanCatch<MessageReceivedEvent>> handlers = new ArrayList<>();
    private final List<ACDMethodCommand> commands = new ArrayList<>();
    private final ACDPermissionsList permissions;
    private final String prefix;
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

    public void dealWithCommand(MessageReceivedEvent event) {
        for (ACDMethodCommand command : commands) {
            try {
                command.dealWithCommand(event);
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
    }

    public ACDPermissionsList getPermissions() {
        return permissions;
    }

    public String getPrefix() {
        return prefix;
    }

    public <T> ACDParameterConverter<?> getDefinedParameter(String id, Class<T> parameterType) {
        return acd.getParameterConverters().get(id,parameterType);
    }
}
