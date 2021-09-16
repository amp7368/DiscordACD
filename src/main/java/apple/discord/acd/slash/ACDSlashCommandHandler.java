package apple.discord.acd.slash;

import net.dv8tion.jda.api.interactions.InteractionHook;

public interface ACDSlashCommandHandler {
    default void onReturnFromRunner() {
    }

    default void onSentReply(InteractionHook interactionHook) {
    }

    default void onSentFailure(Throwable throwable) {
    }

    default void onReturnFromRunnerInit() {
        onReturnFromRunner();
        ACDSlashCommandHandler parent = getParent();
        if (parent != null)
            parent.onReturnFromRunnerInit();
    }

    default void onSentReplyInit(InteractionHook interactionHook) {
        onSentReply(interactionHook);
        ACDSlashCommandHandler parent = getParent();
        if (parent != null) parent.onSentReplyInit(interactionHook);
    }

    default void onSentFailureInit(Throwable throwable) {
        onSentFailure(throwable);
        ACDSlashCommandHandler parent = getParent();
        if (parent != null) parent.onSentFailureInit(throwable);
    }

    default ACDSlashCommandHandler getParent() {
        return null;
    }
}
