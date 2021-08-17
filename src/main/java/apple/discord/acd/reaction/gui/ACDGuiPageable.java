package apple.discord.acd.reaction.gui;

import apple.discord.acd.ACD;
import apple.discord.acd.reaction.buttons.GuiManualButton;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ACDGuiPageable extends ACDGui implements GuiPageMessageable {
    protected int page = 0;
    protected List<DynamicPage<?>> pagesList = new ArrayList<>();

    public ACDGuiPageable(ACD acd, MessageChannel channel) {
        super(acd, channel);
    }

    public ACDGuiPageable(ACD acd, Message message) {
        super(acd, message);
    }

    protected <Page extends GuiPageMessageable> void addPage(Page pageable) {
        this.pagesList.add(new DynamicPage<>(pageable));
    }

    protected <Page extends GuiPageMessageable> void addPage(int index, Page pageable) {
        this.pagesList.add(index, new DynamicPage<>(pageable));
    }

    protected <Page extends GuiPageMessageable> void addPage(Supplier<Page> pageable) {
        this.pagesList.add(new DynamicPage<>(pageable));
    }

    protected <Page extends GuiPageMessageable> void addPage(int index, Supplier<Page> pageable) {
        this.pagesList.add(index, new DynamicPage<>(pageable));
    }

    protected <Page extends GuiPageMessageable> void removePage(Page pageable) {
        this.pagesList.remove(new DynamicPage<>(pageable));
    }

    protected <Page extends GuiPageMessageable> void removePage(Supplier<Page> pageable) {
        this.pagesList.remove(new DynamicPage<>(pageable));
    }

    protected void removePage(int index) {
        this.pagesList.remove(index);
    }

    @Override
    public Message asMessage() {
        return this.makeMessage();
    }

    @Override
    public Message makeMessage() {
        page = Math.max(0, Math.min(pagesList.size() - 1, page + 1));
        Message message = this.getPage().asMessage();
        MessageBuilder messageBuilder = new MessageBuilder(message);
        List<ActionRow> actionRows = new ArrayList<>(message.getActionRows());
        ActionRow navigationRow = this.getNavigationRow();
        if (navigationRow != null)
            actionRows.add(navigationRow);
        messageBuilder.setActionRows(actionRows);
        return messageBuilder.build();
    }

    protected GuiPageMessageable getPage() {
        if (pagesList.isEmpty()) return this::emptyPage;
        return pagesList.get(page).getPage();
    }

    protected Message emptyPage() {
        return new MessageBuilder("Page not available").build();
    }

    @Override
    protected void initButtons() {
    }

    protected ActionRow getNavigationRow() {
        return ActionRow.of(this.getBackButton(), this.getForwardButton());
    }

    protected ButtonImpl getBackButton() {
        addManualButton(this::back, "back");
        return new ButtonImpl("back", "Back", ButtonStyle.DANGER, false, null);
    }

    protected ButtonImpl getForwardButton() {
        addManualButton(this::forward, "next");
        return new ButtonImpl("next", "Next", ButtonStyle.SUCCESS, false, null);
    }

    @GuiManualButton
    public void back(ButtonClickEvent event) {
        page = Math.max(0, page - 1);
        editAsReply(event);
    }

    @GuiManualButton
    public void forward(ButtonClickEvent event) {
        page = Math.max(0, Math.min(pagesList.size() - 1, page + 1));
        editAsReply(event);
    }

    protected static class DynamicPage<Page extends GuiPageMessageable> implements GuiPageMessageable {
        public Supplier<Page> create;
        public Page created;

        public DynamicPage(Page created) {
            this.created = created;
            this.create = null;
        }

        public DynamicPage(Supplier<Page> create) {
            this.create = create;
            this.created = null;
        }

        public Page getPage() {
            if (this.created == null) {
                this.created = this.create.get();
                this.create = null;
            }
            return this.created;
        }

        @Override
        public int hashCode() {
            return getPage().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DynamicPage other) return this.getPage().equals(other.getPage());
            return false;
        }

        @Override
        public Message asMessage() {
            return getPage().asMessage();
        }
    }
}
