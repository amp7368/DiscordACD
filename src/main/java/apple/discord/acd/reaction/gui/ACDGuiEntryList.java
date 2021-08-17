package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public abstract class ACDGuiEntryList extends ACDGuiPageable {
    public ACDGuiEntryList(ACD acd, MessageChannel channel) {
        super(acd, channel);
    }

    public ACDGuiEntryList(ACD acd, Message message) {
        super(acd, message);
    }

    public void backFullPage(ButtonClickEvent event) {
        super.back(event);
    }

    public void forwardFullPage(ButtonClickEvent event) {
        super.forward(event);
    }

    protected void sort() {
        for (DynamicPage<?> page : pagesList) {
            if (page.getPage() instanceof ACDEntryPage entryPage) {
                entryPage.sort();
            }
        }
    }

    @Override
    public void back(ButtonClickEvent event) {
        if (this.getPage() instanceof ACDEntryPage entryPage) {
            entryPage.back(event);
        }
    }


    @Override
    public void forward(ButtonClickEvent event) {
        if (this.getPage() instanceof ACDEntryPage entryPage) {
            entryPage.forward(event);
        }
    }
}
