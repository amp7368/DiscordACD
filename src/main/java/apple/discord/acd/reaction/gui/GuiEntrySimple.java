package apple.discord.acd.reaction.gui;

import apple.discord.acd.reaction.buttons.GuiEntryStringable;

public record GuiEntrySimple(String string) implements GuiEntryStringable {
    @Override
    public String asEntryString(int indexInPage, int indexInList) {
        return string;
    }
}
