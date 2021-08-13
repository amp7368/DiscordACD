package apple.discord.acd.reaction;

import net.dv8tion.jda.api.entities.Emoji;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum DiscordEmoji {
    LEFT("\u2B05"),
    RIGHT("\u27A1"),
    TOP("\u21A9"),
    BASKET("\uD83E\uDDFA"),
    GEM("\uD83D\uDC8E"),
    CLOCK("\uD83D\uDD53"),
    AMOUNT("\uD83D\uDCB5"),
    PERCENTAGE("\uD83D\uDD22"),
    SWITCH("\uD83D\uDD03"),
    HELP("\u2753"),
    GREEN("\uD83D\uDFE9"),
    LEVEL("\uD83D\uDE80"),
    UP("\u2B06"),
    BIG_UP("\u23EB"),
    DOWN("\u2B07"),
    BIG_DOWN("\u23EC"),
    RED_X("\u274C");

    public final static List<String> emojiAlphabet = Arrays.asList("\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED",
            "\uD83C\uDDEE", "\uD83C\uDDEF", "\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4", "\uD83C\uDDF5", "\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9", "\uD83C\uDDFA"
            , "\uD83C\uDDFB", "\uD83C\uDDFC", "\uD83C\uDDFD", "\uD83C\uDDFE", "\uD83C\uDDFF");

    private final String emoji;
    private static HashMap<String, DiscordEmoji> emojis = null;

    DiscordEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public Emoji getDiscordEmoji() {
        return Emoji.fromUnicode(emoji);
    }

    @Nullable
    public static DiscordEmoji get(String s) {
        if (emojis == null) {
            emojis = new HashMap<>();
            for (DiscordEmoji emoji : values()) {
                emojis.put(emoji.getEmoji(), emoji);
            }
        }
        return emojis.get(s);
    }
}
