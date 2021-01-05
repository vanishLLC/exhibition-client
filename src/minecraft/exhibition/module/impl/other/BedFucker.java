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
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import static exhibition.module.impl.player.Scaffold.randomFloat;

/**
 * Created by cool1 on 1/19/2017.
 */
public class BedFucker extends Module {

    private Vec3 teleported = null;

    private BlockPos blockBreaking;
    private MultiBool multiBool = new MultiBool("Blocks", new Setting("BED", true), new Setting("CAKE", true), new Setting("EGG", true));

    public BedFucker(ModuleData data) {
        super(data);
        addSetting("ITEMS", new Setting("ITEMS", multiBool, "The blocks to interact/break."));
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class, EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null)
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
                for (int y = 6; y >= -6; --y) {
                    for (int x = -6; x <= 6; ++x) {
                        for (int z = -6; z <= 6; ++z) {
                            boolean uwot = x != 0 || z != 0;
                            if (mc.thePlayer.isSneaking()) {
                                uwot = !uwot;
                            }
                            if (uwot) {
                                final BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                                if (this.getFacingDirection(pos) != null && this.blockChecks(mc.theWorld.getBlockState(pos).getBlock()) && mc.thePlayer.getDistance(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z) < mc.playerController.getBlockReachDistance() - 0.5) {
                                    final float[] rotations = this.getBlockRotations(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                                    em.setYaw(rotations[0]);
                                    em.setPitch(rotations[1]);
                                    this.blockBreaking = pos;
                                    return;
                                }
                            }
                        }
                    }
                }
                this.blockBreaking = null;
            } else {
                if (this.blockBreaking != null) {
                    if (mc.playerController.blockHitDelay > 1) {
                        mc.playerController.blockHitDelay = 1;
                    }
                    final EnumFacing direction = this.getFacingDirection(this.blockBreaking);
                    if (direction != null) {
                        if (mc.theWorld.getBlockState(blockBreaking).getBlock() == Blocks.bed) {
                            if (mc.playerController.onPlayerDamageBlock(this.blockBreaking, direction)) {
                                mc.thePlayer.swingItem();
                            }
                        } else {
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), blockBreaking, direction, new Vec3(blockBreaking).addVector(0.3F + randomFloat(1), 0.3F + randomFloat(2345),0.3F +  randomFloat(652436)))) {
                                mc.thePlayer.swingItem();
                            }
                        }
                    }
                }
            }
        }
        if (event instanceof EventRender3D) {
            if (blockBreaking != null) {
                RenderingUtil.pre3D();
                drawESP(blockBreaking.getX(), blockBreaking.getY(), blockBreaking.getZ(), blockBreaking.getX() + 1, blockBreaking.getY() + 1, blockBreaking.getZ() + 1, 100, 100, 100);
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

    public void drawESP(final double x, final double y, final double z, final double x2, final double y2, final double z2, final double r, final double g, final double b) {
        final double x3 = x - RenderManager.renderPosX;
        final double y3 = y - RenderManager.renderPosY;
        final double z3 = z - RenderManager.renderPosZ;
        final double x4 = x2 - RenderManager.renderPosX;
        final double y4 = y2 - RenderManager.renderPosY;
        drawFilledBBESP(new AxisAlignedBB(x3, y3, z3, x4, y4, z2 - RenderManager.renderPosZ), Colors.getColor(0, 0, 50, 0));
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
        float red = (color >> 24 & 0xFF) / 255.0F;
        float green = (color >> 16 & 0xFF) / 255.0F;
        float blue = (color >> 8 & 0xFF) / 255.0F;
        float alpha = (color & 0xFF) / 255.0F;
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2896);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawFilledBox(axisalignedbb);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2896);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    private boolean blockChecks(final Block block) {
        return (block == Blocks.bed && multiBool.getValue("BED")) || (block == Blocks.cake && multiBool.getValue("CAKE") || (block == Blocks.dragon_egg && multiBool.getValue("EGG")));
    }

    public float[] getBlockRotations(final double x, final double y, final double z) {
        final double var4 = x - mc.thePlayer.posX + 0.5;
        final double var5 = z - mc.thePlayer.posZ + 0.5;
        final double var6 = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - 1.0);
        final double var7 = MathHelper.sqrt_double(var4 * var4 + var5 * var5);
        final float var8 = (float) (Math.atan2(var5, var4) * 180.0 / 3.141592653589793) - 90.0f;
        return new float[]{var8, (float) (-(Math.atan2(var6, var7) * 180.0 / 3.141592653589793))};
    }

    private EnumFacing getFacingDirection(final BlockPos pos) {
        EnumFacing direction = null;
        if (!mc.theWorld.getBlockState(pos.add(0, 1, 0)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.UP;
        } else if (!mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.DOWN;
        } else if (!mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.EAST;
        } else if (!mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.WEST;
        } else if (!mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.SOUTH;
        } else if (!mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.NORTH;
        }
        final MovingObjectPosition rayResult = mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        if (rayResult != null && rayResult.getBlockPos() == pos) {
            return rayResult.sideHit;
        }
        return direction;
    }

}
