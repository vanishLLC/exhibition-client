package exhibition.module.impl.other;

import com.google.common.collect.Lists;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.management.ColorManager;
import exhibition.module.Module;
import exhibition.module.data.BlockList;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.player.AutoTool;
import exhibition.util.RenderingUtil;
import exhibition.util.RotationUtils;
import exhibition.util.misc.BlockUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SurvivalNuker extends Module {

    private BlockPos blockBreaking;
    private List<BlockPos> blocksToBreak;

    private Options targetMode = new Options("Sorting", "Range", "Range", "FOV", "Angle");
    private BlockList blockList = new BlockList(Arrays.asList(Blocks.redstone_ore, Blocks.diamond_ore, Blocks.lapis_ore, Blocks.coal_ore, Blocks.emerald_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.quartz_ore));

    public SurvivalNuker(ModuleData data) {
        super(data);
        settings.put("SORT", new Setting<>("SORT", targetMode, "Sorting mode for nuker."));
        settings.put("ALL", new Setting<>("ALL", false, "Breaks any blocks within range. (Ignores blocklist)"));
        settings.put("X", new Setting<>("X", 5, "X range to scan for blocks.", 1, 0, 5));
        settings.put("Y", new Setting<>("Y", 3, "Y range to scan for blocks.", 1, 0, 5));
        settings.put("Z", new Setting<>("Z", 5, "Z range to scan for blocks.", 1, 0, 5));


        settings.put("BLOCKLIST", new Setting<>("BLOCKLIST", blockList, "List of blocks to nuke."));
        blocksToBreak = Lists.newArrayList();
    }

    @Override
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPre()) {
                if (!this.blocksToBreak.isEmpty())
                    this.blocksToBreak.clear();
                int scanX = ((Number) settings.get("X").getValue()).intValue(), scanY = ((Number) settings.get("Y").getValue()).intValue(), scanZ = ((Number) settings.get("Z").getValue()).intValue();
                for (int y = scanY; y >= (mc.thePlayer.isSneaking() ? -scanY : 0); y--) {
                    for (int x = 0; x <= scanX; ++x) {
                        for (int bruh = 0; bruh < 2; bruh++) {
                            boolean flipX = bruh == 0;
                            int a = flipX ? -1 : 1;
                            for (int z = 0; z <= scanZ; ++z) {
                                for (int bruh2 = 0; bruh2 < 2; bruh2++) {
                                    boolean flipZ = bruh2 == 0;
                                    int aa = flipZ ? -1 : 1;
                                    final BlockPos pos = new BlockPos(mc.thePlayer.posX + (x * a), mc.thePlayer.posY + y, mc.thePlayer.posZ + (z * aa));
                                    MovingObjectPosition rayResult = mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), false, true, true);
                                    if (rayResult != null && rayResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && rayResult.getBlockPos() != null && rayResult.getBlockPos().equals(pos))
                                        if (this.getFacingDirection(pos) != null &&
                                                (((boolean) settings.get("ALL").getValue() && mc.theWorld.getBlockState(pos).getBlock().getMaterial() != Material.air) ||
                                                        this.blockChecks(mc.theWorld.getBlockState(pos).getBlock())) &&
                                                mc.thePlayer.getDistance(mc.thePlayer.posX + (x * a), mc.thePlayer.posY + y, mc.thePlayer.posZ + (z * aa)) <= mc.playerController.getBlockReachDistance()) {
                                            final float[] rotations = this.getBlockRotations(mc.thePlayer.posX + (x * a), mc.thePlayer.posY + y, mc.thePlayer.posZ + (z * aa));
                                            float newYaw = em.getYaw() + RotationUtils.getYawChange(mc.thePlayer.posX + (x * a), mc.thePlayer.posZ + (z * aa));
                                            float newPitch = rotations[1];

                                            if (!Float.isNaN(newYaw) && !Float.isNaN(newPitch)) {
                                                em.setYaw(newYaw);
                                                em.setPitch(rotations[1]);
                                            }

                                            if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.bedrock)
                                                this.blocksToBreak.add(pos);
                                        }
                                }
                            }
                        }
                    }
                }
                if (this.blocksToBreak.size() > 0) {
                    this.sortList(blocksToBreak);
                    this.blockBreaking = this.blocksToBreak.get(0);
                } else {
                    this.blockBreaking = null;
                }
            } else {
                if (blockBreaking != null) {
                    if (mc.playerController.blockHitDelay > 1) {
                        mc.playerController.blockHitDelay = 0;
                    }
                    mc.thePlayer.swingItem();
                    final EnumFacing direction = this.getFacingDirection(this.blockBreaking);
                    if (direction != null) {
                        if (Client.getModuleManager().isEnabled(AutoTool.class))
                            BlockUtil.updateTool(this.blockBreaking);
                        mc.playerController.onPlayerDamageBlock(this.blockBreaking, direction);
                    }
                }
            }
        }
        if (event instanceof EventRender3D) {
            if (this.blocksToBreak.isEmpty())
                return;
            GlStateManager.pushMatrix();
            RenderingUtil.pre3D();

            mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);
            blocksToBreak.forEach(blockPos -> {
                double x = blockPos.getX() - RenderManager.renderPosX;
                double y = blockPos.getY() - RenderManager.renderPosY;
                double z = blockPos.getZ() - RenderManager.renderPosZ;
                if (blockPos.equals(blockBreaking))
                    RenderingUtil.glColor(ColorManager.hudColor.getColorHex());
                else
                    RenderingUtil.glColor(Colors.getColor(255, 70));

                RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1));
            });
            RenderingUtil.post3D();
            GlStateManager.popMatrix();
        }
    }

    private void sortList(List<BlockPos> weed) {
        String current = targetMode.getSelected();
        if (current.equalsIgnoreCase("Range")) {
            weed.sort(Comparator.comparingDouble(o -> mc.thePlayer.getDistance(o.getX() + 0.5, o.getY() + 0.5 + (mc.thePlayer.isSneaking() ? 0 : mc.thePlayer.getEyeHeight()), o.getZ() + 0.5)));
        } else if (current.equalsIgnoreCase("FOV"))
            weed.sort(Comparator.comparingDouble(o -> (Math.abs(RotationUtils.getYawChange(o.getX() + 0.5, o.getZ() + 0.5)))));
        else if (current.equalsIgnoreCase("Angle")) {
            weed.sort(Comparator.comparingDouble(o -> -RotationUtils.getRotationFromPosition(o.getX() + 0.5, o.getY() + 0.5, o.getZ() + 0.5)[0]));
        }
    }

    private boolean blockChecks(final Block block) {
        return blockList.isBlockInList(block);
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
        } else if (!mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.SOUTH;
        } else if (!mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock().isBlockNormalCube()) {
            direction = EnumFacing.NORTH;
        }
        final MovingObjectPosition rayResult = mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        if (rayResult != null && rayResult.getBlockPos().equals(pos)) {
            return rayResult.sideHit;
        }
        return direction;
    }

}
