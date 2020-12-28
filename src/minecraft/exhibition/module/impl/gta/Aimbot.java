package exhibition.module.impl.gta;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.management.PriorityManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.*;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by cool1 on 2/15/2017.
 */
public class Aimbot extends Module {

    private final List<Weapon> weaponList;

    private final HashMap<EntityPlayer, Aimbot.EntityDelta> deltaHashMap = new HashMap<>();
    private final Queue<Packet> packetList = new ConcurrentLinkedQueue<>();

    public int currentBullet;
    public int fireDelay;
    public float currentRecoil;

    public int buffer = 0;
    private Vec3 initialFlame;

    private boolean setSneak = false;

    public static EntityPlayer target;
    private Vec3 aimPos = null;

    private final Setting<Boolean> showHUD = new Setting<>("HUD", false, "Shows some information on your screen.");

    private final Setting<Boolean> silent = new Setting<>("SILENT", true, "Aims silently for you.");
    private final Setting<Boolean> antiSpread = new Setting<>("ANTI-SPREAD", true, "Reduces weapon spread.");
    private final Setting<Boolean> antiRecoil = new Setting<>("ANTI-RECOIL", true, "Reduces weapon recoil.");
    private final Setting<Boolean> autoFire = new Setting<>("AUTO-FIRE", true, "Automatically fires for you.");
    private final Setting<Boolean> showPrediction = new Setting<>("SHOW-PREDICTION", true, "Shows you target prediction.");

    private final Setting<Boolean> noSpread = new Setting<>("NO-SPREAD", false, "Removes weapon spread.");
    private final Setting<Boolean> autoWall = new Setting<>("AUTO-WALL", false, "Automatically detects if you can penetrate walls.");
    private final Setting<Number> penetrate = new Setting<>("PENETRATE", 3, "The amount of walls you will penetrate.", 1, 1, 10);

    private final Setting<Boolean> fakelag = new Setting<>("FAKELAG", false, "Fakes that you're lagging to break prediction.");
    private final Setting<Number> lagTicks = new Setting<>("CHOKE", 3, "The amount of ticks to choke.", 1, 0, 20);
    private final Options fakelagOptions = new Options("Fakelag Mode", "Always", "Always", "On Shoot");

    private final Setting<Number> predictionScale = new Setting<>("PRED SCALE", 1, "Amount of prediction to be applied", 0.05, 0, 2);
    private final Setting<Number> predictionTicks = new Setting<>("PRED TICKS", 2, "Ticks to predict (50 ms latency per tick)", 1, 0, 10);

    private final Setting<Number> pitchAdjust = new Setting<>("PITCH-ADJUST", 0, "Slightly adds/subtracts from aimbot pitch.", 0.01, -1, 1);
    private final Setting<Number> pitchScale = new Setting<>("RECOIL-SCALE", 1, "Pitch recoil scale.", 0.01, 0, 2);
    private final Setting<Number> delay = new Setting<>("DELAY", 0, "Tick delay before firing again. 0 = Auto weapon fire rate delay", 1, 0, 20);
    private final Setting<Number> bufferSize = new Setting<>("BUFFER", 3, "Prediction buffer size. The higher the value the higher the smoothing.", 1, 1, 10);
    private final Setting<Number> fov = new Setting<>("FOV", 90, "FOV check for the Aimbot.", 5, 1, 180);

    private final Setting<Number> pointScale = new Setting<>("POINT-SCALE", 1.0, "The scale of the multipoint hitbox.", 0.01, 0, 1);

    private final Options multipoint = new Options("Multipoint", "Low", "High", "Medium", "Low", "None");

    private final Options hitbox = new Options("Hitbox", "Hitscan Head", "Hitscan", "Hitscan Head", "Head", "Neck", "Chest", "Pelvis", "Leg", "Feet");

    // Multi Point bools
    private final Setting<Boolean> HEAD = new Setting<>("HEAD", true),
            NECK = new Setting<>("NECK", true),
            CHEST = new Setting<>("CHEST", true),
            PELVIS = new Setting<>("PELVIS", false),
            LEG = new Setting<>("LEG", false),
            FOOT = new Setting<>("FOOT", false);

