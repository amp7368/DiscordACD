package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.buttons.GuiEntryStringable;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Comparator;
import java.util.List;

public abstract class ACDGuiEntryList<T extends GuiEntryStringable> extends ACDGuiPageable {
    protected String border;
    protected List<T> entries;

    public ACDGuiEntryList(ACD acd, MessageChannel message, List<T> entries) {
        super(acd, message);
        this.border = setBorder(entries);
    }

    public ACDGuiEntryList(ACD acd, Message message, List<T> entries) {
        super(acd, message);
        this.border = setBorder(entries);
    }

    private String setBorder(List<T> entries) {
        this.entries = entries;
        GuiEntryBorder borderAnnotation = getClass().getAnnotation(GuiEntryBorder.class);
        if (borderAnnotation == null) {
            return "";
        } else {
            return borderAnnotation.border();
        }
    }

    protected abstract int getEntriesPerPage();

    protected abstract int getEntriesPerSection();

    protected abstract String getDivider();

    @Override
    protected Message makeMessage() {
        int upper = Math.min(entries.size(), (page + 1) * getEntriesPerPage());
        int lower = page * getEntriesPerPage();
        StringBuilder message = new StringBuilder(this.border);
        if (!this.border.isBlank()) {
            message.append("\n");
        }
        for (int i = 0; lower < upper; i++, lower++) {
            String toAdd;
            if ((i + getFirstDashIndex()) % getEntriesPerSection() == 0) {
                toAdd = getDivider();
                if (message.length() + toAdd.length() >= Message.MAX_CONTENT_LENGTH - 4) {
                    break;
                }
                message.append(toAdd);
                message.append("\n");
            }
            T entry = entries.get(lower);
             toAdd = entry.asEntryString(i, lower);
            if (message.length() + toAdd.length() >= Message.MAX_CONTENT_LENGTH - 4) {
                break;
            }
            message.append(toAdd);
            message.append("\n");

        }
        if (!this.border.isBlank()) {
            message.append("```");
        }
        if (message.toString().isBlank()) {
            message.append("null");
        }
        return new MessageBuilder(message).build();
    }

    protected abstract int getFirstDashIndex();

    @Override
    protected int getMaxPages() {
        return this.entries.size() / getEntriesPerPage();
    }

    protected void sort(Comparator<T> comparator) {
        entries.sort(comparator);
        editMessage();
    }
}
