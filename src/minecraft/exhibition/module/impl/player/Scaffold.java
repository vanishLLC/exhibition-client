/**
 * Time: 12:01:26 AM
 * Date: Jan 2, 2017
 * Creator: cool1
 */
package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Scaffold extends Module {

    public static List<Block> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }

    private static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail,
            Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.mob_spawner,
            Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.web, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice, Blocks.double_plant,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.ender_chest, Blocks.chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.cobblestone_wall, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore,
            Blocks.quartz_ore, Blocks.redstone_ore, Blocks.wall_sign, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate,
            Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.beacon, Blocks.ladder, Blocks.sapling, Blocks.oak_fence,
            Blocks.red_flower, Blocks.yellow_flower, Blocks.flower_pot, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.sand, Blocks.tallgrass, Blocks.tripwire_hook, Blocks.tripwire,
            Blocks.gravel, Blocks.dispenser, Blocks.dropper, Blocks.crafting_table, Blocks.furnace, Blocks.redstone_torch, Blocks.standing_sign, Blocks.pumpkin, Blocks.lit_pumpkin, Blocks.cactus,
            Blocks.stone_slab, Blocks.wooden_slab, Blocks.double_stone_slab, Blocks.double_wooden_slab, Blocks.stone_slab2, Blocks.double_stone_slab2, Blocks.slime_block, Blocks.nether_brick_fence);

    private BlockData blockBelowData;
    private Timer timer = new Timer();
    private Timer towerTimer = new Timer();
    private Timer fastPlaceTimer = new Timer();
    private String TOWER = "TOWER";
    private String MODE = "MODE";
    private String SWING = "SWING";
    private Setting delay = new Setting<>("DELAY", 2, "Block placement delay in ticks.", 1, 1, 10);

    private Setting<Boolean> fastTower = new Setting<>("FAST-TOWER", false, "Gives you a timer boost when towering.");

    private Vector2f lastAngles = new Vector2f(-1337, -1337);
    private float targetYaw;

    private int lastSlot = -1;
    private int lastHeldSlot = -1;

    public Scaffold(ModuleData data) {
        super(data);
        settings.put(TOWER, new Setting<>(TOWER, true, "Helps you build up faster. (DISABLE THIS ON HYPIXEL)"));
        settings.put(MODE, new Setting<>(MODE, new Options("Mode", "Normal", "Normal", "Watchdog", "Old"), "Scaffold method."));
        settings.put(SWING, new Setting<>(SWING, true, "Place blocks without swinging client side."));
        settings.put(delay.getName(), delay);
        addSetting(fastTower);
    }

    @Override
    public void onEnable() {
        Module[] modules = new Module[]{Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(LongJump.class)};
        boolean disabled = false;
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.toggle();
                disabled = true;
            }
        }
        if (disabled) {
            Notifications.getManager().post("Movement Check", "Disabled extra modules.", 1000, Notifications.Type.NOTIFY);
        }

        //Resets timer so player can tower up instantly if needed.
        lastAngles = new Vector2f(-1337, -1337);
        towerTimer.reset();

        if (mc.thePlayer != null) {
            lastHeldSlot = mc.thePlayer.inventory.currentItem;
        }

        if (fastTower.getValue()) {
            mc.timer.timerSpeed = 1F;
        }
    }

    public void onDisable() {
        if (!(mc.thePlayer == null)) {
            if (mc.thePlayer.isSwingInProgress) {
                mc.thePlayer.swingProgress = 0;
                mc.thePlayer.swingProgressInt = 0;
                mc.thePlayer.isSwingInProgress = false;
            }

            if (lastHeldSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastHeldSlot;
                mc.playerController.updateController();
                lastHeldSlot = -1;
            }
        }
        if (fastTower.getValue()) {
            mc.timer.timerSpeed = 1F;
        }
    }


    public float getDirection() {
        float var1 = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0.0F) {
            var1 += 180;
        }

        float forward = 1;

        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5f;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5f;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            var1 -= 90.0F * forward;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            var1 += 90.0F * forward;
        }

        var1 *= 0.017453292F;

        return var1;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    private Timer placeTimer = new Timer();

    public boolean isPlacing() {
        return !placeTimer.delay(200);
    }

    @RegisterEvent(events = {EventTick.class, EventMotionUpdate.class, EventRenderGui.class, EventRender3D.class})
    public void onEvent(Event event) {
        String currentMode = ((Options) settings.get(MODE).getValue()).getSelected();
        if (event instanceof EventRenderGui) {
            //Renders block count. TODO: Make text clearer.
            ScaledResolution res = new ScaledResolution(mc);
            int color = Colors.getColor(255, 0, 0, 255);
            if (getBlockCount() >= 64 && 128 > getBlockCount()) {
                color = Colors.getColor(255, 255, 0, 255);
            } else if (getBlockCount() >= 128) {
                color = Colors.getColor(0, 255, 0, 255);
            }
            GlStateManager.enableBlend();
            Depth.pre();
            Depth.mask();
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F, res.getScaledHeight() / 2F - 25, color);
            Depth.render(GL11.GL_LESS);
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F + 1, res.getScaledHeight() / 2F - 25, Colors.getColor(0, 150));
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F, res.getScaledHeight() / 2F - 25 + 1, Colors.getColor(0, 150));
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F - 1, res.getScaledHeight() / 2F - 25, Colors.getColor(0, 150));
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F, res.getScaledHeight() / 2F - 25 - 1, Colors.getColor(0, 150));
            Depth.post();
            mc.fontRendererObj.drawString(getBlockCount() + "", res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(getBlockCount() + "") / 2F, res.getScaledHeight() / 2F - 25, color);
            GlStateManager.disableBlend();
        }
        if (event instanceof EventRender3D) {
            EventRender3D er = (EventRender3D) event;

            double x = mc.thePlayer.posX;

            double z = mc.thePlayer.posZ;

            double height = (mc.thePlayer.posY - (int) mc.thePlayer.posY);

            double y = mc.thePlayer.posY - (mc.gameSettings.keyBindSneak.getIsKeyPressed() && mc.thePlayer.onGround ? 1.2 : (Client.getModuleManager().isEnabled(Speed.class) && PlayerUtil.isMoving()) ?
                    ((((height < 0.24919 && (height > 0.105 || MathUtils.roundToPlace(height, 4) == 0.0993)) || MathUtils.roundToPlace(height, 4) == 0.0013 ||
                            MathUtils.roundToPlace(height, 4) == 0.0156 || MathUtils.roundToPlace(height, 4) == 0.0479 ||
                            MathUtils.roundToPlace(height, 4) == 0.01553 || MathUtils.roundToPlace(height, 4) == 0.0902) && Math.abs(mc.thePlayer.motionY) < 0.45) ? 1.25 : 1) : 0.8);

            if (!mc.gameSettings.keyBindJump.getIsKeyPressed()) {
                towerTimer.reset();
                if (fastTower.getValue()) {
                    mc.timer.timerSpeed = 1;
                }
            }

            if (mc.thePlayer.motionY <= -0.625) {
                y += mc.thePlayer.motionY + ((mc.thePlayer.motionY - 0.08D) * 0.9800000190734863D);
            }

//                if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
//                    double forward = mc.thePlayer.movementInput.moveForward;
//                    double strafe = mc.thePlayer.movementInput.moveStrafe;
//                    float yaw = mc.thePlayer.rotationYaw;
//
//                    double multiplier = 0;
//                    x += (forward * multiplier * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * multiplier * Math.sin(Math.toRadians(yaw + 90.0f))) * (stepDown ? -0.1 : 1);
//                    z += (forward * multiplier * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * multiplier * Math.cos(Math.toRadians(yaw + 90.0f))) * (stepDown ? -0.1 : 1);
//                }
            BlockPos pos = new BlockPos(x, y, z);

            if (AutoPot.potting || AutoPot.haltTicks > 0)
                return;

            Vec3 vec = new Vec3(pos);
            vec = vec.addVector(0.5, 0.5, 0.5);

            if (vec != null) {
                GL11.glPushMatrix();
                RenderingUtil.pre3D();
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);
                RenderingUtil.glColor(Colors.getColor(255, 75));
                RenderingUtil.drawBoundingBox(new AxisAlignedBB(vec.xCoord - RenderManager.renderPosX - 0.1, vec.yCoord - RenderManager.renderPosY - 0.1, vec.zCoord - RenderManager.renderPosZ - 0.1,
                        vec.xCoord - RenderManager.renderPosX + 0.1, vec.yCoord - RenderManager.renderPosY + 0.1, vec.zCoord - RenderManager.renderPosZ + 0.1));

                int[][] intArrays = {new int[]{0, 0, 0},
                        new int[]{1, 0, 0}, new int[]{0, 0, 1},
                        new int[]{-1, 0, 0}, new int[]{0, 0, -1},
                        new int[]{1, 0, 1}, new int[]{-1, 0, -1},
                        new int[]{1, 0, -1}, new int[]{-1, 0, 1}};

                boolean first = true;

                for (int[] array : intArrays) {
                    BlockPos bruhPos = pos.add(array[0], array[1], array[2]);

                    EnumFacing[] face = EnumFacing.values();
                    for (EnumFacing side : face) {
                        BlockPos blockPos = bruhPos.offset(side);
                        EnumFacing otherSide = side.getOpposite();
                        if (blockPos(blockPos).canCollideCheck(blockState(blockPos), false)) {
                            Vec3 addBruh = getVector(new BlockData(blockPos, otherSide));

                            Vec3 bruh = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).addVector(0.5D, 0.5D, 0.5D).add(new Vec3(otherSide.getDirectionVec().getX() / 2F, otherSide.getDirectionVec().getY() / 2F, otherSide.getDirectionVec().getZ() / 2F));
                            RenderingUtil.glColor(first ? Colors.getColor(0, 255, 255, 75) : Colors.getColor(0, 255, 0, 75));
                            RenderingUtil.drawBoundingBox(new AxisAlignedBB(bruh.xCoord - RenderManager.renderPosX - 0.05, bruh.yCoord - RenderManager.renderPosY - 0.05, bruh.zCoord - RenderManager.renderPosZ - 0.05,
                                    bruh.xCoord - RenderManager.renderPosX + 0.05, bruh.yCoord - RenderManager.renderPosY + 0.05, bruh.zCoord - RenderManager.renderPosZ + 0.05));

                            if (first) {
                                bruh = bruh.add(addBruh);

                                RenderingUtil.glColor(Colors.getColor(255, 255, 0, 75));
                                RenderingUtil.drawBoundingBox(new AxisAlignedBB(bruh.xCoord - RenderManager.renderPosX - 0.05, bruh.yCoord - RenderManager.renderPosY - 0.05, bruh.zCoord - RenderManager.renderPosZ - 0.05,
                                        bruh.xCoord - RenderManager.renderPosX + 0.05, bruh.yCoord - RenderManager.renderPosY + 0.05, bruh.zCoord - RenderManager.renderPosZ + 0.05));
                            }
                            first = false;
                        }
                    }
                }

                GL11.glColor4f(1, 1, 1, 1);
                RenderingUtil.post3D();
                GL11.glPopMatrix();
            }
        }
        if (event instanceof EventMotionUpdate) {
            setSuffix(currentMode);
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (!currentMode.equalsIgnoreCase("Watchdog")) {
                boolean mineplex = currentMode.equalsIgnoreCase("Old");
                if (em.isPre()) {
                    blockBelowData = null;

                    BlockPos blockBelow;

                    //Get block based off of movement

                    double x = mc.thePlayer.posX;
                    double y = mc.thePlayer.posY - 1.0;
                    double z = mc.thePlayer.posZ;

                    //Checks if the block below is a valid block + timer delay
                    if (!mineplex || material(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5D, mc.thePlayer.posZ)) == Material.air)
                        if (!mc.thePlayer.isSneaking() && (mc.theWorld.getBlockState(blockBelow = new BlockPos(x, y, z)).getBlock() == Blocks.air ||
                                mc.theWorld.getBlockState(blockBelow).getBlock() == Blocks.snow_layer ||
                                mc.theWorld.getBlockState(blockBelow).getBlock() == Blocks.tallgrass) && timer.delay(100)) {
                            //Grab the block data for the block below
                            blockBelowData = mineplex ? getBlockData(blockBelow, blacklistedBlocks) : getBlockData(blockBelow);
                            if (blockBelowData != null) {
                                //Face in the center of the block
                                float[] rotations = getRotationsBlock(blockBelowData.position, blockBelowData.face);
                                em.setYaw(rotations[0]);
                                em.setPitch(rotations[1]);
                            }
                        }

                } else if (em.isPost()) {

                    if (!mc.gameSettings.keyBindJump.getIsKeyPressed()) {
                        towerTimer.reset();
                        if (fastTower.getValue()) {
                            mc.timer.timerSpeed = 1;
                        }
                    }

                    //If the player can tower
                    if (blockBelowData != null && (Boolean) settings.get(TOWER).getValue() && getBlockCount() > 0 && mc.gameSettings.keyBindJump.getIsKeyPressed()) {
                        //Reduces player's motion and moves the player upwards.
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionZ = 0;
                        mc.thePlayer.motionY = 0.42F;


                        //After about a second and a half, NCP will want to flag you. This will prevent that.
                        if (towerTimer.delay(1500)) {
                            mc.thePlayer.motionY = -0.28;
                            towerTimer.reset();
                        }
                    }

                    //Loop through the items and find the block to place.
                    for (int i = 36; i < 45; i++) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                            Item item = is.getItem();
                            if (item instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) item).getBlock()) && !((ItemBlock) item).getBlock().getLocalizedName().toLowerCase().contains("chest") && blockBelowData != null) {
                                mc.rightClickDelayTimer = 2;
                                int currentItem = mc.thePlayer.inventory.currentItem;

                                //Swap to block.
                                NetUtil.sendPacket(new C09PacketHeldItemChange(i - 36));
                                mc.thePlayer.inventory.currentItem = i - 36;
                                mc.playerController.updateController();

                                //Caused a null pointer for some reason, will look into soon.
                                try {
                                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockBelowData.position, blockBelowData.face, new Vec3((double) blockBelowData.position.getX() + randomFloat(0) + blockBelowData.face.getDirectionVec().getX() * randomFloat(1), (double) blockBelowData.position.getY() + randomFloat(2) + blockBelowData.face.getDirectionVec().getY() * randomFloat(3), (double) blockBelowData.position.getZ() + randomFloat(4) + blockBelowData.face.getDirectionVec().getZ() * randomFloat(5)))) {
                                        if ((Boolean) settings.get(SWING).getValue()) {
                                            NetUtil.sendPacket(new C0APacketAnimation());
                                        } else {
                                            mc.thePlayer.swingItem();
                                        }
                                        timer.reset();
                                    }
                                } catch (Exception ignored) {

                                }

                                //Reset to current hand.
                                mc.thePlayer.inventory.currentItem = currentItem;
                                mc.playerController.updateController();
                                mc.thePlayer.setSprinting(false);
                                timer.reset();
                                return;
                            }
                        }
                    }
                }
            } else {
                double x = mc.thePlayer.posX;

                double z = mc.thePlayer.posZ;

                boolean stepDown = mc.gameSettings.keyBindSneak.getIsKeyPressed() && mc.thePlayer.onGround;

                double height = (mc.thePlayer.posY - (int) mc.thePlayer.posY);

                double y = mc.thePlayer.posY - (mc.gameSettings.keyBindSneak.getIsKeyPressed() && mc.thePlayer.onGround ? 1.2 : (Client.getModuleManager().isEnabled(Speed.class) && PlayerUtil.isMoving()) ?
                        ((((height < 0.24919 && (height > 0.105 || MathUtils.roundToPlace(height, 4) == 0.0993)) || MathUtils.roundToPlace(height, 4) == 0.0013 ||
                                MathUtils.roundToPlace(height, 4) == 0.0156 || MathUtils.roundToPlace(height, 4) == 0.0479 ||
                                MathUtils.roundToPlace(height, 4) == 0.01553 || MathUtils.roundToPlace(height, 4) == 0.0902) && Math.abs(mc.thePlayer.motionY) < 0.45) ? 1.25 : 1) : 0.8);

                if (!mc.gameSettings.keyBindJump.getIsKeyPressed()) {
                    towerTimer.reset();
                    if (fastTower.getValue()) {
                        mc.timer.timerSpeed = 1;
                    }
                }

                if (mc.thePlayer.motionY <= -0.625) {
                    y += mc.thePlayer.motionY + ((mc.thePlayer.motionY - 0.08D) * 0.9800000190734863D);
                }

//                if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
//                    double forward = mc.thePlayer.movementInput.moveForward;
//                    double strafe = mc.thePlayer.movementInput.moveStrafe;
//                    float yaw = mc.thePlayer.rotationYaw;
//
//                    double multiplier = 0;
//                    x += (forward * multiplier * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * multiplier * Math.sin(Math.toRadians(yaw + 90.0f))) * (stepDown ? -0.1 : 1);
//                    z += (forward * multiplier * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * multiplier * Math.cos(Math.toRadians(yaw + 90.0f))) * (stepDown ? -0.1 : 1);
//                }
                BlockPos pos = new BlockPos(x, y, z);

                if (em.isPre()) {
                    Vec3 vec = getBlockLook(pos);

                    if (lastAngles.x == -1337) {
                        em.setYaw(targetYaw = lastAngles.x = mc.thePlayer.rotationYaw - 180);
                        em.setPitch(lastAngles.y = 90F);
                    }

                    int var1 = getBlockSlot();

                    if (vec != null && material(pos).isReplaceable() && var1 != -1) {
                        //Face in the center of the block
                        float[] rotations = look(vec);
                        targetYaw = rotations[0];
                        em.setPitch(Math.min(rotations[1], mc.gameSettings.keyBindJump.getIsKeyPressed() ? 90 : 70));

                        lastAngles.y = em.getPitch();
                        placeTimer.reset();
                    }

                    if (lastAngles.x != -1337) {
                        float max = 35F + (float) (5F * Math.random());

                        lastAngles.x += MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(targetYaw - lastAngles.x), -max, max);

                        em.setYaw(lastAngles.x);
                        em.setPitch(lastAngles.y);
                    }

                    /**
                     * EEEEEEE
                     * */
                } else {
                    if (AutoPot.potting || AutoPot.haltTicks > 0)
                        return;

                    if (invCheck()) {
                        for (int i = 9; i < 36; i++) {
                            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                                Item item = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem();
                                if (item instanceof ItemBlock && stack.stackSize > 0 && !blacklistedBlocks.contains(((ItemBlock) item).getBlock()) && !((ItemBlock) item).getBlock().getLocalizedName().toLowerCase().contains("chest") && isValid(stack)) {
                                    ItemStack slotSeven = mc.thePlayer.inventoryContainer.getSlot(36 + 7).getStack();
                                    int swapSlot = slotSeven != null && (isValid(slotSeven)) ? 8 : 7;
                                    swap(i, swapSlot);
                                    return;
                                }
                            }
                        }
                    }

                    if (mc.gameSettings.keyBindJump.getIsKeyPressed() && getBlockCount() > 0) {
                        if ((Boolean) settings.get(TOWER).getValue()) {
                            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                        }
                    } else {
                        towerTimer.reset();
                    }

                    if (!material(pos).isReplaceable())
                        return;

                    int var1 = getBlockSlot();

                    int delay = ((Number) this.delay.getValue()).intValue();

                    lastAngles.x = em.getYaw();

                    if (var1 == -1 || !timer.roundDelay(delay * 50))
                        return;

                    boolean isJumping = mc.gameSettings.keyBindJump.getIsKeyPressed() || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
                    if (isJumping && getBlockCount() > 0) {
                        if ((!PlayerUtil.isMoving() || (Boolean) settings.get(TOWER).getValue())) {
                            if (fastTower.getValue()) {
                                if (fastPlaceTimer.delay(600)) {
                                    mc.timer.timerSpeed = 2.23F + (float) (0.04F * Math.random());
                                    fastPlaceTimer.reset();
                                }
                                if (fastPlaceTimer.delay(500)) {
                                    mc.timer.timerSpeed = 1;
                                    fastPlaceTimer.reset();
                                }
                            }

                            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                            boolean forceDown = true;
                            if (mc.thePlayer.posY != pos.getY() + 1 && material(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.2, mc.thePlayer.posZ)) != Material.air) {
                                double jump = 0.42F;
                                if (mc.thePlayer.isPotionActive(Potion.jump)) {
                                    jump += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                                }
                                if (jump == (double) 0.42F || mc.thePlayer.motionY <= 0) {
                                    em.setY(mc.thePlayer.motionY = jump);
                                }
                                if (jump != (double) 0.42F) {
                                    forceDown = false;
                                }

                                if ((Boolean) settings.get(TOWER).getValue()) {
                                    mc.thePlayer.motionX = 0;
                                    mc.thePlayer.motionZ = 0;
                                }

                                if (forceDown && towerTimer.delay(1500)) {
                                    mc.thePlayer.motionY = -0.28;
                                    towerTimer.reset();
                                }
                            }
                        } else {
                            if (fastTower.getValue()) {
                                mc.timer.timerSpeed = 1F;
                            }
                            towerTimer.reset();
                        }
                    } else {
                        if (fastTower.getValue()) {
                            mc.timer.timerSpeed = 1F;
                        }
                        towerTimer.reset();
                    }

                    mc.thePlayer.inventory.currentItem = var1;
                    mc.playerController.updateController();

                    if (mc.thePlayer.inventory.getStackInSlot(var1).getItem() instanceof ItemBlock
                            && !blacklistedBlocks.contains(((ItemBlock) mc.thePlayer.inventory.getStackInSlot(var1).getItem()).getBlock()))
                        if (!place(pos))
                            if (!place(pos.add(1, 0, 0)))
                                if (!place(pos.add(0, 0, 1)))
                                    if (!place(pos.add(-1, 0, 0)))
                                        if (!place(pos.add(0, 0, -1)))
                                            if (!place(pos.add(-1, 0, -1)))
                                                if (!place(pos.add(1, 0, -1)))
                                                    if (!place(pos.add(-1, 0, 1)))
                                                        if (!place(pos.add(1, 0, 1))) ;
                }
            }
        }
    }

    public float[] look(Vec3 vector) {
        double diffX = vector.xCoord - mc.thePlayer.posX;
        double diffY = vector.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double diffZ = vector.zCoord - mc.thePlayer.posZ;
        double distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distance));

        float max = 35F + (float) (10F * Math.random());

        return new float[]{lastAngles.x + (yaw - lastAngles.x), lastAngles.y + MathHelper.wrapAngleTo180_float(pitch - lastAngles.y)};
    }

    private Vec3 checkPos(BlockPos pos) {
        EnumFacing[] face = EnumFacing.values();
        for (EnumFacing side : face) {
            BlockPos blockPos = pos.offset(side);
            EnumFacing otherSide = side.getOpposite();
            if (blockPos(blockPos).canCollideCheck(blockState(blockPos), false)) {
                return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).addVector(0.5, 0.5, 0.5);
            }
        }
        return null;
    }

    private boolean place(BlockPos pos) {
        EnumFacing[] face;
        int j = (face = EnumFacing.values()).length;
        for (int i = 0; i < j; i++) {
            EnumFacing side = face[i];
            BlockPos blockPos = pos.offset(side);
            EnumFacing otherSide = side.getOpposite();
            if (blockPos(blockPos).canCollideCheck(blockState(blockPos), false)) {
                Vec3 bruh = getVector(new BlockData(blockPos, otherSide));
                Vec3 vec = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).addVector(0.5, 0.5, 0.5).add(new Vec3(otherSide.getDirectionVec().getX() / 2F, otherSide.getDirectionVec().getY() / 2F, otherSide.getDirectionVec().getZ() / 2F));

                IBlockState var11 = mc.theWorld.getBlockState(blockPos);
                boolean needToSneak = false;
                //Vec3 p = getVector(new BlockData(blockPos, otherSide));
                if ((!mc.thePlayer.isSneaking() || mc.thePlayer.getHeldItem() == null) && var11.getBlock().onBlockActivated(mc.theWorld, blockPos, var11, mc.thePlayer, otherSide, 0.5F, 0.5F, 0.5F)) {
                    needToSneak = true;
                }

                if (needToSneak) {
                    NetUtil.sendPacketNoEvents(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                    mc.thePlayer.movementInput.sneak = true;
                }

                if(mc.thePlayer.isSprinting()) {
                    NetUtil.sendPacketNoEvents(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), blockPos, otherSide, vec)) {
                    if ((Boolean) settings.get(SWING).getValue()) {
                        NetUtil.sendPacket(new C0APacketAnimation());
                    } else {
                        mc.thePlayer.swingItem();
                    }
                    timer.reset();

                    if (mc.thePlayer.getCurrentEquippedItem() != null) {
                        if (mc.thePlayer.getCurrentEquippedItem().stackSize == 0) {
                            mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                        }
                    }

                }

                if(mc.thePlayer.isSprinting()) {
                    NetUtil.sendPacketNoEvents(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                }

                if (needToSneak) {
                    NetUtil.sendPacketNoEvents(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                    mc.thePlayer.movementInput.sneak = false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(ItemStack item) {
        if (isEmpty(item))
            return false;
        if (item.getUnlocalizedName().equalsIgnoreCase("tile.cactus"))
            return false;
        if (!(item.getItem() instanceof ItemBlock))
            return false;
        if (blacklistedBlocks.contains(((ItemBlock) item.getItem()).getBlock()))
            return false;
        ItemBlock itemBlock = (ItemBlock) item.getItem();

        double min = itemBlock.getBlock().getBlockBoundsMinX() + itemBlock.getBlock().getBlockBoundsMinY() + itemBlock.getBlock().getBlockBoundsMinZ();
        double max = itemBlock.getBlock().getBlockBoundsMaxX() + itemBlock.getBlock().getBlockBoundsMaxY() + itemBlock.getBlock().getBlockBoundsMaxZ();

        return min == 0 && max == 3;
    }

    public IBlockState blockState(BlockPos pos) {
        return mc.theWorld.getBlockState(pos);
    }

    public Block blockPos(BlockPos pos) {
        return blockState(pos).getBlock();
    }

    public Material material(BlockPos pos) {
        return blockPos(pos).getMaterial();
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack == null;
    }

    private Vec3 getVector(BlockData blockData) {
        int places = 13;
        double randX = MathUtils.roundToPlace(randomFloat(256), places), randY = MathUtils.roundToPlace(randomFloat(32), places), randZ = MathUtils.roundToPlace(randomFloat(55), places);
        double vx = -0.5;
        double vy = -0.5;
        double vz = -0.5;
        switch (blockData.face) {
            case UP:
                vx += randX;
                vz += randZ;
                vy += 0.5;
                break;
            case DOWN:
                vx += randX;
                vz += randZ;
                break;
            case SOUTH:
            case NORTH:
                vx += randX;
                vy += randY;
                vz += 0.5;
                break;
            case EAST:
            case WEST:
                vy += randY;
                vz += randZ;
                vx += 0.5;
                break;
        }

        Block block = mc.theWorld.getBlock(blockData.position.getX(), blockData.position.getY(), blockData.position.getZ());

        double minX = block.getBlockBoundsMinX();
        double minY = block.getBlockBoundsMinY();
        double minZ = block.getBlockBoundsMinZ();

        double maxX = block.getBlockBoundsMaxX();
        double maxY = block.getBlockBoundsMaxY();
        double maxZ = block.getBlockBoundsMaxZ();

        double scaledX = minX + (maxX - minX) * vx;
        double scaledY = minY + (maxY - minY) * vy;
        double scaledZ = minZ + (maxZ - minZ) * vz;

        return new Vec3(scaledX, scaledY, scaledZ);
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    private boolean invCheck() {
        int stacks = 0;
        for (int i = 36; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (isValid(item)) {
                    stacks++;
                    if (stacks > 1)
                        return false;
                }
            }
        }
        return true;
    }

    private BlockData getBlockData(BlockPos pos, List list) {
        return !list.contains(mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock())
                ? new BlockData(pos.add(0, -1, 0), EnumFacing.UP)
                : (!list.contains(mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock())
                ? new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST)
                : (!list.contains(mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock())
                ? new BlockData(pos.add(1, 0, 0), EnumFacing.WEST)
                : (!list.contains(mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock())
                ? new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH)
                : (!list.contains(mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock())
                ? new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH) : null))));
    }

    private Vec3 getBlockLook(BlockPos pos) {
        Vec3 output;
        if ((output = checkPos(pos)) == null)
            if ((output = checkPos(pos.add(1, 0, 0))) == null)
                if ((output = checkPos(pos.add(0, 0, 1))) == null)
                    if ((output = checkPos(pos.add(-1, 0, 0))) == null)
                        if ((output = checkPos(pos.add(0, 0, -1))) == null)
                            if ((output = checkPos(pos.add(1, 0, 1))) == null)
                                if ((output = checkPos(pos.add(-1, 0, -1))) == null)
                                    if ((output = checkPos(pos.add(1, 0, -1))) == null)
                                        if ((output = checkPos(pos.add(-1, 0, 1))) == null) ;
        return output;
    }

    private BlockData getBlockData(BlockPos pos) {
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock())) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock())) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock())) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add = pos.add(-1, 0, 0);
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add.add(1, 0, 0)).getBlock())) {
            return new BlockData(add.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add.add(0, 0, -1)).getBlock())) {
            return new BlockData(add.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add.add(0, 0, 1)).getBlock())) {
            return new BlockData(add.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add2 = pos.add(1, 0, 0);
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add2.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add2.add(1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add2.add(0, 0, -1)).getBlock())) {
            return new BlockData(add2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add2.add(0, 0, 1)).getBlock())) {
            return new BlockData(add2.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add3 = pos.add(0, 0, -1);
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add3.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add3.add(1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add3.add(0, 0, -1)).getBlock())) {
            return new BlockData(add3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add3.add(0, 0, 1)).getBlock())) {
            return new BlockData(add3.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add4 = pos.add(0, 0, 1);
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add4.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add4.add(1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add4.add(0, 0, -1)).getBlock())) {
            return new BlockData(add4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (!blacklistedBlocks.contains(mc.theWorld.getBlockState(add4.add(0, 0, 1)).getBlock())) {
            return new BlockData(add4.add(0, 0, 1), EnumFacing.NORTH);
        }
        return null;
    }

    public static float[] getRotationsBlock(BlockPos pos, EnumFacing facing) {
        double offsetX = 0;
        double offsetY = 0;
        double offsetZ = 0;
        switch (facing) {
            case NORTH: {
                offsetX = 0.5;
                offsetZ = 0;
                offsetY = 0.9;
                break;
            }
            case EAST: {
                offsetX = 1;
                offsetZ = 0.5;
                offsetY = 0.9;
                break;
            }
            case SOUTH: {
                offsetX = 0.5;
                offsetZ = 1;
                offsetY = 0.9;
                break;
            }
            case WEST: {
                offsetX = 0;
                offsetZ = 0.5;
                offsetY = 0.9;
                break;
            }
            case UP: {
                offsetX = 0.5;
                offsetY = 1;
                offsetZ = 0.5;
                break;
            }
        }

        double d0 = pos.getX() + offsetX - mc.thePlayer.posX;
        double d1 = pos.getY() + offsetY - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
        double d2 = pos.getZ() + offsetZ - mc.thePlayer.posZ;
        double d3 = (double) MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        float f = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
        return new float[]{f, f1};
    }

    public static float randomFloat(long seed) {
        return 0.25000000000f + (new Random(System.currentTimeMillis() + seed).nextInt(50000000) / 100000000.000000000000f) + 0.00000001458745f;
    }

    private int getBlockSlot() {
        for (int i = 36; i < 45; ++i) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                ItemBlock item = (ItemBlock) itemStack.getItem();
                if (blacklistedBlocks.stream().anyMatch(e -> e.equals(item.getBlock())) || !isValid(itemStack)) {
                    continue;
                }
                return i - 36;
            }
        }
        return -1;
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemBlock && is.stackSize > 0) {
                    ItemBlock item = (ItemBlock) is.getItem();
                    if (!blacklistedBlocks.stream().anyMatch(e -> e.equals(item.getBlock())) || isValid(is)) {
                        blockCount += is.stackSize;
                    }
                }
            }
        }
        return blockCount;
    }

    private class BlockData {

        public BlockPos position;
        public EnumFacing face;

        private BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
        }

    }

    /*
    * if (em.isPre()) {
                    int tempSlot = getBlockSlot();
                    blockBelowData = null;
                    slot = -1;
                    //if(resprint) {
                    //    resprint = false;
                    //    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                    //    DevNotifications.getManager().post("\247c[Scaffold]:\247f 1");
                    //}
                    if (!mc.thePlayer.isSneaking()) {
        double x = mc.thePlayer.posX, y = mc.thePlayer.posY - 1, z = mc.thePlayer.posZ;
        BlockPos blockBelow1 = new BlockPos(x, y, z);
        slot = tempSlot;
        if ((mc.theWorld.getBlockState(blockBelow1).getBlock() == Blocks.air || mc.theWorld.getBlockState(blockBelow1).getBlock() == Blocks.snow_layer || mc.theWorld.getBlockState(blockBelow1).getBlock() == Blocks.tallgrass))
            blockBelowData = getBlockData(blockBelow1, blacklistedBlocks);
        if(blockBelowData != null){
            // double xPos = blockBelowData.position.getX() + blockBelowData.face.getFrontOffsetX(), yPos = blockBelowData.position.getY() + blockBelowData.face.getFrontOffsetY(), zPos = blockBelowData.position.getZ() + blockBelowData.face.getFrontOffsetZ();
            float[] rot = getRotationsBlock(blockBelowData.position, blockBelowData.face);
            em.setYaw(rot[0] + randomNumber(-3,3));
            em.setPitch(rot[1] + randomNumber(-3,3));
            //mc.thePlayer.rotationYaw = em.getYaw();
            //mc.thePlayer.rotationPitch = em.getPitch();
        }

    }
} else if (em.isPost()) {
        //if (mc.thePlayer.isSprinting()) mc.thePlayer.setSprinting(false);
        if (mc.gameSettings.keyBindJump.getIsKeyPressed() && (Boolean) settings.get(TOWER).getValue()) {
        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
        mc.thePlayer.motionY = 0.40;
        if (towerTimer.delay(1500)) {
        mc.thePlayer.motionY = -0.28;
        towerTimer.reset();
        }
        } else {
        towerTimer.reset();
        }

        if (blockBelowData == null || slot == -1
        || !mc.thePlayer.worldObj.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX, -1, mc.thePlayer.motionZ)).isEmpty()) {
        return;
        }
        towerTimer.reset();
        timer.reset();

        boolean dohax = mc.thePlayer.inventory.currentItem != slot;
        int oldSlot = mc.thePlayer.inventory.currentItem;
        if (dohax)
        NetUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = slot));
        int places = 13;
        double rand1 = MathUtils.roundToPlace(randomFloat(1), places), rand2 = MathUtils.roundToPlace(randomFloat(20), places), rand3 = MathUtils.roundToPlace(randomFloat(55), places);
        double vx = blockBelowData.position.getX() + blockBelowData.face.getDirectionVec().getX();
        double vy = blockBelowData.position.getY() + blockBelowData.face.getDirectionVec().getY();
        double vz = blockBelowData.position.getZ() + blockBelowData.face.getDirectionVec().getZ();
        switch (blockBelowData.face) {
        case UP:
        vx += rand1;
        vz += rand3;
        break;
        case NORTH:
        vx += rand1;
        vy += rand2;
        break;
        case EAST:
        vy += rand2;
        vz += rand3;
        break;
        case SOUTH:
        vx += rand1;
        vy += rand2;
        break;
        case WEST:
        vy += rand2;
        vz += rand3;
        break;
        }
        Vec3 vec = new Vec3(vx, vy, vz);
        Vec3 stupidShit = new Vec3((double) blockBelowData.position.getX() + randomFloat(0) + blockBelowData.face.getDirectionVec().getX() * randomFloat(1), (double) blockBelowData.position.getY() + randomFloat(2) + blockBelowData.face.getDirectionVec().getY() * randomFloat(3), (double) blockBelowData.position.getZ() + randomFloat(4) + blockBelowData.face.getDirectionVec().getZ() * randomFloat(5));
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockBelowData.position, blockBelowData.face, vec)) {

        if ((Boolean) settings.get(SWING).getValue()) {
        NetUtil.sendPacket(new C0APacketAnimation());
        } else {
        mc.thePlayer.swingItem();
        }
//                        resprint = true;
//                        NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
//                        DevNotifications.getManager().post("\247c[Scaffold]:\247f 2");
//
 //                       mc.thePlayer.motionX *= 0.15;
 //                       mc.thePlayer.motionZ *= 0.15;
        }
        if (dohax)
        NetUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = oldSlot));
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
        }
    */


}
