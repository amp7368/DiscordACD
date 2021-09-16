package apple.discord.acd.slash.sub;

import apple.discord.acd.ACD;
import apple.discord.acd.slash.ACDSlashCommandHandler;
import apple.discord.acd.slash.base.ACDSlashCommand;
import apple.discord.acd.slash.runner.SlashRunner;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Collection;
import java.util.Collections;

public class ACDSlashSubCommand implements ACDSlashCommandHandler {
    private final String description;
    private final String alias;
    private final SlashRunner runner;
    private ACDSlashCommandHandler parent;

    public ACDSlashSubCommand(String path, ACD acd, ACDSlashCommandHandler parent) {
        this.parent = parent;
        ACDSubCommand annotation = getClass().getAnnotation(ACDSubCommand.class);
        this.alias = annotation.alias();
        this.runner = ACDSlashCommand.getRunner(this, getClass(), acd, path + ACD.SLASH_PATH_SEPARATOR + alias);
        this.description = annotation.description();
    }

    @Override
    public ACDSlashCommandHandler getParent() {
        return parent;
    }

    public SubcommandData getCommand() {
        SubcommandData subcommandData = new SubcommandData(alias, description);
        if (runner != null)
            subcommandData.addOptions(runner.getOptionsData());
        return subcommandData;
    }

    public Collection<SlashRunner> getRunners() {
        return this.runner == null ? Collections.emptyList() : Collections.singleton(runner);
    }
}