    public Aimbot(ModuleData data) {
        super(data);
        addSetting(antiRecoil);
        addSetting(antiSpread);
        addSetting(autoFire);
        addSetting(silent);
        addSetting(showPrediction);

        if (Boolean.parseBoolean(System.getProperty("nEoSuCKsBruhReallyNeighbor"))) {
            addSetting(noSpread);
            addSetting(autoWall);
            addSetting(penetrate);

            addSetting(fakelag);
            addSetting(lagTicks);
            addSetting(new Setting<>("FAKELAG-MODE", fakelagOptions, "How the Fakelag should behave."));
        }

        addSetting(new Setting<>("MULTIPOINT", multipoint, "How intense Multipoint should scan the hitbox."));
        addSetting(new Setting<>("HITBOX", hitbox, "The hitbox that the aimbot should aim for."));
        addSetting(new Setting<>("POINTS", new MultiBool("Scan Points", HEAD, NECK, CHEST, PELVIS, LEG, FOOT), "Which hitboxes multipoint should scan."));
        addSetting(pointScale);

        addSetting(predictionTicks);
        addSetting(predictionScale);
        addSetting(pitchAdjust);
        addSetting(pitchScale);
        addSetting(pitchAdjust);
        addSetting(bufferSize);
        addSetting(delay);
        addSetting(fov);

        // SMG
        Weapon MP5 = new Weapon(1, 4, 40, 0.4F, 4.2F, 256, 273);
        Weapon P90 = new Weapon(2, 6, 35, 0.7F, 4.5F, 292, 284);

        // Rifles
        Weapon AK47 = new Weapon(3, 9, 30, 1.2F, 6.0F, 291, 293);
        Weapon AUG = new Weapon(2, 7, 30, 1.1F, 5.0F, 286, 294);
        Weapon M4 = new Weapon(2, 7, 30, 1.0F, 4.5F, 258, 279);

        // Snipers
        Weapon _50CAL = new Weapon(19, 19, 10, 0, 0, 261);

        // Shotgun
        Weapon SPAS_12 = new Weapon(12, 21, 10, 0.45F, 2.0F, 271, 275);
        Weapon PUMP = new Weapon(25, 28, 8, 0.5F, 2.0F, 277, 290);

        // Pistols
        Weapon USP = new Weapon(6, 7, 13, 0.33F, 3.0F, 270, 257);
        Weapon HK45 = new Weapon(5, 7, 10, 0.33F, 2.97F, 274, 269);
        Weapon REVOLVER = new Weapon(8, 13, 7, 0.8F, 4.2F, 285, 278);

        this.weaponList = Arrays.asList(MP5, P90, AK47, AUG, M4, _50CAL, SPAS_12, PUMP, USP, HK45, REVOLVER);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    private boolean isInFOV(EntityLivingBase entity) {
        int fov = this.fov.getValue().intValue();
        return Math.round(Math.abs(RotationUtils.getYawChange(entity.posX, entity.posZ))) <= fov;
    }

    @Override
    public void onToggle() {
        this.resetPackets();
        deltaHashMap.clear();
        target = null;
        aimPos = null;
        setSneak = false;
        counter = 0;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null || !HypixelUtil.isVerifiedHypixel()) {
            return;
        }

        if (event instanceof EventRender3D && showPrediction.getValue()) {
            EventRender3D er = event.cast();

            boolean debug = false;

            if (debug) {
                for (Object o : mc.theWorld.getLoadedEntityList()) {
                    if (o instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) o;

                        if (!deltaHashMap.containsKey(player)) {
                            continue;
                        }

                        double[] p = getPrediction(player, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                        GL11.glPushMatrix();
                        RenderingUtil.pre3D();
                        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                        double x = (player.prevPosX + (player.posX - player.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX + p[0];
                        double y = (player.prevPosY + (player.posY - player.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY + p[1];
                        double z = (player.prevPosZ + (player.posZ - player.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ + p[2];
                        GlStateManager.translate(x, y, z);
                        //GlStateManager.rotate(-(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * er.renderPartialTicks), 0, 1, 0);

                        AxisAlignedBB var11 = player.getEntityBoundingBox();

                        double hitboxScale = pointScale.getValue().doubleValue();

                        double hitboxWidth = (var11.maxX - var11.minX) / 2;

                        double centerX = var11.minX + hitboxWidth - player.posX;
                        double centerZ = var11.minZ + hitboxWidth - player.posZ;

                        double neckOffset = 0.25;
                        double chestOffset = 0.55;
                        double pelvis = 0.87;
                        double leg = 1.25;

                        double footPos = var11.minY;

                        double eyePos = footPos + player.getEyeHeight();

                        GL11.glLineWidth(3.5F);

                        {

                            double height = (var11.maxY - eyePos) * hitboxScale;

                            double centerY = eyePos;

                            double topY = centerY - player.posY + height;
                            double bottomY = centerY - player.posY - height;

                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(startX, startY, startZ, endX, endY, endZ);
                                RenderingUtil.glColor(Colors.getColor(255, 255, 255, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            List<Vec3> multipoints = new ArrayList<>();

                            {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(centerX, bruh, centerZ));
                            }

                            if (!multipoint.getSelected().equals("None") && HEAD.getValue()) {
                                double[] yPositions = {startY, endY};

                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(startX + (endX - startX) / 2, bruh, startZ));
                                multipoints.add(new Vec3(startX, bruh, startZ + (endZ - startZ) / 2));
                                multipoints.add(new Vec3(endX - (endX - startX) / 2, bruh, endZ));
                                multipoints.add(new Vec3(endX, bruh, endZ - (endZ - startZ) / 2));

                                for (double yPos : yPositions) {
                                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                                    }

                                    if (multipoint.getSelected().equals("High")) {
                                        multipoints.add(new Vec3(startX, yPos, startZ));
                                        multipoints.add(new Vec3(startX, yPos, endZ));
                                        multipoints.add(new Vec3(endX, yPos, endZ));
                                        multipoints.add(new Vec3(endX, yPos, startZ));
                                    }
                                }

                                for (Vec3 point : multipoints) {
                                    AxisAlignedBB var12 = new AxisAlignedBB(point.getX() - 0.01, point.getY() - 0.01, point.getZ() - 0.01, point.getX() + 0.01, point.getY() + 0.01, point.getZ() + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }
                            }

                        }

                        {
                            double centerY = eyePos - neckOffset;

                            double height = 0.15;

                            double topY = centerY - player.posY + height * hitboxScale;
                            double bottomY = centerY - player.posY - height * hitboxScale;

                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(startX, startY, startZ, endX, endY, endZ);
                                RenderingUtil.glColor(Colors.getColor(41, 255, 41, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            List<Vec3> multipoints = new ArrayList<>();

                            multipoints.add(new Vec3(centerX, centerY - player.posY, centerZ));

                            double[] yPositions = {startY, endY};

                            for (double yPos : yPositions) {
                                multipoints.add(new Vec3(startX, yPos, startZ));
                                multipoints.add(new Vec3(startX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, startZ));

                                if (true) {
                                    multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                                    multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                                    multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                                    multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                                }
                            }

                            if (true) {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(startX + (endX - startX) / 2, bruh, startZ));

                                multipoints.add(new Vec3(startX, bruh, startZ + (endZ - startZ) / 2));

                                multipoints.add(new Vec3(endX - (endX - startX) / 2, bruh, endZ));

                                multipoints.add(new Vec3(endX, bruh, endZ - (endZ - startZ) / 2));
                            }

                            for (Vec3 point : multipoints) {
                                AxisAlignedBB var12 = new AxisAlignedBB(point.getX() - 0.01, point.getY() - 0.01, point.getZ() - 0.01, point.getX() + 0.01, point.getY() + 0.01, point.getZ() + 0.01);
                                RenderingUtil.drawBoundingBox(var12);
                            }
                        }

                        {
                            double centerY = eyePos - chestOffset;

                            double height = 0.15;

                            double topY = centerY - player.posY + height * hitboxScale;
                            double bottomY = centerY - player.posY - height * hitboxScale;
                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(startX, startY, startZ, endX, endY, endZ);
                                RenderingUtil.glColor(Colors.getColor(41, 41, 255, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            List<Vec3> multipoints = new ArrayList<>();

                            {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(centerX, bruh, centerZ));
                            }

                            double[] yPositions = {startY, endY};

                            for (double yPos : yPositions) {
                                multipoints.add(new Vec3(startX, yPos, startZ));
                                multipoints.add(new Vec3(startX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, startZ));

                                if (true) {
                                    multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                                    multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                                    multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                                    multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                                }
                            }

                            if (true) {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(startX + (endX - startX) / 2, bruh, startZ));

                                multipoints.add(new Vec3(startX, bruh, startZ + (endZ - startZ) / 2));

                                multipoints.add(new Vec3(endX - (endX - startX) / 2, bruh, endZ));

                                multipoints.add(new Vec3(endX, bruh, endZ - (endZ - startZ) / 2));
                            }

                            for (Vec3 point : multipoints) {
                                AxisAlignedBB var12 = new AxisAlignedBB(point.getX() - 0.01, point.getY() - 0.01, point.getZ() - 0.01, point.getX() + 0.01, point.getY() + 0.01, point.getZ() + 0.01);
                                RenderingUtil.drawBoundingBox(var12);
                            }

                        }

                        {
                            double centerY = eyePos - pelvis;

                            double height = 0.19;

                            double topY = centerY - player.posY + height * hitboxScale;
                            double bottomY = centerY - player.posY - height * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(centerX - hitboxWidth * hitboxScale, bottomY, centerZ - hitboxWidth * hitboxScale, centerX + hitboxWidth * hitboxScale, topY, centerZ + hitboxWidth * hitboxScale);
                                RenderingUtil.glColor(Colors.getColor(255, 255, 41, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            List<Vec3> multipoints = new ArrayList<>();

                            {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(centerX, bruh, centerZ));
                            }

                            double[] yPositions = {startY, endY};

                            for (double yPos : yPositions) {
                                multipoints.add(new Vec3(startX, yPos, startZ));
                                multipoints.add(new Vec3(startX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, endZ));
                                multipoints.add(new Vec3(endX, yPos, startZ));

                                if (true) {
                                    multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                                    multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                                    multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                                    multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                                }
                            }

                            if (true) {
                                double bruh = centerY - player.posY;

                                multipoints.add(new Vec3(startX + (endX - startX) / 2, bruh, startZ));

                                multipoints.add(new Vec3(startX, bruh, startZ + (endZ - startZ) / 2));

                                multipoints.add(new Vec3(endX - (endX - startX) / 2, bruh, endZ));

                                multipoints.add(new Vec3(endX, bruh, endZ - (endZ - startZ) / 2));
                            }

                            for (Vec3 point : multipoints) {
                                AxisAlignedBB var12 = new AxisAlignedBB(point.getX() - 0.01, point.getY() - 0.01, point.getZ() - 0.01, point.getX() + 0.01, point.getY() + 0.01, point.getZ() + 0.01);
                                RenderingUtil.drawBoundingBox(var12);
                            }
                        }

                        {
                            double centerY = eyePos - leg;

                            double height = 0.15;

                            double topY = centerY - player.posY + height * hitboxScale;
                            double bottomY = centerY - player.posY - height * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(centerX - hitboxWidth * hitboxScale, bottomY, centerZ - hitboxWidth * hitboxScale, centerX + hitboxWidth * hitboxScale, topY, centerZ + hitboxWidth * hitboxScale);
                                RenderingUtil.glColor(Colors.getColor(255, 91, 41, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            {
                                double bruh = centerY - player.posY;

                                AxisAlignedBB var12 = new AxisAlignedBB(centerX - 0.01, bruh - 0.01, centerZ - 0.01, centerX + 0.01, bruh + 0.01, centerZ + 0.01);
                                RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                RenderingUtil.drawBoundingBox(var12);
                            }

                            if (true) {
                                double bruh = centerY - player.posY;

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX + (endX - startX) / 2 - 0.01, bruh - 0.01, startZ - 0.01, startX + (endX - startX) / 2 + 0.01, bruh + 0.01, startZ + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }


                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, bruh - 0.01, startZ + (endZ - startZ) / 2 - 0.01, startX + 0.01, bruh + 0.01, startZ + (endZ - startZ) / 2 + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - (endX - startX) / 2 - 0.01, bruh - 0.01, endZ - 0.01, endX - (endX - startX) / 2 + 0.01, bruh + 0.01, endZ + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }


                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, bruh - 0.01, endZ - (endZ - startZ) / 2 - 0.01, endX + 0.01, bruh + 0.01, endZ - (endZ - startZ) / 2 + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }
                            }

                            double[] yPositions = {startY, endY};

                            for (double yPos : yPositions) {
                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, startZ - 0.01, startX + 0.01, yPos + 0.01, startZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, endZ - 0.01, startX + 0.01, yPos + 0.01, endZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, endZ - 0.01, endX + 0.01, yPos + 0.01, endZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, startZ - 0.01, endX + 0.01, yPos + 0.01, startZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                if (true) {
                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(startX + (endX - startX) / 2 - 0.01, yPos - 0.01, startZ - 0.01, startX + (endX - startX) / 2 + 0.01, yPos + 0.01, startZ + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }


                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, startZ + (endZ - startZ) / 2 - 0.01, startX + 0.01, yPos + 0.01, startZ + (endZ - startZ) / 2 + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }

                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(endX - (endX - startX) / 2 - 0.01, yPos - 0.01, endZ - 0.01, endX - (endX - startX) / 2 + 0.01, yPos + 0.01, endZ + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }


                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, endZ - (endZ - startZ) / 2 - 0.01, endX + 0.01, yPos + 0.01, endZ - (endZ - startZ) / 2 + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(255, 91, 41));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }
                                }
                            }
                        }

                        {
                            double centerY = footPos + 0.15;

                            double height = 0.15;

                            double topY = centerY - player.posY + height * hitboxScale;
                            double bottomY = centerY - player.posY - height * hitboxScale;

                            {
                                AxisAlignedBB var12 = new AxisAlignedBB(centerX - hitboxWidth * hitboxScale, bottomY, centerZ - hitboxWidth * hitboxScale, centerX + hitboxWidth * hitboxScale, topY, centerZ + hitboxWidth * hitboxScale);
                                RenderingUtil.glColor(Colors.getColor(41, 255, 255, 150));
                                RenderingUtil.drawOutlinedBoundingBox(var12);
                            }

                            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

                            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

                            {
                                double bruh = centerY - player.posY;

                                AxisAlignedBB var12 = new AxisAlignedBB(centerX - 0.01, bruh - 0.01, centerZ - 0.01, centerX + 0.01, bruh + 0.01, centerZ + 0.01);
                                RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                RenderingUtil.drawBoundingBox(var12);
                            }

                            if (true) {
                                double bruh = centerY - player.posY;

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX + (endX - startX) / 2 - 0.01, bruh - 0.01, startZ - 0.01, startX + (endX - startX) / 2 + 0.01, bruh + 0.01, startZ + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }


                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, bruh - 0.01, startZ + (endZ - startZ) / 2 - 0.01, startX + 0.01, bruh + 0.01, startZ + (endZ - startZ) / 2 + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - (endX - startX) / 2 - 0.01, bruh - 0.01, endZ - 0.01, endX - (endX - startX) / 2 + 0.01, bruh + 0.01, endZ + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }


                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, bruh - 0.01, endZ - (endZ - startZ) / 2 - 0.01, endX + 0.01, bruh + 0.01, endZ - (endZ - startZ) / 2 + 0.01);
                                    RenderingUtil.drawBoundingBox(var12);
                                }
                            }

                            double[] yPositions = {startY, endY};

                            for (double yPos : yPositions) {
                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, startZ - 0.01, startX + 0.01, yPos + 0.01, startZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, endZ - 0.01, startX + 0.01, yPos + 0.01, endZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, endZ - 0.01, endX + 0.01, yPos + 0.01, endZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                {
                                    AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, startZ - 0.01, endX + 0.01, yPos + 0.01, startZ + 0.01);
                                    RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                    RenderingUtil.drawBoundingBox(var12);
                                }

                                if (true) {
                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(startX + (endX - startX) / 2 - 0.01, yPos - 0.01, startZ - 0.01, startX + (endX - startX) / 2 + 0.01, yPos + 0.01, startZ + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }


                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(startX - 0.01, yPos - 0.01, startZ + (endZ - startZ) / 2 - 0.01, startX + 0.01, yPos + 0.01, startZ + (endZ - startZ) / 2 + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }

                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(endX - (endX - startX) / 2 - 0.01, yPos - 0.01, endZ - 0.01, endX - (endX - startX) / 2 + 0.01, yPos + 0.01, endZ + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }


                                    {
                                        AxisAlignedBB var12 = new AxisAlignedBB(endX - 0.01, yPos - 0.01, endZ - (endZ - startZ) / 2 - 0.01, endX + 0.01, yPos + 0.01, endZ - (endZ - startZ) / 2 + 0.01);
                                        RenderingUtil.glColor(Colors.getColor(41, 255, 255));
                                        RenderingUtil.drawBoundingBox(var12);
                                    }
                                }
                            }
                        }

                        RenderingUtil.post3D();
                        if (!GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                            GL11.glEnable(GL11.GL_LIGHTING);
                        }
                        GL11.glPopMatrix();
                    }
                }
            } else {
                for (Entity o : mc.theWorld.getLoadedEntityList()) {
                    if (o instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) o;

                        if (!deltaHashMap.containsKey(player)) {
                            continue;
                        }

                        double[] p = getPrediction(player, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                        GL11.glPushMatrix();
                        RenderingUtil.pre3D();
                        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                        double x = (player.prevPosX + (player.posX - player.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX + p[0];
                        double y = (player.prevPosY + (player.posY - player.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY + p[1];
                        double z = (player.prevPosZ + (player.posZ - player.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ + p[2];
                        GlStateManager.translate(x, y, z);
                        GlStateManager.rotate(-(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * er.renderPartialTicks), 0, 1, 0);
                        float collisSize = player.getCollisionBorderSize();

                        AxisAlignedBB var11 = player.getEntityBoundingBox().expand(collisSize, collisSize, collisSize);
                        AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - player.posX + 0.2, var11.minY + player.getEyeHeight() - 0.2 - player.posY, var11.minZ - player.posZ + 0.2, var11.maxX - player.posX - 0.2, var11.minY + player.getEyeHeight() + 0.2 - player.posY, var11.maxZ - player.posZ - 0.2);

                        RenderingUtil.glColor(player == target ? Colors.getColor(41, 255, 41, 200) : Colors.getColor(255, 255, 41, 150));
                        RenderingUtil.drawBoundingBox(var12);

                        RenderingUtil.post3D();
                        if (!GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                            GL11.glEnable(GL11.GL_LIGHTING);
                        }
                        GL11.glPopMatrix();
                    }
                }
            }
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                if (mc.thePlayer.inventory.getCurrentItem() != null && fireDelay >= getResetDelay()) {
                    currentBullet = 0;
                }

                initialFlame = null;
                buffer = 0;

                for (Object o : mc.theWorld.getLoadedEntityList()) {
                    if (o instanceof EntityPlayer) {
                        EntityPlayer p = (EntityPlayer) o;
                        if (p != mc.thePlayer && (p.ticksExisted > 5 && !FriendManager.isFriend(p.getName()) && !AntiBot.isBot(p) && !p.isInvisible() && !TeamUtils.isTeam(mc.thePlayer, p))) {
                            double xDelta = p.posX - p.lastTickPosX;
                            double zDelta = p.posZ - p.lastTickPosZ;

                            if (Math.hypot(xDelta, zDelta) < 3) {
                                deltaHashMap.putIfAbsent(p, new Aimbot.EntityDelta(xDelta, zDelta));
                                if (deltaHashMap.containsKey(p)) {
                                    deltaHashMap.get(p).logDeltas(xDelta, zDelta, mc.thePlayer.ticksExisted);
                                }
                            }
                        } else {
                            deltaHashMap.remove(p);
                        }
                    }
                }
                fireDelay++;
            }

            if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().isItemStackDamageable() && !mc.thePlayer.inventory.getCurrentItem().isItemDamaged() && mc.thePlayer.isAllowEdit()) {
                if (em.isPre()) {

                    double targetWeight = Double.NEGATIVE_INFINITY;
                    target = null;
                    aimPos = null;
                    for (Object o : mc.theWorld.getLoadedEntityList()) {
                        if (o instanceof EntityPlayer) {
                            EntityPlayer p = (EntityPlayer) o;
                            double[] simulated = getPrediction(p, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                            Vec3 hitVec = getHitVec(p, simulated);

                            if (p != mc.thePlayer && p.ticksExisted > 5 && !FriendManager.isFriend(p.getName()) && !AntiBot.isBot(p) && !p.isInvisible() && !TeamUtils.isTeam(mc.thePlayer, p) && isInFOV(p) && hitVec != null) {
                                if (target == null) {
                                    aimPos = hitVec;
                                    target = p;
                                    targetWeight = getTargetWeight(p);
                                } else if (getTargetWeight(p) > targetWeight) {
                                    aimPos = hitVec;
                                    target = p;
                                    targetWeight = getTargetWeight(p);
                                }
                            }
                        }
                    }

                    for (Object o : this.deltaHashMap.keySet().toArray()) {
                        EntityPlayer player = (EntityPlayer) o;
                        if (!mc.theWorld.getLoadedEntityList().contains(player)) {
                            this.deltaHashMap.remove(player);
                        }
                    }

                    if (target != null && aimPos != null) {
                        boolean canFire = canFire();

                        boolean recoil = antiRecoil.getValue();
                        if ((!Client.getModuleManager().isEnabled(AntiAim.class)) || canFire) {
                            double xDiff = aimPos.getX() - mc.thePlayer.posX;
                            double yDiff = aimPos.getY() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
                            double zDiff = aimPos.getZ() - mc.thePlayer.posZ;

                            float yaw = RotationUtils.getYawChange(aimPos.getX(), aimPos.getZ());
                            double dist = Math.hypot(xDiff, zDiff);

                            float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);

                            em.setYaw(mc.thePlayer.rotationYaw + yaw);
                            em.setPitch(MathHelper.clamp_float(pitch + pitchAdjust.getValue().floatValue(), -90, 90));

                            if (!em.isOnground() && noSpread.getValue() && canFire)
                                em.setGround(true);

                            if (!silent.getValue()) {
                                mc.thePlayer.rotationYaw = em.getYaw();
                                mc.thePlayer.rotationPitch = em.getPitch();
                            }

                            if (recoil) {
                                currentRecoil = getCurrentRecoil(currentBullet) * pitchScale.getValue().floatValue();
                                em.setPitch(MathHelper.clamp_float(em.getPitch() + currentRecoil, -90, 90));
                            }
                        }

                        if (!mc.thePlayer.isSneaking() && mc.thePlayer.inventory.getCurrentItem() != null && !mc.thePlayer.inventory.getCurrentItem().isItemDamaged()) {
                            int item = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());
                            if (item == 286 || item == 294 || item == 261) {
                                setSneak = true;
                                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                                KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                            }
                        }

                        if (mc.thePlayer.isSneaking() && setSneak && mc.gameSettings.keyBindSneak.getIsKeyPressed()) {
                            if (mc.thePlayer.inventory.getCurrentItem() != null) {
                                int item = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());
                                if (item != 286 && item != 294 && item != 261) {
                                    setSneak = false;
                                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                                    KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                                }
                            } else {
                                setSneak = false;
                                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                                KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                            }
                        }

                    } else {
                        if (mc.thePlayer.isSneaking() && setSneak && mc.gameSettings.keyBindSneak.getIsKeyPressed() && mc.thePlayer.inventory.getCurrentItem() != null) {
                            setSneak = false;
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                            KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                        }

                        if (fireDelay > getFireDelay())
                            currentBullet--;
                        if (currentBullet <= 0) {
                            currentRecoil = 0;
                            currentBullet = 0;
                        }
                    }
                } else {
                    if (target != null && aimPos != null && autoFire.getValue()) {
                        boolean noSneak = false;
                        if (mc.thePlayer.inventory.getCurrentItem() != null) {
                            int item = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());
                            if (item == 286 || item == 294 || item == 261) {
                                noSneak = true;
                                if (!mc.thePlayer.isSneaking()) {
                                    return;
                                }
                            }
                        }

                        boolean canFire = canFire();
                        if (canFire) {
                            boolean nospread = antiSpread.getValue() && !noSneak;
                            if (nospread)
                                NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                            if (mc.thePlayer.inventory.getCurrentItem() != null) {
                                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                            }
                            if (nospread)
                                NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));

                        }
                    }
                }
            } else {
                resetPackets();
                target = null;
                aimPos = null;

                if (mc.thePlayer.isSneaking() && setSneak && mc.gameSettings.keyBindSneak.getIsKeyPressed()) {
                    setSneak = false;
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                }
            }
        }
        if (event instanceof EventPacket) {
            if (mc.thePlayer.isAllowEdit()) {

                EventPacket ep = (EventPacket) event;
                Packet p = ep.getPacket();

                if (p instanceof C03PacketPlayer) {
                    if (counter >= lagTicks.getValue().intValue()) {
                        sendPackets();
                    }

                    if (fakelag.getValue()) {
                        counter++;
                        event.setCancelled(true);
                        packetList.add(p);
                    }
                }

                if (p instanceof S02PacketChat) {
                    S02PacketChat packetChat = (S02PacketChat) p;
                    if (packetChat.getChatComponent().getFormattedText().startsWith("\247r\2476+")) {
                        event.setCancelled(true);
                    }
                    if (packetChat.getChatComponent().getFormattedText().startsWith("\247r\247b+")) {
                        event.setCancelled(true);
                    }
                }

//                if (p instanceof S2APacketParticles) {
//                    S2APacketParticles packetIn = (S2APacketParticles) p;
//                    if (packetIn.getParticleType() == EnumParticleTypes.FLAME) {
//                        if (buffer == 0) {
//                            initialFlame = new Vec3(packetIn.getXCoordinate(), packetIn.getYCoordinate(), packetIn.getZCoordinate());
//                            buffer++;
//                            return;
//                        }
//                        buffer++;
//
//                        if (initialFlame != null && buffer == 3) {
//                            double xDiff = packetIn.getXCoordinate() - initialFlame.xCoord;
//                            double yDiff = packetIn.getYCoordinate() - initialFlame.yCoord;
//                            double zDiff = packetIn.getZCoordinate() - initialFlame.zCoord;
//
//                            double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
//                            float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
//                            float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
//
//                            ChatUtil.debug("\247bRecoil Pitch: " + MathUtils.roundToPlace((mc.thePlayer.rotationPitch - pitch), 3));
//                            ChatUtil.debug("------ " + mc.thePlayer.ticksExisted + " ------");
//                            initialFlame = null;
//                        }
//
//                    }
//                }

                if (p instanceof C08PacketPlayerBlockPlacement) {
                    if (fakelag.getValue()) {
                        initialFlame = null;
                        buffer = 0;
                        if (fakelagOptions.getSelected().equals("On Shoot")) {
                            sendPackets();
                        } else {
                            event.setCancelled(true);
                            packetList.add(p);
                        }
                    }
                    if (mc.thePlayer.inventory.getCurrentItem() != null && canFire()) {
                        currentBullet++;
                        fireDelay = 0;
                    }
                }
                if (p instanceof C09PacketHeldItemChange) {
                    currentBullet = 0;
                    fireDelay = -5;
                }
            }
        }
    }

    private int counter = 0;

    public void sendPackets() {
        counter = 0;
        while (packetList.peek() != null) {
            NetUtil.sendPacketNoEvents(packetList.poll());
        }
        this.resetPackets();
    }

    public void resetPackets() {
        this.counter = 0;
        this.packetList.clear();
    }

    public boolean shouldAntiAim() {
        if (mc.thePlayer == null || mc.thePlayer.inventory.getCurrentItem() == null)
            return false;

        int currentItemID = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());

        for (Weapon weapon : weaponList) {
            if (weapon.isWeapon(currentItemID)) {
                return true;
            }
        }

        return false;
    }

    private Vec3 getHitVec(EntityPlayer player, double[] simulated) {

        AxisAlignedBB var11 = player.getEntityBoundingBox();

        double footPos = var11.minY;

        double hitboxScale = pointScale.getValue().doubleValue();

        double hitboxWidth = (var11.maxX - var11.minX) / 2;

        double eyePos = footPos + player.getEyeHeight() + simulated[1];

        double centerX = var11.minX + hitboxWidth + simulated[0];
        double centerZ = var11.minZ + hitboxWidth + simulated[2];

        double headPos = 0;
        double neckOffset = 0.25;
        double chestOffset = 0.55;
        double pelvis = 0.87;
        double leg = 1.25;

        List<Vec3> multipoints = new ArrayList<>();

        boolean allowHitScan = hitbox.getSelected().contains("Hitscan");

        boolean headFirst = hitbox.getSelected().contains("Head");

        // Head First
        if (headFirst) {
            double centerY = eyePos - headPos;

            double height = (var11.maxY - eyePos) * hitboxScale;

            double topY = centerY + height;
            double bottomY = centerY - height;

            double startX = centerX - hitboxWidth * hitboxScale, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {bottomY, topY};

            if (!multipoint.getSelected().equals("None") && HEAD.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double d0 = point.getX() - centerX;
                double d1 = point.getY() - centerY;
                double d2 = point.getZ() - centerZ;
                double currentDistance = Math.hypot(mc.thePlayer.posX - point.getX(), mc.thePlayer.posZ - point.getZ()) + (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        // No optimal head points, move to chest

        // Chest
        if (hitbox.getSelected().equals("Neck") || allowHitScan) {
            double centerY = eyePos - neckOffset;

            double height = 0.15;

            double topY = centerY + height * hitboxScale;
            double bottomY = centerY - height * hitboxScale;

            double startX = centerX - hitboxWidth * hitboxScale, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {bottomY, topY};

            if (!multipoint.getSelected().equals("None") && NECK.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        // Head First
        if (allowHitScan) {
            double centerY = eyePos - headPos;

            double height = (var11.maxY - eyePos) * hitboxScale;

            double topY = centerY + height;
            double bottomY = centerY - height;

            double startX = centerX - hitboxWidth * hitboxScale;
            double startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {bottomY, topY};

            if (!multipoint.getSelected().equals("None") && HEAD.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        if (hitbox.getSelected().equals("Chest") || allowHitScan) {
            double centerY = eyePos - chestOffset;

            double height = 0.15;

            double topY = centerY + height * hitboxScale;
            double startX = centerX - hitboxWidth * hitboxScale, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {centerY - player.posY - height * hitboxScale, endY};

            if (!multipoint.getSelected().equals("None") && CHEST.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        if (hitbox.getSelected().equals("Pelvis") || allowHitScan) {
            double centerY = eyePos - pelvis;

            double height = 0.19;

            double topY = centerY + height * hitboxScale;
            double bottomY = centerY - height * hitboxScale;

            double startX = centerX - hitboxWidth * hitboxScale, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {bottomY, topY};

            if (!multipoint.getSelected().equals("None") && PELVIS.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        if (hitbox.getSelected().equals("Leg") || allowHitScan) {
            double centerY = eyePos - leg;

            double height = 0.15;

            double topY = centerY + height * hitboxScale;
            double bottomY = centerY - height * hitboxScale;

            double startX = centerX - hitboxWidth * hitboxScale, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {bottomY, topY};

            if (!multipoint.getSelected().equals("None") && LEG.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            if (closest != null)
                return closest;
        }

        if (hitbox.getSelected().equals("Foot") || allowHitScan) {
            double centerY = footPos + 0.15;

            double height = 0.15;

            double topY = centerY + height * hitboxScale;
            double bottomY = centerY - height * hitboxScale;

            double startX = centerX - hitboxWidth * hitboxScale, startY = bottomY, startZ = centerZ - hitboxWidth * hitboxScale;

            double endX = centerX + hitboxWidth * hitboxScale, endY = topY, endZ = centerZ + hitboxWidth * hitboxScale;

            multipoints.add(new Vec3(centerX, centerY, centerZ));

            double[] yPositions = {startY, endY};

            if (!multipoint.getSelected().equals("None") && FOOT.getValue()) {
                multipoints.add(new Vec3(startX + (endX - startX) / 2, centerY, startZ));
                multipoints.add(new Vec3(startX, centerY, startZ + (endZ - startZ) / 2));
                multipoints.add(new Vec3(endX - (endX - startX) / 2, centerY, endZ));
                multipoints.add(new Vec3(endX, centerY, endZ - (endZ - startZ) / 2));

                for (double yPos : yPositions) {
                    if (multipoint.getSelected().equals("Medium") || multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX + (endX - startX) / 2, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, startZ + (endZ - startZ) / 2));
                        multipoints.add(new Vec3(endX - (endX - startX) / 2, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ - (endZ - startZ) / 2));
                    }

                    if (multipoint.getSelected().equals("High")) {
                        multipoints.add(new Vec3(startX, yPos, startZ));
                        multipoints.add(new Vec3(startX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, endZ));
                        multipoints.add(new Vec3(endX, yPos, startZ));
                    }
                }
            }

            Vec3 closest = null;
            double distance = Double.POSITIVE_INFINITY;

            for (Vec3 point : multipoints) {
                double currentDistance = mc.thePlayer.getDistance(point.getX(), point.getY(), point.getZ());
                if (currentDistance < distance) {
                    if (canBeSeen(point)) {
                        distance = currentDistance;
                        closest = point;
                    }
                }
            }

            return closest;
        }


        return null;
    }

    private boolean canBeSeen(Vec3 point) {
        return autoWall.getValue() ? mc.theWorld.rayTraceBlocksAutoWall(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), point, penetrate.getValue().intValue()) == null :
                mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), point) == null;
    }

    private int getMagSize() {
        int currentItemID = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());

        for (Weapon weapon : weaponList) {
            if (weapon.isWeapon(currentItemID)) {
                return weapon.getMagazineSize();
            }
        }

        return 50;
    }

    private float getCurrentRecoil(int currentBullet) {
        int currentItemID = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());

        for (Weapon weapon : weaponList) {
            if (weapon.isWeapon(currentItemID)) {
                return Math.min(currentBullet * weapon.getRecoilIncrement(), weapon.getRecoilMaximum());
            }
        }

        return Math.min(currentBullet * 1.2F, 6.0F);
    }

    private int getResetDelay() {
        int currentItemID = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());

        for (Weapon weapon : weaponList) {
            if (weapon.isWeapon(currentItemID)) {
                return weapon.getRecoverTicks();
            }
        }

        return 15;
    }

    private int getFireDelay() {
        int currentItemID = Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());

        for (Weapon weapon : weaponList) {
            if (weapon.isWeapon(currentItemID)) {
                return weapon.getTickDelay();
            }
        }

        return delay.getValue().intValue();
    }

    private boolean canFire() {
        return currentBullet < getMagSize() && fireDelay >= getFireDelay() && mc.thePlayer.inventory.getCurrentItem().isItemStackDamageable() && !mc.thePlayer.inventory.getCurrentItem().isItemDamaged();
    }

    public double getTargetWeight(EntityPlayer p) {
        if (Killaura.vip == p || PriorityManager.isPriority(p)) {
            return Double.POSITIVE_INFINITY;
        }

        double weight = -mc.thePlayer.getDistanceToEntity(p);
        if ((p.lastTickPosX == p.posX) && (p.lastTickPosY == p.posY) && (p.lastTickPosZ == p.posZ)) {
            weight += 200.0D;
        }
        weight -= p.getDistanceToEntity(mc.thePlayer) / 5.0F;
        return weight;
    }

    private final double[] ZERO = new double[]{0, 0, 0};

    private double[] getPrediction(EntityPlayer player, int ticks, double scale) {
        if (!deltaHashMap.containsKey(player) || (player.lastTickPosX == player.posX && player.lastTickPosZ == player.posZ)) {
            return ZERO;
        }

        Aimbot.EntityDelta delta = deltaHashMap.get(player);
        double[] weightedDeltas = delta.getWeightedDeltas();
        double yDelta = (player.posY - player.lastTickPosY);
        if (MathUtils.roundToPlace(yDelta, 1) != 0 || !player.onGround) {
            yDelta -= 0.08D;
            yDelta *= 0.9800000190734863D;
        }

        if (yDelta >= 0.5) {
            yDelta = 0;
        }

        double lastMotionY = yDelta;

        double currentPos = 0;

        double finalX = 0, finalY = 0, finalZ = 0;

        for (int i = 0; i < ticks; i++) {
            double motionX = (weightedDeltas[0] * (i));
            double motionZ = (weightedDeltas[1] * (i));

            double motionY = lastMotionY;

            AxisAlignedBB playerBoundingBox = player.getEntityBoundingBox();
            AxisAlignedBB tempBoundingBox = new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.maxX, playerBoundingBox.maxY, playerBoundingBox.maxZ).offset(motionX, currentPos, motionZ);

            final List<AxisAlignedBB> var15 = mc.theWorld.getCollidingBoundingBoxes(player, tempBoundingBox.addCoord(0, motionY, 0));
            for (final AxisAlignedBB var18 : var15) {
                motionY = var18.calculateYOffset(tempBoundingBox, motionY);
            }

            currentPos += motionY;

            finalX = motionX;
            finalY = motionY;
            finalZ = motionZ;

            if (player.isOnLadder()) {
                if (yDelta > 0) {
                    motionY = 0.15F;
                } else if (yDelta < 0) {
                    motionY = -0.15F;
                }
            } else {
                motionY -= 0.08D;
                motionY *= 0.9800000190734863D;
            }

            lastMotionY = motionY;
        }

        return new double[]{finalX * scale, finalY, finalZ * scale};
    }

    private class EntityDelta {
        private final ArrayBlockingQueue<double[]> deltas = new ArrayBlockingQueue<>(10);
        private int lastUpdatedTick;
        private float headingYaw = 0F;

        private EntityDelta(double initialDeltaX, double initialDeltaY) {
            deltas.add(new double[]{initialDeltaX, initialDeltaY});
        }

        private void logDeltas(double deltaX, double deltaY, int currentTick) {
            if (Math.hypot(deltaX, deltaY) > 1) {
                deltas.clear();
                return;
            }

            if (Math.hypot(deltaX, deltaY) == 0) {
                deltas.clear();
                return;
            }

            int tickDelay = (currentTick - lastUpdatedTick);

            if (currentTick - lastUpdatedTick > 2) {
                deltas.clear();
            }

            while (deltas.remainingCapacity() == 0 || deltas.size() > bufferSize.getValue().intValue()) {
                deltas.remove();
            }

            float newHeading = RotationUtils.getYawChangeGiven(deltaX, deltaY, headingYaw);
            headingYaw += newHeading;

            if (newHeading >= 15) {
                while (deltas.size() >= 2) {
                    deltas.remove();
                }
            }

            if (deltaX != 0 && deltaY != 0)
                lastUpdatedTick = currentTick;

            deltas.add(new double[]{deltaX / tickDelay, deltaY / tickDelay});
        }

        public double[] getWeightedDeltas() {
            int denominator = deltas.size();
            double deltaX = 0, deltaY = 0;
            for (double[] deltas : deltas) {
                deltaX += deltas[0];
                deltaY += deltas[1];
            }
            return new double[]{deltaX / denominator, deltaY / denominator};
        }
    }

    private static class Weapon {

        private final int tickDelay;

        private final int recoverTicks;
        private final int magazineSize;
        private final float recoilIncrement;
        private final float recoilMaximum;

        private final List<Integer> items;

        public Weapon(int tickDelay, int recoverTicks, int magazineSize, float recoilIncrement, float recoilMaximum, Integer... items) {
            this.tickDelay = tickDelay;
            this.recoverTicks = recoverTicks;
            this.magazineSize = magazineSize;
            this.recoilIncrement = recoilIncrement;
            this.recoilMaximum = recoilMaximum;

            this.items = Arrays.asList(items);
        }

        public int getTickDelay() {
            return tickDelay;
        }

        public int getRecoverTicks() {
            return recoverTicks;
        }

        public int getMagazineSize() {
            return magazineSize;
        }

        public float getRecoilIncrement() {
            return recoilIncrement;
        }

        public float getRecoilMaximum() {
            return recoilMaximum;
        }

        public boolean isWeapon(int itemID) {
            return this.items.contains(itemID);
        }

    }
}
