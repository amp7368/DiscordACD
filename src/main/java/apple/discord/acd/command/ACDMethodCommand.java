package apple.discord.acd.command;

import apple.discord.acd.command.ACDCommandCalled.CallingState;
import apple.discord.acd.parameters.*;
import apple.discord.acd.permission.ACDPermission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

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
        Parameter[] parameters = this.method.getParameters();
        this.typesRequired = new ACDParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            ParameterVargs vargsAnnotation = parameters[i].getAnnotation(ParameterVargs.class);
            if (vargsAnnotation == null) {
                ParameterSingle singleAnnotation = parameters[i].getAnnotation(ParameterSingle.class);
                if (singleAnnotation == null) {
                    ParameterDefined parameterAnnotation = parameters[i].getAnnotation(ParameterDefined.class);
                    if (parameterAnnotation == null) {
                        if (parameterType == MessageReceivedEvent.class) {
                            // requires the event as the message
                            this.typesRequired[i] = new ACDParameter<>("", "", (event, s) -> new ACDParameterMessageEaten<>(s, event), null);
                        } else if (parameterType == Member.class) {
                            this.typesRequired[i] = new ACDParameter<>("", "", (event, s) -> {
                                Member member = event.getMember();
                                if (member == null) throw new IllegalArgumentException("Member is null");
                                return new ACDParameterMessageEaten<>(s, member);
                            }, null);
                        } else {
                            throw new IllegalStateException("The annotation for the parameter is invalid");
                        }
                    } else {
                        this.typesRequired[i] = new ACDParameter<>(
                                parameterAnnotation.usage(),
                                parameterAnnotation.splitter(),
                                acdCommand.getDefinedParameter(parameterAnnotation.id(), parameterType),
                                OptionType.STRING
                        );
                    }
                } else {
                    String splitter = singleAnnotation.splitter();
                    String usage = singleAnnotation.usage();
                    // requires a word from the input
                    Function<String, ?> parse;
                    OptionType optionType;
                    if (parameterType == long.class || parameterType == Long.class) {
                        parse = Long::parseLong;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == double.class || parameterType == Double.class) {
                        parse = Double::parseDouble;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == float.class || parameterType == Float.class) {
                        parse = Float::parseFloat;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == int.class || parameterType == Integer.class) {
                        parse = Integer::parseInt;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == short.class || parameterType == Short.class) {
                        parse = Short::parseShort;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == byte.class || parameterType == Byte.class) {
                        parse = Byte::parseByte;
                        optionType = OptionType.INTEGER;
                    } else if (parameterType == boolean.class || parameterType == Boolean.class) {
                        parse = Boolean::parseBoolean;
                        optionType = OptionType.BOOLEAN;
                    } else {
                        parse = parameterType::cast;
                        optionType = OptionType.STRING;
                    }
                    this.typesRequired[i] = new ACDParameter<>(usage, splitter, new ACDParameterConverterSingle<>(parse, splitter), optionType);
                }
            } else {
                String usage = vargsAnnotation.usage();
                boolean nonEmpty = vargsAnnotation.nonEmpty();
                this.typesRequired[i] = new ACDParameter<>(usage, "", (event, input) -> {
                    if (nonEmpty && input.isBlank())
                        throw new IllegalArgumentException("the input has a empty String in a nonEmpty required parameter");
                    return new ACDParameterMessageEaten<>("", input);
                }, OptionType.STRING);
            }

        }
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
            for (String name : alias) {
                if (ignoreCase) {
                    if (contentRaw.toLowerCase(Locale.ROOT).startsWith((acdCommand.getPrefix() + name).toLowerCase(Locale.ROOT))) {
                        shouldDealWith = true;
                        contentRemoved = (acdCommand.getPrefix() + name).length();
                        break;
                    }
                } else {
                    if (contentRaw.startsWith(acdCommand.getPrefix() + name)) {
                        shouldDealWith = true;
                        contentRemoved = (acdCommand.getPrefix() + name).length();
                        break;
                    }
                }
            }
            if (shouldDealWith) {
                try {
                    return callWithArgs(event, contentRaw.substring(contentRemoved).trim());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ACDCommandCalled(CallingState.NOT_CALLED, "", ACDCommandResponse.EMPTY);
    }

    private ACDCommandCalled callWithArgs(MessageReceivedEvent event, String contentRaw) throws InvocationTargetException, IllegalAccessException {
        Object[] arguments = new Object[this.typesRequired.length];
        StringBuilder usage = new StringBuilder(acdCommand.getPrefix() + alias[0]);
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
                return new ACDCommandCalled(CallingState.COULD_SEND_USAGE, overrideUsage.isEmpty() ? String.format(usageFormat, usage) : overrideUsage, null);
            }
        }
        ACDCommandResponse response;
        if (method.getReturnType() == Void.class) {
            method.invoke(classObj, arguments);
            response = ACDCommandResponse.EMPTY;
        } else {
            Object r = method.invoke(classObj, arguments);
            if (r instanceof ACDCommandResponse cResponse) {
                response = cResponse;
                response.addAcdCommand(acdCommand);
                response.addMethodCommand(this);
            } else {
                response = new ACDCommandResponse(r, acdCommand, this);
            }
        }
        return new ACDCommandCalled(CallingState.CALLED, "", response);
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
