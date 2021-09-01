package apple.discord.acd.text;

import apple.discord.acd.ACD;
import apple.discord.acd.parameters.ParameterVargs;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.function.BiConsumer;

public abstract class ACDChannelListenerFromUser extends ACDChannelListener {
    private final HashSet<Long> users = new HashSet<>();

    public ACDChannelListenerFromUser(ACD acd, MessageChannel channel, User... users) {
        super(acd, channel);
        for (User user : users) {
            this.users.add(user.getIdLong());
        }
    }

    @Override
    public void dealWithMessage(MessageReceivedEvent event) {
        if (!users.contains(event.getAuthor().getIdLong())) {
            return;
        }
        super.dealWithMessage(event);
    }

    public static class ListenerSimple extends ACDChannelListenerFromUser {
        private long millisToOld;
        private BiConsumer<MessageReceivedEvent, String> callback;
        private boolean nonEmpty;
        private boolean called = false;

        public ListenerSimple(ACD acd, MessageChannel channel, long millisToOld, BiConsumer<MessageReceivedEvent, String> callback, boolean nonEmpty, User... users) {
            super(acd, channel, users);
            this.millisToOld = millisToOld;
            this.callback = callback;
            this.nonEmpty = nonEmpty;
        }


        @DiscordChannelListener(order = 1)
        public void listenNonEmpty(MessageReceivedEvent event, @ParameterVargs(usage = "", nonEmpty = true) String vargs) {
            if (!called)
                callback.accept(event, vargs);
            called = true;
        }

        @DiscordChannelListener(order = 2)
        public void listen(MessageReceivedEvent event, @ParameterVargs(usage = "") String vargs) {
            if (!called)
                callback.accept(event, vargs);
        }

        @Override
        protected long getMillisToOld() {
            return millisToOld;
        }
    }
}
