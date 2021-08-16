package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public abstract class ACDGui {
    public static final int BUTTONS_IN_ACTION_ROW = 5;
    public static final int ACTION_ROWS_IN_MESSAGE = 5;
    public static final int MAX_OPTIONS_IN_SELECTION_MENU = 25;

    protected Message message = null;

    private long lastUpdated = System.currentTimeMillis();

    protected final ACD acd;
    private final MessageChannel channel;
    private final OnInteractionCompleteMap onInteractionMap = new OnInteractionCompleteMap();

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
                onInteractionMap.put(MessageReactionAddEvent.class, definedEmojiButton.emote(), new OnInteractionDoSimple<>(this, method));
            GuiReactionCustomEmoji emojiButton = method.getAnnotation(GuiReactionCustomEmoji.class);
            if (emojiButton != null)
                onInteractionMap.put(MessageReactionAddEvent.class, emojiButton.emote(), new OnInteractionDoSimple<>(this, method));
            GuiButton actualButton = method.getAnnotation(GuiButton.class);
            if (actualButton != null)
                onInteractionMap.put(ButtonClickEvent.class, actualButton.id(), new OnInteractionDoSimple<>(this, method));
            GuiMenu menuButton = method.getAnnotation(GuiMenu.class);
            if (menuButton != null)
                onInteractionMap.put(SelectionMenuEvent.class, menuButton.id(), new OnInteractionDoSimple<>(this, method));
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

    protected void editAsReply(ComponentInteraction interaction) {
        interaction.editMessage(makeMessage()).queue();
    }

    public void addReaction(String emoji) {
        message.addReaction(emoji).queue();
        synchronized (emojisOnMessage) {
            emojisOnMessage.add(emoji);
        }
    }

    public <Key> void addManualButton(Consumer<ButtonClickEvent> consumer, Key key) {
        addManualInteraction(ButtonClickEvent.class, key, new OnInteractionDoComplex<>(consumer));
    }

    public <Key> void addManualSimpleButton(Consumer<ButtonClickEvent> consumer, Key key) {
        addManualButton(e -> {
            consumer.accept(e);
            editAsReply(e);
        }, key);
    }

    public <Key> void addManualMenu(Consumer<SelectionMenuEvent> consumer, Key key) {
        addManualInteraction(SelectionMenuEvent.class, key, new OnInteractionDoComplex<>(consumer));
    }

    public <Key> void addManualSimpleMenu(Consumer<SelectionMenuEvent> consumer, Key key) {
        addManualMenu(e -> {
            consumer.accept(e);
            editAsReply(e);
        }, key);
    }

    public <Key> void addManualReaction(Consumer<MessageReactionAddEvent> consumer, Key key) {
        addManualInteraction(MessageReactionAddEvent.class, key, new OnInteractionDoComplex<>(consumer));
    }

    public <Key, Consumed> void addManualInteraction(Class<Consumed> eventType, Key key, OnInteractionDo<Consumed> onInteraction) {
        onInteractionMap.put(eventType, key, onInteraction);
    }

    public void onButtonClick(@NotNull ButtonClickEvent event) {
        onInteractionMap.onInteraction(event.getComponentId(), event);
    }

    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        onInteractionMap.onInteraction(event.getComponentId(), event);
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
            if (onInteractionMap.onInteraction(definedEmoji == null ? emoji : definedEmoji, event)) {
                removeReaction(event);
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
