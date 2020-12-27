package exhibition.module.impl.movement;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventLiquidCollide;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class Jesus extends Module {


    public Jesus(ModuleData data) {
        super(data);
    }

    int ticks;

    @Override
    public void onEnable() {
        super.onEnable();
        ticks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ticks = 0;
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventLiquidCollide.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if ((em.isPre() && PlayerUtil.isOnLiquid()) && !PlayerUtil.isInLiquid() && !mc.thePlayer.isSneaking() && !mc.gameSettings.keyBindJump.getIsKeyPressed()) {
                if (ticks == 0 && PlayerUtil.isOnLiquid() && PlayerUtil.isMoving() && mc.thePlayer.isSprinting()) {
                    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    mc.thePlayer.setSprinting(false);
                    ticks = 1;
                }
                if (mc.thePlayer.fallDistance == 0)
                    em.setY(em.getY() + (mc.thePlayer.ticksExisted % 2 == 0 ? 0x1.10cd523494dffp-40 + 0.000000000000007683421 : -(0x1.10cd523494dffp-40 + 0.000000000000007683421)));
            }
            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock() == Blocks.lava)
                em.setGround(mc.thePlayer.ticksExisted % 2 != 0);
            if (!shouldSetBoundingBox() && PlayerUtil.isInLiquid()) {
                mc.thePlayer.fallDistance = 0;
                mc.thePlayer.motionY = mc.thePlayer.isSneaking() ? -0.1300000000 : 0.10009909384578543;
            }
            if (ticks == 1 && !PlayerUtil.isOnLiquid() && !PlayerUtil.isInLiquid()) {
                ticks = 0;
            }
            if (PlayerUtil.isOnLiquid() && mc.theWorld.getBlockState((new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ))).getBlock() == Blocks.lava && !PlayerUtil.isMoving()) {
                em.setCancelled(true);
            }
        }
        if (event instanceof EventLiquidCollide) {
            EventLiquidCollide ebb = event.cast();
            if (ebb.getPos().getY() + 0.9 < mc.thePlayer.boundingBox.minY) {
                ebb.setBounds(new AxisAlignedBB(ebb.getPos().getX(), ebb.getPos().getY(), ebb.getPos().getZ(), ebb.getPos().getX() + 1, ebb.getPos().getY() + 1, ebb.getPos().getZ() + 1));
                ebb.setCancelled(shouldSetBoundingBox());
            }
        }
    }

    private boolean shouldSetBoundingBox() {
        return (!mc.thePlayer.isSneaking()) && (mc.thePlayer.fallDistance < 4.0F) && !PlayerUtil.isInLiquid();
    }
}

