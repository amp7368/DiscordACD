package apple.discord.acd.handler;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ACDMiscExceptionHandler<T> implements ACDCanCatch<T> {
    private final Class<? extends Exception>[] catches;
    private final Object caller;
    private final Method method;

    public ACDMiscExceptionHandler(Object caller, Method method, DiscordMiscExceptionHandler annotation) {
        this.caller = caller;
        this.method = method;
        this.catches = annotation.catchThese();
    }

    @Override
    public boolean canCatch(Exception e) {
        for (Class<? extends Exception> c : catches) {
            if (c.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doCatch(Exception e, T ignored) throws InvocationTargetException, IllegalAccessException {
        method.invoke(caller, e);
    }
}
