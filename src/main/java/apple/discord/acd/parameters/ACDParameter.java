package apple.discord.acd.parameters;

public record ACDParameter<T>(String usage, String splitter,
                              ACDParameterConverter<T> converter) {
}
