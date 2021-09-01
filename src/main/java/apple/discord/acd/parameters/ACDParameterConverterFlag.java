package apple.discord.acd.parameters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class ACDParameterConverterFlag implements ACDParameterConverter<Boolean> {
    private final String splitter;
    private final String[] flags;
    private final boolean ignoreCase;

    public ACDParameterConverterFlag(ParameterFlag annotation) {
        this.splitter = annotation.splitter();
        this.flags = annotation.flags();
        this.ignoreCase = annotation.ignoreCase();
    }

    @Override
    public ACDParameterMessageEaten<Boolean> eatInput(MessageReceivedEvent event, String input) throws IllegalArgumentException {
        String[] inputSplit = input.split(splitter);
        for (int i = 0; i < inputSplit.length; i++) {
            String in = inputSplit[i];
            for (String flag : flags) {
                boolean success = false;
                if (ignoreCase) {
                    if (in.equalsIgnoreCase(flag)) {
                        success = true;
                    }
                } else {
                    if (in.equals(flag)) {
                        success = true;
                    }
                }
                if (success) {
                    List<String> combiningInput = new ArrayList<>(List.of(inputSplit));
                    combiningInput.remove(i);
                    return new ACDParameterMessageEaten<>(String.join(splitter, combiningInput), true);
                }
            }
        }
        return new ACDParameterMessageEaten<>(input, false);
    }
}
