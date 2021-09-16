package apple.discord.acd.slash.runner;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

public class SlashRunnerReturn {
    public ReplyAction handleAfterCommand(SlashCommandEvent e) {
        return null;
    }
}
