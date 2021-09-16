package apple.discord.acd.reaction.buttons;

import java.util.HashMap;
import java.util.Map;

public class OnInteractionCompleteMap {
    private final Map<Class<?>, Map<Object, OnInteractionDo<?>>> map = new HashMap<>();

    public <Consumed, Key> void put(Class<Consumed> mapType, Key key, OnInteractionDo<Consumed> onInteraction) {
        Map<Object, OnInteractionDo<?>> onInteractionDoMap = map.computeIfAbsent(mapType, (t) -> new HashMap<>());
        onInteractionDoMap.put(key, onInteraction);
    }

    public <Consumed, Key> boolean onInteraction(Key key, Consumed consumed) {
        Map<Object, OnInteractionDo<?>> interactionMap = map.get(consumed.getClass());
        if (interactionMap == null) return false;
        // this cast is okay because put() enforces this
        OnInteractionDo<?> interactionHandler = interactionMap.get(key);
        if (interactionHandler == null) return false;
        @SuppressWarnings("unchecked")
        OnInteractionDo<Consumed> castedInteractionHandler = (OnInteractionDo<Consumed>) interactionHandler;
        castedInteractionHandler.onInteraction(consumed);
        return true;
    }
}
