package apple.discord.acd.reaction;

import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGui;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;

public class ReactableMessageList {
    private final HashMap<Long, ACDGui> guis = new HashMap<>();

    public ReactableMessageList() {
        new Thread(() -> {
            while (true) {
                synchronized (guis) {
                    Iterator<ACDGui> iterator = guis.values().iterator();
                    while (iterator.hasNext()) {
                        ACDGui gui = iterator.next();
                        if (gui.isOld()) {
                            iterator.remove();
                            gui.doOldCompletion();
                        }
                    }
                }
                try {
                    Thread.sleep(MillisTimeUnits.MINUTE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void remove(ACDGui gui) {
        synchronized (guis) {
            guis.remove(gui.getId());
        }
    }

    public void register(ACDGui gui) {
        synchronized (guis) {
            guis.put(gui.getId(), gui);
        }
    }

    public void onReaction(@NotNull MessageReactionAddEvent event) {
        long id = event.getMessageIdLong();
        ACDGui gui;
        synchronized (guis) {
            gui = guis.get(id);
        }
        if (gui != null) {
            gui.onReaction(event);
        }
    }

    public void onButtonClick(ButtonClickEvent event) {
        long id = event.getMessageIdLong();
        ACDGui gui;
        synchronized (guis) {
            gui = guis.get(id);
        }
        if (gui != null) {
            gui.onButtonClick(event);
        }
    }

    public void onSelectionMenu(SelectionMenuEvent event) {
        long id = event.getMessageIdLong();
        ACDGui gui;
        synchronized (guis) {
            gui = guis.get(id);
        }
        if (gui != null) {
            gui.onSelectionMenu(event);
        }
    }
}
