/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventSpawnEntity;
import exhibition.management.PriorityManager;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.misc.ChatUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.stream.Collectors;

public class HackerDetect extends Module {

    private Setting<Boolean> killaura = new Setting<>("AURA", false);
    private Setting<Boolean> autoBlock = new Setting<>("AUTOBLOCK", true);
    private Setting<Boolean> speed = new Setting<>("BHOP", true);
    private Setting<Boolean> cleaner = new Setting<>("CLEANER", false);
    private Setting<Boolean> fastfly = new Setting<>("BLINK-FLY", false);
    private Setting<Boolean> scaffold = new Setting<>("FLY/SCAFFOLD", true);
    private Setting<Boolean> phase = new Setting<>("CAGE PHASE", true);


    private MultiBool checks = new MultiBool("Checks", killaura, autoBlock, cleaner, fastfly, scaffold, phase);

    private Vec3 teleported = null;

    double phasePosY;

    public HackerDetect(ModuleData data) {
        super(data);
        settings.put("REPORT", new Setting("REPORT", false, "Automatically report players who are suspicious."));
        settings.put("CHECKS", new Setting("CHECKS", checks, "Which checks HackerDetect should use."));
    }

    private double defaultSpeed(EntityPlayer ent) {
        double baseSpeed = 0.27999999999999997D;
        if (ent.isPotionActive(Potion.moveSpeed)) {
            int amplifier = ent.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventSpawnEntity.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook posLook = (S08PacketPlayerPosLook) packet;
                /*
                BUM CHECK DOESNT WORK AND FLAGS EVERYONE FOR PHASING
                 */
//                boolean isOverGlass = false;
//
//                for (int i = 0; i < 4; i++) {
//                    IBlockState bruh = mc.theWorld.getBlockState(new BlockPos(posLook.getX(), (int) posLook.getY() - (i + 1), posLook.getZ()));
//
//                    if(bruh.getBlock().getMaterial() == Material.glass || bruh.getBlock() == Blocks.stained_glass || bruh.getBlock() == Blocks.glass) {
//                        isOverGlass = true;
//                        break;
//                    }
//                }
                    if (mc.thePlayer.ticksExisted <= 5 && HypixelUtil.isGameStarting()) {
                        teleported = new Vec3(posLook.getX(), posLook.getY(), posLook.getZ());
                    }
            }
            return;
        }

        if (event instanceof EventSpawnEntity) {
            if (cleaner.getValue()) {
                EventSpawnEntity esp = event.cast();
                Entity entity = esp.getEntity();
                if (entity instanceof EntityItem) {
                    for (Entity ent : mc.theWorld.getLoadedEntityList()) {
                        if (ent == mc.thePlayer)
                            continue;

                        if (ent instanceof EntityPlayer) {
                            if (ent.getDistanceToEntity(entity) < 2 && !FriendManager.isFriend(ent.getName())) {
                                EntityPlayer player = (EntityPlayer) ent;

                                if (PriorityManager.isPriority(player))
                                    continue;

                                double motionX = Math.abs(player.posX - player.lastTickPosX);
                                double motionZ = Math.abs(player.posZ - player.lastTickPosZ);

                                boolean above = entity.posY > player.posY + 1;

                                double velocity = MathUtils.roundToPlace(Math.sqrt(Math.pow(motionX, 2) + Math.pow(motionZ, 2)), 3);

                                boolean isValidItem = (player.getHeldItem() == null || player.getHeldItem().getItem() == null) || !(player.getHeldItem().getItem() instanceof ItemFishingRod);

                                if (above && velocity >= 0.2 && (player.lastDroppedTick == -1 || (player.ticksExisted - player.lastDroppedTick) < 10) && player.lastDroppedTick != player.ticksExisted && isValidItem) {
                                    player.invWalkTicks++;
                                    if (player.invWalkTicks > 10) {
                                        Notifications.getManager().post("Hacker Detected", player.getName() + " may be using InventoryCleaner.", 7500, Notifications.Type.WARNING);
                                        if ((boolean) settings.get("REPORT").getValue())
                                            ChatUtil.sendChat("/wdr " + ent.getName() + " fly");
                                        PriorityManager.setAsPriority(player);
                                    }
                                    player.lastDroppedTick = player.ticksExisted;
                                }
                            }
                        }
                    }
                }
                return;
            }
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (!em.isPre())
                return;

            if (teleported != null && (HypixelUtil.isInGame("SKYWARS") && HypixelUtil.isGameActive())) {
                teleported = null;
            }

            List<Entity> validPlayers = mc.theWorld.getLoadedEntityList().stream().filter(o -> o instanceof EntityPlayer && o != mc.thePlayer && !AntiBot.isBot(o)).collect(Collectors.toList());
            for (Entity entityPlayer : validPlayers) {
                EntityPlayer ent = (EntityPlayer) entityPlayer;
                if (ent.isInvisible() || FriendManager.isFriend(ent.getName()))
                    continue;

                {
                    if ((ent.ticksExisted - ent.lastDroppedTick) > 40) {
                        ent.lastDroppedTick = -1;
                    }
                }


                //
                if (autoBlock.getValue() && !ent.isRiding()) {
                    double motionX = Math.abs(ent.posX - ent.lastTickPosX);
                    double motionZ = Math.abs(ent.posZ - ent.lastTickPosZ);

                    double speed = MathUtils.roundToPlace(Math.hypot(motionX, motionZ), 2);

                    boolean isOverGround = mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 0.5, ent.posZ)).getBlock().getMaterial() != Material.air ||
                            mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 1, ent.posZ)).getBlock().getMaterial() != Material.air ||
                            mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 1.5, ent.posZ)).getBlock().getMaterial() != Material.air;

                    if (isOverGround && (ent.isBlocking() || (ent.isUsingItem() && ent.getItemInUseDuration() > 5) && ent.isSprinting()) && speed > 0.4 && speed < 7 && !PriorityManager.isPriority(ent)) {
                        //ChatUtil.debug(ent.getName() + " is moving fast " + ent.isSprinting() + " " + Math.hypot(motionX, motionZ));
                        //ChatUtil.debug(ent.isBlocking() + " " + ent.getItemInUseDuration());
                        ent.speedFlags += speed > 0.45 ? (speed / 0.4 * 3) : 1;
                        if (ent.speedFlags > 25) {
                            Notifications.getManager().post("Hacker Detected", ent.getName() + " is using " + (ent.isBlocking() ? "AutoBlock!" : "NoSlowdown!"), 7500, Notifications.Type.WARNING);
                            if ((boolean) settings.get("REPORT").getValue())
                                ChatUtil.sendChat("/wdr " + ent.getName() + " fly");
                            PriorityManager.setAsPriority(ent);
                        }
                    }

