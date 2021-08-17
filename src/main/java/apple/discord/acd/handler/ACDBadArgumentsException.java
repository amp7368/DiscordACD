package apple.discord.acd.handler;

public class ACDBadArgumentsException extends RuntimeException {
    public ACDBadArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ACDBadArgumentsException(String message) {
        super(message);
    }
}
