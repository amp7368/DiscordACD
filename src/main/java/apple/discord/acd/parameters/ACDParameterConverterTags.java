package apple.discord.acd.parameters;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class ACDParameterConverterTags implements ACDParameterConverter<Member[]> {
    private final boolean requireNonEmpty;
    private final boolean isDistinct;

    public ACDParameterConverterTags(boolean requireNonEmpty, boolean isDistinct) {
        this.requireNonEmpty = requireNonEmpty;
        this.isDistinct = isDistinct;
    }

    public ACDParameterConverterTags(boolean requireNonEmpty) {
        this(requireNonEmpty, false);
    }

    public ACDParameterConverterTags() {
        this(false, false);
    }

    @Override
    public ACDParameterMessageEaten<Member[]> eatInput(MessageReceivedEvent event, String input) throws IllegalArgumentException {
        List<Member> mentioned = event.getMessage().getMentionedMembers();
        List<Member> mentionedStillThere = new ArrayList<>(mentioned.size());
        for (Member member : mentioned) {
            String memberAsString = getMemberAsString(member);
            int oldInputLength = input.length();
            input = input.replaceFirst(Pattern.quote(memberAsString), "");
            if (oldInputLength != input.length()) {
                mentionedStillThere.add(member);
            }
        }
        if (requireNonEmpty && mentionedStillThere.isEmpty())
            throw new IllegalArgumentException("no members were mentioned");
        if (isDistinct) {
            mentionedStillThere = new ArrayList<>(new HashSet<>(mentionedStillThere));
        }
        return new ACDParameterMessageEaten<>(input, mentionedStillThere.toArray(new Member[0]));
    }

    private static String getMemberAsString(Member member) {
        return String.format("<@!%s>", member.getId());
    }
}
