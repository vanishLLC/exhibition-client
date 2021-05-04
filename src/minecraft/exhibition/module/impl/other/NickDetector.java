package exhibition.module.impl.other;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.UUIDResolver;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.playerInfoMap;

public class NickDetector extends Module {

    private final Setting<Boolean> disconnect = new Setting<>("DISCONNECT", false, "Notifies you if a nicked player disconnects. \247e(May help identify Staff)");
    public final Setting<Boolean> denick = new Setting<>("DENICK", true, "Attempts to reveal the name of nicked players.");
    public final Setting<Boolean> vanish = new Setting<>("VANISH", true, "Attempts to reveal vanished players.");

    public final List<ResolvePair> resolvePairList = new CopyOnWriteArrayList<>();

    public NickDetector(ModuleData data) {
        super(data);
        addSetting(disconnect);
        addSetting(denick);
        addSetting(vanish);
    }

    private final Timer timer = new Timer();
    private final Timer vanishTimer = new Timer();
    private final Timer loadBuffer = new Timer();

    private int secondCounter = 0;

    List<String> vanishPeopleNotRacist = new ArrayList<>();
    List<String> possibleVanishPeople = new ArrayList<>();
    List<String> seenEntitiesFuckMe = new ArrayList<>();
    List<String> purgeEntityList = new ArrayList<>();
    List<String> purgeBuffer = new ArrayList<>();
    List<String> temp = new ArrayList<>();

    boolean exitFlag = false;
    private int i = 0;

    boolean skip = false;
    int counter = 0;
    boolean hasChangedOnce = false;


    @Override
    public void toggle() {
        super.toggle();
        UUIDResolver.instance.checkedUsernames.clear();
        UUIDResolver.instance.resolvedMap.clear();
        UUIDResolver.instance.validMap.clear();
    }

    @Override
    public void worldChange() {
        this.vanishPeopleNotRacist.clear();
        this.skip = true;
        this.seenEntitiesFuckMe.clear();
        this.hasChangedOnce = true;
        this.loadBuffer.reset();
    }

