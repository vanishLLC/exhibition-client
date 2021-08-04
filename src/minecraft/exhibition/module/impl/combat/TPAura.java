package exhibition.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.management.PriorityManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.packet.PacketDispatcher;
import exhibition.pathfinding.Node;
import exhibition.pathfinding.TeleportResult;
import exhibition.pathfinding.Utils;
import exhibition.util.NetUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.Timer;

import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

public class TPAura extends Module {
    Timer timer = new Timer();
    private static EntityLivingBase en = null;
    private boolean attack = false;
    private boolean isReady = false;

    ArrayList<Vec3> positions = new ArrayList<Vec3>();
    ArrayList<Vec3> positionsBack = new ArrayList<Vec3>();
    ArrayList<Node> triedPaths = new ArrayList<Node>();

    public static final double maxXZTP = 9.5;
    public static final int maxYTP = 9;
    private final PacketDispatcher packetDispatcher = new PacketDispatcher();
    private final Setting<Number> attacksPerSecond = new Setting<>("APS", 10, "Attacks per second.", 1, 1, 20);

    public TPAura(ModuleData data) {
        super(data);
        packetDispatcher.start();
    }


    @Override
    public void onToggle() {
        en = null;
        this.attack = false;
        this.isReady = false;
    }

    private List<EntityLivingBase> getClosestEntities(float range) {
        List<EntityLivingBase> targets = new ArrayList<>();
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) o;
                if (validEntity(entity)) {
                    if (entity instanceof EntityPlayer && PriorityManager.isPriority((EntityPlayer) entity)) {
                        targets.add(entity);
                    }
                }
            }
        }
        return targets;
    }

    private EntityLivingBase getClosestEntity(float range) {
        List<EntityLivingBase> targets = getClosestEntities(range);
        return targets.size() == 0 ? null : targets.get(0);
    }

    public boolean validEntity(EntityLivingBase entity) {
        if ((mc.thePlayer.getHealth() > 0) && (entity.getHealth() > 0 && !entity.isDead && entity.deathTime <= 0) || Float.isNaN(entity.getHealth())) {
            if (entity instanceof EntityPlayer) {
                if (AntiBot.isBot(entity) || entity.isPlayerSleeping())
                    return false;
                return !(entity.isInvisible()) && (!FriendManager.isFriend(entity.getName()));
            }
        }
        return false;
    }

    public void onUpdate() {

    }

    public void updateStages() {

//        if (currentMode.equals("Improved")) {
        List<EntityLivingBase> list = getClosestEntities((float) 500F);
        list.sort(new Comparator<EntityLivingBase>() {
            public int compare(EntityLivingBase o1, EntityLivingBase o2) {
                if (mc.thePlayer.getDistanceToEntity(o1) > mc.thePlayer.getDistanceToEntity(o2)) {
                    return 1;
                }
                if (mc.thePlayer.getDistanceToEntity(o1) < mc.thePlayer.getDistanceToEntity(o2)) {
                    return -1;
                }
                if (mc.thePlayer.getDistanceToEntity(o1) == mc.thePlayer.getDistanceToEntity(o2)) {
                    return 0;
                }
                return 0;
            }
        });
        int i = 0;
        for (EntityLivingBase en : list) {
//            if (i >= ClientSettings.TpAuramaxTargets) {
//                break;
//            }

//				Jigsaw.chatMessage(en.posX);
//				Jigsaw.chatMessage(en.posY);
//				Jigsaw.chatMessage(en.posZ);
//				System.out.println(en.posX + "," + en.posY + "," + en.posZ);
            TeleportResult result = Utils.pathFinderTeleportTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(en.posX, en.posY, en.posZ));

            boolean weirdBruh = false;

            if(!result.foundPath && positions.size() > 0) {
                TeleportResult testResult = Utils.pathFinderTeleportTo(positions.get(positions.size() - 1), new Vec3(en.posX, en.posY, en.posZ));

                if(testResult.foundPath) {
                    positions.addAll(testResult.positions);
                    result = testResult;
                    result.positions = positions;
                    weirdBruh = true;
                }
            }

            if(!weirdBruh) {
                positions.clear();
                positionsBack.clear();
                triedPaths.clear();
                triedPaths = result.triedPaths;

                if (!result.foundPath) {
                    continue;
                }
            }


            List<Packet> packetList = new ArrayList<>();
            for (Vec3 position : result.positions) {
                packetList.add(new C03PacketPlayer.C04PacketPlayerPosition(position.getX(), position.getY(), position.getZ(), false));
            }

            //mc.thePlayer.swingItemFake();
            //packetList.add(new C0APacketAnimation());
            //Criticals.disable = true;
            //Criticals.crit(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord);
            packetList.add(new C02PacketUseEntity(en, Action.ATTACK));
            //Criticals.disable = false;

            positions = result.positions;

            TeleportResult resultBack = Utils.pathFinderTeleportBack(positions);

            positionsBack = resultBack.positionsBack;

            for (Vec3 position : resultBack.positions) {
                packetList.add(new C03PacketPlayer.C04PacketPlayerPosition(position.getX(), position.getY(), position.getZ(), false));
            }

            for (Packet packet : packetList) {
                NetUtil.sendPacket(packet);
            }

            //PacketGroup packetGroup = new PacketGroup(packetList);

            //packetDispatcher.sendGroup(packetGroup);

            i++;
        }
