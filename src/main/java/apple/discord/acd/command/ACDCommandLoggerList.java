package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACDCommandLoggerList {
    private Map<Integer, CommandLoggerLevel> logLevels = new HashMap<>();
    private final List<CommandLogger> loggers = new ArrayList<>();
    private CommandLoggerLevel defaultLevel = null;

    public ACDCommandLoggerList() {
        setLogLevels(DefaultCommandLoggerLevel.values(), DefaultCommandLoggerLevel.INFO);
    }

    public void setLogLevels(@NotNull CommandLoggerLevel[] levels, @NotNull CommandLoggerLevel defaultLevel) {
        this.logLevels = new HashMap<>();
        int levelVal = Integer.MIN_VALUE;
        for (CommandLoggerLevel level : levels) {
            logLevels.put(level.getLevel(), level);
        }
        this.defaultLevel = defaultLevel;
    }

    public void addLogger(CommandLogger onCommand) {
        this.loggers.add(onCommand);
    }

    public void log(@NotNull MessageReceivedEvent event, List<ACDCommandResponse> responses) {
        for (CommandLogger logger : loggers) {
            for (ACDCommandResponse response : responses) {
                if (logger.shouldLog(response.getLevel()))
                    logger.logAll(event, response);
            }
        }
    }

    public CommandLoggerLevel getDefaultLogLevel() {
        return defaultLevel;
    }
}
