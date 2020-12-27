package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Created by Arithmo on 9/13/2017 at 3:07 PM.
 */
public class Box extends Module {

    private String ESPMODE = "FILLED";
    private String TEAM = "TEAM";

    public Box(ModuleData data) {
        super(data);
        settings.put(ESPMODE, new Setting<>(ESPMODE, false, "Fill in box."));
        settings.put(TEAM, new Setting<>(TEAM, false, "Teams shit."));
        settings.put("PLAYERS", new Setting<>("PLAYERS", true, "Render on Players."));
        settings.put("ANIMALS", new Setting<>("ANIMALS", false, "Render on Animals."));
        settings.put("MOBS", new Setting<>("MOBS", false, "Render on Mobs."));
    }

    @RegisterEvent(events = {EventRender3D.class})
    public void onEvent(Event event) {
        EventRender3D er = (EventRender3D) event;
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase && o != mc.thePlayer) {
                EntityLivingBase player = (EntityLivingBase) o;
                if (!isValid(player))
                    continue;

                GL11.glPushMatrix();
                RenderingUtil.pre3D();
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                int color = ColorManager.getEnemyVisible().getColorHex();
                if (FriendManager.isFriend(player.getName())) {
                    color = mc.thePlayer.canEntityBeSeen(player) ? ColorManager.getFriendlyVisible().getColorHex() : ColorManager.getFriendlyInvisible().getColorHex();
                } else if (!mc.thePlayer.canEntityBeSeen(player)) {
                    color = ColorManager.getEnemyInvisible().getColorHex();
                }
                if (((Boolean) settings.get(TEAM).getValue()) && player instanceof EntityPlayer) {
                    if (TeamUtils.isTeam(mc.thePlayer, ((EntityPlayer) player))) {
                        color = (Colors.getColor(20, 40, 255, 100));
                    } else {
                        color = (Colors.getColor(255, 40, 20, 100));
                    }
                }

                double x = (player.prevPosX + (player.posX - player.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX;
                double y = (player.prevPosY + (player.posY - player.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY;
                double z = (player.prevPosZ + (player.posZ - player.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ;
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(-(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * er.renderPartialTicks), 0, 1, 0);
                if (player.deathTime > 0) {
                    float var5 = ((float) player.deathTime + er.renderPartialTicks - 1.0F) / 20.0F * 1.6F;
                    var5 = MathHelper.sqrt_float(var5);

                    if (var5 > 1.0F) {
                        var5 = 1.0F;
                    }

                    GlStateManager.rotate(-(var5 * 90.0F), 0.0F, 0.0F, 1.0F);
                }
                float collisSize = player.getCollisionBorderSize();
                AxisAlignedBB var11 = player.getEntityBoundingBox();
                AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - player.posX, var11.minY - player.posY, var11.minZ - player.posZ, var11.maxX - player.posX, var11.maxY - player.posY, var11.maxZ - player.posZ).expand(collisSize, collisSize, collisSize);

                if ((boolean) settings.get(ESPMODE).getValue()) {
                    RenderingUtil.filledBox(var12, color, true);
                    RenderingUtil.glColor(color);
                    GL11.glLineWidth(2);
                    RenderingUtil.drawOutlinedBoundingBox(var12);
                } else {
                    RenderingUtil.glColor(color);
                    GL11.glLineWidth(2);
                    RenderingUtil.drawOutlinedBoundingBox(var12);
                }

                RenderingUtil.post3D();
                if (!GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                    GL11.glEnable(GL11.GL_LIGHTING);
                }
                GL11.glPopMatrix();
            }
        }
    }

    private boolean isValid(EntityLivingBase player) {
        return !player.isInvisible() && ((player instanceof EntityPlayer && player != mc.thePlayer && (Boolean) settings.get("PLAYERS").getValue())
                || ((player instanceof EntityMob || player instanceof EntitySlime || player instanceof EntityGhast || player instanceof EntityDragon) && (Boolean) settings.get("MOBS").getValue())
                || (player instanceof EntityAnimal && (Boolean) settings.get("ANIMALS").getValue()));
    }

}
