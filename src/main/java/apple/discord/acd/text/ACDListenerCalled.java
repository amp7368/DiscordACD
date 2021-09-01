package apple.discord.acd.text;


public record ACDListenerCalled(CallingState called) {
    public enum CallingState {
        CALLED,
        NOT_CALLED
    }
}
