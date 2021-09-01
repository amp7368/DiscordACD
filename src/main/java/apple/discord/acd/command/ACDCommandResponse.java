package apple.discord.acd.command;

public class ACDCommandResponse {
    public static final ACDCommandResponse EMPTY = new ACDCommandResponse();
    private CommandLoggerLevel level = null;
    private final Object customReturn;
    private ACDCommand acdCommand;
    private ACDMethodCommand methodCommand;
    private ACDCommandCalled.CallingState callingState = null;

    public ACDCommandResponse(Object r, ACDCommand acdCommand, ACDMethodCommand acdMethodCommand) {
        this.customReturn = r;
        this.acdCommand = acdCommand;
        this.methodCommand = acdMethodCommand;
    }

    public ACDCommandResponse() {
        this.customReturn = null;
    }

    public static ACDCommandResponse empty() {
        return new ACDCommandResponse();
    }

    public CommandLoggerLevel getLevel() {
        if (level == null) level = acdCommand.acd.getCommandLogger().getDefaultLogLevel();
        return level;
    }

    public void setLevel(CommandLoggerLevel level) {
        this.level = level;
    }

    public ACDCommandCalled.CallingState getCallingState() {
        return callingState;
    }

    public void addAcdCommand(ACDCommand acdCommand) {
        this.acdCommand = acdCommand;
    }

    public String getCommandAlias() {
        return this.methodCommand.getName();
    }

    public void addMethodCommand(ACDMethodCommand methodCommand) {
        this.methodCommand = methodCommand;
    }

    public void setCallingState(ACDCommandCalled.CallingState callingState) {
        this.callingState = callingState;
    }
}
