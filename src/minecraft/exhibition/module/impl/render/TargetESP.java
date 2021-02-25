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
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.HypixelUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

        double longestString = 75;

        List<EntityPlayer> players = new ArrayList<>();
        for (Entity entity : mc.theWorld.getLoadedEntityList())
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player != mc.thePlayer && TargetESP.isPriority(player) && !FriendManager.isFriend(player.getName())) {
                    players.add(player);
                    longestString = Math.max(Client.fss.getWidth(player.getDisplayName().getFormattedText()), longestString);
                }
            }

        double x = (((Number) this.x.getValue()).doubleValue()) / er.getResolution().getScaleFactor() - 75 / 2D;
        double y = ((Number) this.y.getValue()).doubleValue() / er.getResolution().getScaleFactor();
        double width = longestString;
        double height = 10 + (players.size() == 0 ? 0 : 1 + players.size() * 8);

        String string = "Target List [" + players.size() + "]";

        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(x + 1.5, y + 3, x + Client.fss.getWidth(string) + 2, y + 3.5, -1);
        RenderingUtil.rectangle(x + 2, y + 3.5, x + Client.fss.getWidth(string) + 1.5, y + 4, -1);
        Depth.render(GL11.GL_LESS);
        RenderingUtil.rectangleBordered(x, y + 3, x + width, y + height, 0.5, Colors.getColor(0,0), Colors.getColor(10));
        RenderingUtil.rectangleBordered(x + 0.5, y + 3.5, x + width - 0.5, y + height - 0.5, 0.5, Colors.getColor(17), Colors.getColor(48));
        Depth.post();

        RenderingUtil.rectangle(x + 3, y + 9, x + width - 3, y + 9.5, Colors.getColor(48));

        Client.fss.drawStringWithShadow(string, (float) x + 2, (float) y + 2.5F, -1);

        float yOff = 1;
        for (EntityPlayer player : players) {
            yOff += 8;
            Client.fss.drawStringWithShadow(player.getDisplayName().getFormattedText(), (float) x + 2, (float) y + 2.5F + yOff, -1);
        }

        RenderingUtil.rectangle(0, 0, 0, 0, -1);
    }

    private static final Pattern bountyPattern = Pattern.compile("\247l[\\d]+g");

    public static boolean isPriority(EntityPlayer player) {
        if (player.equals(Client.getModuleManager().get(Killaura.class).vip) || PriorityManager.isPriority(player))
            return true;

        String formatted = player.getDisplayName().getFormattedText();

        if(Client.getModuleManager().isEnabled(TargetESP.class) && (bountyPattern.matcher(formatted).find() && !formatted.contains("\247l100g"))) {
            return true;
        }
        if(Client.getModuleManager().isEnabled(TargetESP.class) && (formatted.contains("HELD") || (formatted.contains("BEAST") && !mc.thePlayer.getDisplayName().getFormattedText().contains("\247lBEAST"))) && HypixelUtil.scoreboardContains("Event")) {
            return true;
        }
        return false;
    }

}
