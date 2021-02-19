package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockHopper;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/*
 * Created by cool1 on 1/17/2017.
 */

public class Phase extends Module {

    private int delay;

    private Vec3 enablePos;

    private String PM = "PHASEMODE";
    private Setting distance = new Setting<>("DIST", 0.5, "Distance for HCF phase.", 0.1, 0.1, 2);
    private Setting<Boolean> toggle = new Setting<>("TOGGLE", false, "Automatically disable when you move 5 blocks away.");
    private Setting<Boolean> step = new Setting<>("STEP", false, "Disables Step when Phase is Enabled");

    public Phase(ModuleData data) {
        super(data);
        settings.put(PM, new Setting<>(PM, new Options("Phase Mode", "Normal", "Spider", "Skip", "Normal", "FullBlock", "Silent", "HCF", "Hypixel"), "Phase exploit method."));
        settings.put("DIST", distance);
        settings.put("STEP", step);
        addSetting(toggle);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            enablePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
    }

    @Override
    public void onDisable() {
        enablePos = null;
    }

    @RegisterEvent(events = {EventBlockBounds.class, EventMotionUpdate.class, EventPushBlock.class, EventPacket.class, EventStep.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (event instanceof EventMotionUpdate) {
            if (toggle.getValue() && !isInsideBlock() && mc.thePlayer.getDistance(enablePos.xCoord, enablePos.yCoord, enablePos.zCoord) > 5) {
                toggle();
                return;
            }
        }
        String currentPhase = ((Options) settings.get(PM).getValue()).getSelected();
        this.setSuffix(currentPhase);
        if (currentPhase.equalsIgnoreCase("Hypixel") && mc.getIntegratedServer() == null) {
            if (event instanceof EventMotionUpdate) {
                double multiplier = 0.162905382 + (0.00000000000094385093485943 * Math.random());
                double mx = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw + 90.0F));
                double mz = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw + 90.0F));
                double x = (mc.thePlayer.movementInput.moveForward * multiplier * mx + mc.thePlayer.movementInput.moveStrafe * multiplier * mz);
                double z = (mc.thePlayer.movementInput.moveForward * multiplier * mz - mc.thePlayer.movementInput.moveStrafe * multiplier * mx);
                if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder()) {
                    NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + (x), mc.thePlayer.posY + (0.0000194385093485943 * Math.random()), mc.thePlayer.posZ + (z), false));
                    NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - (0.48209538902354 + (0.00000000000094385093485943 * Math.random())), mc.thePlayer.posZ, false));
                }
            }
            return;
        }
        if (event instanceof EventPushBlock) {
            event.setCancelled(true);
        }
        if (event instanceof EventStep){
            EventStep step = event.cast();
            if (step.isPre() && this.step.getValue()) {
                event.setCancelled(true);
                step.setActive(false);
            }
        }
        if (event instanceof EventPacket && !currentPhase.equalsIgnoreCase("HCF")) {
            EventPacket ep = (EventPacket) event;
            Packet packet = ep.getPacket();
            if (isInsideBlock()) {
                return;
            }
            if (mc.thePlayer.isCollidedHorizontally && packet instanceof C03PacketPlayer) {
                delay++;
                if (this.delay >= 5) {
                    final C03PacketPlayer player = (C03PacketPlayer) packet;
                    final double multiplier = 0.2;
                    final double mx = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                    final double mz = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                    final double x = mc.thePlayer.movementInput.moveForward * multiplier * mx + mc.thePlayer.movementInput.moveStrafe * multiplier * mz;
                    final double z = mc.thePlayer.movementInput.moveForward * multiplier * mz - mc.thePlayer.movementInput.moveStrafe * multiplier * mx;
                    player.x += x;
                    player.z += z;
                    --player.y;
                    this.delay = 0;
                }
            }
        }
        if (event instanceof EventBlockBounds) {
            EventBlockBounds ebb = (EventBlockBounds) event;
            if (mc.thePlayer == null || mc.theWorld == null) {
                return;
            }
            try {
                if ((ebb.getBounds() != null && mc.thePlayer.boundingBox != null) && (ebb.getBounds().maxY > mc.thePlayer.boundingBox.minY) && (mc.thePlayer.isSneaking() || isInsideBlock())) {
                    ebb.setBounds(null);
                    return;
                } else if (currentPhase.equalsIgnoreCase("HCF")) {
                    return;
                }
            } catch (Exception ignored) {

            }
            mc.thePlayer.noClip = true;
            if (ebb.getPos().getY() > mc.thePlayer.posY + (isInsideBlock() ? 0 : 1)) {
                ebb.setBounds(null);
            }
            if (mc.thePlayer.isCollidedHorizontally && ebb.getPos().getY() > mc.thePlayer.boundingBox.minY - 0.4) {
                ebb.setBounds(null);
            }
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre() && (currentPhase.equalsIgnoreCase("HCF") || currentPhase.equalsIgnoreCase("Hypixel")) && isInsideBlock() && mc.thePlayer.isSneaking() && mc.thePlayer.boundingBox != null) {
                final float yaw = mc.thePlayer.rotationYaw;
                double dist = ((Number) distance.getValue()).doubleValue();
                mc.thePlayer.boundingBox = mc.thePlayer.boundingBox.offset(dist * Math.cos(Math.toRadians(yaw + 90.0f)), 0.0, dist * Math.sin(Math.toRadians(yaw + 90.0f)));
            }
            if (em.isPost()) {
                double multiplier = 0.3;
                final double mx = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                final double mz = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                if (currentPhase.equals("FullBlock")) {
                    multiplier = 0.31;
                }
                final double x = mc.thePlayer.movementInput.moveForward * multiplier * mx + mc.thePlayer.movementInput.moveStrafe * multiplier * mz;
                final double z = mc.thePlayer.movementInput.moveForward * multiplier * mz - mc.thePlayer.movementInput.moveStrafe * multiplier * mx;
                switch (currentPhase) {
                    case "FullBlock": {
                        if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder() && !isInsideBlock()) {
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
                            for (int i = 1; i < 11; ++i) {
                                NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.MAX_VALUE * i, mc.thePlayer.posZ, false));
                            }
                            final double posX = mc.thePlayer.posX;
                            final double posY = mc.thePlayer.posY;
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY - (PlayerUtil.isOnLiquid() ? 9000.0 : 0.1), mc.thePlayer.posZ, false));
                            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                            break;
                        }
                        break;
                    }
                    case "Normal": {
                        if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder() && !isInsideBlock()) {
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
                            final double posX2 = mc.thePlayer.posX;
                            final double posY2 = mc.thePlayer.posY;
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(posX2, posY2 - (PlayerUtil.isOnLiquid() ? 9000.0 : 0.09), mc.thePlayer.posZ, false));
                            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                            break;
                        }
                        break;
                    }
                    case "Silent": {
                        if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder() && !isInsideBlock()) {
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
                            for (int i = 1; i < 10; ++i) {
                                NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 8.988465674311579E307, mc.thePlayer.posZ, false));
                            }
                            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                            break;
                        }
                        break;
                    }
                    case "Skip": {
                        if (!mc.thePlayer.isCollidedHorizontally) {
                            break;
                        }
                        mc.thePlayer.motionX *= 0.5;
                        mc.thePlayer.motionZ *= 0.5;
                        final double[] OPOP = {-0.02500000037252903, -0.028571428997176036, -0.033333333830038704, -0.04000000059604645, -0.05000000074505806, -0.06666666766007741, -0.10000000149011612, 0.0, -0.20000000298023224, -0.04000000059604645, -0.033333333830038704, -0.028571428997176036, -0.02500000037252903};
                        for (int j = 0; j < OPOP.length; ++j) {
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + OPOP[j], mc.thePlayer.posZ, false));
                            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x * j, mc.thePlayer.boundingBox.minY, mc.thePlayer.posZ + z * j, false));
                        }
                        NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                        NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.boundingBox.minY, mc.thePlayer.posZ, false));
                        break;
                    }
                    case "Spider": {
                        if (!isInsideBlock()) {
                            break;
                        }
                        mc.thePlayer.posY += 0.1;
                        mc.thePlayer.motionY = 0.065;
                        mc.thePlayer.resetHeight();
                        break;
                    }
                }
            }
        }
    }

    private boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(mc.thePlayer.boundingBox.minX); x < MathHelper.floor_double(mc.thePlayer.boundingBox.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(mc.thePlayer.boundingBox.minY); y < MathHelper.floor_double(mc.thePlayer.boundingBox.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(mc.thePlayer.boundingBox.minZ); z < MathHelper.floor_double(mc.thePlayer.boundingBox.maxZ) + 1; ++z) {
                    final Block block = mc.thePlayer.getEntityWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir)) {
                        AxisAlignedBB boundingBox = block.getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z)));
                        if (block instanceof BlockHopper) {
                            boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                        }
                        if (boundingBox != null && mc.thePlayer.boundingBox.intersectsWith(boundingBox)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
