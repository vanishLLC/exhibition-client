package exhibition.management.friend;

import exhibition.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendManager {
    private static final File FRIEND_DIR;
    public static final HashMap<String, String> friendsMap;

    static {
        FRIEND_DIR = FileUtils.getConfigFile("Friends");
        friendsMap = new HashMap<>();
    }

    public static void start() {
        load();
    }

    public static void addFriend(final String name, final String alias) {
        if (name.equals("")) {
            return;
        }
        if (!isFriend(name)) {
            FriendManager.friendsMap.putIfAbsent(name, alias);
            save();
        }
    }

    public static String getAlias(final String name) {
        return FriendManager.friendsMap.getOrDefault(StringUtils.stripControlCodes(name), "null");
    }

    public static void removeFriend(final String name) {
        if(FriendManager.friendsMap.remove(name) != null) {
            save();
        }
    }

    public static boolean isFriend(final String name) {
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getGameProfile() != null && Minecraft.getMinecraft().thePlayer.getGameProfile().getName().equals(name)) {
            return true;
        }
        return FriendManager.friendsMap.containsKey(name);
    }

    public static void load() {
        FriendManager.friendsMap.clear();
        final List<String> fileContent = FileUtils.read(FriendManager.FRIEND_DIR);
        for (final String line : fileContent) {
            try {
                final String[] split = line.split(":");
                final String name = split[0];
                final String alias = split[1];
                if (!isFriend(name) && !name.equals(""))
                    FriendManager.friendsMap.putIfAbsent(name, alias);
            } catch (Exception ex) {
            }
        }
    }

    public static void save() {
        final List<String> fileContent = new ArrayList<String>();
        for (Map.Entry<String, String> friend : FriendManager.friendsMap.entrySet()) {
            String name = friend.getKey();
            String alias = friend.getValue();
            fileContent.add(String.format("%s:%s", name, alias));
        }
        FileUtils.write(FriendManager.FRIEND_DIR, fileContent, true);
    }
}

