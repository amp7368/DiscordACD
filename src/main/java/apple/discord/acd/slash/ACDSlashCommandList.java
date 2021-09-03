package apple.discord.acd.slash;

import apple.discord.acd.slash.base.ACDSlashCommand;
import apple.discord.acd.slash.runner.SlashRunner;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ACDSlashCommandList {
    private final Map<String, SlashRunner> pathToSlashCommand = new HashMap<>();
    private final List<ACDSlashCommand> commands = new ArrayList<>();

    public void onSlashCommand(SlashCommandEvent event) throws InvocationTargetException, IllegalAccessException {
        SlashRunner slashRunner = pathToSlashCommand.get(event.getCommandPath());
        if (slashRunner != null)
            slashRunner.onSlashCommand(event);
        else
            event.reply(event.getCommandPath() + " is not a valid path").queue();
    }

    public Collection<? extends CommandData> getCommnads() {
        Collection<CommandData> commands = new ArrayList<>();
        for (ACDSlashCommand command : this.commands) {
            commands.add(command.getCommand());
        }
        return commands;
    }

    public void addCommand(ACDSlashCommand command) {
        for (SlashRunner runner : command.getRunners()) {
            pathToSlashCommand.put(runner.getPath(), runner);
        }
        this.commands.add(command);
    }
}
