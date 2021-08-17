package apple.discord.acd.permission;

import net.dv8tion.jda.api.entities.Member;

public abstract class ACDPermission {
    private final String uniqueName;
    private final int strictness;

    public ACDPermission(String uniqueName, int strictness) {
        this.uniqueName = uniqueName;
        this.strictness = strictness;
    }

    public abstract boolean hasPermission(Member member);

    public String getName() {
        return uniqueName;
    }

    public int getStrictness() {
        return strictness;
    }
}
