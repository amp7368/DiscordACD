package apple.discord.acd.text;

import apple.discord.acd.ACD;
import apple.discord.acd.handler.*;
import apple.discord.acd.parameters.ACDParameterConverter;
import apple.discord.acd.permission.ACDPermissionsList;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ACDChannelListener {
    protected final ACD acd;
    protected final MessageChannel channel;
    private final ACDPermissionsList permissions;
    private final List<ACDMethodChannelListener> listeners = new ArrayList<>();
    private final List<ACDCanCatch<MessageReceivedEvent>> handlers = new ArrayList<>();
    private final long lastUpdated = System.currentTimeMillis();

    public ACDChannelListener(ACD acd, MessageChannel channel) {
        this.acd = acd;
        this.channel = channel;
        this.permissions = acd.getPermissions();
        for (Method method : getClass().getMethods()) {
            DiscordChannelListener annotation = method.getAnnotation(DiscordChannelListener.class);
            if (annotation != null) {
                ACDMethodChannelListener listener = new ACDMethodChannelListener(this, this, method, annotation);
                listeners.add(listener);
            }
            DiscordMiscExceptionHandler miscExceptionHandler = method.getAnnotation(DiscordMiscExceptionHandler.class);
            if (miscExceptionHandler != null) {
                handlers.add(new ACDMiscExceptionHandler<>(this, method, miscExceptionHandler));
            }
            DiscordExceptionHandler exceptionHandler = method.getAnnotation(DiscordExceptionHandler.class);
            if (exceptionHandler != null) {
                handlers.add(new ACDExceptionHandler<>(this, method, exceptionHandler));
            }
        }
        listeners.sort(Comparator.comparingInt(ACDMethodChannelListener::getOrder).thenComparingInt(o -> o.getPermission().getStrictness()));
        acd.addChannelListener(channel.getIdLong(), this);
    }

    public void dealWithMessage(MessageReceivedEvent event) {
        for (ACDMethodChannelListener listener : listeners) {
            try {
                ACDListenerCalled commandCalled = listener.dealWithMessage(event);
                if (commandCalled.called() == ACDListenerCalled.CallingState.CALLED) {
                    return;
                }
            } catch (Exception e) {
                boolean shouldThrow = true;
                for (ACDCanCatch<MessageReceivedEvent> handle : handlers) {
                    if (handle.canCatch(e)) {
                        try {
                            handle.doCatch(e, event);
                        } catch (InvocationTargetException | IllegalAccessException ex) {
                            ex.printStackTrace();
                            continue;
                        }
                        shouldThrow = false;
                        break;
                    }
                }
                if (shouldThrow) throw e;
            }
        }
    }

    public void remove() {
        acd.removeChannelListener(this);
        doOldCompletion();
    }

    protected void doOldCompletion() {
    }

    protected abstract long getMillisToOld();

    public boolean isOld() {
        return System.currentTimeMillis() - this.lastUpdated > getMillisToOld();
    }

    public long getId() {
        return channel.getIdLong();
    }

    public ACDPermissionsList getPermissions() {
        return permissions;
    }

    public ACDParameterConverter<?> getDefinedParameter(String id, Class<?> type) {
        return acd.getParameterConverters().get(id, type);
    }
}
