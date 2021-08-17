package apple.discord.acd.command;

public record ACDCommandCalled(CallingState called, String usageMessage) {
    public enum CallingState {
        CALLED,
        COULD_SEND_USAGE,
        NOT_CALLED
    }
}
