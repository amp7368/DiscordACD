package apple.discord.acd.parameters;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class ACDParameterConverterChannelTags implements ACDParameterConverter<TextChannel[]> {
    private final boolean requireNonEmpty;
    private final boolean isDistinct;
    private final ChannelType[] channelTypes;

    public ACDParameterConverterChannelTags(boolean requireNonEmpty, boolean isDistinct, ChannelType... channelTypes) {
        this.requireNonEmpty = requireNonEmpty;
        this.isDistinct = isDistinct;
        this.channelTypes = channelTypes;
    }


    public ACDParameterConverterChannelTags(boolean requireNonEmpty) {
        this(requireNonEmpty, false);
    }

    public ACDParameterConverterChannelTags() {
        this(false, false);
    }

    private static String getChannelAsString(TextChannel member) {
        return String.format("<#%s>", member.getId());
    }

    @Override
    public ACDParameterMessageEaten<TextChannel[]> eatInput(MessageReceivedEvent event, String input) throws IllegalArgumentException {
        List<TextChannel> mentioned = event.getMessage().getMentionedChannels();
        List<TextChannel> mentionedStillThere = new ArrayList<>(mentioned.size());
        for (TextChannel mention : mentioned) {
            String memberAsString = getChannelAsString(mention);
            int oldInputLength = input.length();
            input = input.replaceFirst(Pattern.quote(memberAsString), "");
            if (oldInputLength != input.length()) {
                if (channelTypes.length == 0)
                    mentionedStillThere.add(mention);
                else {
                    for (ChannelType required : channelTypes) {
                        if (mention.getType() == required) {
                            mentionedStillThere.add(mention);
                            break;
                        }
                    }
                }
            }
        }
        if (requireNonEmpty && mentionedStillThere.isEmpty())
            throw new IllegalArgumentException("no members were mentioned");
        if (isDistinct) {
            mentionedStillThere = new ArrayList<>(new HashSet<>(mentionedStillThere));
        }
        return new ACDParameterMessageEaten<>(input, mentionedStillThere.toArray(new TextChannel[0]));
    }
}
