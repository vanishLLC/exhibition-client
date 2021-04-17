package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventSpawnEntity;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PitNotifications extends Module {

    private final MultiBool options;
    private final Setting<Boolean> sewers = new Setting<>("SEWER CHEST", true);

    // Containers could be further optimized, however this is way better than per-frame iteration
    private final List<EntityItem> updatedItems = new CopyOnWriteArrayList<>();
    private final List<EntityItem> trackedItems = new CopyOnWriteArrayList<>();

    public PitNotifications(ModuleData data) {
        super(data);
        settings.put("SELFISH", new Setting<>("SELF-ONLY", true, "Only notifies you of messages that pertain to you."));
        Setting[] Notifications = new Setting[]{
                new Setting<>("MAJOR EVENT", true),
                new Setting<>("MINOR EVENT", true),
                new Setting<>("MEGASTREAK", true),
                new Setting<>("STREAK", true),
                new Setting<>("BOUNTY", true),
                new Setting<>("TRADE REQUEST", true),
                new Setting<>("NIGHT QUEST", true),
                new Setting<>("DRAGON EGG", true),
                new Setting<>("MYSTIC DROP", true),
                sewers};
        settings.put("OPTIONS", new Setting<>("OPTIONS", options = new MultiBool("Notifications", Notifications), "Things to notify"));
    }

    @Override
    @RegisterEvent(events = {EventPacket.class, EventTick.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D && options.getValue("MYSTIC DROP")) {
            if (trackedItems.size() == 0)
                return;

            EventRender3D er = event.cast();
            final boolean bobbing = mc.gameSettings.viewBobbing;
            GL11.glLoadIdentity();
            mc.gameSettings.viewBobbing = false;
            mc.entityRenderer.orientCamera(er.renderPartialTicks);
            // Cache the entity instead? Avoid doing loops on *all* entities every frame.
            GlStateManager.pushMatrix();
            for (EntityItem ent : trackedItems) {
                double posX = ent.lastTickPosX + ((ent.posX - ent.lastTickPosX) * er.renderPartialTicks) - RenderManager.renderPosX;
                double posY = ent.lastTickPosY + ((ent.posY - ent.lastTickPosY) * er.renderPartialTicks) - RenderManager.renderPosY;
                double posZ = ent.lastTickPosZ + ((ent.posZ - ent.lastTickPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ;
                RenderingUtil.draw3DLine(posX, posY, posZ, Colors.getColor(255, 156, 0));
            }
            GlStateManager.popMatrix();
            mc.gameSettings.viewBobbing = bobbing;

        }
        if (event instanceof EventTick) {
            //ChatUtil.printChat(trackedItems.size() + " " + spawnedItems.size() + " " + trackedItems.size());

            trackedItems.removeIf(trackedItem -> !mc.theWorld.getLoadedEntityList().contains(trackedItem));

            for (EntityItem updatedItem : new ArrayList<>(updatedItems)) {
                if (mc.theWorld.getLoadedEntityList().contains(updatedItem)) {
                    ItemStack itemStack = updatedItem.getEntityItem();
                    if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("ExtraAttributes", 10)) {
                        long nonce = -1;
                        NBTTagCompound nbttagcompound = itemStack.getTagCompound().getCompoundTag("ExtraAttributes");
                        if (nbttagcompound.hasKey("Nonce", 3)) {
                            try {
                                nonce = nbttagcompound.getLong("Nonce");
                            } catch (Exception e) {

                            }
                        }

                        List<String> enchantList = HypixelUtil.getPitEnchants(itemStack);

                        String enchant =  enchantList.size() == 0 ? "" : Arrays.toString(enchantList.toArray());

                        Notifications.getManager().post(itemStack.getDisplayName() + " \247rhas dropped! (" + (int) mc.thePlayer.getDistanceToEntity(updatedItem) + "m)", enchant + (nonce > 1000 ? " (" + nonce + ")" : ""), 5000L, Notifications.Type.NOTIFY);
                        System.out.println(enchant);

                        trackedItems.add(updatedItem);
                        updatedItems.remove(updatedItem);
                    }
                } else {
                    updatedItems.remove(updatedItem);
                }
            }

        }
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            if (mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            Packet packet = ep.getPacket();
            if (packet instanceof S1CPacketEntityMetadata) {
                S1CPacketEntityMetadata entityMetadata = (S1CPacketEntityMetadata) packet;
                Entity entity = mc.theWorld.getEntityByID(entityMetadata.getEntityId());
                if (entity instanceof EntityItem) {
                    if (!updatedItems.contains(entity))
                        updatedItems.add((EntityItem) entity);
                }
            }
            if (packet instanceof S02PacketChat) {
                S02PacketChat packetChat = (S02PacketChat) packet;
                String formatted = packetChat.getChatComponent().getFormattedText();
                if (formatted.contains("\247r\247d\247lMINOR EVENT!") && options.getValue("MINOR EVENT")) {
                    try {
                        formatted = formatted.replace("\247r\247d\247lMINOR EVENT! ", "");
                        Notifications.getManager().post("\247r\247d\247lMINOR EVENT!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }

                if (sewers.getValue() && formatted.contains("SEWERS!") && formatted.toLowerCase().contains("spawned")) {
                    try {
                        Notifications.getManager().post("\2473\247lSEWERS!", "A sewers chest has spawned!", 2000L, Notifications.Type.OKAY);
                    } catch (Exception ignored) {
                    }
                }

                if (formatted.contains("\2475\247lMAJOR EVENT!") && options.getValue("MAJOR EVENT")) {
                    try {
                        formatted = formatted.replace("\2475\247lMAJOR EVENT! ", "");
                        Notifications.getManager().post("\2475\247lMAJOR EVENT!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("\2479\247lNIGHT QUEST!") && options.getValue("NIGHT QUEST")) {
                    try {
                        formatted = formatted.replace("\2479\247lNIGHT QUEST! ", "");
                        Notifications.getManager().post("\2479\247lNIGHT QUEST!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("\2476\247lTRADE REQUEST!") && options.getValue("TRADE REQUEST")) {
                    try {
                        formatted = formatted.replace("\2476\247lTRADE REQUEST! ", "");
                        Notifications.getManager().post("\2476\247lTRADE REQUEST!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("\247r\2475\247lDRAGON EGG!") && options.getValue("DRAGON EGG")) {
                    try {
                        formatted = formatted.replace("\247r\2475\247lDRAGON EGG! ", "");
                        Notifications.getManager().post("\247r\2475\247lDRAGON EGG!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }

                if ((Boolean) settings.get("SELFISH").getValue() && formatted.contains(mc.thePlayer.getName())) {
                    if (formatted.contains("\247r\247c\247lSTREAK!") && options.getValue("STREAK")) {
                        try {
                            formatted = formatted.replace("\247r\247c\247lSTREAK! ", "");
                            Notifications.getManager().post("\247r\247c\247lSTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                    if (formatted.contains("\247r\2476\247lBOUNTY!") && options.getValue("BOUNTY")) {
                        try {
                            formatted = formatted.replace("\247r\2476\247lBOUNTY! ", "");
                            Notifications.getManager().post("\247r\2476\247lBOUNTY!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                    if (formatted.contains("\247r\247c\247lMEGASTREAK!") && options.getValue("MEGASTREAK")) {
                        try {
                            formatted = formatted.replace("\247r\247c\247lMEGASTREAK! ", "");
                            Notifications.getManager().post("\247r\247c\247lMEGASTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (!((Boolean) settings.get("SELFISH").getValue())) {
                    if (formatted.contains("\247r\247c\247lSTREAK!") && options.getValue("STREAK")) {
                        try {
                            formatted = formatted.replace("\247r\247c\247lSTREAK! ", "");
                            Notifications.getManager().post("\247r\247c\247lSTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                    if (formatted.contains("\247r\2476\247lBOUNTY!") && options.getValue("BOUNTY")) {
                        try {
                            formatted = formatted.replace("\247r\2476\247lBOUNTY! ", "");
                            Notifications.getManager().post("\247r\2476\247lBOUNTY!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                    if (formatted.contains("\247r\247c\247lMEGASTREAK!") && options.getValue("MEGASTREAK")) {
                        try {
                            formatted = formatted.replace("\247r\247c\247lMEGASTREAK! ", "");
                            Notifications.getManager().post("\247r\247c\247lMEGASTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}

