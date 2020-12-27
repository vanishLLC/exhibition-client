package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderEntity;
import exhibition.management.ColorManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.TeamUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class SimsESP extends Module {

    private String TEAM = "TEAM";

    public SimsESP(ModuleData data) {
        super(data);
        settings.put(TEAM, new Setting<>(TEAM, false, "Teams shit."));
    }

    @RegisterEvent(events = EventRender3D.class)
    public void onEvent(Event event) {
        EventRender3D er = event.cast();
        for (Entity e : mc.theWorld.loadedEntityList) {
            if(e instanceof EntityPlayer) {
                EntityPlayer entity = (EntityPlayer)e;
                if((entity != mc.thePlayer || mc.gameSettings.thirdPersonView != 0) && !entity.isInvisible()) {
                    float pTicks = mc.timer.renderPartialTicks;
                    double a = (entity.prevPosX + (entity.posX - entity.prevPosX) * pTicks) - RenderManager.renderPosX;
                    double b = (entity.prevPosY + (entity.posY - entity.prevPosY) * pTicks) - RenderManager.renderPosY + 0.5;
                    double c = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * pTicks) - RenderManager.renderPosZ;

                    double pX = 0;
                    double pY = 0;
                    double pZ = 0;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(a, b, c);
                    float rot = ((entity.ticksExisted - 1) + (entity.ticksExisted - (entity.ticksExisted - 1)) * pTicks) * 5;
                    GlStateManager.rotate(rot, 0, 1, 0);
                    RenderingUtil.pre3D();

                    int color = Colors.getColor(50,255,50);
                    if (FriendManager.isFriend(entity.getName())) {
                        color = ColorManager.getFriendlyVisible().getColorHex();
                    }
                    if (((Boolean) settings.get(TEAM).getValue())) {
                        if (TeamUtils.isTeam(mc.thePlayer, entity)) {
                            color = (Colors.getColor(50, 50, 255, 100));
                        } else {
                            color = (Colors.getColor(255,50,50));
                        }
                    }
                    if(Client.getModuleManager().isEnabled(TargetESP.class) && TargetESP.isPriority(entity)) {
                        color = Colors.getColor(255,50,50);
                    }

                    RenderingUtil.glColor(Colors.getColorOpacity(color,150));

                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);

                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);

                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);

                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);

                    GL11.glVertex3d(pX, pY + 1.5, pZ);
                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);

                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 1.5, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);

                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);
                    GL11.glVertex3d(pX, pY + 1.5, pZ);

                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);
                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 1.5, pZ);
                    GL11.glEnd();

                    RenderingUtil.glColor(Colors.getColorOpacity(color,200));
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);
                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 1.5, pZ);
                    GL11.glEnd();

                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    GL11.glVertex3d(pX, pY + 1.5, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);
                    GL11.glVertex3d(pX, pY + 2.5, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);
                    GL11.glEnd();

                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    GL11.glVertex3d(pX, pY + 2, pZ + 0.25);
                    GL11.glVertex3d(pX + 0.25, pY + 2, pZ);
                    GL11.glVertex3d(pX, pY + 2, pZ - 0.25);
                    GL11.glVertex3d(pX - 0.25, pY + 2, pZ);

                    GL11.glEnd();

                    RenderingUtil.post3D();
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