//                if(ent.isBlocking() && ent.isSwingInProgress) {
//
//                    int lastSwingDelta = mc.thePlayer.ticksExisted - ent.lastBlockSwingTick;
//
//                    if(lastSwingDelta > 11) {
//                        ent.lastBlockSwingTick = mc.thePlayer.ticksExisted;
//                    }
//
//                    if(lastSwingDelta > 5) {
//
//                    }
//
//                    ent.lastBlockSwingTick = mc.thePlayer.ticksExisted;
//
//                    ChatUtil.debug(mc.thePlayer.ticksExisted + " " + ent.getName() + " is blocking and swinging " + ent.swingProgressInt + " " + ent.prevSwingProgress + " " + ent.swingProgress);
//                    if(ent.swingProgressInt == -1 && ) {
//                    }
//
//                }
                }

                // Cage Phase detection
                if (phase.getValue()) {
                    if ((HypixelUtil.scoreboardContains("start 0:09") && HypixelUtil.isInGame("SKYWARS") && HypixelUtil.scoreboardContains("teams left"))){
                        phasePosY = mc.thePlayer.posY;
                    }
                    if (!PriorityManager.isPriority(ent) && (HypixelUtil.scoreboardContains("start") && HypixelUtil.isInGame("SKYWARS") && HypixelUtil.scoreboardContains("teams left"))){
                        if (phasePosY - ent.posY > 4.5) {
                            Notifications.getManager().post("Hacker Detected", ent.getName() + " has phased out of their cage!", 7500, Notifications.Type.WARNING);
                            PriorityManager.setAsPriority(ent);
                        }
                    }

                    if (!PriorityManager.isPriority(ent) && ent.ticksExisted > 40 && teleported != null && HypixelUtil.isInGame("SKYWARS") && !HypixelUtil.isGameActive() && HypixelUtil.isGameStarting()) {
                        if (teleported.yCoord - ent.posY > 4.5) {
                            Notifications.getManager().post("Hacker Detected", ent.getName() + " has phased out of their cage!", 7500, Notifications.Type.WARNING);
                            PriorityManager.setAsPriority(ent);
                        }
                    }
                }

                // Blink fly detection
                if (fastfly.getValue()) {
                    if (ent.hurtTime == 9 && ent.getDistance(ent.lastTickPosX, ent.posY, ent.lastTickPosZ) < 1) {
                        ent.lastMovedTick = ent.ticksExisted;
                    }

                    if (ent.lastMovedTick != -1 && (ent.ticksExisted - ent.lastMovedTick) > 5 && !PriorityManager.isPriority(ent) && ent.hurtTime <= 1) {
                        if (ent.getDistance(ent.lastTickPosX, ent.posY, ent.lastTickPosZ) > 5 && ent.getDistance(ent.posX, ent.lastTickPosY, ent.posZ) < 2) {
                            if ((ent.ticksExisted - ent.lastMovedTick) > 20) {
                                Notifications.getManager().post("Hacker Detected", ent.getName() + " has irregular movements (Blink Fly).", 7500, Notifications.Type.WARNING);
                                if ((boolean) settings.get("REPORT").getValue())
                                    ChatUtil.sendChat("/wdr " + ent.getName() + " fly");
                                PriorityManager.setAsPriority(ent);

                            }
                        }
                    }
                }

                // Fly Detection
                if (scaffold.getValue()) {
                    double motionX = Math.abs(ent.posX - ent.lastTickPosX);
                    double motionZ = Math.abs(ent.posZ - ent.lastTickPosZ);
                    double motionY = Math.abs(ent.posY - ent.lastTickPosY);

                    if ((Math.sqrt(motionX * motionX + motionZ * motionZ) > 0.23) && motionY <= 0.005 && !ent.isInvisible() && !ent.isRiding() && !ent.isSneaking())
                        if (mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 0.45, ent.posZ)).getBlock() == Blocks.air && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 1.35, ent.posZ)).getBlock() == Blocks.air) {
                            ent.flags += ent.onGround ? 4 : 1;
                            if (Math.sqrt(motionX * motionX + motionZ * motionZ) > defaultSpeed(ent))
                                ent.flags += Math.pow(Math.sqrt(motionX * motionX + motionZ * motionZ), 2);
                            if (ent.flags >= 35 && !PriorityManager.isPriority(ent)) {
                                Notifications.getManager().post("Hacker Detected", ent.getName() + " has irregular movements (Fly/Scaffold).", 7500, Notifications.Type.WARNING);
                                if ((boolean) settings.get("REPORT").getValue())
                                    ChatUtil.sendChat("/wdr " + ent.getName() + " fly");
                                PriorityManager.setAsPriority(ent);
                            }
                            //DevNotifications.getManager().post(ent.getName() + " is flying?");
                        }

                    if (!PriorityManager.isPriority(ent) && ent.flags != 0 && ent.flags < 10 && ent.lastTickPosY > ent.posY && ent.onGround && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 0.5, ent.posZ)).getBlock() != Blocks.air) {
                        ent.flags = 0;
                        //DevNotifications.getManager().post(ent.getName() + " reset flags!");
                    }
                }

                // Speed Detection
                if (speed.getValue() && !ent.isRiding()) {
                    double motionX = ent.posX - ent.lastTickPosX;
                    double motionZ = ent.posZ - ent.lastTickPosZ;

                    double motionY = ent.posY - ent.lastTickPosY;

                    double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);

                    boolean jumped = motionY > 0.1 && motionY < 0.5 && ent.lastMotionY < 0.1;

                    int tickInAir = ent.onGround ? 0 : ent.ticksExisted - ent.jumpedTick;

                    if (jumped) {
                        ent.jumpedTick = ent.ticksExisted;

                        float yawDirection;

                        if ((motionZ < 0.0D) && (motionX < 0.0D)) {
                            yawDirection = 90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else if ((motionZ < 0.0D) && (motionX > 0.0D)) {
                            yawDirection = -90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else {
                            yawDirection = (float) Math.toDegrees(-Math.atan(motionX / motionZ));
                        }

                        if (Float.isNaN(yawDirection)) {
                            yawDirection = ent.rotationYaw;
                        }

                        ent.jumpedYaw = yawDirection;
                        ent.lastAirYaw = yawDirection;

                        float diff = Math.abs(MathHelper.wrapAngleTo180_float(-(MathHelper.wrapAngleTo180_float(ent.rotationYaw) - (float) MathHelper.wrapAngleTo180_float(yawDirection))));

                        if (MathUtils.roundToPlace(velocity, 1) >= 0.5 && diff > 46) {
                            ent.speedFlags += diff > 120 ? 15 : diff > 90 ? 7 : 3;
                            //ChatUtil.debug("Yaw Diff: " + diff + " " + ent.getName() + " " + velocity);
                        }
                    }

                    if (tickInAir > 0 && tickInAir < 10) {
                        float yawDirection;

                        if ((motionZ < 0.0D) && (motionX < 0.0D)) {
                            yawDirection = 90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else if ((motionZ < 0.0D) && (motionX > 0.0D)) {
                            yawDirection = -90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else {
                            yawDirection = (float) Math.toDegrees(-Math.atan(motionX / motionZ));
                        }

                        if (Float.isNaN(yawDirection)) {
                            yawDirection = ent.rotationYaw;
                        }

                        float diff = Math.abs(MathHelper.wrapAngleTo180_float(-(MathHelper.wrapAngleTo180_float(ent.lastAirYaw) - (float) MathHelper.wrapAngleTo180_float(yawDirection))));

                        if (diff > 60 && MathUtils.roundToPlace(velocity, 1) >= 0.36 && velocity < 2) {
                            ent.speedFlags += diff > 100 ? 7 : diff > 60 ? 5 : 2;
                            ent.lastFlaggedTick = ent.ticksExisted;
                            ent.lastAirYaw = yawDirection;
                            //ChatUtil.debug("Yaw Diff Air " + tickInAir + ": " + diff + " " + ent.getName() + " " + velocity);
                        }
                    }

                    if (tickInAir > 0 && !jumped && motionY > -0.3 && motionY < 0 && (ent.ticksExisted - ent.jumpedTick) <= 2 && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY + 2, ent.posZ)).getBlock() == Blocks.air) {
                        ent.speedFlags += 5;
                        ent.lowhopFlags++;
                        if (ent.ticksExisted - ent.lastFlaggedTick < 15 && ent.lowhopFlags > 2 && !PriorityManager.isPriority(ent) && velocity > 0.2) {
                            Notifications.getManager().post("Hacker Detected", ent.getName() + " is using LowHop.", 7500, Notifications.Type.WARNING);
                            if ((boolean) settings.get("REPORT").getValue())
                                ChatUtil.sendChat("/wdr " + ent.getName() + " killaura fly speed scaffold");
                            PriorityManager.setAsPriority(ent);
                        }

                        ent.lastFlaggedTick = ent.ticksExisted;
                    }

//                    if((ent.lastMotionY < 0 || ent.lastMotionY == 0) && (motionY > 0.2 && motionY < 0.47) && ent.onGround && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - motionY - 0.5, ent.posZ)).getBlock() != Blocks.air) {
//                        ChatUtil.debug("Hmm? " + ent.getName() + " " + ent.ticksExisted + " " + motionY + " " + ent.posY);
//                    }
//
//                    if(motionY < -0.55 && ent.lastMotionY < 0 && ent.lastMotionY > motionY && ent.onGround && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - 1 + motionY, ent.posZ)).getBlock() == Blocks.air) {
//                        ChatUtil.debug("\247cNoFall? " + ent.getName() + " " + ent.ticksExisted + " " + motionY + " " + ent.posY);
//                    }

                    ent.lastMotionY = motionY;

                    if (ent.onGround && (ent.ticksExisted - ent.jumpedTick) > 20 && !PriorityManager.isPriority(ent)) {
                        if (ent.speedFlags > 0)
                            ent.speedFlags -= 5;
                        if (ent.lowhopFlags > 0)
                            ent.lowhopFlags--;
                    }

                    if (ent.speedFlags >= 25 && (ent.ticksExisted - ent.lastFlaggedTick) < 5 && !PriorityManager.isPriority(ent)) {
                        Notifications.getManager().post("Hacker Detected", ent.getName() + " is using Speed.", 7500, Notifications.Type.WARNING);
                        if ((boolean) settings.get("REPORT").getValue())
                            ChatUtil.sendChat("/wdr " + ent.getName() + " killaura fly speed scaffold");
                        PriorityManager.setAsPriority(ent);
                    }

                }

                // Irregular Movement Detection
                if (killaura.getValue()) {
                    double motionX = ent.posX - ent.lastTickPosX;
                    double motionZ = ent.posZ - ent.lastTickPosZ;
                    double motionY = ent.posY - ent.lastTickPosY;
                    boolean flagged = false;
                    if (Math.sqrt(motionX * motionX + motionZ * motionZ) > (defaultSpeed(ent) * 1.5) && motionY <= 0) { // Teleport
                        float yawDirection;

                        if ((motionZ < 0.0D) && (motionX < 0.0D)) {
                            yawDirection = 90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else if ((motionZ < 0.0D) && (motionX > 0.0D)) {
                            yawDirection = -90.0F + (float) Math.toDegrees(Math.atan(motionZ / motionX));
                        } else {
                            yawDirection = (float) Math.toDegrees(-Math.atan(motionX / motionZ));
                        }

                        if (Float.isNaN(yawDirection)) {
                            yawDirection = ent.rotationYaw;
                        }

                        float diff = MathHelper.wrapAngleTo180_float(-(MathHelper.wrapAngleTo180_float(ent.rotationYaw) - (float) MathHelper.wrapAngleTo180_float(yawDirection)));

                        if (Math.abs(diff) > 85) {
                            ent.flags++;
                            flagged = true;
                        }
                    }


                    if (ent.flags >= 30 && !PriorityManager.isPriority(ent) && flagged) {
                        Notifications.getManager().post("Hacker Detected", ent.getName() + " has irregular movements (Aura/Hop).", 7500, Notifications.Type.WARNING);
                        if ((boolean) settings.get("REPORT").getValue())
                            ChatUtil.sendChat("/wdr " + ent.getName() + " killaura fly speed scaffold");
                        PriorityManager.setAsPriority(ent);
                    }
                }

            }
        }
    }


}
