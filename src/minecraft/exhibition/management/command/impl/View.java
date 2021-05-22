package exhibition.management.command.impl;

import exhibition.event.EventSystem;
import exhibition.gui.screen.GuiView;
import exhibition.management.command.Command;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class View extends Command {

    public View(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public void fire(String[] args) {
        if (args.length == 1) {
            String playerName = args[0];

            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                if (entity instanceof EntityPlayer && entity.canBeCollidedWith()) {
                    if (entity.getName().equalsIgnoreCase(playerName)) {
                        new Thread("View Thread"){
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mc.addScheduledTask(new Runnable() {
                                    @Override
                                    public void run() {
                                        mc.displayGuiScreen(new GuiView((EntityPlayer)entity));
                                    }
                                });
                            }
                        }.start();
                        return;
                    }
                }
            }

            ChatUtil.printChat(chatPrefix + "\247f" + args[0] + "\247c does not exist.");
        }


    }

}
