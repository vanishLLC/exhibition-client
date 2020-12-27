package exhibition.management.friend;

import exhibition.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final File FRIEND_DIR;
    public static ArrayList<Friend> friendsList;

    static {
        FRIEND_DIR = FileUtils.getConfigFile("Friends");
        FriendManager.friendsList = new ArrayList<>();
    }

    public static void start() {
        load();
    }

    public static void addFriend(final String name, final String alias) {
        if (!isFriend(name))
            FriendManager.friendsList.add(new Friend(name, alias));
        save();
    }

    public static String getAlias(final String name) {
        String alias = "";
        for (final Friend friend : FriendManager.friendsList) {
            if (friend.name.equalsIgnoreCase(StringUtils.stripControlCodes(name))) {
                alias = friend.alias;
                break;
            }
        }
        return alias;
    }

    public static void removeFriend(final String name) {
        for (final Friend friend : FriendManager.friendsList) {
            if (friend.name.equalsIgnoreCase(name)) {
                FriendManager.friendsList.remove(friend);
                break;
            }
        }
        save();
    }

    public static boolean isFriend(final String name) {
        boolean isFriend = false;
        for (final Friend friend : FriendManager.friendsList) {
            if (friend.name.equalsIgnoreCase(StringUtils.stripControlCodes(name))) {
                isFriend = true;
                break;
            }
        }
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getGameProfile() != null && Minecraft.getMinecraft().thePlayer.getGameProfile().getName().equals(name)) {
            isFriend = true;
        }
        return isFriend;
    }

    public static void load() {
        FriendManager.friendsList.clear();
        final List<String> fileContent = FileUtils.read(FriendManager.FRIEND_DIR);
        for (final String line : fileContent) {
            try {
                final String[] split = line.split(":");
                final String name = split[0];
                final String alias = split[1];
                if (!isFriend(name))
                    FriendManager.friendsList.add(new Friend(name, alias));
            } catch (Exception ex) {
            }
        }
    }

    public static void save() {
        final List<String> fileContent = new ArrayList<String>();
        for (final Friend friend : FriendManager.friendsList) {
            fileContent.add(String.format("%s:%s", friend.name, friend.alias));
        }
        FileUtils.write(FriendManager.FRIEND_DIR, fileContent, true);
    }
}

