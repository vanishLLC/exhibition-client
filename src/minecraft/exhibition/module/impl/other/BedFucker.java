package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

/**
 * Created by cool1 on 1/19/2017.
 */
public class BedFucker extends Module {

    private Vec3 teleported = null;

    private BlockPos blockBreaking;
    private MultiBool multiBool = new MultiBool("Blocks", new Setting<>("BED", true), new Setting<>("WHEAT", true), new Setting<>("CAKE", true), new Setting<>("EGG", true));

    public BedFucker(ModuleData data) {
        super(data);
        addSetting("ITEMS", new Setting("ITEMS", multiBool, "The blocks to interact/break."));
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (HypixelUtil.isInGame("BED WARS") && HypixelUtil.isGameStarting() && teleported != null) {
                teleported = null;
            }

            if (HypixelUtil.isInGame("BED WARS") && teleported != null) {
                if (mc.thePlayer.getDistance(teleported.getX(), mc.thePlayer.posY, teleported.getZ()) < 30) {
                    return;
                }
            }

            if (em.isPre()) {
                this.blockBreaking = null;
                outer:
                for (int y = 6; y >= -6; --y) {
                    for (int x = -6; x <= 6; ++x) {
                        for (int z = -6; z <= 6; ++z) {
                            boolean uwot = x != 0 || z != 0;
                            if (mc.thePlayer.isSneaking()) {
                                uwot = !uwot;
                            }
                            if (uwot) {
                                double posX = mc.thePlayer.posX + x;
                                double posY = mc.thePlayer.posY + y;
                                double posZ = mc.thePlayer.posZ + z;

                                final BlockPos pos = new BlockPos(posX, posY, posZ);

                                if ((mc.theWorld.getBlockState(pos).getBlock() != Blocks.air && this.blockChecks(mc.theWorld.getBlockState(pos).getBlock()) && this.getHitResult(pos) != null) &&
                                        mc.thePlayer.getDistance(posX, posY, posZ) <= 6.0) {

                                    IBlockState blockState = mc.theWorld.getBlockState(pos);
                                    Block block = blockState.getBlock();

                                    if (block == Blocks.wheat && block instanceof BlockCrops) {
                                        BlockCrops cropBlock = (BlockCrops) block;
                                        if (cropBlock.canGrow(null, null, blockState, true)) {
                                            continue;
                                        }
                                    }

                                    this.blockBreaking = pos;
                                    break outer;
                                }
                            }
                        }
                    }
                }

                if(this.blockBreaking != null) {
                    IBlockState blockState = mc.theWorld.getBlockState(blockBreaking);
                    Block block = blockState.getBlock();
                    AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, blockBreaking);

                    double centerX = bb.minX + (bb.maxX - bb.minX)/2;
                    double centerY = bb.minY + (bb.maxY - bb.minY)/2;
                    double centerZ = bb.minZ + (bb.maxZ - bb.minZ)/2;

                    final float[] rotations = this.getBlockRotations(centerX, centerY, centerZ);
                    em.setYaw(rotations[0]);
                    em.setPitch(rotations[1]);
                }

            } else {
                if (this.blockBreaking != null) {
                    if (mc.playerController.blockHitDelay > 0) {
                        mc.playerController.blockHitDelay = 0;
                    }
                    MovingObjectPosition hitResult = this.getHitResult(this.blockBreaking);
                    if (hitResult != null) {
                        IBlockState blockState = mc.theWorld.getBlockState(blockBreaking);
                        Block block = blockState.getBlock();
                        if (block == Blocks.bed || block == Blocks.wheat) {
                            if (mc.playerController.onPlayerDamageBlock(this.blockBreaking, hitResult.sideHit)) {
                                mc.thePlayer.swingItem();
                            }
                        } else {
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), blockBreaking, hitResult.sideHit, hitResult.hitVec)) {
                                mc.thePlayer.swingItem();
                            }
                        }
                    }
                }
            }
        }
        if (event instanceof EventRender3D) {
            if (blockBreaking != null) {

                IBlockState blockState = mc.theWorld.getBlockState(blockBreaking);
                Block block = blockState.getBlock();
                AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, blockBreaking);
                RenderingUtil.pre3D();
                drawESP(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 100, 255, 100);
                RenderingUtil.post3D();
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook posLook = (S08PacketPlayerPosLook) packet;
                if (mc.thePlayer.ticksExisted > 10) {
                    if (Math.abs((int) posLook.getX() - posLook.getX()) == 0.5 || Math.abs((int) posLook.getZ() - posLook.getZ()) == 0.5) {
                        teleported = new Vec3(posLook.getX(), posLook.getY(), posLook.getZ());
                    }
                }
            }
        }

    }

    public void drawESP(final double x, final double y, final double z, final double x2, final double y2,
                        final double z2, final double r, final double g, final double b) {
        final double x3 = x - RenderManager.renderPosX;
        final double y3 = y - RenderManager.renderPosY;
        final double z3 = z - RenderManager.renderPosZ;
        final double x4 = x2 - RenderManager.renderPosX;
        final double y4 = y2 - RenderManager.renderPosY;
        drawFilledBBESP(new AxisAlignedBB(x3, y3, z3, x4, y4, z2 - RenderManager.renderPosZ), Colors.getColor((int)r, (int)g, (int)b, 150));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawFilledBox(final AxisAlignedBB boundingBox) {
        if (boundingBox == null) {
            return;
        }
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(7);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glEnd();
    }

    public void drawFilledBBESP(final AxisAlignedBB axisalignedbb, final int color) {
        GL11.glPushMatrix();
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        drawFilledBox(axisalignedbb);
        GL11.glPopMatrix();
    }

    private boolean blockChecks(final Block block) {
        return (block == Blocks.bed && multiBool.getValue("BED")) || (block == Blocks.wheat && multiBool.getValue("WHEAT")) || (block == Blocks.cake && multiBool.getValue("CAKE") || (block == Blocks.dragon_egg && multiBool.getValue("EGG")));
    }

    public float[] getBlockRotations(final double x, final double y, final double z) {
        double diffX = x - mc.thePlayer.posX;
        double diffY = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double diffZ = z - mc.thePlayer.posZ;
        double distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distance));

        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
    }

    private MovingObjectPosition getHitResult(final BlockPos pos) {
        IBlockState blockState = mc.theWorld.getBlockState(pos);
        Block block = blockState.getBlock();
        AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, pos);

        double centerX = bb.minX + (bb.maxX - bb.minX)/2;
        double centerY = bb.minY + (bb.maxY - bb.minY)/2;
        double centerZ = bb.minZ + (bb.maxZ - bb.minZ)/2;

        return mc.theWorld.rayTraceOnBlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(centerX, centerY, centerZ), pos);
    }

}
