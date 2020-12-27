package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RotationUtils;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AimAssist extends Module {

    private EntityLivingBase target;

    public AimAssist(ModuleData data) {
        super(data);
        settings.put(WEAPON, new Setting<>(WEAPON, true, "Checks if you have a sword in hand."));
        settings.put(X, new Setting<>(X, 0, "Randomization on XZ axis.", 0.1, 0, 1.5));
        settings.put(Y, new Setting<>(Y, 0, "Randomization on Y axis.", 0.1, 0, 1.5));
        settings.put(RANGE, new Setting<>(RANGE, 4.5, "The distance in which an entity is valid to attack.", 0.1D, 1.0D, 10.0D));
        settings.put(HORIZONTAL, new Setting<>(HORIZONTAL, 20, "Horizontal speed.", 0.25, 0, 10));
        settings.put(VERTICAL, new Setting<>(VERTICAL, 15, "Vertical speed.", 0.25, 0, 10));
        settings.put(FOVYAW, new Setting<>(FOVYAW, 45, "Yaw FOV check.", 1, 5D, 50));
        settings.put(FOVPITCH, new Setting<>(FOVPITCH, 25, "Vertical FOV check.", 1, 5D, 50));
        settings.put(HEIGHT, new Setting<>(HEIGHT, 0, "Adjust aim height.", 0.1, -1, 1));
    }

    private String HEIGHT = "HEIGHT";
    private String WEAPON = "WEAPON";
    private String RANGE = "RANGE";
    private String HORIZONTAL = "SPEED-H";
    private String VERTICAL = "SPEED-V";
    private String FOVYAW = "FOVYAW";
    private String FOVPITCH = "FOVPITCH";
    private String X = "RANDOM-XZ";
    private String Y = "RANDOM-Y";

    private int randomNumber() {
        return (-100 + (int) (Math.random() * ((100 - (-100)) + 1)));
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                target = getBestEntity();
            } else if (em.isPost() && mc.currentScreen == null) {
                if (mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem() == null) {
                    return;
                }
                final Item heldItem = mc.thePlayer.getHeldItem().getItem();
                if ((Boolean) settings.get(WEAPON).getValue() && heldItem != null) {
                    if (!(heldItem instanceof ItemSword)) {
                        return;
                    }
                }
                if (target != null && !FriendManager.isFriend(target.getName()) && mc.thePlayer.isEntityAlive()) {
                    stepAngle();
                }
            }
        }
    }


    private void stepAngle() {
        float yawFactor = ((Number) settings.get(HORIZONTAL).getValue()).floatValue();
        float pitchFactor = ((Number) settings.get(VERTICAL).getValue()).floatValue();
        double xz = ((Number) settings.get(X).getValue()).doubleValue();
        double y = ((Number) settings.get(Y).getValue()).doubleValue();
        double yOff = ((Number) settings.get(HEIGHT).getValue()).doubleValue();
        float targetYaw = RotationUtils.getYawChange(target.posX + (xz * randomNumber()/100), target.posZ + (xz * randomNumber()/100));

        if (targetYaw > 0 && targetYaw > yawFactor) {
            mc.thePlayer.rotationYaw += yawFactor;
        } else if (targetYaw < 0 && targetYaw < -yawFactor) {
            mc.thePlayer.rotationYaw -= yawFactor;
        } else {
            mc.thePlayer.rotationYaw += targetYaw;
        }

        float targetPitch = RotationUtils.getPitchChange(target, target.posY + yOff + (y * randomNumber()/100));

        if (targetPitch > 0 && targetPitch > pitchFactor) {
            mc.thePlayer.rotationPitch += pitchFactor;
        } else if (targetPitch < 0 && targetPitch < -pitchFactor) {
            mc.thePlayer.rotationPitch -= pitchFactor;
        } else {
            mc.thePlayer.rotationPitch += targetPitch;
        }

    }

    private EntityLivingBase getBestEntity() {
        List<EntityLivingBase> loaded = new CopyOnWriteArrayList<>();
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase ent = (EntityLivingBase) o;
                if (ent.isEntityAlive() && ent instanceof EntityPlayer && ent
                        .getDistanceToEntity(mc.thePlayer) < ((Number) settings.get(RANGE).getValue()).floatValue()
                        && fovCheck(ent)) {
                    if (ent == Killaura.vip) {
                        return ent;
                    }
                    loaded.add(ent);
                }
            }
        }
        if (loaded.isEmpty()) {
            return null;
        }
        try {
            loaded.sort((o1, o2) -> {
                float[] rot1 = RotationUtils.getRotations(o1);
                float[] rot2 = RotationUtils.getRotations(o2);
                return (int) ((RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationYaw, rot1[0])
                        + RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationPitch, rot1[1]))
                        - (RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationYaw, rot2[0])
                        + RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationPitch, rot2[1])));
            });
        } catch (Exception e) {
            ChatUtil.printChat("Exception with TM: " + e.getMessage());
        }
        return loaded.get(0);
    }

    private boolean fovCheck(EntityLivingBase ent) {
        float[] rotations = RotationUtils.getRotations(ent);
        float dist = mc.thePlayer.getDistanceToEntity(ent);
        if (dist == 0) {
            dist = 1;
        }
        float yawDist = RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationYaw, rotations[0]);
        float pitchDist = RotationUtils.getDistanceBetweenAngles(mc.thePlayer.rotationPitch, rotations[1]);
        float fovYaw = ((Number) settings.get(FOVYAW).getValue()).floatValue() * 3 / dist;
        float fovPitch = (((Number) settings.get(FOVPITCH).getValue()).floatValue() * 3) / dist;
        return yawDist < fovYaw && pitchDist < fovPitch;
    }

}
