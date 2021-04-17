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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.playerInfoMap;

public class NickDetector extends Module {

    private final Setting<Boolean> disconnect = new Setting<>("DISCONNECT", false, "Notifies you if a nicked player disconnects. \247e(May help identify Staff)");
    public final Setting<Boolean> denick = new Setting<>("DENICK", true, "Attempts to reveal the name of nicked players.");

    public final List<ResolvePair> resolvePairList = new CopyOnWriteArrayList<>();

    public NickDetector(ModuleData data) {
        super(data);
        addSetting(disconnect);
        addSetting(denick);
    }

    private final Timer timer = new Timer();

    @Override
    public void toggle() {
        super.toggle();
        UUIDResolver.instance.checkedUsernames.clear();
        UUIDResolver.instance.resolvedMap.clear();
        UUIDResolver.instance.validMap.clear();
    }

    @RegisterEvent(events = {EventTick.class, EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null)
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
                    if (UUIDResolver.instance.checkedUsernames.containsKey(player.getName()) && UUIDResolver.instance.isInvalidUUID(player.getGameProfile().getId()) && !UUIDResolver.instance.resolvedMap.containsKey(player.getName())) {
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
            if (mc.currentScreen instanceof GuiChest) {
                GuiChest guiChest = ((GuiChest) mc.currentScreen);
                String name = guiChest.lowerChestInventory.getDisplayName().getUnformattedText();
                if (guiChest.lowerChestInventory.hasCustomName() && name.contains("Profile Viewer")) {
                    ItemStack stack = guiChest.lowerChestInventory.getStackInSlot(11);
                    if (stack != null) {
                        if (stack.getItem() == Items.skull) {
                            if (stack.getMetadata() == 3) {
                                String headName = stack.getDisplayName();

                                if (headName.contains(" ") && headName.endsWith("\247f")) {
                                    headName = headName.split(" ")[1].replace("\247f", "");
                                }

                                if (UUIDResolver.instance.isInvalidName(headName) && !UUIDResolver.instance.resolvedMap.containsKey(StringUtils.stripControlCodes(headName))) {
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
                    if (playerInfo.getGameProfile() != null && !playerInfo.getGameProfile().equals(mc.thePlayer.getGameProfile())) {
                        IChatComponent e = new ChatComponentText(ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName()));
                        String displayName = e.getFormattedText();
                        String name = playerInfo.getGameProfile().getName();
                        if (displayName.equals("\247r" + name) || displayName.equals(name) || displayName.equals("\247r" + name + "\247r") || displayName.equals(name + "\247r")) {
                            continue;
                        }
                        if (UUIDResolver.instance.checkedUsernames.containsKey(name) && (!denick.getValue() || (!UUIDResolver.instance.isInvalidName(name)) && (!UUIDResolver.instance.resolvedMap.containsKey(name)))) {
                            continue;
                        }
                        usernameList.put(name, playerInfo.getGameProfile().getId());
                    }
                }
            } catch (Exception e) {

            }

            if (!usernameList.isEmpty()) {
                timer.reset();
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
