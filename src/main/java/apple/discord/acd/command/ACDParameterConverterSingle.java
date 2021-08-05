package apple.discord.acd.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Function;

public record ACDParameterConverterSingle<T>(Function<String, T> eater,
                                             String splitter) implements ACDParameterConverter<T> {

    @Override
    public ACDParameterMessageEaten<T> eatInput(MessageReceivedEvent event, String input) {
        if (input.isEmpty()) throw new IllegalArgumentException("The input is not long enough");
        String[] inputSplit = input.split(splitter, 2);
        if (inputSplit.length == 1) {
            return new ACDParameterMessageEaten<>("", eater.apply(inputSplit[0]));
        }else{
            return new ACDParameterMessageEaten<>(inputSplit[1], eater.apply(inputSplit[0]));
        }
    }
}
