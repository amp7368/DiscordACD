package apple.discord.acd.permission;

import net.dv8tion.jda.api.entities.Member;

public class ACDPermissionExpression extends ACDPermission {
    private final boolean isAnd; // false = OR :: true = AND
    private final ACDPermission[] allowedUsers;

    public ACDPermissionExpression(String uniqueName, boolean isAnd, ACDPermission... permissions) {
        super(uniqueName, permissions.length);
        this.isAnd = isAnd;
        this.allowedUsers = permissions;
    }

    public ACDPermissionExpression(String uniqueName, boolean isAnd, int strictness, ACDPermission... permissions) {
        super(uniqueName, strictness);
        this.isAnd = isAnd;
        this.allowedUsers = permissions;
    }

    @Override
    public boolean hasPermission(Member member) {
        if (isAnd) {
            for (ACDPermission permission : allowedUsers) {
                if (!permission.hasPermission(member)) {
                    return false;
                }
            }
            return true;
        } else {
            for (ACDPermission permission : allowedUsers) {
                if (permission.hasPermission(member)) {
                    return true;
                }
            }
            return false;

        }
    }
}
