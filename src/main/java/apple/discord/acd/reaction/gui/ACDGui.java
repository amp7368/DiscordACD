package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ACDGui {
    protected Message message = null;

    private long lastUpdated = System.currentTimeMillis();

    private final ACD acd;
    private final MessageChannel channel;
    private final Map<DiscordEmoji, OnInteractionDo<MessageReactionAddEvent>> definedEmojiToReactionButton = new HashMap<>();
    private final Map<String, OnInteractionDo<ButtonClickEvent>> idToButton = new HashMap<>();
    private final Map<String, OnInteractionDo<SelectionMenuEvent>> idToMenu = new HashMap<>();
    private final Map<String, OnInteractionDo<MessageReactionAddEvent>> emojiToReactionButton = new HashMap<>();
    private final Collection<Emote> emotesOnMessage = new ArrayList<>();
    private final Collection<String> emojisOnMessage = new ArrayList<>();

    public ACDGui(ACD acd, MessageChannel channel) {
        this.acd = acd;
        this.channel = channel;
    }

    public ACDGui(ACD acd, Message message) {
        this.acd = acd;
        this.channel = message.getChannel();
        this.message = message;
    }

    public void makeFirstMessage() {
        if (this.message == null) {
            this.message = channel.sendMessage(this.makeMessage()).complete();
        } else {
            this.message.editMessage(this.makeMessage()).queue();
        }
        for (Method method : getClass().getMethods()) {
            // if the method is a definedEmojiButton, register it as such
            GuiReactionEmoji definedEmojiButton = method.getAnnotation(GuiReactionEmoji.class);
            if (definedEmojiButton != null)
                definedEmojiToReactionButton.put(definedEmojiButton.emote(), new OnInteractionDo<>(this, method));
            GuiReactionCustomEmoji emojiButton = method.getAnnotation(GuiReactionCustomEmoji.class);
            if (emojiButton != null)
                emojiToReactionButton.put(emojiButton.emote(), new OnInteractionDo<>(this, method));
            GuiButton actualButton = method.getAnnotation(GuiButton.class);
            if (actualButton != null)
                idToButton.put(actualButton.id(), new OnInteractionDo<>(this, method));
            GuiMenu menuButton = method.getAnnotation(GuiMenu.class);
            if (menuButton != null)
                idToMenu.put(menuButton.id(), new OnInteractionDo<>(this, method));
        }
        acd.addReactable(this);
        initButtons();
    }

    protected abstract void initButtons();

    public void addReaction(long guildId, long emote) {
        Guild guild = acd.getJDA().getGuildById(guildId);
        if (guild != null) {
            guild.retrieveEmoteById(emote).queue(
                    emoteReal -> {
                        synchronized (emotesOnMessage) {
                            emotesOnMessage.add(emoteReal);
                        }
                        message.addReaction(emoteReal).queue();
                    }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_EMOJI)
            );
        }
    }


    protected abstract Message makeMessage();

    protected void editMessage() {
        message.editMessage(makeMessage()).queue();
    }

    public void addReaction(String emoji) {
        message.addReaction(emoji).queue();
        synchronized (emojisOnMessage) {
            emojisOnMessage.add(emoji);
        }
    }

    public void onButtonClick(@NotNull ButtonClickEvent event) {
        OnInteractionDo<ButtonClickEvent> button = idToButton.get(event.getButton().getId());
        if (button != null) {
            button.onInteraction(event);
        }
    }

    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        OnInteractionDo<SelectionMenuEvent> menu = idToMenu.get(event.getComponentId());
        if (menu != null) {
            menu.onInteraction(event);
        }
    }

    public void onReaction(@NotNull MessageReactionAddEvent event) {
        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
        if (reactionEmote.isEmote()) {
            Emote emote = reactionEmote.getEmote();
            // todo deal with custom emotes
        } else if (reactionEmote.isEmoji()) {
            String emoji = reactionEmote.getEmoji();
            DiscordEmoji definedEmoji = null;
            try {
                definedEmoji = DiscordEmoji.get(emoji);
            } catch (IllegalArgumentException ignored) {
            }
            if (definedEmoji != null) {
                OnInteractionDo<MessageReactionAddEvent> button = this.definedEmojiToReactionButton.get(definedEmoji);
                if (button != null) {
                    button.onInteraction(event);
                    removeReaction(event);
                    return;
                }
            }
            OnInteractionDo<MessageReactionAddEvent> button = this.emojiToReactionButton.get(emoji);
            if (button != null) {
                button.onInteraction(event);
                removeReaction(event);
                return;
            }
        }

        this.lastUpdated = System.currentTimeMillis();
    }

    private void removeReaction(@NotNull MessageReactionAddEvent event) {
        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
        User user = event.getUser();
        if (user != null) {
            if (reactionEmote.isEmoji()) {
                message.removeReaction(reactionEmote.getEmoji(), user).queue();
            } else if (reactionEmote.isEmote()) {
                message.removeReaction(reactionEmote.getEmote(), user).queue();
            }
        }
    }

    public void remove() {
        acd.removeReactable(this);
        doOldCompletion();
    }

    public long getId() {
        return message.getIdLong();
    }

    protected abstract long getMillisToOld();

    public boolean isOld() {
        return System.currentTimeMillis() - this.lastUpdated > getMillisToOld();
    }

    public void doOldCompletion() {
        synchronized (emotesOnMessage) {
            for (Emote emote : emotesOnMessage)
                message.removeReaction(emote, acd.getSelfUser()).queue();
        }
        synchronized (emojisOnMessage) {
            for (String emoji : emojisOnMessage)
                message.removeReaction(emoji, acd.getSelfUser()).queue();
        }

    }
}
