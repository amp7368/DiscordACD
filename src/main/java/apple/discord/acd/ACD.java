package apple.discord.acd;


import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.ACDCommandList;
import apple.discord.acd.permission.ACDPermissionAllowed;
import apple.discord.acd.permission.ACDPermissionsList;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.discord.acd.reaction.ReactableMessageList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ACD extends ListenerAdapter {
    private final ACDPermissionsList permissions = new ACDPermissionsList();
    private final ACDCommandList commands = new ACDCommandList();
    private final ReactableMessageList guis = new ReactableMessageList();
    private final String prefix;
    private final JDA client;
    private Consumer<Exception> messageReceivedExceptionHandler = null;
    private Consumer<Exception> addReactionExceptionHandler = null;
    private Consumer<Exception> buttonClickExceptionHandler = null;
    private Consumer<Exception> selectionMenuExceptionHandler = null;

    public ACD(String prefix, JDA client) {
        this.prefix = prefix;
        this.client = client;
        ACDPermissionAllowed.init();
        this.client.addEventListener(this);
    }

    public void setUncaughtExceptionLogger(Consumer<Exception> exceptionHandler) {
        messageReceivedExceptionHandler = addReactionExceptionHandler = buttonClickExceptionHandler = selectionMenuExceptionHandler = exceptionHandler;
    }

    public void setMessageReceivedExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.messageReceivedExceptionHandler = exceptionHandler;
    }

    public void setAddReactionExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.addReactionExceptionHandler = exceptionHandler;
    }

    public void setButtonClickExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.buttonClickExceptionHandler = exceptionHandler;
    }

    public void setSelectionMenuExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.selectionMenuExceptionHandler = exceptionHandler;
    }

    public ACDPermissionsList getPermissions() {
        return permissions;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (event.getAuthor().isBot()) {
                return;
            }
            commands.dealWithCommands(event);
        } catch (Exception e) {
            if (this.messageReceivedExceptionHandler == null)
                throw e;
            else this.messageReceivedExceptionHandler.accept(e);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        try {
            User user = event.getUser();
            if (user != null && user.isBot()) {
                return;
            }
            guis.onReaction(event);
        } catch (Exception e) {
            if (this.addReactionExceptionHandler == null)
                throw e;
            else this.addReactionExceptionHandler.accept(e);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        try {
            User user = event.getUser();

            if (user.isBot()) {
                return;
            }
            guis.onButtonClick(event);
        } catch (Exception e) {
            if (this.buttonClickExceptionHandler == null)
                throw e;
            else this.buttonClickExceptionHandler.accept(e);
        }
    }

    @Override
    public void onSelectionMenu(@Nonnull SelectionMenuEvent event) {
        try {
            User user = event.getUser();
            if (user.isBot()) {
                return;
            }
            guis.onSelectionMenu(event);
        } catch (Exception e) {
            if (this.selectionMenuExceptionHandler == null)
                throw e;
            else this.selectionMenuExceptionHandler.accept(e);
        }
    }

    public void addCommand(ACDCommand command) {
        commands.addCommand(command);
    }

    public String getPrefix() {
        return prefix;
    }

    public void addReactable(ACDGui acdGui) {
        guis.register(acdGui);
    }

    public SelfUser getSelfUser() {
        return client.getSelfUser();
    }

    public JDA getJDA() {
        return client;
    }

    public void removeReactable(ACDGui acdGui) {
        guis.remove(acdGui);
    }
}
