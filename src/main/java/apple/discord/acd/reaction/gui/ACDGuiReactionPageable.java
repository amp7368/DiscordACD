package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiReactionEmoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public abstract class ACDGuiReactionPageable extends ACDGui {
    protected int page = 0;

    public ACDGuiReactionPageable(ACD acd, MessageChannel channel) {
        super(acd, channel);
    }

    public ACDGuiReactionPageable(ACD acd, Message message) {
        super(acd, message);
    }

    @Override
    protected void initButtons() {
        addReaction(DiscordEmoji.LEFT.getEmoji());
        addReaction(DiscordEmoji.RIGHT.getEmoji());
    }

    @GuiReactionEmoji(emote = DiscordEmoji.LEFT)
    public void back(MessageReactionAddEvent event) {
        page = Math.max(0, page - 1);
        editMessage();
    }

    @GuiReactionEmoji(emote = DiscordEmoji.RIGHT)
    public void forward(MessageReactionAddEvent event) {
        page = Math.max(0, Math.min(getMaxPages() - 1, page + 1));
        editMessage();
    }

    protected abstract int getMaxPages();
}
