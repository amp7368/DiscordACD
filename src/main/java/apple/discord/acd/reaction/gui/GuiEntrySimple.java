package apple.discord.acd.reaction.gui;

public record GuiEntrySimple(String string) implements GuiEntryStringable {
    @Override
    public String asEntryString(int indexInPage, int indexInList) {
        return string;
    }
}
