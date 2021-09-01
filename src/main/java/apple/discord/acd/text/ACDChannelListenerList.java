package apple.discord.acd.text;

import apple.discord.acd.MillisTimeUnits;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ACDChannelListenerList {
    private final HashMap<Long, List<ACDChannelListener>> listeners = new HashMap<>();

    public ACDChannelListenerList() {
        new Thread(this::trimOld).start();
    }

    public void addListener(long channelId, ACDChannelListener listener) {
        synchronized (this) {
            listeners.computeIfAbsent(channelId, k -> new ArrayList<>()).add(listener);
        }
    }

    public void removeListener(ACDChannelListener listener) {
        synchronized (this) {
            List<ACDChannelListener> channel = listeners.get(listener.getId());
            if (channel != null) channel.remove(listener);
        }
    }

    public void dealWithMessage(MessageReceivedEvent event) {
        List<ACDChannelListener> channel;
        synchronized (this) {
            channel = listeners.get(event.getChannel().getIdLong());
            if (channel != null) {
                channel = new ArrayList<>(channel);
            }
        }
        if (channel != null) {
            for (ACDChannelListener listener : channel) {
                listener.dealWithMessage(event);
            }
        }
    }

    private void trimOld() {
        while (true) {
            synchronized (this) {
                for (List<ACDChannelListener> channel : listeners.values()) {
                    Iterator<ACDChannelListener> listenerIterator = channel.iterator();
                    while (listenerIterator.hasNext()) {
                        ACDChannelListener listener = listenerIterator.next();
                        if (listener.isOld()) {
                            listenerIterator.remove();
                            listener.doOldCompletion();
                        }
                    }
                }
            }
            try {
                Thread.sleep(MillisTimeUnits.MINUTE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
