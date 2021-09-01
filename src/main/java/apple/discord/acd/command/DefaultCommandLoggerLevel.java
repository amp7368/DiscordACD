package apple.discord.acd.command;

public enum DefaultCommandLoggerLevel implements CommandLoggerLevel {
    CATASTROPHIC,
    ERROR,
    IMPORTANT,
    INFO,
    MINOR,
    IGNORE;

    @Override
    public int getLevel() {
        return ordinal();
    }
}
