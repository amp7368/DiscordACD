package apple.discord.acd.reaction.buttons;

@FunctionalInterface
public interface OnInteractionDo<Event> {
    void onInteraction(Event event);
}
