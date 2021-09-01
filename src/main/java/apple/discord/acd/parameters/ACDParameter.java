package apple.discord.acd.parameters;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;
import java.util.function.Function;

public record ACDParameter<T>(String usage, String splitter,
                              ACDParameterConverter<T> converter,
                              OptionType optionType) {
    @NotNull
    public static String verifyName(String s) {
        return s.replace(" ", "-")
                .replaceAll("[\\[\\]]", "")
                .trim();
    }

    public static ACDParameter<?>[] getParameterTypesRequired(Method method, BiFunction<String, Class<?>, ACDParameterConverter<?>> getDefinedParameter) {
        Parameter[] parameters = method.getParameters();
        ACDParameter<?>[] typesRequired = new ACDParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            ParameterVargs vargsAnnotation = parameters[i].getAnnotation(ParameterVargs.class);
            if (vargsAnnotation != null) {
                String usage = vargsAnnotation.usage();
                boolean nonEmpty = vargsAnnotation.nonEmpty();
                typesRequired[i] = new ACDParameter<>(usage, "", (event, input) -> {
                    if (nonEmpty && input.isBlank())
                        throw new IllegalArgumentException("the input has a empty String in a nonEmpty required parameter");
                    return new ACDParameterMessageEaten<>("", input);
                }, OptionType.STRING);
            } else {
                ParameterSingle singleAnnotation = parameters[i].getAnnotation(ParameterSingle.class);
                if (singleAnnotation != null) {
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
                    typesRequired[i] = new ACDParameter<>(usage, splitter, new ACDParameterConverterSingle<>(parse, splitter), optionType);
                } else {
                    ParameterDefined parameterAnnotation = parameters[i].getAnnotation(ParameterDefined.class);
                    if (parameterAnnotation != null) {
                        typesRequired[i] = new ACDParameter<>(
                                parameterAnnotation.usage(),
                                parameterAnnotation.splitter(),
                                getDefinedParameter.apply(parameterAnnotation.id(), parameterType),
                                OptionType.STRING
                        );
                    } else {
                        ParameterFlag flagAnnotation = parameters[i].getAnnotation(ParameterFlag.class);
                        if (flagAnnotation != null) {
                            typesRequired[i] = new ACDParameter<>(flagAnnotation.usage(), flagAnnotation.splitter(), new ACDParameterConverterFlag(flagAnnotation), OptionType.STRING);
                        } else {
                            if (parameterType == MessageReceivedEvent.class) {
                                // requires the event as the message
                                typesRequired[i] = new ACDParameter<>("", "", (event, s) -> new ACDParameterMessageEaten<>(s, event), null);
                            } else if (parameterType == Member.class) {
                                typesRequired[i] = new ACDParameter<>("", "", (event, s) -> {
                                    Member member = event.getMember();
                                    if (member == null) throw new IllegalArgumentException("Member is null");
                                    return new ACDParameterMessageEaten<>(s, member);
                                }, null);
                            } else {
                                throw new IllegalStateException("The annotation for the parameter is invalid");
                            }
                        }
                    }
                }
            }
        }
        return typesRequired;
    }

    public OptionData asOption() {
        if (optionType == null) return null;
        return new OptionData(optionType, verifyName(usage), verifyName(usage));
    }
}
