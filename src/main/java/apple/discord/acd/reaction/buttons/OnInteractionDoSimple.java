package apple.discord.acd.reaction.buttons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record OnInteractionDoSimple<Event>(Object onReactionObject,
                                           Method onInteraction)
        implements OnInteractionDo<Event> {
    @Override
    public void onInteraction(Event event) {
        try {
            onInteraction.invoke(onReactionObject, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
