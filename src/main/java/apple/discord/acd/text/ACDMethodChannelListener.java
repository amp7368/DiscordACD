package apple.discord.acd.text;

import apple.discord.acd.parameters.ACDParameter;
import apple.discord.acd.parameters.ACDParameterMessageEaten;
import apple.discord.acd.permission.ACDPermission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ACDMethodChannelListener {
    private final ACDChannelListener callObj;
    private final Method method;
    private final ACDPermission permission;
    private final int order;
    private final ACDParameter<?>[] typesRequired;

    public ACDMethodChannelListener(ACDChannelListener acdChannelListener, ACDChannelListener classObj, Method method, DiscordChannelListener annotation) {
        this.callObj = classObj;
        this.method = method;
        this.permission = acdChannelListener.getPermissions().getPermission(annotation.permission());
        this.order = annotation.order();
        this.typesRequired = ACDParameter.getParameterTypesRequired(this.method, acdChannelListener::getDefinedParameter);
    }

    public ACDListenerCalled dealWithMessage(MessageReceivedEvent event) {
        if (permission.hasPermission(event.getMember())) {
            try {
                return callWithArgs(event, event.getMessage().getContentRaw().trim());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new ACDListenerCalled(ACDListenerCalled.CallingState.NOT_CALLED);
    }

    private ACDListenerCalled callWithArgs(MessageReceivedEvent event, String contentRaw) throws InvocationTargetException, IllegalAccessException {
        Object[] arguments = new Object[this.typesRequired.length];
        for (int i = 0; i < this.typesRequired.length; i++) {
            // try to eat the provided input
            try {
                ACDParameterMessageEaten<?> eaten = this.typesRequired[i].converter().eatInput(event, contentRaw);
                contentRaw = eaten.newInput();
                arguments[i] = eaten.outputObject();
            } catch (IllegalArgumentException e) {
                return new ACDListenerCalled(ACDListenerCalled.CallingState.NOT_CALLED);
            }
        }
        method.invoke(this.callObj, arguments);
        return new ACDListenerCalled(ACDListenerCalled.CallingState.CALLED);
    }

    public int getOrder() {
        return order;
    }

    public ACDPermission getPermission() {
        return permission;
    }
}
