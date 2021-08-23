package apple.discord.acd.reaction.gui;

public record GuiEntryNumbered<Entry extends GuiEntryStringable>(int indexInList, Entry entry) {
    public String asString(int indexInPage) {
        return entry.asEntryString(indexInPage, indexInList);
    }
}
