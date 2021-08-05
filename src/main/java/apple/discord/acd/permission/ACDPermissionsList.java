package apple.discord.acd.permission;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ACDPermissionsList {
    public HashMap<String, ACDPermission> permissions = new HashMap<>();

    public void addPermission(ACDPermission acdPermission) {
        permissions.put(acdPermission.getName(), acdPermission);
    }

    @NotNull
    public ACDPermission getPermission(String name) {
        return permissions.getOrDefault(name, ACDPermissionAllowed.getInstance());
    }
}
