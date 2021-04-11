/**
 * Time: 5:29:22 AM
 * Date: Dec 29, 2016
 * Creator: cool1
 */
package exhibition.module.impl.combat;

import com.mojang.authlib.GameProfile;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.management.PriorityManager;
import exhibition.management.command.Command;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.gta.Aimbot;
import exhibition.module.impl.render.ESP2D;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.*;

public class AntiBot extends Module {

    private String MODE = "MODE";
    private String DEAD = "DEAD";
    private String REMOVE = "REMOVE";

    private Timer waitTimer = new Timer();

    private Timer timer = new Timer();

    private final List<EntityPlayer> spawnedList = new ArrayList<>();

    public AntiBot(ModuleData data) {
        super(data);
        settings.put(DEAD, new Setting<>(DEAD, true, "Removes dead bodies from the game."));
        settings.put(REMOVE, new Setting<>(REMOVE, true, "Removes bots from the game."));
        settings.put(MODE, new Setting<>(MODE, new Options("Mode", "Hypixel", "Hypixel", "Packet", "Mineplex"), "Check method for bots."));
    }

    private static List<EntityPlayer> getInvalid() {
        return invalid;
    }

    private static final List<EntityPlayer> invalid = new ArrayList<>();

    private static final HashMap<Integer, Integer> ticksOnGroundMap = new HashMap<>();

    @Override
    public void onToggle() {
        invalid.clear();
        ticksOnGroundMap.clear();
    }

