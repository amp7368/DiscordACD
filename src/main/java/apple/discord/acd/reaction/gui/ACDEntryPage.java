package apple.discord.acd.reaction.gui;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ACDEntryPage<Entry extends GuiEntryStringable> implements GuiPageMessageable {
    private final ACDGui gui;
    private final int entriesPerPage;
    protected List<Entry> entries = new ArrayList<>();
    private int indexInList = 0;
    private Comparator<Entry> sorter;

    public ACDEntryPage(ACDGui gui, Comparator<Entry> sorter, int entriesPerPage) {
        this.gui = gui;
        this.sorter = sorter;
        this.entriesPerPage = entriesPerPage;
    }

    @Override
    public Message asMessage() {
        int upper = Math.min(indexInList + getEntriesPerPage(), entries.size());
        List<GuiEntryNumbered<Entry>> entriesThisPage = new ArrayList<>(getEntriesPerPage());
        for (int lower = indexInList; lower < upper; lower++) {
            entriesThisPage.add(new GuiEntryNumbered<>(lower, entries.get(lower)));
        }
        return asMessage(entriesThisPage);
    }

    protected Message asMessage(List<GuiEntryNumbered<Entry>> entriesThisPage) {
        StringBuilder message = new StringBuilder();
        int i = 0;
        for (GuiEntryNumbered<Entry> entry : entriesThisPage) {
            if (i != 0) message.append("\n");
            message.append(entry.entry().asEntryString(i++, entry.indexInList()));
        }
        return new MessageBuilder(message).build();
    }

    public void sort() {
        entries.sort(sorter);
    }

    public void resetIndexInList() {
        indexInList = 0;
    }

    public void setSorter(Comparator<Entry> sorter) {
        this.sorter = sorter;
    }

    public void back(ButtonClickEvent event) {
        if (entries.isEmpty()) this.indexInList = 0;
        else this.indexInList = Math.min(entries.size() - 1, this.indexInList - getEntriesPerPage());
    }

    public void forward(ButtonClickEvent event) {
        if (entries.isEmpty()) this.indexInList = 0;
        else this.indexInList = Math.min(entries.size() - 1, this.indexInList + getEntriesPerPage());
    }

    public int getEntriesPerPage() {
        return entriesPerPage;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
    }

    public void addAllEntry(Collection<Entry> entry) {
        entries.addAll(entry);
    }

    public void removeAllEntry(Collection<Entry> entry) {
        entries.removeAll(entry);
    }

    public int getPageNumber() {
        return indexInList / getEntriesPerPage();
    }

    public ACDGui getGui() {
        return gui;
    }
}
