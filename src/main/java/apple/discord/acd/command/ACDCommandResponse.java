package apple.discord.acd.command;

public class ACDCommandResponse {
    public static final ACDCommandResponse EMPTY = new ACDCommandResponse();
    private final Object customReturn;
    private ACDCommand acdCommand;
    private ACDMethodCommand methodCommand;

    public ACDCommandResponse(Object r, ACDCommand acdCommand, ACDMethodCommand acdMethodCommand) {
        this.customReturn = r;
        this.acdCommand = acdCommand;
        this.methodCommand = acdMethodCommand;
    }

    public ACDCommandResponse() {
        this.customReturn = null;
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
}
