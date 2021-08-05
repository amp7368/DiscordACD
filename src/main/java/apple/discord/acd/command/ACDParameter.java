package apple.discord.acd.command;

public record ACDParameter<T>(String usage, String splitter,
                              ACDParameterConverter<T> converter) {
}
