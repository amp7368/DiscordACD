package apple.discord.acd.slash.group;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.ACDSlashCommandHandler;
import apple.discord.acd.slash.base.ACDSlashCommand;
import apple.discord.acd.slash.runner.SlashRunner;
import apple.discord.acd.slash.sub.ACDSlashSubCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.ArrayList;
import java.util.Collection;

public class ACDSlashSubGroupCommand implements ACDSlashCommandHandler {
    private final String alias;
    private final String description;
    private final SlashRunner runner;
    private final Collection<ACDSlashSubCommand> subCommands;
    private final ACDSlashCommandHandler parent;

    public ACDSlashSubGroupCommand(String path, ACD acd, ACDSlashCommandHandler parent) {
        this.parent = parent;
        ACDSubGroupCommand annotation = getClass().getAnnotation(ACDSubGroupCommand.class);
        this.alias = annotation.alias();
        path = path + ACD.SLASH_PATH_SEPARATOR + alias;
        this.description = annotation.description();
        this.runner = ACDSlashCommand.getRunner(this, getClass(), acd, path);

        // register all sub-classes that are of type ACDSlashSubGroupCommand
        this.subCommands = ACDSlashCommand.getSubCommands(this, this.getClass(), path, acd);
    }

    @Override
    public ACDSlashCommandHandler getParent() {
        return parent;
    }

    public SubcommandGroupData getCommand() {
        SubcommandGroupData subcommandGroupData = new SubcommandGroupData(alias, description);
        // add those sub-classes to the commandData to report to Discord
        Collection<SubcommandData> subcommandsData = new ArrayList<>();
        for (ACDSlashSubCommand subCommand : this.subCommands) {
            subcommandsData.add(subCommand.getCommand());
        }
        subcommandGroupData.addSubcommands(subcommandsData);
        return subcommandGroupData;
    }

    public Collection<SlashRunner> getRunners() {
        Collection<SlashRunner> runners = new ArrayList<>();
        for (ACDSlashSubCommand subCommand : subCommands) {
            runners.addAll(subCommand.getRunners());
        }
        if (runner != null)
            runners.add(runner);
        return runners;
    }
}