    public static int getTicksOnGround(EntityLivingBase ent) {
        return ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0);
    }

    public static void clear() {
        invalid.clear();
        ticksOnGroundMap.clear();
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    private int spawnedSinceUpdate = 0;

    private NetworkPlayerInfo playerInfo;

    @RegisterEvent(events = {EventPacket.class, EventMotionUpdate.class, EventAttack.class, EventSpawnPlayer.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            spawnedSinceUpdate = 0;
            spawnedList.clear();
            return;
        }

        boolean remove = (Boolean) settings.get(REMOVE).getValue();
        String currentSetting = ((Options) settings.get(MODE).getValue()).getSelected();
        if (event instanceof EventSpawnPlayer) {
            EventSpawnPlayer e = event.cast();
            if (e.getPlayer().getEntityId() == 69420) {
                return;
            }

            spawnedSinceUpdate++;


        }

        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            Packet bruhPacket = ep.getPacket();

            if (currentSetting.equalsIgnoreCase("Packet"))
                if (bruhPacket instanceof S0CPacketSpawnPlayer) {
                    S0CPacketSpawnPlayer packet = (S0CPacketSpawnPlayer) ep.getPacket();
                    double entX = packet.getX() / 32D;
                    double entY = packet.getY() / 32D;
                    double entZ = packet.getZ() / 32D;
                    double posX = mc.thePlayer.posX;
                    double posY = mc.thePlayer.posY;
                    double posZ = mc.thePlayer.posZ;
                    double var7 = posX - entX;
                    double var9 = posY - entY;
                    double var11 = posZ - entZ;
                    float distance = MathHelper.sqrt_double(var7 * var7 + var9 * var9 + var11 * var11);
                    if (mc.thePlayer.ticksExisted > 50 && distance <= 17 && entY > mc.thePlayer.posY + 1 && (mc.thePlayer.posX != entX && mc.thePlayer.posY != entY && mc.thePlayer.posZ != entZ)) {
                        ep.setCancelled(true);
                    }
                }
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            setSuffix(currentSetting);
            if (em.isPre()) {
                if (mc.thePlayer.isAllowEdit()) {
                    if (HypixelUtil.isGameStarting()) {
                        waitTimer.reset();
                    }
                } else {
                    waitTimer.reset();
                }

                spawnedList.clear();
                if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
                    if ((mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net")) && !currentSetting.equals("Hypixel")) {
                        ((Options) settings.get(MODE).getValue()).setSelected("Hypixel");
                        ChatUtil.printChat(Command.chatPrefix + "AntiBot has been set to the proper mode.");
                    } else if (mc.getCurrentServerData().serverIP.toLowerCase().contains("mineplex") && !currentSetting.equals("Mineplex")) {
                        ((Options) settings.get(MODE).getValue()).setSelected("Mineplex");
                        ChatUtil.printChat(Command.chatPrefix + "AntiBot has been set to the proper mode.");
                    }
                }
                if (((Boolean) settings.get(DEAD).getValue()))
                    for (Object o : mc.theWorld.getPlayerEntities()) {
                        if (!(o instanceof EntityPlayerSP)) {
                            EntityPlayer ent = (EntityPlayer) o;
                            if (ent.isPlayerSleeping() || ent.isDead) {
                                DevNotifications.getManager().post("Dead player cleared: " + ent.getName() + " " + (ent.isPlayerSleeping() ? "1" : 0) + " " + (ent.isDead ? "1" : 0));
                                mc.theWorld.removeEntity(ent);
                            }
                        }
                    }
            }
            if (em.isPre() && !currentSetting.equalsIgnoreCase("Packet")) {
                //Clears the invalid player list after a second to prevent false positives staying permanent.
                if (timer.delay(100)) {
                    invalid.clear();
                    timer.reset();
                }
                // Loop through entity list

                if (spawnedSinceUpdate > 2) {
                    for (EntityPlayer entityPlayer : spawnedList) {
                        ticksOnGroundMap.put(entityPlayer.getEntityId(), -20);
                    }
                }

                final NetHandlerPlayClient var4 = mc.thePlayer.sendQueue;
                List<NetworkPlayerInfo> list = new ArrayList<>(var4.getPlayerInfoMap());
                if (playerInfo == null || !list.contains(playerInfo)) {
                    GameProfile gameProfile = mc.thePlayer.getGameProfile();
                    for (NetworkPlayerInfo networkPlayerInfo : list) {
                        if (networkPlayerInfo.getGameProfile() != null && gameProfile != null)
                            if (gameProfile.equals(networkPlayerInfo.getGameProfile()) || gameProfile.getName().equals(networkPlayerInfo.getGameProfile().getName())) {
                                playerInfo = networkPlayerInfo;
                                break;
                            }
                    }
                }

                List<EntityPlayer> playersToRemove = new ArrayList<>();

                boolean inPit = HypixelUtil.isInGame("PIT");
                boolean inSkywars = HypixelUtil.isInGame("SKYWARS");

                for (EntityPlayer ent : mc.theWorld.playerEntities) {
                    if (!(ent instanceof EntityPlayerSP)) {
                        if (FriendManager.isFriend(ent.getName())) continue;

                        if (inSkywars && HypixelUtil.isGameActive()) {
                            if (ent.isDead) {
                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + ent.getHealth() + " " + ent.getName() + " has died.");
                                ticksOnGroundMap.remove(ent.getEntityId());
                            }
                            if (ent.isInvisible() && !ent.lastTickInvisible && ent.ticksExisted > 5) {
                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + ent.getName() + " has vanished.");
                            }
                        }

                        if (waitTimer.delay(1250) && ent.ticksExisted <= 2 && !ent.illegalSpawn) {
                            spawnedList.add(ent);

                            double posX = mc.thePlayer.posX;
                            double posY = mc.thePlayer.posY;
                            double posZ = mc.thePlayer.posZ;
                            double var7 = posX - ent.posX;
                            double var9 = posY - ent.posY;
                            double var11 = posZ - ent.posZ;
                            double distance = MathHelper.sqrt_double(var7 * var7 + var9 * var9 + var11 * var11);

//                                double roundedY = MathUtils.roundToPlace(ent.posY, 6);

                            if (Math.abs(distance) > 3 && mc.thePlayer.ticksExisted > 260 && spawnedSinceUpdate <= 12) {
//                        DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "------------------------------------------------------------------------");
//                        DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Illegal Spawn: " + player.getDisplayName().getFormattedText() + " \247a Distance: " + MathUtils.roundToPlace(distance, 3) +
//                                " \247bDX: " + MathUtils.roundToPlace(var7, 3) +
//                                " \247cDY: " + MathUtils.roundToPlace(var9, 3) + " " +
//                                " \247dDZ: " + MathUtils.roundToPlace(var11, 3) + " \247e" +
//                                spawnedSinceUpdate);
//                        DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Spawned player pos \247a" + MathUtils.roundToPlace(player.posX, 6) + " \247b" +
//                                MathUtils.roundToPlace(player.posY, 6) + " \247c" +
//                                MathUtils.roundToPlace(player.posZ, 6) + " \247dT: " +
//                                isInTabList(player) + " \247eI: " +
//                                player.isInvisibleToPlayer(mc.thePlayer));
//                        DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Local player pos   \247a" + MathUtils.roundToPlace(mc.thePlayer.posX, 6) + " \247b" +
//                                MathUtils.roundToPlace(mc.thePlayer.posY, 6) + " \247c" +
//                                MathUtils.roundToPlace(mc.thePlayer.posZ, 6));

//                                    if ((roundedY == -200 || roundedY == 400)) {
//                                        //DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " \247e" + "Weird bot spawn pos?");
//                                    }
                                //Get formatted name
                                String str = ent.getDisplayName().getFormattedText();
                                // if the formatted name is equal to a default name "name + \247r" or the name contains "NPC" or they're not in the tab list, remove the entity.
                                boolean botNameFormat = str.endsWith("\247c" + ent.getName() + "\247r");

                                if (botNameFormat)
                                    DevNotifications.getManager().post("Suspicious Spawn " + ent.getName() + " " + ent.getDisplayName().getFormattedText() + " " + ent.ticksExisted + " " + Math.abs(distance));

                                if (botNameFormat && Math.abs(distance) >= 300) {
                                    ticksOnGroundMap.put(ent.getEntityId(), -300);
                                }
                                //DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "------------------------------------------------------------------------");
                                if (botNameFormat)
                                    ent.illegalSpawn = true;
                            }
                        }

                        if (ent.ticksExisted <= 150 && mc.thePlayer.getDistanceToEntity(ent) < 50 && mc.thePlayer.ticksExisted > 150) {
                            double deltaX = ent.lastTickPosX - ent.posX;
                            double deltaY = ent.lastTickPosY - ent.posY;
                            double deltaZ = ent.lastTickPosZ - ent.posZ;

                            double distance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                            if (distance > 2 && ent.illegalSpawn && (!mc.thePlayer.isInvisible() || mc.thePlayer.capabilities.allowFlying) && !ent.isRiding()) {
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "------------------------------------------------------------------------");
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Illegal TP: " + ent.getDisplayName().getFormattedText() + " \247aDelta: " + MathUtils.roundToPlace(distance, 6) + " \247bDist: " + mc.thePlayer.getDistanceToEntity(ent) + " \247c" + ent.ticksExisted);
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Teleported player pos \247a" + MathUtils.roundToPlace(ent.posX, 6) + " \247b" +
//                                        MathUtils.roundToPlace(ent.posY, 6) + " \247c" +
//                                        MathUtils.roundToPlace(ent.posZ, 6) + " \247dTab: " +
//                                        isInTabList(ent) + " \247eI1" +
//                                        ent.isInvisibleToPlayer(mc.thePlayer) + " \247fI2 " + ent.isInvisible());
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Teleported last pos   \247a" + MathUtils.roundToPlace(ent.lastTickPosX, 6) + " \247b" +
//                                        MathUtils.roundToPlace(ent.lastTickPosY, 6) + " \247c" +
//                                        MathUtils.roundToPlace(ent.lastTickPosZ, 6));
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "Local player pos      \247a" + MathUtils.roundToPlace(mc.thePlayer.posX, 6) + " \247b" +
//                                        MathUtils.roundToPlace(mc.thePlayer.posY, 6) + " \247c" +
//                                        MathUtils.roundToPlace(mc.thePlayer.posZ, 6));
//                                if (bruh)
//                                    DevNotifications.getManager().post("Is Bruh");
//                                DevNotifications.getManager().post(mc.thePlayer.ticksExisted + " " + "------------------------------------------------------------------------");

                                if (ent.flagged != 1 && !ent.isInvisible() && ent.getDisplayName().getFormattedText().contains("\247c" + ent.getName() + "\247r") && mc.thePlayer.getDistanceToEntity(ent) < 10) {
                                    Notifications.getManager().post("Possible Staff Bot", ent.getDisplayName().getFormattedText() + " \247fmay be a staff bot!", 5000, Notifications.Type.WARNING);
                                    ent.flagged = 1;
                                }

                                invalid.add(ent);
                                int ticksOnGround = ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0);
                                ticksOnGroundMap.put(ent.getEntityId(), Math.min(ticksOnGround - 2000, -2000));
                            }
                        }

                        ent.lastTickInvisible = ent.isInvisible();

                        double motionYNormal = ent.lastTickPosY - ent.posY;
                        double motionY = Math.abs(ent.lastTickPosY - ent.posY);

                        /*
                        *  boolean onGround = motionY < 1 && ((
                                (mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - (motionYNormal < 0.05 ? 0.45 : 0.9), ent.posZ)).getBlock().getMaterial() != Material.air &&
                                mc.theWorld.getBlockState(new BlockPos(ent.lastTickPosX, ent.lastTickPosY - (motionYNormal < 0.05 ? 0.45 : 0.9), ent.lastTickPosZ)).getBlock().getMaterial() != Material.air)
                                        ||
                                (mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - (motionYNormal < 0.05 ? 0.85 : 1.3), ent.posZ)).getBlock().getMaterial() != Material.air &&
                                        mc.theWorld.getBlockState(new BlockPos(ent.lastTickPosX, ent.lastTickPosY - (motionYNormal < 0.05 ? 0.85 : 1.3), ent.lastTickPosZ)).getBlock().getMaterial() != Material.air)) ||
                                isTouchingGround(ent)) && !mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY + ent.getEyeHeight(), ent.posZ)).getBlock().isBlockNormalCube();
                        * */

                        boolean shouldUpdateTOG = true;

                        if (!invalid.contains(ent)) {
                            switch (currentSetting) {
                                case "Hypixel": {

                                    boolean isOnHypixel = mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net")) && mc.getIntegratedServer() == null;

                                    boolean doPingCheck = Math.max(playerInfo.getResponseTime(), 0) == 1;

                                    boolean isInTabList = isInTabList(list, ent);

                                    //Get formatted name
                                    String str = ent.getDisplayName().getFormattedText();
                                    // if the formatted name is equal to a default name "name + \247r" or the name contains "NPC" or they're not in the tab list, remove the entity.
                                    boolean botNameFormat = str.endsWith("\247c" + ent.getName() + "\247r");

                                    if (inPit) {
                                        boolean hasPitCharacters = str.contains("[") || str.contains("]") || str.contains("\247l");

                                        if (!hasPitCharacters) {
                                            invalid.add(ent);
                                        }
                                        if (botNameFormat || str.equalsIgnoreCase(ent.getName()) || str.contains("[NPC]")) {
                                            if (!isInTabList && isOnHypixel) {
                                                invalid.add(ent);
                                                shouldUpdateTOG = false;
                                                if (remove && ent.isInvisible() && mc.thePlayer.getDistanceToEntity(ent) < 10 && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20) {
                                                    playersToRemove.add(ent);
                                                    DevNotifications.getManager().post("Removed " + ent.getName() + " C*");
                                                    continue;
                                                }
                                            }
                                        }

                                        if (((str.equals(ent.getName() + "\247r") || str.equals("\247r" + ent.getName()) || str.equals("\247r" + ent.getName() + "\247r")) && !isInTabList) || str.contains("[NPC]")) {
                                            invalid.add(ent);
                                            shouldUpdateTOG = false;
                                            if (remove && ent.isInvisible() && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20) {
                                                playersToRemove.add(ent);
                                                continue;
                                            }
                                        }
                                        break;
                                    }

                                    if (doPingCheck) {
                                        if (ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < 15 && isInTabList && ESP2D.getPlayerPing(ent) > 1) {
                                            invalid.add(ent);
                                            shouldUpdateTOG = false;
                                        }
                                    }


                                    if (Client.getModuleManager().isEnabled(Aimbot.class) && HypixelUtil.scoreboardContains("team: ") && (HypixelUtil.scoreboardContains("co\uD83C\uDF89ps") || HypixelUtil.scoreboardContains("cr\uD83C\uDF89ims"))) {
                                        if (mc.thePlayer.getEquipmentInSlot(1) != null && mc.thePlayer.getEquipmentInSlot(1).getItem() == Items.clay_ball) {
                                            if (botNameFormat) {
                                                invalid.add(ent);
                                                ticksOnGroundMap.put(ent.getEntityId(), -69420);
                                                Notifications.getManager().post("Illegal Player", "An Illegal player has spawned in.", 6000, Notifications.Type.WARNING);
                                                shouldUpdateTOG = false;
                                            }
                                        }
                                    }

                                    if (HypixelUtil.isInGame("PIT")) {
                                        if (botNameFormat && !str.contains("[")) {
                                            invalid.add(ent);
                                            ticksOnGroundMap.put(ent.getEntityId(), -300);
                                            shouldUpdateTOG = false;
                                        }
                                    }

                                    if (ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < 15) {
                                        invalid.add(ent);
                                    }

                                    if (botNameFormat || str.equalsIgnoreCase(ent.getName())) {
                                        if (botNameFormat && !isInTabList && isOnHypixel) {
                                            if (ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < 15) {
                                                invalid.add(ent);
                                                shouldUpdateTOG = false;
                                                if (remove && (ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20 && ent.isInvisible()) && mc.thePlayer.getDistanceToEntity(ent) < 10) {
                                                    playersToRemove.add(ent);
                                                    DevNotifications.getManager().post("Removed " + ent.getName() + " A");
                                                    continue;
                                                }
                                            }
                                        }
                                    }

                                    if (ent.isInvisible() && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < 15 && botNameFormat) {
                                        invalid.add(ent);
                                        shouldUpdateTOG = false;
                                        if (remove && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20 && !isInTabList) {
                                            playersToRemove.add(ent);
                                            DevNotifications.getManager().post("Removed " + ent.getName() + " B");
                                            continue;
                                        }
                                    }

                                    if (botNameFormat || str.equalsIgnoreCase(ent.getName()) || str.contains("[NPC]")) {
                                        if (!isInTabList && isOnHypixel && (ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < 15)) {
                                            invalid.add(ent);
                                            shouldUpdateTOG = false;
                                            if (remove && ent.isInvisible() && mc.thePlayer.getDistanceToEntity(ent) < 10 && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20) {
                                                playersToRemove.add(ent);
                                                DevNotifications.getManager().post("Removed " + ent.getName() + " C");
                                                continue;
                                            }
                                        }
                                    }

                                    if (((str.equals(ent.getName() + "\247r") || str.equals("\247r" + ent.getName()) || str.equals("\247r" + ent.getName() + "\247r")) && !isInTabList) || str.contains("[NPC]")) {
                                        invalid.add(ent);
                                        shouldUpdateTOG = false;
                                        if (remove && ent.isInvisible() && ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0) < -20) {
                                            playersToRemove.add(ent);
                                            continue;
                                        }
                                    }
                                    break;
                                }
                                case "Mineplex": {
                                    if (ent.getHealth() >= 0) invalid.add(ent);
                                    if (ent.isPlayerSleeping()) invalid.add(ent);
                                    break;
                                }
                            }
                        }

                        if (shouldUpdateTOG) {
                            boolean onGround = motionY < 1 && (((mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - (motionYNormal < 0.05 ? 0.45 : 0.9), ent.posZ)).getBlock().getMaterial() != Material.air &&
                                    mc.theWorld.getBlockState(new BlockPos(ent.lastTickPosX, ent.lastTickPosY - (motionYNormal < 0.05 ? 0.45 : 0.9), ent.lastTickPosZ)).getBlock().getMaterial() != Material.air)
                                    || (mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY - (motionYNormal < 0.1 ? 0.65 : 1.255), ent.posZ)).getBlock().getMaterial() != Material.air &&
                                    mc.theWorld.getBlockState(new BlockPos(ent.lastTickPosX, ent.lastTickPosY - (motionYNormal < 0.1 ? 0.65 : 1.255), ent.lastTickPosZ)).getBlock().getMaterial() != Material.air)) ||
                                    isTouchingGround(ent)) && !mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY + ent.getEyeHeight(), ent.posZ)).getBlock().isBlockNormalCube();

                            if (Killaura.getTarget() != ent) {
                                if (onGround) {
                                    int ticksOnGround = ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0);
                                    ticksOnGroundMap.put(ent.getEntityId(), ticksOnGround + (motionY < 0.05 ? ent.isInvisible() ? 1 : ent.flagged == 1 ? 1 : 3 : 1));
                                } else if (ent.hurtTime <= 0 && !ent.isSneaking() && !ent.isRiding() && motionY > 0.1 || (ent.isInvisible() && (motionY == 0 || motionY > 0.5))) {
                                    int ticksOnGround = ticksOnGroundMap.getOrDefault(ent.getEntityId(), 0);
                                    ticksOnGroundMap.put(ent.getEntityId(), ticksOnGround - (ent.isInvisible() ? 10 : 1));
                                }
                            }
                        }
                    }
                }

                for (EntityPlayer entityPlayer : playersToRemove) {
                    mc.theWorld.removeEntity(entityPlayer);
                }

                spawnedSinceUpdate = 0;
            }
        } else if (event instanceof EventAttack) {
            EventAttack eventAttack = event.cast();
            if (eventAttack.isPreAttack()) {
                if (eventAttack.getEntity() instanceof EntityPlayer) {
                    EntityPlayer player = ((EntityPlayer) eventAttack.getEntity());
                    invalid.remove(player);
                }
            }
        }
    }

    public boolean isPosOnGround(double posX, double posY, double posZ) {
        boolean isOnSlab = MathUtils.roundToPlace((posY - (int) posY), 1) == 0.5;

        Block nextBlockUnder = mc.theWorld.getBlockState(new BlockPos(posX, posY - (isOnSlab ? 0 : 0.1), posZ)).getBlock();

        boolean feetBlockAir = isOnSlab ? nextBlockUnder.getMaterial() == Material.air : (nextBlockUnder instanceof BlockSlab && !nextBlockUnder.isFullBlock()) || nextBlockUnder.getMaterial() == Material.air;

        return !feetBlockAir && !mc.theWorld.getBlockState(new BlockPos(posX, posY + 1.5D, posZ)).getBlock().isBlockNormalCube();
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

    public static boolean isBot(Entity ent) {
        if (!Client.getModuleManager().isEnabled(AntiBot.class))
            return false;

        if (ent instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) ent;
            if (PriorityManager.isPriority(player))
                return false;
            return AntiBot.getInvalid().contains(ent);
        }
        return false;
    }

    public static boolean isInTabList(List<NetworkPlayerInfo> players, EntityPlayer player) {
        for (NetworkPlayerInfo info : players) {
            if (info == null) {
                continue;
            }
            if (info.getGameProfile().getId().equals(player.getUniqueID())) {
                return true;
            }
        }
        return false;
    }

}
