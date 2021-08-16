package apple.discord.acd.reaction.gui;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface GuiPageMessageable {
    Message asMessage();
}
