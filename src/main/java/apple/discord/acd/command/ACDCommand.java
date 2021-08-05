package apple.discord.acd.command;

import apple.discord.acd.ACD;
import apple.discord.acd.permission.ACDPermission;
import apple.discord.acd.permission.ACDPermissionsList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;


public class ACDCommand {
    private final ACDPermissionsList permissions;
    private final List<MethodCommand> commands = new ArrayList<>();
    private final String prefix;

    public ACDCommand(ACD acd) {
        permissions = acd.getPermissions();
        prefix = acd.getPrefix();
        for (Method method : getClass().getMethods()) {
            DiscordCommandAlias annotation = method.getAnnotation(DiscordCommandAlias.class);
            if (annotation != null) {
                commands.add(new MethodCommand(this, method, annotation));
            }
        }
        acd.addCommand(this);
    }

    public void dealWithCommand(MessageReceivedEvent event) {
        for (MethodCommand command : commands) {
            command.dealWithCommand(event);
        }
    }

    private class MethodCommand {
        private final ACDCommand classObj;
        private final Method method;
        private final String[] alias;
        private final boolean ignoreCase;
        private final ACDPermission permission;
        private final ACDParameter<?>[] typesRequired;
        private final String overrideUsage;
        private final String usageFormat;

        public MethodCommand(ACDCommand classObj, Method method, DiscordCommandAlias annotation) {
            this.classObj = classObj;
            this.method = method;
            this.alias = annotation.alias();
            this.overrideUsage = annotation.overrideUsage();
            this.usageFormat = annotation.usageFormat();
            this.ignoreCase = annotation.ignoreCase();
            this.permission = permissions.getPermission(annotation.permission());
            Parameter[] parameters = this.method.getParameters();
            this.typesRequired = new ACDParameter[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameterType = parameters[i].getType();
                if (parameterType == MessageReceivedEvent.class) {
                    // requires the event as the message
                    this.typesRequired[i] = new ACDParameter<>("", "", (event, s) -> new ACDParameterMessageEaten<>(s, event));
                } else {
                    ParameterVargs vargsAnnotation = parameters[i].getAnnotation(ParameterVargs.class);
                    if (vargsAnnotation == null) {
                        ParameterSingle singleAnnotation = parameters[i].getAnnotation(ParameterSingle.class);
                        String splitter = singleAnnotation.splitter();
                        String usage = singleAnnotation.usage();
                        // requires a word from the input
                        Function<String, ?> parse = null;
                        if (parameterType == long.class || parameterType ==Long.class) {
                            parse = Long::parseLong;
                        } else if (parameterType == double.class ||parameterType == Double.class) {
                            parse = Double::parseDouble;
                        } else if (parameterType == float.class ||parameterType == Float.class) {
                            parse = Float::parseFloat;
                        } else if (parameterType == int.class ||parameterType == Integer.class) {
                            parse = Integer::parseInt;
                        } else if (parameterType == short.class ||parameterType == Short.class) {
                            parse = Short::parseShort;
                        } else if (parameterType == byte.class ||parameterType == Byte.class) {
                            parse = Byte::parseByte;
                        } else if (parameterType == boolean.class ||parameterType == Boolean.class) {
                            parse = Boolean::parseBoolean;
                        }
                        if (parse == null) {
                            parse = parameterType::cast;
                        }
                        this.typesRequired[i] = new ACDParameter<>(usage, splitter, new ACDParameterConverterSingle<>(parse, splitter));
                    } else {
                        String usage = vargsAnnotation.usage();
                        this.typesRequired[i] = new ACDParameter<>(usage, "", (event, input) -> new ACDParameterMessageEaten<>("", input));
                    }
                }
            }
        }

        public void dealWithCommand(MessageReceivedEvent event) {
            if (permission.hasPermission(event.getMember())) {
                boolean shouldDealWith = false;
                int contentRemoved = 0;
                String contentRaw = event.getMessage().getContentRaw();
                if (ignoreCase) {
                    for (String name : alias) {
                        if (contentRaw.toLowerCase(Locale.ROOT).startsWith((prefix + name).toLowerCase(Locale.ROOT))) {
                            shouldDealWith = true;
                            contentRemoved = (prefix + name).length();
                            break;
                        }
                    }
                } else {
                    for (String name : alias) {
                        if (contentRaw.startsWith(prefix + name)) {
                            shouldDealWith = true;
                            contentRemoved = (prefix + name).length();
                            break;
                        }
                    }
                }
                if (shouldDealWith) {
                    try {
                        callWithArgs(event, contentRaw.substring(contentRemoved).trim());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void callWithArgs(MessageReceivedEvent event, String contentRaw) throws InvocationTargetException, IllegalAccessException {
            Object[] arguments = new Object[this.typesRequired.length];
            StringBuilder usage = new StringBuilder(prefix +alias[0]);
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
                    System.out.println(eaten.outputObject());
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage(overrideUsage.isEmpty() ? String.format(usageFormat,  usage) : overrideUsage).queue();
                    return;
                }
            }
            method.invoke(classObj, arguments);
        }
    }
}