//        }
//        if (currentMode.equals("Old")) {
//            positions.clear();
//            positionsBack.clear();
//            int targets = 0;
//            List<EntityLivingBase> list = getClosestEntities((float) 500F);
//            list.sort(new Comparator<EntityLivingBase>() {
//                public int compare(EntityLivingBase o1, EntityLivingBase o2) {
//                    if (mc.thePlayer.getDistanceToEntity(o1) > mc.thePlayer.getDistanceToEntity(o2)) {
//                        return 1;
//                    }
//                    if (mc.thePlayer.getDistanceToEntity(o1) < mc.thePlayer.getDistanceToEntity(o2)) {
//                        return -1;
//                    }
//                    if (mc.thePlayer.getDistanceToEntity(o1) == mc.thePlayer.getDistanceToEntity(o2)) {
//                        return 0;
//                    }
//                    return 0;
//                }
//            });
//            for (EntityLivingBase en : list) {
//                AuraUtils.targets.add(en);
//                boolean up = false;
//                positions.clear();
//                positionsBack.clear();
//                this.en = en;
//                doReach(mc.thePlayer.getDistanceToEntity(this.en), up, list);
//                stage = 0;
//                targets++;
//                if (targets >= ClientSettings.TpAuramaxTargets) {
//                    // Jigsaw.chatMessage(targets);
//                    break;
//                }
//            }
//        }


    }

