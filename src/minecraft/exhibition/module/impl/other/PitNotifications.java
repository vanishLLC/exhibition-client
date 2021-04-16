/**
 * Time: 8:22:36 PM
 * Date: Jan 5, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import com.google.common.collect.Lists;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.font.TTFFontRenderer;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.render.ESP2D;
import exhibition.module.impl.render.Nametags;
import exhibition.module.impl.render.Tags;
import exhibition.module.impl.render.TargetESP;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.TeamUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class PitNotifications extends Module {

    private final MultiBool options;

    private final List<Entity> bruhList = Lists.<Entity>newArrayList();

    private final Setting<Boolean> sewers = new Setting<>("SEWER CHEST", true);

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
    @RegisterEvent(events = {EventPacket.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D er = (EventRender3D) event;
            final boolean bobbing = mc.gameSettings.viewBobbing;
            GL11.glLoadIdentity();
            mc.gameSettings.viewBobbing = false;
            mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);

            for (Entity ent : mc.theWorld.getLoadedEntityList()) {
                if (!(ent instanceof EntityItem && options.getValue("MYSTIC DROP")))
                    continue;
                try {
                    if (ent instanceof EntityItem) {
                        ItemStack itemStack = ((EntityItem) ent).getEntityItem();
                        if (itemStack.getTagCompound().hasKey("ExtraAttributes", 10)){
                            if (!bruhList.isEmpty()){
                                if (bruhList.contains(ent)){
                                    float posX = (float) ((float) ent.getPosition().getX() + 0.5 - RenderManager.renderPosX);
                                    float posY = (float) ((float) ent.getPosition().getY() + 0.5 - RenderManager.renderPosY);
                                    float posZ = (float) ((float) ent.getPosition().getZ() + 0.5 - RenderManager.renderPosZ);
                                    RenderingUtil.draw3DLine(posX, posY, posZ, Colors.getColor(255, 156, 0));
                                    return;
                                }
                            }
                            String enchant = HypixelUtil.getPitEnchants(itemStack).toString().replaceAll("\\[([^\\]]+)\\]", "[$1\247r]");

                            double ex = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * mc.timer.renderPartialTicks;
                            double ey = ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * mc.timer.renderPartialTicks;
                            double ez = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * mc.timer.renderPartialTicks;

                            double px = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks;
                            double py = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks;
                            double pz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks;

                            double d0 = px - ex;
                            double d1 = py - ey;
                            double d2 = pz - ez;

                            double distance = MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

                            Notifications.getManager().post(itemStack.getDisplayName() + " \247rhas dropped!"  + " (" + ((int) distance) + "m)", enchant, 5000L, Notifications.Type.NOTIFY);
                            System.out.println(HypixelUtil.getPitEnchants(itemStack).toString());

                            bruhList.add(ent);
                        }
                    }
                } catch (Exception e){

                }
                mc.gameSettings.viewBobbing = bobbing;
            }
        }
        if (event instanceof EventPacket){
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S02PacketChat) {
                S02PacketChat packetChat = (S02PacketChat) packet;
                String formatted = packetChat.getChatComponent().getFormattedText();
                if (mc.thePlayer == null || mc.theWorld == null) {
                    return;
                }
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

