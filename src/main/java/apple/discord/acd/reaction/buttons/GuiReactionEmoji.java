package apple.discord.acd.reaction.buttons;

import apple.discord.acd.reaction.DiscordEmoji;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiReactionEmoji {
    DiscordEmoji emote();
}
