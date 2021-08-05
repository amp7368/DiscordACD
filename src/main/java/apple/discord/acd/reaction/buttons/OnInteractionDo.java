package apple.discord.acd.reaction.buttons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record OnInteractionDo<Event>(Object onReactionObject, Method onInteraction) {
    public void onInteraction(Event event) {
        try {
            onInteraction.invoke(onReactionObject, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
