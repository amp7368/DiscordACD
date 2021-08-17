package apple.discord.acd.handler;

import java.lang.reflect.InvocationTargetException;

public interface ACDCanCatch<T> {
    boolean canCatch(Exception e);

    void doCatch(Exception e, T event) throws InvocationTargetException, IllegalAccessException;
}
