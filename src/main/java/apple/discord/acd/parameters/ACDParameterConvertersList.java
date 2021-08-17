package apple.discord.acd.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ACDParameterConvertersList {
    private final Map<String, Map<Class<?>, ACDParameterConverter<?>>> parameterConverters = new HashMap<>();

    public <T> void add(String id, Class<T> parameterType, ACDParameterConverter<T> converter) {
        parameterConverters.computeIfAbsent(id, o -> new HashMap<>()).put(parameterType, converter);
    }

    @Nullable
    public <T> ACDParameterConverter<?> get(String id, Class<T> parameterType) {
        Map<Class<?>, ACDParameterConverter<?>> convertersWithId = parameterConverters.get(id);
        if (convertersWithId == null) return null;
        return convertersWithId.get(parameterType);
    }
}
