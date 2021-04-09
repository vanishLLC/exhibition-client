package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMouse;
import exhibition.management.PriorityManager;
import exhibition.management.command.Command;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * Created by Neohack on 3/24/2021 at 4:41 PM.
 */
public class MCV extends Module {

    public MCV(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = EventMouse.class)
    public void onEvent(Event event) {
        EventMouse em = (EventMouse) event;

        if (em.getButtonID() == 2 && Mouse.getEventButtonState()) {
            boolean priority = mc.thePlayer.isSneaking();
            if (mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) mc.objectMouseOver.entityHit;
                if (priority) {
                    if (PriorityManager.isPriority(entityPlayer)) {
                        PriorityManager.removePriority(entityPlayer);
                        ChatUtil.printChat(Command.chatPrefix + "\247c" + entityPlayer.getName() + "\2477 is no longer prioritized.");
                    } else {
                        PriorityManager.setAsPriority(entityPlayer);
                        ChatUtil.printChat(Command.chatPrefix + "\247c" + entityPlayer.getName() + "\2477 has been \247cprioritized.");
                    }
                } else {
                    if (FriendManager.isFriend(entityPlayer.getName())) {
                        ChatUtil.printChat(Command.chatPrefix + "\247b" + entityPlayer.getName() + "\2477 has been \247cunfriended.");
                        FriendManager.removeFriend(entityPlayer.getName());
                    } else {
                        ChatUtil.sendChat(".view " + entityPlayer.getName());
                    }
                }
            } else {
                double blockReachDistance = 150;
                {
                    EntityPlayer pointedEntity = null;
                    Vec3 attackerVec = mc.getRenderViewEntity().getPositionEyes(mc.timer.renderPartialTicks);
                    final Vec3 lookVec = mc.getRenderViewEntity().getLook(mc.timer.renderPartialTicks);
                    final Vec3 expandedVec = attackerVec.addVector(lookVec.xCoord * blockReachDistance, lookVec.yCoord * blockReachDistance, lookVec.zCoord * blockReachDistance);
                    final float one = 1.0f;
                    final List<Entity> entitiesWithinBoundingBox = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(lookVec.xCoord * blockReachDistance, lookVec.yCoord * blockReachDistance, lookVec.zCoord * blockReachDistance).expand((double) one, (double) one, (double) one));
                    double reachTemp = blockReachDistance;
                    for (Entity entityInBox : entitiesWithinBoundingBox) {
                        if (entityInBox.canBeCollidedWith() && entityInBox instanceof EntityPlayer) {
                            final float borderSize = entityInBox.getCollisionBorderSize();
                            final AxisAlignedBB expandedBox = entityInBox.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
                            final MovingObjectPosition var19 = expandedBox.calculateIntercept(attackerVec, expandedVec);
                            if (expandedBox.isVecInside(attackerVec)) {
                                if (0.0 < reachTemp || reachTemp == 0.0) {
                                    pointedEntity = (EntityPlayer) entityInBox;
                                    reachTemp = 0.0;
                                }
                            } else if (var19 != null) {
                                final double var20 = attackerVec.distanceTo(var19.hitVec);
                                if (var20 < reachTemp || reachTemp == 0.0) {
                                    if (entityInBox == mc.getRenderViewEntity().ridingEntity) {
                                        if (reachTemp == 0.0) {
                                            pointedEntity = (EntityPlayer) entityInBox;
                                        }
                                    } else {
                                        pointedEntity = (EntityPlayer) entityInBox;
                                        reachTemp = var20;
                                    }
                                }
                            }
                        }
                    }
                    if (pointedEntity != null && (reachTemp < blockReachDistance)) {
                        ChatUtil.sendChat(".view " + pointedEntity.getName());
                    }
                }
            }
        }
    }

}

