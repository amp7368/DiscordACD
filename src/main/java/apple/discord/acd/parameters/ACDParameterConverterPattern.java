package apple.discord.acd.parameters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record ACDParameterConverterPattern(String splitter,
                                           Function<String, Boolean> matcher
) implements ACDParameterConverter<String[]> {

    @Override
    public ACDParameterMessageEaten<String[]> eatInput(MessageReceivedEvent event, String input) throws IllegalArgumentException {
        List<String> matches = new ArrayList<>();
        StringBuilder newInput = new StringBuilder();
        boolean skippedSplitter = false;
        for (String i : input.split(splitter)) {
            if (matcher.apply(i)) matches.add(i);
            else {
                if (skippedSplitter) {
                    newInput.append(splitter);
                } else {
                    skippedSplitter = true;
                }
                newInput.append(i);
            }
        }
        return new ACDParameterMessageEaten<>(newInput.toString(), matches.toArray(new String[0]));
    }
}
