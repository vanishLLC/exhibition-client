package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.gui.GuiScreen;

public class Name extends Command {

    public Name(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public void fire(String[] args) {
        if(mc.thePlayer != null) {
            GuiScreen.setClipboardString(mc.thePlayer.getName());
        }
        ChatUtil.printChat(chatPrefix + "\2477copied name to clipboard!");
    }

}
