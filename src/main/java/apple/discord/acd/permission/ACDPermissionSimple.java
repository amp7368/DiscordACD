package apple.discord.acd.permission;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class ACDPermissionSimple extends ACDPermission {
    private final Permission[] allowedUsers;

    public ACDPermissionSimple(String uniqueName, Permission... allowedUsers) {
        super(uniqueName);
        this.allowedUsers = allowedUsers;
    }

    @Override
    public boolean hasPermission(Member member) {
        for (Permission allowed : allowedUsers) {
            if (member.hasPermission(allowed)) {
                return true;
            }
        }
        return false;
    }

}
