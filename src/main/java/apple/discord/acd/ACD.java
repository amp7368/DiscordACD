package apple.discord.acd;


import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.ACDCommandList;
import apple.discord.acd.command.ACDCommandLoggerList;
import apple.discord.acd.command.ACDMethodCommand;
import apple.discord.acd.handler.ACDBadArgumentsException;
import apple.discord.acd.parameters.ACDParameterConvertersList;
import apple.discord.acd.permission.ACDPermissionAllowed;
import apple.discord.acd.permission.ACDPermissionsList;
import apple.discord.acd.reaction.ReactableMessageList;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.discord.acd.slash.ACDChoiceProviderList;
import apple.discord.acd.slash.ACDSlashCommandList;
import apple.discord.acd.slash.base.ACDSlashCommand;
import apple.discord.acd.text.ACDChannelListener;
import apple.discord.acd.text.ACDChannelListenerList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ACD extends ListenerAdapter {
    public static final String SLASH_PATH_SEPARATOR = "/";
    private final ACDPermissionsList permissions = new ACDPermissionsList();
    private final ACDCommandList commands = new ACDCommandList();
    private final ACDCommandLoggerList commandLogger = new ACDCommandLoggerList();
    private final ReactableMessageList guis = new ReactableMessageList();
    private final ACDParameterConvertersList parameterConverters = new ACDParameterConvertersList();
    private final ACDChannelListenerList channelListeners = new ACDChannelListenerList();
    private final ACDSlashCommandList slashCommands = new ACDSlashCommandList();
    private final ACDChoiceProviderList choiceProviderList = new ACDChoiceProviderList();
    private final String prefix;
    private final JDA client;
    private Consumer<Exception> messageReceivedExceptionHandler = null;
    private Consumer<Exception> addReactionExceptionHandler = null;
    private Consumer<Exception> buttonClickExceptionHandler = null;
    private Consumer<Exception> selectionMenuExceptionHandler = null;
    private Consumer<Exception> initializerExceptionHandler = null;
    private long testGuildId = 0;
    private boolean isReady = false;
    private boolean wantToUpdate = false;


    public ACD(String prefix, JDA client) {
        this.prefix = prefix;
        this.client = client;
        ACDPermissionAllowed.init();
        this.client.addEventListener(this);
    }

    public ACD(String prefix, JDA client, long testGuildId) {
        this(prefix, client);
        this.testGuildId = testGuildId;
    }

    public void setUncaughtExceptionLogger(Consumer<Exception> exceptionHandler) {
        messageReceivedExceptionHandler =
                addReactionExceptionHandler =
                        buttonClickExceptionHandler =
                                selectionMenuExceptionHandler =
                                        initializerExceptionHandler =
                                                exceptionHandler;
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

    public void setInitializerExceptionHandler(Consumer<Exception> initializerExceptionHandler) {
        this.initializerExceptionHandler = initializerExceptionHandler;
    }

    public ACDCommandLoggerList getCommandLogger() {
        return this.commandLogger;
    }

    public ACDPermissionsList getPermissions() {
        return permissions;
    }

    public ACDCommandList getCommandList() {
        return commands;
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            slashCommands.onSlashCommand(event);
        } catch (Exception e) {
            if (this.messageReceivedExceptionHandler == null)
                throw new RuntimeException(e);
            else this.messageReceivedExceptionHandler.accept(e);
        }
    }

    public synchronized void updateCommands() {
        this.wantToUpdate = true;
        internalUpdateCommands();
    }

    @Override
    public synchronized void onReady(@NotNull ReadyEvent event) {
        this.isReady = true;
        internalUpdateCommands();
    }

    private void internalUpdateCommands() {
        if (this.isReady && wantToUpdate) {
            this.wantToUpdate = false;
            if (testGuildId != 0) {
                Guild testGuild = this.client.getGuildById(testGuildId);
                if (testGuild != null) {
                    testGuild.updateCommands().addCommands(slashCommands.getCommnads()).queue();
                } else {
                    System.err.println("Test guild doesn't exist");
                }
            }
            this.client.updateCommands().addCommands(slashCommands.getCommnads()).queue();
        }
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (event.getAuthor().isBot()) {
                return;
            }
            channelListeners.dealWithMessage(event);
            commandLogger.log(event, commands.dealWithCommands(event));
        } catch (ACDBadArgumentsException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
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

    public void addCommand(ACDSlashCommand command) {
        slashCommands.addCommand(command);
    }

    public void addOverlappingCommand(ACDMethodCommand command) {
        commands.addOverlappingCommand(command);
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

    public ACDParameterConvertersList getParameterConverters() {
        return parameterConverters;
    }


    public void addChannelListener(long channelId, ACDChannelListener listener) {
        getChannelListeners().addListener(channelId, listener);
    }

    public ACDChannelListenerList getChannelListeners() {
        return channelListeners;
    }

    public void removeChannelListener(ACDChannelListener listener) {
        getChannelListeners().removeListener(listener);
    }

    public void doInitializerException(Exception e) {
        if (this.messageReceivedExceptionHandler == null)
            throw new RuntimeException(e);
        this.initializerExceptionHandler.accept(e);
    }

    public ACDChoiceProviderList getChoiceProviders() {
        return this.choiceProviderList;
    }
}
