package apple.discord.acd.reaction.buttons;

import java.util.function.Consumer;

public record OnInteractionDoComplex<Event>(Consumer<Event> consumer) implements OnInteractionDo<Event> {
    @Override
    public void onInteraction(Event event) {
        consumer.accept(event);
    }
}
