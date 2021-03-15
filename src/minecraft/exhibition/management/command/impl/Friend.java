package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.management.friend.FriendManager;
import exhibition.util.misc.ChatUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Map;

public class Friend extends Command {

    public Friend(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        // Intended arguements
        // 1 - Module
        // 2 - Key
        // 3 - Mask
        if (args == null || args.length < 1) {
            printUsage();
            return;
        }
        try {
            if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a")) && args.length > 1) {
                if (FriendManager.isFriend(args[1])) {
                    ChatUtil.printChat(chatPrefix + String.valueOf(args[1]) + " is already your friend.");
                    return;
                }
                FriendManager.removeFriend(args[1]);
                FriendManager.addFriend(args[1], args.length == 3 ? args[2] : args[1]);
                ChatUtil.printChat(chatPrefix + "Added \247b" + args[1] + (args.length == 3 ? " \2478as \247b" + args[2] : ""));
            } else if ((args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("d") ||
                    args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rem") ||
                    args[0].equalsIgnoreCase("r")) && args.length > 1) {
                if (FriendManager.isFriend(args[1])) {
                    FriendManager.removeFriend(args[1]);
                    ChatUtil.printChat(chatPrefix + "Removed \247b" + args[1]);
                } else {
                    ChatUtil.printChat(chatPrefix + String.valueOf(args[1]) + " is not your friend.");
                }
            } else if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
                printChat("Friends [\247b" + FriendManager.friendsMap.size() + "\2477]");

                for (Map.Entry<String, String> friend : FriendManager.friendsMap.entrySet()) {
                    String name = friend.getKey();
                    String alias = friend.getValue();

                    IChatComponent unfriendComponent = new ChatComponentText(" [Remove]");
                    unfriendComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".friend remove " + name));
                    unfriendComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Unfriend " + name)));
                    unfriendComponent.getChatStyle().setColor(EnumChatFormatting.RED);
                    unfriendComponent.getChatStyle().setBold(true);

                    IChatComponent refriendComponent = new ChatComponentText("[Re-Add]");
                    refriendComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".friend add " + name + (alias.equalsIgnoreCase(name) ? "" : " " + alias)));
                    refriendComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Re-Friend " + name + (alias.equalsIgnoreCase(name) ? "" : " as \"" + alias + "\""))));
                    refriendComponent.getChatStyle().setColor(EnumChatFormatting.GREEN);
                    refriendComponent.getChatStyle().setBold(true);

                    IChatComponent chatComponent = new ChatComponentTranslation("\247b"+ name + (name.equals(alias) ? "" : " \2477=> \2479" + alias) + " %s %s", new Object[] {unfriendComponent, refriendComponent});
                    this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
                }
            }
        } catch(NullPointerException e) {
            printUsage();
        }
        return;
    }

    @Override
    public String getUsage() {
        return "friend <add/del> " + EnumChatFormatting.RESET + "<name> " + EnumChatFormatting.RESET + "<alias>";
    }

}