    public void vanish() {
        if (vanish.getValue() && HypixelUtil.isInGame("PIT") && vanishTimer.delay(330)) {
            this.vanishTimer.reset();
            this.i = 0;
            this.temp.clear();
            try {
                if (this.secondCounter == 20) {
                    this.purgeBuffer.clear();
                    this.secondCounter = 0;
                    for (String tempPlayer2 : this.seenEntitiesFuckMe) {
                        this.exitFlag = false;
                        for (NetworkPlayerInfo tempPlayerNetwork : mc.getNetHandler().getPlayerInfoMap()) {
                            if (!tempPlayerNetwork.getGameProfile().getName().contains(tempPlayer2)) continue;
                            this.exitFlag = true;
                            break;
                        }
                        for (EntityPlayer tempPlayerWorld : mc.theWorld.playerEntities) {
                            if (!tempPlayerWorld.getName().contains(tempPlayer2)) continue;
                            this.exitFlag = true;
                            break;
                        }
                        if (this.exitFlag || tempPlayer2.length() == 10) continue;
                        this.purgeBuffer.add(tempPlayer2);
                    }
                    for (String tempPlayer2 : this.purgeEntityList) {
                        this.seenEntitiesFuckMe.remove(tempPlayer2);
                    }
                    this.purgeEntityList.clear();
                    this.purgeEntityList.addAll(this.purgeBuffer);
                } else {
                    ++this.secondCounter;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Collection<ScorePlayerTeam> teamList = mc.theWorld.getScoreboard().getTeams();
            for (ScorePlayerTeam tempTeam : teamList) {
                for (String tempIgn : tempTeam.getMembershipCollection()) {
                    if (tempTeam.getTeamName().contains("team")) continue;
                    ++this.i;
                    this.temp.add(tempIgn);
                }
            }
            for (EntityPlayer tempPlayer3 : mc.theWorld.playerEntities) {
                String name = tempPlayer3.getName();
                if (this.seenEntitiesFuckMe.contains(name) || AntiBot.isBot(tempPlayer3)) continue;
                this.seenEntitiesFuckMe.add(name);
            }
            if (this.loadBuffer.getDifference() < 2000L) {
                this.hasChangedOnce = false;
                for (NetworkPlayerInfo tempPlayer3 : mc.getNetHandler().getPlayerInfoMap()) {
                    int index = this.seenEntitiesFuckMe.indexOf(tempPlayer3.getGameProfile().getName());
                    if (index != -1) continue;
                    this.seenEntitiesFuckMe.add(tempPlayer3.getGameProfile().getName());
                }
            }
            for (String tempPlayer3 : this.seenEntitiesFuckMe) {
                int index = this.temp.indexOf(tempPlayer3);
                if (index == -1) continue;
                this.temp.remove(index);
            }
            if (this.skip) {
                if (this.counter == 5) {
                    this.skip = false;
                    this.counter = 0;
                    return;
                }
                ++this.counter;
                return;
            }
            this.vanishPeopleNotRacist.clear();
            for (String tempString : this.temp) {
                if (this.possibleVanishPeople.contains(tempString)) {
                    this.vanishPeopleNotRacist.add(tempString);
                    Notifications.getManager().post("Vanished Player", tempString + "\247r may be vanished!", 5000, Notifications.Type.WARNING);
                    continue;
                }
                this.possibleVanishPeople.add(tempString);
            }
        }
    }

    @RegisterEvent(events = {EventTick.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S38PacketPlayerListItem && disconnect.getValue()) {
                S38PacketPlayerListItem packetPlayerListItem = (S38PacketPlayerListItem) packet;
                for (S38PacketPlayerListItem.AddPlayerData addPlayerData : packetPlayerListItem.getPlayerList()) {
                    if (packetPlayerListItem.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                        if (UUIDResolver.instance.isInvalidUUID(addPlayerData.getProfile().getId())) {
                            try {
                                Notifications.getManager().post("Nick Detector", mc.getNetHandler().getPlayerInfo(addPlayerData.getProfile().getId()).getGameProfile().getName() + " has left your game.", Notifications.Type.INFO);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
                return;
            }
            if (packet instanceof S04PacketEntityEquipment) {
                S04PacketEntityEquipment packetIn = (S04PacketEntityEquipment) packet;
                Entity entity = mc.theWorld.getEntityByID(packetIn.getEntityID());

                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (UUIDResolver.instance.isInvalidName(player.getName()) && !UUIDResolver.instance.resolvedMap.containsKey(player.getName())) {
                        ItemStack stack = packetIn.getItemStack();
                        if (stack != null && stack.hasTagCompound()) {
                            if (stack.getTagCompound().hasKey("ExtraAttributes", 10)) {
                                NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("ExtraAttributes");

                                if (nbttagcompound.hasKey("Nonce", 3)) {
                                    try {
                                        long nonceLong = nbttagcompound.getLong("Nonce");

                                        if (nonceLong <= 100)
                                            return;

                                        resolvePairList.add(new ResolvePair(player.getName(), stack));
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        if (event instanceof EventTick) {

            if (mc.currentScreen instanceof GuiChest && timer.delay(1000)) {
                timer.reset();
                GuiChest guiChest = ((GuiChest) mc.currentScreen);
                String name = guiChest.lowerChestInventory.getDisplayName().getUnformattedText();
                if (guiChest.lowerChestInventory.hasCustomName() && name.contains("Profile Viewer")) {
                    ItemStack stack = guiChest.lowerChestInventory.getStackInSlot(11);
                    if (stack != null) {
                        if (stack.getItem() == Items.skull) {
                            if (stack.getMetadata() == 3) {
                                String headName = stack.getDisplayName();

                                if (headName.contains(" ") && headName.endsWith("\247f")) {
                                    headName = StringUtils.stripControlCodes(headName.split(" ")[1].replace("\247f", ""));
                                }

                                if (UUIDResolver.instance.checkedUsernames.containsKey(StringUtils.stripControlCodes(headName)) && UUIDResolver.instance.isInvalidName(StringUtils.stripControlCodes(headName)) && !UUIDResolver.instance.resolvedMap.containsKey(StringUtils.stripControlCodes(headName))) {
                                    GameProfile gameprofile = null;
                                    if (stack.hasTagCompound()) {
                                        NBTTagCompound nbttagcompound = stack.getTagCompound();

                                        if (nbttagcompound.hasKey("SkullOwner", 10)) {
                                            gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                                        } else if (nbttagcompound.hasKey("SkullOwner", 8) && nbttagcompound.getString("SkullOwner").length() > 0) {
                                            gameprofile = new GameProfile((UUID) null, nbttagcompound.getString("SkullOwner"));
                                        }
                                    }

                                    if (gameprofile != null && gameprofile.getId() != null) {
                                        String uuid = String.valueOf(gameprofile.getId());
                                        Connection profileConnection = new Connection("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                                        Connector.get(profileConnection);
                                        JsonObject profileJsonObject = (JsonObject) JsonParser.parseString(profileConnection.getResponse());

                                        if (profileJsonObject.has("name")) {
                                            String resolvedName = profileJsonObject.get("name").getAsString();
                                            if (resolvedName != null) {
                                                UUIDResolver.instance.resolvedMap.put(headName, resolvedName);
                                                Notifications.getManager().post("Nick Detector", headName + " may be " + resolvedName + "!*", 2500, Notifications.Type.NOTIFY);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HashMap<String, UUID> usernameList = new HashMap<>();

            if (UUIDResolver.instance.isChecking || mc.thePlayer == null || !mc.thePlayer.isAllowEdit() || mc.thePlayer.ticksExisted < 100) {
                return;
            }

            try {
                final NetHandlerPlayClient netHandler = mc.thePlayer.sendQueue;
                List<NetworkPlayerInfo> list = playerInfoMap.sortedCopy(netHandler.getPlayerInfoMap());
                for (NetworkPlayerInfo playerInfo : list) {
                    GameProfile gameProfile = playerInfo.getGameProfile();
                    if (gameProfile != null && !gameProfile.equals(mc.thePlayer.getGameProfile())) {
                        IChatComponent e = new ChatComponentText(ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), gameProfile.getName()));
                        String displayName = e.getFormattedText();
                        String name = gameProfile.getName();
                        if (displayName.equals("\247r" + name) || displayName.equals(name) || displayName.equals("\247r" + name + "\247r") || displayName.equals(name + "\247r")) {
                            continue;
                        }
                        try {
                            if (!UUIDResolver.instance.checkedUsernames.containsKey(name) && gameProfile.getId().version() == 1) {
                                if (!UUIDResolver.instance.isSkinValid(gameProfile)) {
                                    if (UUIDResolver.instance.resolvedMap.containsKey(name)) {
                                        Notifications.getManager().post("Nick Detector", name + " may be " + UUIDResolver.instance.resolvedMap.get(name) + "! (S)", 2500, Notifications.Type.NOTIFY);
                                    } else {
                                        Notifications.getManager().post("Nick Detector", name + " is in /nick!", 2500, Notifications.Type.NOTIFY);
                                    }
                                    UUIDResolver.instance.checkedUsernames.put(name, gameProfile.getId());
                                }
                            }
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }

                        if (UUIDResolver.instance.checkedUsernames.containsKey(name) && (!denick.getValue() || (!UUIDResolver.instance.isInvalidName(name)) && (!UUIDResolver.instance.resolvedMap.containsKey(name)))) {
                            continue;
                        }

                        usernameList.put(name, gameProfile.getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!usernameList.isEmpty()) {
                UUIDResolver.instance.checkNames(usernameList);
            }
        }
    }

    public static class ResolvePair {

        public String username;
        public ItemStack item;

        public ResolvePair(String username, ItemStack item) {
            this.username = username;
            this.item = item;
        }

        public String getUsername() {
            return username;
        }

        public ItemStack getStack() {
            return item;
        }

    }

}
