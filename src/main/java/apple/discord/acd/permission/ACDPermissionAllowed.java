package apple.discord.acd.permission;

import net.dv8tion.jda.api.entities.Member;

public class ACDPermissionAllowed extends ACDPermission {
    private static final ACDPermissionAllowed instance = new ACDPermissionAllowed();

    public ACDPermissionAllowed() {
        super("");
    }

    public static ACDPermissionAllowed getInstance() {
        return instance;
    }

    /**
     * make sure the static is done and this permission was added
     */
    public static void init() {

    }

    @Override
    public boolean hasPermission(Member member) {
        return true;
    }
}