//    @Override
//    public void onLateUpdate() {
//
//    }
//
//    @Override
//    public void onRender() {
//
////		 GL11.glPushMatrix();
////		 GL11.glEnable(GL11.GL_BLEND);
////		 GL11.glEnable(GL11.GL_LINE_SMOOTH);
////		 GL11.glDisable(GL11.GL_TEXTURE_2D);
////		 GL11.glDisable(GL11.GL_DEPTH_TEST);
////		 GL11.glBlendFunc(770, 771);
////		 GL11.glEnable(GL11.GL_BLEND);
////		 RenderTools.lineWidth(2);
////		 RenderTools.color4f(0.3f, 1f, 0.3f, 1f);
////		 RenderTools.glBegin(3);
////		 int i = 0;
////		 for (Vec3 vec : positions) {
////		 RenderTools.putVertex3d(RenderTools.getRenderPos(vec.xCoord,
////		 vec.yCoord, vec.zCoord));
////		 i++;
////		 }
////		 RenderTools.glEnd();
////		 RenderTools.color4f(0.3f, 0.3f, 1f, 1f);
////		 RenderTools.glBegin(3);
////		 i = 0;
////		 for (Vec3 vec : positionsBack) {
////		 RenderTools.putVertex3d(RenderTools.getRenderPos(vec.xCoord,
////		 vec.yCoord, vec.zCoord));
////		 i++;
////		 }
////		 RenderTools.glEnd();
////		 GL11.glDisable(GL11.GL_BLEND);
////		 GL11.glEnable(GL11.GL_TEXTURE_2D);
////		 GL11.glDisable(GL11.GL_LINE_SMOOTH);
////		 GL11.glDisable(GL11.GL_BLEND);
////		 GL11.glEnable(GL11.GL_DEPTH_TEST);
////		 GL11.glPopMatrix();
////		 RenderTools.lineWidth(3);
////		 for (Vec3 vec : positions) {
////		 drawESP(1f, 0.3f, 0.3f, 1f, vec.xCoord, vec.yCoord, vec.zCoord);
////		 }
////		 RenderTools.lineWidth(1.5f);
////		 for (Vec3 vec : positionsBack) {
////		 drawESP(0.3f, 0.3f, 1f, 1f, vec.xCoord, vec.yCoord, vec.zCoord);
////		 }
//        super.onRender();
//    }

    public void drawESP(float red, float green, float blue, float alpha, double x, double y, double z) {
        double xPos = x - mc.getRenderManager().renderPosX;
        double yPos = y - mc.getRenderManager().renderPosY;
        double zPos = z - mc.getRenderManager().renderPosZ;
//        RenderTools.drawOutlinedEntityESP(xPos, yPos, zPos, mc.thePlayer.width / 2, mc.thePlayer.height, red, green,
//                blue, alpha);
    }

    @Override
    public void onDisable() {
        if(isSneaking) {
            NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }
    }

    public boolean isSneaking = false;

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class})
    public void onEvent(Event event) {

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();

            if (!isReady)
                if (em.isPre()) {
                    if (mc.thePlayer.isRiding()) {
                        NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        isSneaking = true;
                        isReady = true;
                        timer.setDifference(-1000);
                    }
                    return;
                }

            if (em.isPre()) {
                if(isSneaking) {
                    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                    isSneaking = false;
                } else {
                    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                    isSneaking = true;
                }

                if (!timer.delay(2000)) {
                    return;
                }
                en = getClosestEntity(500);
                if (en == null) {
                    return;
                }
                updateStages();
                if(positions.size() != 0) {
                    em.setCancelled(true);
                    mc.thePlayer.posY += 0.5;
                }
                timer.reset();
            } else {
                if (!attack) {
                    return;
                }
                attack = false;
            }
        } else {
            if (event instanceof EventRender3D) {
                for (Vec3 position : positions) {
                    double x = (position.getX()) - RenderManager.renderPosX;
                    double y = (position.getY()) - RenderManager.renderPosY;
                    double z = (position.getZ()) - RenderManager.renderPosZ;

                    GlStateManager.pushMatrix();
                    RenderingUtil.pre3D();
                    GlStateManager.translate(x, y, z);
                    AxisAlignedBB var12 = new AxisAlignedBB(-0.15, -0.01, -0.15, 0.15, 0.01, 0.15);
                    RenderingUtil.glColor(Colors.getColor(255, 60, 60));
                    GL11.glLineWidth(1);
                    RenderingUtil.drawBoundingBox(var12);
                    RenderingUtil.post3D();
                    GlStateManager.popMatrix();
                }
                RenderingUtil.pre3D();
                RenderingUtil.glColor(Colors.getColor(0,255,0,150));
                GL11.glLineWidth(5);
                GL11.glBegin(GL_LINE_STRIP);
                for (Vec3 position : positions) {
                    double x = (position.getX()) - RenderManager.renderPosX;
                    double y = (position.getY()) - RenderManager.renderPosY;
                    double z = (position.getZ()) - RenderManager.renderPosZ;
                    GL11.glVertex3d(x, y, z);
                }
                GL11.glEnd();
                RenderingUtil.post3D();
            }
        }
    }
}
