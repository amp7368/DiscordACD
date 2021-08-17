package apple.discord.acd.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ACDExceptionHandler<T> implements ACDCanCatch<T> {
    private final Object caller;
    private final Method method;

    public ACDExceptionHandler(Object caller, Method method, DiscordExceptionHandler annotation) {
        this.caller = caller;
        this.method = method;
    }

    public boolean canCatch(Exception e) {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.getType().isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    public void doCatch(Exception e, T event) throws InvocationTargetException, IllegalAccessException {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType() == event.getClass()) {
                arguments[i] = event;
            } else if (parameter.getType().isInstance(e)) {
                arguments[i] = e;
            } else {
                arguments[i] = null;
            }
        }
        method.invoke(caller, arguments);
    }
}
