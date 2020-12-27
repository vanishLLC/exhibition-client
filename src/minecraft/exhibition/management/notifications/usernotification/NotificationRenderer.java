package exhibition.management.notifications.usernotification;

import exhibition.Client;
import exhibition.management.GlobalValues;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.hud.HUD;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class NotificationRenderer implements INotificationRenderer {

    private ResourceLocation SPOTIFY = new ResourceLocation("textures/spotify.png");
    private ResourceLocation WARNING = new ResourceLocation("textures/warning.png");
    private ResourceLocation NOTIFY = new ResourceLocation("textures/notify.png");
    private ResourceLocation OKAY = new ResourceLocation("textures/okay.png");
    private ResourceLocation INFO = new ResourceLocation("textures/info.png");

    @Override
    public void draw(List<INotification> notifications) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        float y = (float) (scaledRes.getScaledHeight()) - (notifications.size() * (24) + (mc.currentScreen instanceof GuiChat ? 14 : 0));
        int count = 0;
        if((GlobalValues.centerNotifs.getValue() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat))) {
            y = scaledRes.getScaledHeight()/2F + 50;
        }

        boolean isTargetHudActive = false;
        Killaura killaura = (Killaura) Client.getModuleManager().get(Killaura.class);
        if (killaura.isEnabled() && killaura.getCurrentTarget() != null) {
            isTargetHudActive = true;
        }

        for (INotification notification : notifications) {
            Notification not = (Notification) notification;

            String headerString = not.getHeader();

            if(headerString.contains("{s}")) {
                long timeLeft = (not.getDisplayTime() + not.getStart()) - not.checkTime();
                if(timeLeft < 0)
                    timeLeft = 0;

                headerString = headerString.replace("{s}", String.valueOf(MathUtils.roundToPlace((timeLeft/1000D), 1)));
            }

            String footerString = not.getSubtext();

            if(footerString.contains("{s}")) {
                long timeLeft = (not.getDisplayTime() + not.getStart()) - not.checkTime();
                if(timeLeft < 0)
                    timeLeft = 0;

                footerString = footerString.replace("{s}", String.valueOf(MathUtils.roundToPlace((timeLeft/1000D), 1)));
            }

            float subHeaderWidth = (Client.subHeader.getWidth(not.getSubtext()));
            float headerWidth = (Client.header.getWidth(headerString));
            double tarX = not.getTarX() >= scaledRes.getScaledWidth() ? not.getTarX() : scaledRes.getScaledWidth() - 23 - (Math.max(headerWidth, subHeaderWidth));

            if((GlobalValues.centerNotifs.getValue() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat))) {
                tarX = not.getTarX() >= scaledRes.getScaledWidth() ? not.getTarX() : (scaledRes.getScaledWidth()/2D + (-23 - (Math.max(headerWidth, subHeaderWidth)))/(isTargetHudActive && count < 2 ? 1 : 2D));
                tarX = MathUtils.getIncremental(tarX, 0.5);
            }

            not.translate.interpolate((tarX + 3), y, (GlobalValues.centerNotifs.getValue() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat)) ? 0.45F : 0.3F);

            float x = not.translate.getX();

            GL11.glPushMatrix();
            Depth.pre();
            Depth.mask();
            RenderingUtil.rectangle(not.translate.getX(), not.translate.getY(), not.translate.getX() + Math.max(headerWidth, subHeaderWidth) + 23, not.translate.getY() + (23),-1);
            Depth.render();

            RenderingUtil.rectangle(not.translate.getX(), not.translate.getY(), not.translate.getX() + Math.max(headerWidth, subHeaderWidth) + 23, not.translate.getY() + (23), Colors.getColor(0, 200));

            GlStateManager.pushMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();

            switch (not.getType().name) {
                case "NOTIFY":
                    mc.getTextureManager().bindTexture(NOTIFY);
                    break;
                case "WARNING":
                    mc.getTextureManager().bindTexture(WARNING);
                    break;
                case "INFO":
                    mc.getTextureManager().bindTexture(INFO);
                    break;
                case "SPOTIFY":
                    mc.getTextureManager().bindTexture(SPOTIFY);
                    break;
                case "OKAY":
                    mc.getTextureManager().bindTexture(OKAY);
                    break;
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GlStateManager.translate(x + 1, (not.translate.getY() + 1.5), 0);
            GlStateManager.pushMatrix();
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 18, 18, 18, 18);
            GlStateManager.popMatrix();

            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.popMatrix();

            Client.header.drawStringWithShadow(headerString, (x + 20), (not.translate.getY() + 1), -1);
            Client.subHeader.drawStringWithShadow(footerString, x + 20, not.translate.getY() + 12, -1);

            double percent = ((float)(System.currentTimeMillis() - not.getStart())/(float)not.getDisplayTime());
            if(percent > 1)
                percent = 1;
            if(percent < 0)
                percent = 0;
            double width = (x + Math.max(headerWidth, subHeaderWidth) + 23 - (x - 1)) * percent;

            RenderingUtil.rectangle(x - 1, not.translate.getY() + (21.5), x + Math.max(headerWidth, subHeaderWidth) + 23, not.translate.getY() + (23), Colors.getColorOpacity(getColor(not.getType()), 45));
            RenderingUtil.drawGradientSideways(x - 3 + width, not.translate.getY() + (21.5), x + width + 2, not.translate.getY() + (23), Colors.getColor(0,0), getColor(not.getType()));
            RenderingUtil.rectangle(x + 2 + width, not.translate.getY() + (21.5), x + Math.max(headerWidth, subHeaderWidth) + 23, not.translate.getY() + (23), getColor(not.getType()));

            Depth.post();
            GL11.glPopMatrix();

            if (not.checkTime() >= not.getDisplayTime() + not.getStart()) {
                not.setTarX(scaledRes.getScaledWidth() + 1);
                if (not.translate.getX() >= scaledRes.getScaledWidth()) {
                    notifications.remove(notification);
                }
            }
            y += (24);
            count++;
        }
    }

    private int getColor(Notifications.Type type) {
        int color = -1;
        switch (type.name) {
            case "INFO":
                color = Colors.getColor(64, 131, 214);
                break;
            case "NOTIFY":
                color = Colors.getColor(242, 206, 87);
                break;
            case "WARNING":
                color = Colors.getColor(226, 74, 74);
                break;
            case "SPOTIFY":
                color = Colors.getColor(30, 215, 96);
                break;
            case "OKAY":
                color = Colors.getColor(65, 252, 65);
                break;
            default:
                break;
        }
        return color;
    }

}
