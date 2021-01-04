/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.PriorityManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetESP extends Module {

    private Setting x, y, showList;

    public TargetESP(ModuleData data) {
        super(data);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        showList = new Setting<>("SHOW-LIST", false, "Show target list.");
        x = new Setting<>("X", screenSize.getWidth() / 2, "X Position", 1, 0, screenSize.getWidth());
        y = new Setting<>("Y", 20, "Y Position", 1, 0, screenSize.getHeight());
        addSetting(x.getName(), x);
        addSetting(y.getName(), y);
        addSetting(showList.getName(), showList);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @RegisterEvent(events = EventRenderGui.class)
    public void onEvent(Event event) {
        if (!(boolean) showList.getValue())
            return;

        EventRenderGui er = event.cast();

        List<EntityPlayer> players = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList)
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player != mc.thePlayer && TargetESP.isPriority(player))
                    players.add(player);
            }

        double x = (((Number) this.x.getValue()).doubleValue()) / er.getResolution().getScaleFactor() - 75 / 2D;
        double y = ((Number) this.y.getValue()).doubleValue() / er.getResolution().getScaleFactor();
        double width = 75;
        double height = 10 + (players.size() == 0 ? 0 : 1 + players.size() * 8);

        RenderingUtil.drawRoundedRect(x, y, x + width, y + height, 2, Colors.getColor(0));
        RenderingUtil.drawRoundedRect(x + 1, y + 1, x + width - 1, y + height - 1, 2, Colors.getColor(55));

        RenderingUtil.rectangle(x + 3, y + 9, x + width - 3, y + 9.5, Colors.getColor(20));

        Client.fss.drawStringWithShadow("Target List [" + players.size() + "]", (float) x + 2, (float) y + 2.5F, -1);

        float yOff = 1;
        for (EntityPlayer player : players) {
            yOff += 8;
            Client.fss.drawStringWithShadow(player.getDisplayName().getFormattedText(), (float) x + 2, (float) y + 2.5F + yOff, -1);
        }

        RenderingUtil.rectangle(0, 0, 0, 0, -1);
    }

    public static boolean isPriority(EntityPlayer player) {
        if (player.equals(Killaura.vip))
            return true;
        if (PriorityManager.isPriority(player))
            return true;
        if(Client.getModuleManager().isEnabled(TargetESP.class) && (player.getDisplayName().getFormattedText().contains("0g") || player.getDisplayName().getFormattedText().contains("5g"))) {
            if (player.getDisplayName().getFormattedText().contains(" \2476\247l")) {
                return true;
            }
            if (player.getDisplayName().getFormattedText().contains(" \247b\247l")) {
                return true;
            }
            return player.getDisplayName().getFormattedText().contains(" \247c\247l");
        }
        return false;
    }

}
