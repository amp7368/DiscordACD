package apple.discord.acd.permission;

import net.dv8tion.jda.api.entities.Member;

public abstract class ACDPermission {
    private final String uniqueName;

    public ACDPermission(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public abstract boolean hasPermission(Member member);

    public String getName() {
        return uniqueName;
    }
}
