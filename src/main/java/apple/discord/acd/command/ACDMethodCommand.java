package apple.discord.acd.command;

import apple.discord.acd.command.ACDCommandCalled.CallingState;
import apple.discord.acd.parameters.ACDParameter;
import apple.discord.acd.parameters.ACDParameterMessageEaten;
import apple.discord.acd.permission.ACDPermission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class ACDMethodCommand {
    private final ACDCommand acdCommand;
    private final ACDCommand classObj;
    private final Method method;
    private final String[] alias;
    private final boolean ignoreCase;
    private final ACDPermission permission;
    private final ACDParameter<?>[] typesRequired;
    private final String overrideUsage;
    private final String usageFormat;
    private final String overrideCommandId;
    private final int order;
    private final ChannelType[] channelTypes;
    private final String description;

    public ACDMethodCommand(ACDCommand acdCommand, ACDCommand classObj, Method method, DiscordCommandAlias annotation) {
        this.acdCommand = acdCommand;
        this.classObj = classObj;
        this.method = method;
        this.alias = annotation.alias();
        this.overrideUsage = annotation.overrideUsage();
        this.usageFormat = annotation.usageFormat();
        this.ignoreCase = annotation.ignoreCase();
        this.overrideCommandId = annotation.overlappingCommands();
        this.order = annotation.order();
        this.channelTypes = annotation.channelType();
        this.description = annotation.description();
        this.permission = acdCommand.getPermissions().getPermission(annotation.permission());
        this.typesRequired = ACDParameter.getParameterTypesRequired(this.method, this.acdCommand::getDefinedParameter);
    }


    public ACDCommandCalled dealWithCommand(MessageReceivedEvent event) {
        if (this.channelTypes.length != 0) {
            boolean fail = true;
            for (ChannelType type : this.channelTypes) {
                if (type == event.getChannelType()) {
                    fail = false;
                    break;
                }
            }
            if (fail) {
                return new ACDCommandCalled(CallingState.NOT_CALLED, "", ACDCommandResponse.EMPTY);
            }
        }

        if (permission.hasPermission(event.getMember())) {
            boolean shouldDealWith = false;
            int contentRemoved = 0;
            String contentRaw = event.getMessage().getContentRaw();
            String aliasUsed = "";
            for (String name : alias) {
                aliasUsed = acdCommand.getPrefix() + name;
                if (ignoreCase) {
                    if (contentRaw.toLowerCase(Locale.ROOT).startsWith(aliasUsed.toLowerCase(Locale.ROOT))) {
                        shouldDealWith = true;
                        contentRemoved = aliasUsed.length();
                        break;
                    }
                } else {
                    if (contentRaw.startsWith(aliasUsed)) {
                        shouldDealWith = true;
                        contentRemoved = aliasUsed.length();
                        break;
                    }
                }
            }
            if (shouldDealWith) {
                try {
                    return callWithArgs(event, contentRaw.substring(contentRemoved).trim(), aliasUsed);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ACDCommandCalled(CallingState.NOT_CALLED, "", ACDCommandResponse.EMPTY);
    }

    private ACDCommandCalled callWithArgs(MessageReceivedEvent event, String contentRaw, String aliasUsed) throws InvocationTargetException, IllegalAccessException {
        Object[] arguments = new Object[this.typesRequired.length];
        StringBuilder usage = new StringBuilder(aliasUsed);
        String splitter = " ";
        for (int i = 0; i < this.typesRequired.length; i++) {
            // create the usage message as we parse this message
            usage.append(splitter);
            usage.append(this.typesRequired[i].usage());
            splitter = this.typesRequired[i].splitter();

            // try to eat the provided input
            try {
                ACDParameterMessageEaten<?> eaten = this.typesRequired[i].converter().eatInput(event, contentRaw);
                contentRaw = eaten.newInput();
                arguments[i] = eaten.outputObject();
            } catch (IllegalArgumentException e) {
                for (int j = i + 1; j < this.typesRequired.length; j++) {
                    usage.append(splitter);
                    usage.append(this.typesRequired[j].usage());
                    splitter = this.typesRequired[j].splitter();
                }
                return new ACDCommandCalled(CallingState.COULD_SEND_USAGE, getUsageMessage(usage), null);
            }
        }
        ACDCommandResponse response;
        if (method.getReturnType() == Void.class) {
            method.invoke(classObj, arguments);
            response = ACDCommandResponse.empty();
            response.addAcdCommand(acdCommand);
            response.addMethodCommand(this);
        } else {
            Object r = method.invoke(classObj, arguments);

            if (r instanceof ACDCommandResponse cResponse) {
                response = cResponse;
                response.addAcdCommand(acdCommand);
                response.addMethodCommand(this);
                if (response.getCallingState() != null) {
                    return new ACDCommandCalled(response.getCallingState(), getUsageMessage(usage), response);
                }
            } else if (r == null) {
                response = ACDCommandResponse.empty();
                response.addAcdCommand(acdCommand);
                response.addMethodCommand(this);
            } else {
                response = new ACDCommandResponse(r, acdCommand, this);
            }
        }
        return new ACDCommandCalled(CallingState.CALLED, getUsageMessage(usage), response);
    }

    private String getUsageMessage(StringBuilder usage) {
        return overrideUsage.isEmpty() ? String.format(usageFormat, usage) : overrideUsage;
    }

    public String getOverrideCommandId() {
        return overrideCommandId;
    }

    public int getOrder() {
        return order;
    }

    public ACDPermission getPermission() {
        return permission;
    }

    @NotNull
    public static String verifyName(String s) {
        return s.replace(" ", "-");
    }

    public String getName() {
        return verifyName(this.alias.length == 0 ? "null" : this.alias[0]);
    }

    public String getDescription() {
        return this.description.isBlank() ? getName() : this.description;
    }

    public Collection<? extends OptionData> getOptions() {
        Collection<OptionData> options = new ArrayList<>();
        for (ACDParameter<?> typeRequired : this.typesRequired) {
            OptionData option = typeRequired.asOption();
            if (option != null)
                options.add(option);
        }
        return options;
    }
}
