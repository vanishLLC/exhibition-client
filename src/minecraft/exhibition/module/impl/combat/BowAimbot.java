/**
 * Time: 10:13:34 PM
 * Date: Jan 5, 2017
 * Creator: cool1
 */
package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cool1
 */
public class BowAimbot extends Module {

    public BowAimbot(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                if ((this.mc.thePlayer.isUsingItem()) && ((this.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBow))) {
                    EntityLivingBase target = getTarg();
                    if (target != this.mc.thePlayer && target != null) {
                        float[] rotations = RotationUtils.getBowAngles(target);
                        em.setYaw(rotations[0]);
                        em.setPitch(rotations[1]);
                    }
                }
            }
        }
    }

    private EntityLivingBase getTarg() {
        List<EntityLivingBase> loaded = new ArrayList<>();
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase ent = (EntityLivingBase) o;
                if (ent instanceof EntityPlayer && mc.thePlayer.canEntityBeSeen(ent) && !FriendManager.isFriend(ent.getName()) && ent.isEntityAlive()) {
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
        EntityLivingBase target = loaded.get(0);
        return target;
    }

}
