package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ACDGui {
    public static final int BUTTONS_IN_ACTION_ROW = 5;
    public static final int ACTION_ROWS_IN_MESSAGE = 5;
    public static final int MAX_OPTIONS_IN_SELECTION_MENU = 25;

    protected Message message = null;
    private final ACDGui parent;

    private long lastUpdated = System.currentTimeMillis();

    protected final ACD acd;
    private final MessageChannel channel;
    private final OnInteractionCompleteMap onInteractionMap = new OnInteractionCompleteMap();

    private final Collection<Emote> emotesOnMessage = new ArrayList<>();
    private final Collection<String> emojisOnMessage = new ArrayList<>();
    private BiConsumer<PermissionException, ActionBeingPerformed> permissionHandler = null;
    private BiConsumer<RuntimeException, ActionBeingPerformed> exceptionHandler = null;

    private long lastUpdate = 0;

    public ACDGui(ACD acd, MessageChannel channel) {
        this.acd = acd;
        this.channel = channel;
        this.parent = null;
    }

    public ACDGui(ACD acd, Message message) {
        this.acd = acd;
        this.channel = message.getChannel();
        this.message = message;
        this.parent = null;
    }

    public ACDGui(ACD acd, MessageChannel channel, ACDGui parent) {
        this.acd = acd;
        this.channel = channel;
        this.parent = parent;
    }

    public ACDGui(ACD acd, Message message, ACDGui parent) {
        this.acd = acd;
        this.channel = message.getChannel();
        this.message = message;
        this.parent = parent;
    }

    public void makeFirstMessage() {
        try {
            if (this.message == null) {
                this.message = channel.sendMessage(this.makeMessage()).complete();
            } else {
                this.message.editMessage(this.makeMessage()).queue();
            }
            for (Method method : getClass().getMethods()) {
                // if the method is a definedEmojiButton, register it as such
                GuiReactionEmoji definedEmojiButton = method.getAnnotation(GuiReactionEmoji.class);
                if (definedEmojiButton != null)
                    addManualInteraction(MessageReactionAddEvent.class, definedEmojiButton.emote(), new OnInteractionDoSimple<>(this, method));
                GuiReactionCustomEmoji emojiButton = method.getAnnotation(GuiReactionCustomEmoji.class);
                if (emojiButton != null)
                    addManualInteraction(MessageReactionAddEvent.class, emojiButton.emote(), new OnInteractionDoSimple<>(this, method));
                GuiButton actualButton = method.getAnnotation(GuiButton.class);
                if (actualButton != null)
                    addManualInteraction(ButtonClickEvent.class, actualButton.id(), new OnInteractionDoSimple<>(this, method));
                GuiMenu menuButton = method.getAnnotation(GuiMenu.class);
                if (menuButton != null)
                    addManualInteraction(SelectionMenuEvent.class, menuButton.id(), new OnInteractionDoSimple<>(this, method));
            }
            acd.addReactable(this);
            initButtons();
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.MAKE_FIRST_MESSAGE);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.MAKE_FIRST_MESSAGE);
        }
    }

    protected abstract void initButtons();

    public void addReaction(long guildId, long emote) {
        try {
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
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.ADD_REACTION);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.ADD_REACTION);
        }
    }


    protected abstract Message makeMessage();

    protected void editMessage() {
        try {
            message.editMessage(this.makeMessage()).queue();
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.EDIT_MESSAGE);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.EDIT_MESSAGE);
        }
    }

    protected void editMessageOnTimer() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate > getMillisEditTimer()) {
            lastUpdate = now;
            editMessage();
        }
    }

    public void editAsReply(@NotNull ComponentInteraction interaction) {
        try {
            if (parent == null)
                interaction.editMessage(this.makeMessage()).queue();
            else
                parent.editAsReply(interaction);
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.EDIT_AS_REPLY);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.EDIT_AS_REPLY);
        }
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

    public <Key, Consumed> void addManualInteraction(Class<Consumed> eventType, Key
            key, OnInteractionDo<Consumed> onInteraction) {
        if (parent == null)
            onInteractionMap.put(eventType, key, onInteraction);
        else
            parent.addManualInteraction(eventType, key, onInteraction);
    }

    public void onButtonClick(@NotNull ButtonClickEvent event) {
        try {
            onInteractionMap.onInteraction(event.getComponentId(), event);
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.ON_BUTTON);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.ON_BUTTON);
        }
    }

    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        try {
            onInteractionMap.onInteraction(event.getComponentId(), event);
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.ON_SELECTION_MENU);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.ON_SELECTION_MENU);
        }
    }

    public void onReaction(@NotNull MessageReactionAddEvent event) {
        try {
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
        } catch (PermissionException e) {
            onPermissionException(e, ActionBeingPerformed.ON_REACTION);
        } catch (RuntimeException e) {
            onException(e, ActionBeingPerformed.ON_REACTION);
        }
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

    public ACDGui withPermissionExceptionHandler(BiConsumer<PermissionException, ActionBeingPerformed> permissionHandler) {
        this.permissionHandler = permissionHandler;
        return this;
    }

    public ACDGui withExceptionHandler(BiConsumer<RuntimeException, ActionBeingPerformed> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    protected void onPermissionException(PermissionException e, ActionBeingPerformed actionState) {
        if (permissionHandler == null)
            throw e;
        else permissionHandler.accept(e, actionState);
    }

    protected void onException(RuntimeException e, ActionBeingPerformed actionState) {
        if (exceptionHandler == null)
            throw e;
        else exceptionHandler.accept(e, actionState);
    }

    public void remove() {
        acd.removeReactable(this);
        doOldCompletion();
    }

    public long getId() {
        return message.getIdLong();
    }

    protected abstract long getMillisToOld();

    protected long getMillisEditTimer() {
        return -1;
    }

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
