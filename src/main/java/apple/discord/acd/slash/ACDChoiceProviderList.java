package apple.discord.acd.slash;

import apple.discord.acd.slash.options.converter.SlashChoiceConverter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashMap;
import java.util.Map;

public class ACDChoiceProviderList {
    private final Map<String, Map<Class<?>, SlashChoiceConverter<?>>> choiceProviders = new HashMap<>();

    public ACDChoiceProviderList() {
        HashMap<Class<?>, SlashChoiceConverter<?>> defaultConverters = new HashMap<>();
        defaultConverters.put(SlashCommandEvent.class, (event, option) -> event);
        defaultConverters.put(MessageChannel.class, (event, option) -> event.getChannel());
        defaultConverters.put(String.class, (event, option) -> event.getCommandPath());
        choiceProviders.put("", defaultConverters);
    }

    public <R> void add(String id, Class<R> parameterType, SlashChoiceConverter<R> slashChoice) {
        choiceProviders.computeIfAbsent(id, o -> new HashMap<>()).put(parameterType, slashChoice);
    }

    public <T> SlashChoiceConverter<?> getChoiceConverter(String id, Class<T> parameterType) {
        Map<Class<?>, SlashChoiceConverter<?>> choicesWithId = choiceProviders.get(id);
        if (choicesWithId == null) return null;
        return choicesWithId.get(parameterType);
    }

}
