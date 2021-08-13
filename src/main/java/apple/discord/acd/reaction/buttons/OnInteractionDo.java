package apple.discord.acd.reaction.buttons;

public interface OnInteractionDo<Event> {
    void onInteraction(Event event);
}
