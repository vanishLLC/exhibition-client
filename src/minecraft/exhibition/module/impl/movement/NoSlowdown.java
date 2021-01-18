package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.MathUtils;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;

public class NoSlowdown extends Module {

    public NoSlowdown(ModuleData data) {
        super(data);
        settings.put("VANILLA", new Setting<>("VANILLA", true, "Vanilla. Bypasses on Hypixel with KillAura."));
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null)
            return;
        boolean shouldUnblock = (mc.thePlayer.isBlocking() || (mc.thePlayer.isUsingItem() && mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow)) && PlayerUtil.isMoving();
        if (event instanceof EventMotionUpdate) {
            if ((boolean) settings.get("VANILLA").getValue())
                return;
            if (!mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround){
                return;
            }
            EventMotionUpdate em = (EventMotionUpdate) event;
            Killaura killaura = ((Killaura) Client.getModuleManager().get(Killaura.class));
            if (shouldUnblock && !(Boolean) settings.get("VANILLA").getValue()) {
                if (em.isPre() && (mc.thePlayer.isBlocking() && killaura.isBlocking)) {
                    if (isTouchingGround(mc.thePlayer)) {
                        killaura.isBlocking = false;
                        NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
                    }
                }
                if (em.isPost() && (mc.thePlayer.isBlocking() && !killaura.isBlocking)) {
                    killaura.isBlocking = true;
                    NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                }
            }
        }
    }

    public boolean isTouchingGround(EntityPlayer player) {
        boolean touchingGround = false;
        double[][] offsets = new double[][]{new double[]{0, 0}, new double[]{-0.35, -0.35}, new double[]{-0.35, 0.35}, new double[]{0.35, 0.35}, new double[]{0.35, -0.35}};
        for (double[] offset : offsets) {
            double offsetX = offset[0];
            double offsetZ = offset[1];

            double posX = offsetX + player.posX;
            double posY = -0.5 + player.posY;
            double posZ = offsetZ + player.posZ;

            double lastPosX = offsetX + player.lastTickPosX;
            double lastPosY = -0.5 + player.lastTickPosY;
            double lastPosZ = offsetZ + player.lastTickPosZ;

            if (isPosOnGround(posX, posY, posZ) && isPosOnGround(lastPosX, lastPosY, lastPosZ)) {
                touchingGround = true;
                break;
            }
        }
        return touchingGround;
    }
    public boolean isPosOnGround(double posX, double posY, double posZ) {
        boolean isOnSlab = MathUtils.roundToPlace((posY - (int) posY), 1) == 0.5;

        Block nextBlockUnder = mc.theWorld.getBlockState(new BlockPos(posX, posY - (isOnSlab ? 0 : 0.1), posZ)).getBlock();

        boolean feetBlockAir = isOnSlab ? nextBlockUnder.getMaterial() == Material.air : (nextBlockUnder instanceof BlockSlab && !nextBlockUnder.isFullBlock()) || nextBlockUnder.getMaterial() == Material.air;

        return !feetBlockAir && !mc.theWorld.getBlockState(new BlockPos(posX, posY + 1.5D, posZ)).getBlock().isBlockNormalCube();
    }
}


