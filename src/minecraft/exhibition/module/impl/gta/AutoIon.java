package exhibition.module.impl.gta;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.RotationUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Arithmo on 9/3/2017 at 1:44 PM.
 */
public class AutoIon extends Module {

    public static EntityPlayer target;
    int ticks;
    public static float oldYaw;
    private float oldPitch;

    public AutoIon(ModuleData data) {
        super(data);
        settings.put("OLYMPIAS", new Setting<>("OLYMPIAS", 5, "Oly count", 1, 1, 9));
        settings.put("AIMBOT", new Setting<>("AIMBOT", false, "Silent Aim"));
        settings.put("SHOWAIM", new Setting<>("SHOWAIM", true, "Shows where Aimbot will aim."));
        settings.put("RANGE", new Setting<>("RANGE", 5, "Range"));
    }

    public void onEnable() {

    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if(AutoBandage.shouldBand)
                return;
            if(em.isPre() && target == null) {
                oldYaw = mc.thePlayer.rotationYaw;
            }
            if (mc.thePlayer.isSneaking()) {
                if (em.isPre()) {
                    if ((Boolean) settings.get("AIMBOT").getValue()) {
                        target = ((EntityPlayer) getBestEntity());
                        if (target != null) {
                            float[] rotations = getPlayerRotations(target);
                            em.setYaw(oldYaw = rotations[0]);
                            em.setPitch(rotations[1]);
                        }
                    }
                    if (!AutoBandage.shouldBand) {
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(ticks));
                    }
                }
                if (em.isPost()) {
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    int olys = ((Number) settings.get("OLYMPIAS").getValue()).intValue();
                    if (ticks >= olys - 1) {
                        ticks = 0;
                    } else {
                        ticks += 1;
                    }
                }
            } else {
                target = null;
                ticks = 0;
            }
        } else {
            EventRender3D er = (EventRender3D)event;
            if(target != null) {
                double p[] = getPredPos(target);
                double x = (mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX;
                double y = (mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY;
                double z = (mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                RenderingUtil.filledBox(new AxisAlignedBB(p[0] - 0.5,p[1],p[2] - 0.5,p[0]+0.5,p[1]+2,p[2]+0.5), Colors.getColor(255,0,0,100),true);
                GlStateManager.popMatrix();
            }
        }
    }

    private double[] getPredPos(Entity entity) {
        double xDelta = entity.posX - entity.lastTickPosX;
        double zDelta = entity.posZ - entity.lastTickPosZ;
        double yDelta = entity.posY - entity.lastTickPosY;
        double d = mc.thePlayer.getDistanceToEntity(entity);
        d -= d % 0.8D;
        double xMulti = 1.0D;
        double zMulti = 1.0D;
        boolean sprint = entity.isSprinting();
        xMulti = d / 0.8D * xDelta * (sprint ? 1.2D : 1.1D);
        zMulti = d / 0.8D * zDelta * (sprint ? 1.2D : 1.1D);
        double yMulti = d / 0.8D * yDelta * 0.1D;
        double x = entity.posX + xMulti - mc.thePlayer.posX;
        double z = entity.posZ + zMulti - mc.thePlayer.posZ;
        double y = entity.posY + yMulti - mc.thePlayer.posY;
        return new double[]{x, y, z};
    }

    private float[] getPlayerRotations(Entity entity) {
        double xDelta = entity.posX - entity.lastTickPosX;
        double zDelta = entity.posZ - entity.lastTickPosZ;
        double yDelta = entity.posY - entity.lastTickPosY;
        double d = mc.thePlayer.getDistanceToEntity(entity);
        d -= d % 0.8D;
        double xMulti = 1.0D;
        double zMulti = 1.0D;
        boolean sprint = entity.isSprinting();
        xMulti = d / 0.8D * xDelta * (sprint ? 1.2D : 1.1D);
        zMulti = d / 0.8D * zDelta * (sprint ? 1.2D : 1.1D);
        double yMulti = d / 0.8D * yDelta * 0.1D;
        double x = entity.posX + xMulti - mc.thePlayer.posX;
        double z = entity.posZ + zMulti - mc.thePlayer.posZ;
        double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - (entity.posY + entity.getEyeHeight()) - yMulti;
        double dist = mc.thePlayer.getDistanceToEntity(entity);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0F;
        float pitch = (float) Math.toDegrees(Math.atan2(y, dist));
        return new float[]{yaw, pitch};
    }

    private EntityLivingBase getBestEntity() {
        List<EntityLivingBase> loaded = new ArrayList<>();
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if ((o instanceof EntityLivingBase)) {
                EntityLivingBase ent = (EntityLivingBase) o;
                if (validEntity(ent)) {
                    loaded.add(ent);
                }
            }
        }
        if (loaded.isEmpty()) {
            return null;
        }
        loaded.sort(Comparator.comparingDouble(o -> (Math.abs(RotationUtils.getYawChange(o.posX, o.posZ)) + Math.abs(RotationUtils.getPitchChange(o, o.posY)))));
        return loaded.get(0);
    }

    private boolean validEntity(Entity entity) {
        if ((entity instanceof EntityLivingBase)) {
            EntityLivingBase entityLiving = (EntityLivingBase) entity;
            float range = ((Number) settings.get("RANGE").getValue()).floatValue();
            if ((mc.thePlayer.isEntityAlive()) && (entityLiving.isEntityAlive()) &&
                    (entityLiving.getDistanceToEntity(mc.thePlayer) <= range) &&
                    (mc.thePlayer.canEntityBeSeen(entityLiving)) &&
                    ((entityLiving instanceof EntityPlayer))) {
                EntityPlayer ent = (EntityPlayer) entityLiving;
                return !FriendManager.isFriend(ent.getName());
            }
        }
        return false;
    }
}
