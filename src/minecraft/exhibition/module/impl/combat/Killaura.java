package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventStep;
import exhibition.management.PriorityManager;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.*;
import exhibition.module.impl.player.Scaffold;
import exhibition.module.impl.render.TargetESP;
import exhibition.ncp.Angle;
import exhibition.ncp.Direction;
import exhibition.ncp.Location;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Killaura extends Module {
    private String AUTOBLOCK = /*AUTOBLOCK*/decodeByteArray(new byte[]{65, 85, 84, 79, 66, 76, 79, 67, 75});
    private String RANGE = /*RANGE*/decodeByteArray(new byte[]{82, 65, 78, 71, 69});
    private String ANGLESTEP = /*ANGLESTEP*/decodeByteArray(new byte[]{65, 78, 71, 76, 69, 83, 84, 69, 80});
    private String TICK = /*EXISTED*/decodeByteArray(new byte[]{69, 88, 73, 83, 84, 69, 68});
    private String MAX = /*MXAXAPS*/decodeByteArray(new byte[]{77, 88, 65, 88, 65, 80, 83});
    private String MIN = /*MINAPS*/decodeByteArray(new byte[]{77, 73, 78, 65, 80, 83});
    private String SWITCH = /*SWITCH*/decodeByteArray(new byte[]{83, 87, 73, 84, 67, 72});
    private String DEATH = /*DEATH*/decodeByteArray(new byte[]{68, 69, 65, 84, 72});
    private String TARGETMODE = /*PRIORITY*/decodeByteArray(new byte[]{80, 82, 73, 79, 82, 73, 84, 89});
    private String FOVCHECK = /*FOV*/decodeByteArray(new byte[]{70, 79, 86});
    private String RAYTRACE = /*RAYTRACE*/decodeByteArray(new byte[]{82, 65, 89, 84, 82, 65, 67, 69});
    private String TARGETING = /*TARGETING*/decodeByteArray(new byte[]{84, 65, 82, 71, 69, 84, 73, 78, 71});
    private Timer delay = new Timer();
    private Timer fakeSwingTimer = new Timer();

    private Timer deathTimer = new Timer();
    private Timer switchTimer = new Timer();
    private Timer blockTimer = new Timer();

    private Timer angleTimer = new Timer();

    private Setting<Boolean> pitSpawn = new Setting<>("PIT-SPAWN", true, "Disables Killaura when in PIT spawn.");
    private Setting<Boolean> antiLag = new Setting<>("ANTI-LAG", true, "Prevents the Killaura from flagging you when lagging.");
    private Setting<Boolean> reduce = new Setting<>("REDUCE", false, "Reduces your rotations to prevent flags.");
    private Setting<Boolean> antiCritFunky = new Setting<>("ANTI-CF", false, "Attacks players without proccing Critically Funky.");
    private Setting<Boolean> prediction = new Setting<>("PREDICTION", true, "Predicts where the player will be on server side.");
    private Setting<Number> predictionTicks = new Setting<>("PTICKS", 1, "The amount of ticks to predict. 1 tick = 50ms", 1, 1, 10);
    private Setting<Number> predictionScale = new Setting<>("PSCALE", 1.0, "The scale of how much prediction is applied.", 0.05, 0, 2);
    private Setting<Number> maxTargets = new Setting<>("SWITCH-SIZE", 4, "The maximum amount of targets to switch through.", 1, 1, 10);

    private Options attackMode = new Options("Attack Mode", "Smart", "Precise", "Smart", "Always");

    private final HashMap<EntityLivingBase, EntityDelta> deltaHashMap = new HashMap<>();

    private int index;
    public static int setupTick;
    private boolean isCritSetup;
    private int nextRandom = -1;
    public static boolean blockJump;
    public static boolean wantedToJump;
    public Vector2f lastAngles = new Vector2f(0, 0);
    private Setting blockRange = new Setting<>(/*BLOCK-RANGE*/decodeByteArray(new byte[]{66, 76, 79, 67, 75, 45, 82, 65, 78, 71, 69}), 4.5, /*Range for killaura.*/decodeByteArray(new byte[]{82, 97, 110, 103, 101, 32, 102, 111, 114, 32, 107, 105, 108, 108, 97, 117, 114, 97, 46}), 0.1, 1, 10);

    private static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

    public Killaura(ModuleData data) {
        super(data);
        settings.put(FOVCHECK, new Setting<>(FOVCHECK, 180, /*Targets must be in FOV.*/decodeByteArray(new byte[]{84, 97, 114, 103, 101, 116, 115, 32, 109, 117, 115, 116, 32, 98, 101, 32, 105, 110, 32, 70, 79, 86, 46}), 15, 45, 180));
        settings.put(TICK, new Setting<>(TICK, 50, /*Existed ticks before attacking.*/decodeByteArray(new byte[]{69, 120, 105, 115, 116, 101, 100, 32, 116, 105, 99, 107, 115, 32, 98, 101, 102, 111, 114, 101, 32, 97, 116, 116, 97, 99, 107, 105, 110, 103, 46}), 5, 1, 120));
        settings.put(AUTOBLOCK, new Setting<>(AUTOBLOCK, true, /*Automatically blocks for you.*/decodeByteArray(new byte[]{65, 117, 116, 111, 109, 97, 116, 105, 99, 97, 108, 108, 121, 32, 98, 108, 111, 99, 107, 115, 32, 102, 111, 114, 32, 121, 111, 117, 46})));
        settings.put(RANGE, new Setting<>(RANGE, 4.5, /*Range for killaura.*/decodeByteArray(new byte[]{82, 97, 110, 103, 101, 32, 102, 111, 114, 32, 107, 105, 108, 108, 97, 117, 114, 97, 46}), 0.1, 1, 7));
        settings.put(/*BLOCK-RANGE*/decodeByteArray(new byte[]{66, 76, 79, 67, 75, 45, 82, 65, 78, 71, 69}), blockRange);

        settings.put(MIN, new Setting<>(MIN, 5, /*Minimum APS.*/decodeByteArray(new byte[]{77, 105, 110, 105, 109, 117, 109, 32, 65, 80, 83, 46}), 1, 1, 20));
        settings.put(MAX, new Setting<>(MAX, 15, /*Maximum APS.*/decodeByteArray(new byte[]{77, 97, 120, 105, 109, 117, 109, 32, 65, 80, 83, 46}), 1, 1, 20));
        settings.put(ANGLESTEP, new Setting<>(ANGLESTEP, 180, "The amount of degrees KillAura can step per tick.", 5, 0, 180));
        settings.put(DEATH, new Setting<>(DEATH, true, /*Disables killaura when you die.*/decodeByteArray(new byte[]{68, 105, 115, 97, 98, 108, 101, 115, 32, 107, 105, 108, 108, 97, 117, 114, 97, 32, 119, 104, 101, 110, 32, 121, 111, 117, 32, 100, 105, 101, 46})));
        settings.put(RAYTRACE, new Setting<>(RAYTRACE, true, /*Visible check for target.*/decodeByteArray(new byte[]{86, 105, 115, 105, 98, 108, 101, 32, 99, 104, 101, 99, 107, 32, 102, 111, 114, 32, 116, 97, 114, 103, 101, 116, 46})));
        settings.put(TARGETMODE, new Setting<>(TARGETMODE, new Options(/*Priority*/decodeByteArray(new byte[]{80, 114, 105, 111, 114, 105, 116, 121}),
                /*Angle*/decodeByteArray(new byte[]{65, 110, 103, 108, 101}),
                /*Angle*/decodeByteArray(new byte[]{65, 110, 103, 108, 101}),
                /*Range*/decodeByteArray(new byte[]{82, 97, 110, 103, 101}),
                /*FOV*/decodeByteArray(new byte[]{70, 79, 86}),
                /*Armor*/decodeByteArray(new byte[]{65, 114, 109, 111, 114}),
                /*Health*/decodeByteArray(new byte[]{72, 101, 97, 108, 116, 104}),
                /*Bounty*/decodeByteArray(new byte[]{66, 111, 117, 110, 116, 121}),
                "Health Vamp",
                "Bounty Vamp"),
                /*Target mode priority.*/decodeByteArray(new byte[]{84, 97, 114, 103, 101, 116, 32, 109, 111, 100, 101, 32, 112, 114, 105, 111, 114, 105, 116, 121, 46})));
        settings.put(/*PARTICLES*/decodeByteArray(new byte[]{80, 65, 82, 84, 73, 67, 76, 69, 83}), new Setting<>(/*PARTICLES*/decodeByteArray(new byte[]{80, 65, 82, 84, 73, 67, 76, 69, 83}), false, /*Render enchant particles.*/decodeByteArray(new byte[]{82, 101, 110, 100, 101, 114, 32, 101, 110, 99, 104, 97, 110, 116, 32, 112, 97, 114, 116, 105, 99, 108, 101, 115, 46})));
        settings.put(/*STEPCOMPAT*/decodeByteArray(new byte[]{83, 84, 69, 80, 67, 79, 77, 80, 65, 84}), new Setting<>(/*STEPCOMPAT*/decodeByteArray(new byte[]{83, 84, 69, 80, 67, 79, 77, 80, 65, 84}), true, /*Adds extra compatability when stepping up blocks with Criticals.*/decodeByteArray(new byte[]{65, 100, 100, 115, 32, 101, 120, 116, 114, 97, 32, 99, 111, 109, 112, 97, 116, 97, 98, 105, 108, 105, 116, 121, 32, 119, 104, 101, 110, 32, 115, 116, 101, 112, 112, 105, 110, 103, 32, 117, 112, 32, 98, 108, 111, 99, 107, 115, 32, 119, 105, 116, 104, 32, 67, 114, 105, 116, 105, 99, 97, 108, 115, 46})));

        settings.put(SWITCH, new Setting<>(SWITCH, 300, /*Switch speed delay.*/decodeByteArray(new byte[]{83, 119, 105, 116, 99, 104, 32, 115, 112, 101, 101, 100, 32, 100, 101, 108, 97, 121, 46}), 50, 50, 1000));
        Setting[] ents = new Setting[]{
                new Setting<>(/*PLAYERS*/decodeByteArray(new byte[]{80, 76, 65, 89, 69, 82, 83}), true, /*Attack players.*/decodeByteArray(new byte[]{65, 116, 116, 97, 99, 107, 32, 112, 108, 97, 121, 101, 114, 115, 46})),
                new Setting<>(/*MOBS*/decodeByteArray(new byte[]{77, 79, 66, 83}), false, /*Attack mobs.*/decodeByteArray(new byte[]{65, 116, 116, 97, 99, 107, 32, 109, 111, 98, 115, 46})),
                new Setting<>(/*PASSIVE*/decodeByteArray(new byte[]{80, 65, 83, 83, 73, 86, 69}), false, /*Attack passive.*/decodeByteArray(new byte[]{65, 116, 116, 97, 99, 107, 32, 112, 97, 115, 115, 105, 118, 101, 46})),
                new Setting<>(/*VILLAGERS*/decodeByteArray(new byte[]{86, 73, 76, 76, 65, 71, 69, 82, 83}), false, /*Attack villagers.*/decodeByteArray(new byte[]{65, 116, 116, 97, 99, 107, 32, 118, 105, 108, 108, 97, 103, 101, 114, 115, 46})),
                new Setting<>(/*GOLEMS*/decodeByteArray(new byte[]{71, 79, 76, 69, 77, 83}), false, /*Attack villagers.*/""),
                new Setting<>(/*INVISIBLES*/decodeByteArray(new byte[]{73, 78, 86, 73, 83, 73, 66, 76, 69, 83}), false, /*Attack invisible.*/decodeByteArray(new byte[]{65, 116, 116, 97, 99, 107, 32, 105, 110, 118, 105, 115, 105, 98, 108, 101, 46})),
                new Setting<>(/*TEAMS*/decodeByteArray(new byte[]{84, 69, 65, 77, 83}), false, /*Check if player is not on your team.*/decodeByteArray(new byte[]{67, 104, 101, 99, 107, 32, 105, 102, 32, 112, 108, 97, 121, 101, 114, 32, 105, 115, 32, 110, 111, 116, 32, 111, 110, 32, 121, 111, 117, 114, 32, 116, 101, 97, 109, 46})),
                new Setting<>(/*ARMOR*/decodeByteArray(new byte[]{65, 82, 77, 79, 82}), true, /*Check if player has armor equipped.*/decodeByteArray(new byte[]{67, 104, 101, 99, 107, 32, 105, 102, 32, 112, 108, 97, 121, 101, 114, 32, 104, 97, 115, 32, 97, 114, 109, 111, 114, 32, 101, 113, 117, 105, 112, 112, 101, 100, 46})),
                new Setting<>("FRIENDS", false, "Attack friends.")};
        settings.put(TARGETING, new Setting<>(TARGETING, new MultiBool(/*Target Filter*/decodeByteArray(new byte[]{84, 97, 114, 103, 101, 116, 32, 70, 105, 108, 116, 101, 114}), ents), /*Properties the aura will target.*/decodeByteArray(new byte[]{80, 114, 111, 112, 101, 114, 116, 105, 101, 115, 32, 116, 104, 101, 32, 97, 117, 114, 97, 32, 119, 105, 108, 108, 32, 116, 97, 114, 103, 101, 116, 46})));
        settings.put(/*NOSWING*/decodeByteArray(new byte[]{78, 79, 83, 87, 73, 78, 71}), noswing);
        settings.put(indicator.getName(), indicator);
        addSetting(reduce);
        addSetting(prediction);
        addSetting(predictionTicks);
        addSetting(predictionScale);
        addSetting(maxTargets);
        addSetting(antiLag);
        addSetting(pitSpawn);
        addSetting(antiCritFunky);
        addSetting(new Setting<>("ATTACK-MODE", attackMode, "Customizes the Killaura attack mode."));
    }

    private Setting noswing = new Setting<>(/*NOSWING*/decodeByteArray(new byte[]{78, 79, 83, 87, 73, 78, 71}), false, /*Blocks swinging server sided.*/decodeByteArray(new byte[]{66, 108, 111, 99, 107, 115, 32, 115, 119, 105, 110, 103, 105, 110, 103, 32, 115, 101, 114, 118, 101, 114, 32, 115, 105, 100, 101, 100, 46}));

    private Setting indicator = new Setting<>("INDICATOR", false, "Renders an indicator on target.");

    private double randomNumber(double max, double min) {
        return (Math.random() * (max - min)) + min;
    }

    private double randomInt(int max, int min) {
        return (Math.random() * (max - min)) + min;
    }

    public List<EntityLivingBase> loaded = new CopyOnWriteArrayList<>();
    public boolean isBlocking;
    public EntityLivingBase target;
    public EntityLivingBase vip;
    public boolean shouldToggle;
    static boolean allowCrits;
    public static boolean wantsToStep;

    public static EntityLivingBase getTarget() {
        Killaura killaura = Client.getModuleManager().get(Killaura.class).cast();
        return killaura.isEnabled() ? killaura.target : null;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            lastAngles.x = mc.thePlayer.rotationYaw;
            lastAngles.y = mc.thePlayer.rotationPitch;
        }
        deltaHashMap.clear();
        disabled = false;
        allowCrits = true;
        isBlocking = false;
        isCritSetup = false;
        blockJump = false;
        wantsToStep = false;
        wantedToJump = false;
        wait = -1;
        stepDelay = -2;
        setupTick = 0;

    }

    @Override
    public void onEnable() {
        deltaHashMap.clear();
        disabled = false;
        allowCrits = true;
        if (mc.thePlayer != null) {
            lastAngles.x = mc.thePlayer.rotationYaw;
            lastAngles.y = mc.thePlayer.rotationPitch;
        }
        disabled = false;
        isBlocking = false;
        isCritSetup = false;
        blockJump = false;
        wantsToStep = false;
        wantedToJump = false;
        wait = 0;
        stepDelay = -2;
        setupTick = 0;
    }

    @Override
    public void onToggle() {
        shouldToggle = false;
        critWaitTicks = 0;
        try {
            target = null;
            loaded.clear();
        } catch (Exception ignored) {

        }
    }

    private boolean disabled;

    public static boolean pre;
    private int wait;
    public static int stepDelay;

    private int critWaitTicks;


    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventStep.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        Criticals critModule = (Criticals) Client.getModuleManager().get(Criticals.class);
        int maxTargets = this.maxTargets.getValue().intValue();

        if (event instanceof EventPacket) {
            EventPacket e = event.cast();
            Packet packet = e.getPacket();
            boolean attacking = target != null && (boolean) getSetting("AUTOBLOCK").getValue();
            if (attacking) {
//                if (packet instanceof S2FPacketSetSlot) {
//                    try {
//                        S2FPacketSetSlot packetSetSlot = (S2FPacketSetSlot) packet;
//                        if ((packetSetSlot.getSlotID() == 36 + mc.thePlayer.inventory.currentItem)) {
//                            //wait = 1;
//                        }
//                    } catch (Exception ignored) {
//                    }
//                }
                if (packet instanceof S08PacketPlayerPosLook) {
                    critWaitTicks = critModule.isPacket() ? 15 : 6;
                    setupTick = 0;
                }

//                if(packet instanceof C07PacketPlayerDigging) {
//                    ChatUtil.debug("Unblocked " + mc.thePlayer.ticksExisted + " " + isBlocking);
//                }
//                if(packet instanceof C08PacketPlayerBlockPlacement) {
//                    ChatUtil.debug("Blocked " + mc.thePlayer.ticksExisted + " " + isBlocking);
//                }

                if (packet instanceof C01PacketChatMessage) {
                    C01PacketChatMessage chatMessage = (C01PacketChatMessage) packet;
                    if (chatMessage.getMessage().contains("/spawn")) {
                        ChatUtil.printChat("Spawn " + chatMessage.getMessage());
                    }
                }

                if (packet instanceof S45PacketTitle && (boolean) settings.get(DEATH).getValue()) {
                    S45PacketTitle titlePacket = ((S45PacketTitle) packet);
                    if (titlePacket.getType().equals(S45PacketTitle.Type.TITLE)) {
                        String text = StringUtils.stripControlCodes(titlePacket.getMessage().getUnformattedText());
                        if ((text.contains("DIED") || text.contains("GAME OVER")) && isEnabled()) {
                            shouldToggle = true;
                            Notifications.getManager().post("Aura Death", "Aura disabled due to death.");
                        }
                    }
                }
            }
            return;
        }
        if (event instanceof EventStep) {
            EventStep step = event.cast();
            boolean crits = (Client.getModuleManager().isEnabled(Criticals.class) && critWaitTicks <= 0) && (!Client.getModuleManager().isEnabled(Speed.class) && !Client.getModuleManager().isEnabled(Fly.class) && !Client.getModuleManager().isEnabled(LongJump.class));
            if (step.isPre() && crits && stepDelay > 0) {
                critWaitTicks = critModule.isPacket() ? 2 : 3;
                Killaura.wantsToStep = true;
                event.setCancelled(true);
                step.setActive(false);
            }
            if (!step.isPre()) {
                Killaura.wantsToStep = false;
                critWaitTicks = 2;
                stepDelay = 0;
            }
            return;
        }
        if (event instanceof EventRender3D) {
            if ((boolean) indicator.getValue()) {
                EventRender3D er = event.cast();
                GL11.glPushMatrix();
                RenderingUtil.pre3D();
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                try {
                    Iterator<EntityLivingBase> loadedIter = loaded.iterator();
                    while (loadedIter.hasNext()) {
                        GlStateManager.pushMatrix();
                        EntityLivingBase target = loadedIter.next();
                        double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionTicks.getValue().doubleValue());

                        double x = (target.prevPosX + (target.posX - target.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX + p[0];
                        double y = (target.prevPosY + (target.posY - target.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY + p[1];
                        double z = (target.prevPosZ + (target.posZ - target.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ + p[2];
                        GlStateManager.translate(x, y, z);
                        GlStateManager.rotate(-(target.prevRotationYawHead + (target.rotationYawHead - target.prevRotationYawHead) * er.renderPartialTicks), 0, 1, 0);
                        float collisSize = target.getCollisionBorderSize();

                        AxisAlignedBB var11 = target.getEntityBoundingBox().expand(collisSize, collisSize, collisSize);
                        AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - target.posX, var11.minY - target.posY, var11.minZ - target.posZ, var11.maxX - target.posX, var11.maxY - target.posY, var11.maxZ - target.posZ);
                        RenderingUtil.glColor(target != this.target ? Colors.getColor(255, 45) : target.hurtTime > 0 ? Colors.getColorOpacity(1186128252, 75) : Colors.getColor(255, 41, 41, 75));
                        RenderingUtil.drawBoundingBox(var12);
                        RenderingUtil.glColor(target != this.target ? Colors.getColor(255, 150) : target.hurtTime > 0 ? Colors.getColorOpacity(-5054084, 200) : Colors.getColor(255, 41, 41, 200));
                        GL11.glLineWidth(1);
                        RenderingUtil.drawOutlinedBoundingBox(var12);

                        GL11.glPopMatrix();
                    }
                } catch (Exception ignored) {

                }

                RenderingUtil.post3D();
                GL11.glPopMatrix();

            }
            return;
        }

        setSuffix(maxTargets == 1 ? "Single" : "Switch");
        allowCrits = false;
        int min = ((Number) settings.get(MIN).getValue()).intValue();
        int max = ((Number) settings.get(MAX).getValue()).intValue();
        if (min > max) {
            settings.get(MIN).setValue(max);
        }
        if (((Number) settings.get(/*BLOCK-RANGE*/decodeByteArray(new byte[]{66, 76, 79, 67, 75, 45, 82, 65, 78, 71, 69})).getValue()).floatValue() < ((Number) settings.get(RANGE).getValue()).floatValue()) {
            settings.get(/*BLOCK-RANGE*/decodeByteArray(new byte[]{66, 76, 79, 67, 75, 45, 82, 65, 78, 71, 69})).setValue(settings.get(RANGE).getValue());
        }
        if (nextRandom == -1)
            nextRandom = (int) Math.round((20 / randomInt(min, max)));

        boolean block = (Boolean) settings.get(AUTOBLOCK).getValue();
        EventMotionUpdate em = event.cast();
        if (em.isPre()) {
            if (shouldToggle) {
                if (isBlocking) {
                    isBlocking = false;
                    NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                toggle();
                return;
            }

            tickEntities();
            if (stepDelay > 0) {
                stepDelay--;
            }
            if (critWaitTicks > 0)
                critWaitTicks--;
            if (wantedToJump && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
                if (blockJump)
                    mc.thePlayer.jump();
                wantedToJump = false;
            }
            if ((Boolean) settings.get(DEATH).getValue()) {
                if (!mc.thePlayer.isEntityAlive() && !disabled) {
                    target = null;
                    loaded.clear();
                    toggle();
                    deathTimer.reset();
                    Notifications.getManager().post(/*Aura Death*/decodeByteArray(new byte[]{65, 117, 114, 97, 32, 68, 101, 97, 116, 104}), /*Aura disabled due to death.*/decodeByteArray(new byte[]{65, 117, 114, 97, 32, 100, 105, 115, 97, 98, 108, 101, 100, 32, 100, 117, 101, 32, 116, 111, 32, 100, 101, 97, 116, 104, 46}));
                    return;
                }
            }
            if (blockJump)
                blockJump = false;
            if (isBlocking && (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword) || AutoSoup.isHealing)) {
                isBlocking = false;
            }
        }
        Scaffold scaffold = (Scaffold) Client.getModuleManager().get(Scaffold.class);
        LongJump longjump = (LongJump) Client.getModuleManager().get(LongJump.class);
        boolean disable = false;
        if ((AutoPot.potting || AutoPot.haltTicks > 0) || scaffold.isEnabled() || scaffold.isPlacing() || longjump.allowAttack() || longjump.isBruhing()) {
            disable = true;
        }

        boolean ignorePit = HypixelUtil.isInGame("THE HYPIXEL PIT") && pitSpawn.getValue();

        if (ignorePit) {
            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY;
            double z = mc.thePlayer.posZ;
            if (y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
                disable = true;
            }
        }

        float range = ((Number) settings.get(RANGE).getValue()).floatValue();
        boolean crits = (critModule.isEnabled() && critWaitTicks <= 0) &&
                ((!Client.getModuleManager().isEnabled(Speed.class) || (mc.thePlayer.onGround && !PlayerUtil.isMoving())) && !Client.getModuleManager().isEnabled(Fly.class) && !Client.getModuleManager().isEnabled(LongJump.class)) &&
                !(Client.getModuleManager().isEnabled(Jesus.class) && PlayerUtil.isOnLiquid());
        String attack = attackMode.getSelected();
        if (true) {
            boolean single = maxTargets == 1;
            if (single) {
                index = 0;
            }

            if (em.isPre()) {
                // We load the targets each tick
                loaded = getTargets();
                if (index >= loaded.size()) {
                    index = 0;
                }

                if (!mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
                    stepDelay = 1;
                }

                if (stepDelay < 0) {
                    wantsToStep = false;
                }

                if (loaded.size() > 0) {
                    sortList();
                    for (EntityLivingBase e : loaded) {
                        double xDelta = e.posX - e.lastTickPosX;
                        double zDelta = e.posZ - e.lastTickPosZ;

                        if (Math.hypot(xDelta, zDelta) < 3) {
                            if (deltaHashMap.containsKey(e)) {
                                deltaHashMap.get(e).logDeltas(xDelta, zDelta, mc.thePlayer.ticksExisted);
                            } else {
                                deltaHashMap.put(e, new EntityDelta(xDelta, zDelta));
                            }
                        }
                    }

                    // Set the target each switch delay
                    if (switchTimer.delay(((Number) settings.get(SWITCH).getValue()).intValue()) && !single) {
                        EntityLivingBase lastTarget = target;
                        incrementIndex();
                        target = loaded.get(Math.min(loaded.size() - 1, index));

                        if (lastTarget == target && loaded.size() > 1) { // We're attacking the same entity, switch to the next
                            incrementIndex();
                            target = loaded.get(Math.min(loaded.size() - 1, index));
                        }

                        switchTimer.reset();
                    }

                    if (target == null || single) {
                        target = loaded.get(single ? 0 : Math.min(loaded.size() - 1, index));
                    }

                    if (target != null) {
                        if (!validEntity(target)) {
                            loaded = getTargets();
                            sortList();
                            if (loaded.size() > 0) {
                                target = loaded.get(0);
                                switchTimer.reset();
                            } else {
                                if (!isBlocking)
                                    blockTimer.reset();
                                lastAngles.x = mc.thePlayer.rotationYaw;
                                lastAngles.y = mc.thePlayer.rotationPitch;
                                target = null;
                                deltaHashMap.clear();
                                wait = 0;
                                setupTick = 0;
                            }
                        }
                    }

                    if (target != null) {
                        // If the target is invalid, choose another target
                        if (!disable) {
                            double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionTicks.getValue().doubleValue());
                            double xDiff = (target.posX + p[0]) - mc.thePlayer.posX;
                            double yDiff = (target.posY + p[1]) - mc.thePlayer.posY - 1;
                            double zDiff = (target.posZ + p[2]) - mc.thePlayer.posZ;

                            double yDifference = Math.abs((target.posY + p[2]) - mc.thePlayer.posY);

                            double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);

                            double distance = MathUtils.roundToPlace(dist, 1);

                            boolean shouldReduce = reduce.getValue() && distance <= 3;

                            float targetYaw = MathHelper.clamp_float(RotationUtils.getYawChangeGiven(target.posX, target.posZ, lastAngles.x), -180, 180);
                            int maxAngleStep = reduce.getValue() ? 180 : ((Number) settings.get(ANGLESTEP).getValue()).intValue();

                            if (maxAngleStep > 5 && maxAngleStep < 175) {
                                maxAngleStep += randomNumber(5, -5);
                            }

                            if (targetYaw > maxAngleStep) targetYaw = maxAngleStep;
                            else if (targetYaw < -maxAngleStep) targetYaw = -maxAngleStep;

                            Bypass bypass = Client.getModuleManager().get(Bypass.class);

                            int bypassTicks = bypass.bruh - 10;

                            boolean allowInvalidAngles = bypass.allowBypassing() && (bypass.option.getSelected().equals("Watchdog Off") || (bypass.option.getSelected().equals("Dong") ?
                                    bypassTicks > 5 && bypassTicks <= (40 + bypass.randomDelay) : bypass.bruh > 10 && bypass.bruh % 100 > 10 && bypass.bruh % 100 < 99)) && HypixelUtil.isVerifiedHypixel();

                            if (shouldReduce) {
                                float pitch = (float) -(Math.atan2(yDiff - (distance > 2.1 ? 0.75 : 1), dist) * 180.0D / 3.141592653589793D);
                                float newYaw = 0F;

                                if (distance <= (HypixelUtil.isInGame("DUEL") ? 1.12 : 0.75) && yDifference <= 1.5) {
                                    em.setYaw(lastAngles.x);
                                    em.setPitch(MathHelper.clamp_float(88.9F + (float) (0.5F * Math.random()), -89.5F, 89.5F));
                                } else {
                                    em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -89.5F, 89.5F));

                                    if (lastAngles.y > 90 || lastAngles.y < -90) {
                                        //em.setPitch(180 - em.getPitch());
                                    }

                                    Vec3 v = getDirection(lastAngles.x, em.getPitch());
                                    double off = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), v,
                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                                    Vec3 backwardsBruh = getDirection(lastAngles.x, 180 - em.getPitch());
                                    double backwardsOff = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), backwardsBruh,
                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                                    if (allowInvalidAngles && em.getPitch() >= 0 && off >= 0.1 && backwardsOff < 0.1) {
                                        boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
                                        boolean canAttackRightNow = (attack.equals("Always")) ||
                                                (attack.equals("Precise") ? target.waitTicks <= 0 :
                                                        target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                                        if (isAttacking && canAttackRightNow) {
                                            em.setPitch(MathHelper.wrapAngleTo180_float(180 - em.getPitch()));
                                        } else {
                                            em.setPitch(em.getPitch() + 360);
                                        }
                                    } else {

                                        float tempNewYaw = (float) MathUtils.getIncremental(lastAngles.x + (targetYaw / 1.1F), 20);

                                        boolean willViolate = target.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tempNewYaw, 0), target);

                                        if ((angleTimer.roundDelay(1000) && off >= 0.11 && !willViolate) || (angleTimer.roundDelay(200) && !willViolate && off >= 0.2)) {
                                            newYaw += targetYaw;

                                            float normalDiff = Math.abs(newYaw);
                                            float backwardsDiff = Math.abs(MathHelper.wrapAngleTo180_float(newYaw + 180));

                                            Vec3 vecReverse = getDirection((float) MathUtils.getIncremental(lastAngles.x + MathHelper.wrapAngleTo180_float((newYaw + 180)), 30), 180 - em.getPitch());
                                            double newOffReverse = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), vecReverse,
                                                    target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                                                    HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                                            if (allowInvalidAngles && em.getPitch() >= 0 && backwardsDiff < normalDiff && newOffReverse < 0.1 && normalDiff > 90) {
                                                angleTimer.reset();
                                                em.setYaw((float) MathUtils.getIncremental(lastAngles.x += MathHelper.wrapAngleTo180_float((newYaw + 180)), 20));
                                                em.setPitch(180 - em.getPitch());
                                            } else {
                                                angleTimer.reset();
                                                em.setYaw((float) MathUtils.getIncremental(lastAngles.x += (newYaw), 20));
                                            }
                                        }
                                    }

                                    em.setYaw(lastAngles.x);

                                }
                            } else {
                                // Allow reduced to still have your heads backwards
                                if (reduce.getValue() && allowInvalidAngles) {
                                    float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
                                    em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -90, 90));

                                    float normalDiff = Math.abs(targetYaw);
                                    float backwardsDiff = Math.abs(MathHelper.wrapAngleTo180_float(targetYaw + 180));

                                    Vec3 vecReverse = getDirection((float) MathUtils.getIncremental(lastAngles.x + MathHelper.wrapAngleTo180_float((targetYaw + 180)), 30), 180 - em.getPitch());
                                    double newOffReverse = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), vecReverse,
                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                                    if (backwardsDiff < normalDiff && newOffReverse < 0.1 && normalDiff > 120) {
                                        em.setYaw(lastAngles.x += MathHelper.wrapAngleTo180_float((targetYaw + 180)) / 1.1F);

                                        boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
                                        boolean canAttackRightNow = (attack.equals("Always")) ||
                                                (attack.equals("Precise") ? target.waitTicks <= 0 :
                                                        target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                                        // Only headsnap when attacking to reduce others figuring this out
                                        em.setPitch(MathHelper.wrapAngleTo180_float(180 - em.getPitch()));
                                    } else {
                                        angleTimer.reset();
                                        em.setYaw((lastAngles.x += targetYaw / 1.1F));
                                    }
                                } else {
                                    float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);

                                    em.setYaw((lastAngles.x += targetYaw / 1.1F));
                                    em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -90, 90));
                                }
                            }

                            lastAngles.y = em.getPitch();

                            boolean setupCrits = critModule.isOldCrits() || target.hurtTime <= 1 || (target.waitTicks <= 1);

                            boolean badCrits = allowInvalidAngles && antiCritFunky.getValue() && shouldntCrit(target);

                            if (!badCrits) {
                                if (crits) {
                                    if (critModule.isNewCrits()) {
                                        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && isNextTickGround()) {
                                            if (setupTick == 0 && setupCrits) {
                                                stepDelay = 1;
                                                blockJump = true;
                                                isCritSetup = false;
                                                em.setY(em.getY() + 0.123259982345);
                                                em.setGround(true);
                                                em.setForcePos(true);
                                                setupTick = 1;
                                                //ChatUtil.printChat(target.waitTicks + " stage 1");
                                            } else if (setupTick == 1 || setupTick == 4 || setupTick == 8) {
                                                if ((em.getY() == mc.thePlayer.posY && em.isOnground())) {
                                                    blockJump = true;
                                                    isCritSetup = true;
                                                    em.setY(em.getY() + 0x1.cb5c6eba0ceabp-8);
                                                    em.setGround(false);
                                                    em.setForcePos(true);
                                                    setupTick = 2;

                                                    //ChatUtil.printChat(target.waitTicks + " stage 2");
                                                }
                                            } else if (setupTick == 2 || setupTick == 5 || setupTick == 9) {
                                                if ((em.getY() == mc.thePlayer.posY && em.isOnground())) {
                                                    isCritSetup = false;
                                                    em.setForcePos(true);
                                                    //ChatUtil.printChat("2");
                                                    setupTick = 0;
                                                }
                                            }
                                            if (setupTick == 6)
                                                setupTick++;
                                            if (setupTick == 10)
                                                setupTick = 0;
                                        } else {
                                            setupTick = 0;
                                        }
                                    } else if (critModule.isOldCrits()) {
                                        boolean canAttackRightNow = (attack.equals("Always")) ||
                                                (attack.equals("Precise") ? target.waitTicks <= 1 :
                                                        target.waitTicks <= 1 || (target.hurtResistantTime <= 11 && target.hurtResistantTime >= 6) || target.hurtTime > 6);

                                        if (canAttackRightNow && isNextTickGround() && !Client.instance.isLagging()) {
                                            if (setupCrits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                                if (setupTick == 0) {
                                                    stepDelay = 2;
                                                    blockJump = true;
                                                    em.setY(em.getY() + 0.07234F + (0.0000023F) * Math.random());
                                                    em.setGround(false);
                                                    em.setForcePos(true);
                                                    isCritSetup = false;
                                                    setupTick = 1;
                                                } else if (setupTick == 1) {
                                                    isCritSetup = true;
                                                    if (HypixelUtil.isInGame("HYPIXEL PIT"))
                                                        em.setY(em.getY() + 0.0076092939542 - (0.0000000002475776F) * Math.random());
                                                    em.setGround(false);
                                                    em.setForcePos(true);
                                                    setupTick = 0;
                                                } else {
                                                    setupTick = 0;
                                                }
                                            } else {
                                                setupTick = 0;
                                            }
                                        } else {
                                            setupTick = 0;
                                        }
                                    } else if (critModule.isPacket() && HypixelUtil.isVerifiedHypixel() && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                                        boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
                                        boolean canAttackRightNow = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                                        if (isAttacking && canAttackRightNow && isNextTickGround())
                                            if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                                stepDelay = 2;
                                                blockJump = true;
                                                em.setY(em.getY() + 0.07840000152587834 + (0.0000023423F) * Math.random());
                                                em.setGround(false);
                                                em.setForcePos(true);
                                                isCritSetup = true;
                                            }
                                    }
                                } else {
                                    setupTick = 0;
                                }
                            } else {
                                isCritSetup = true;
                            }

                            if (antiCritFunky.getValue() && isCritFunky(target) && !em.isOnground() && !mc.thePlayer.onGround && allowInvalidAngles) {
                                boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
                                boolean canAttackRightNow = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                                if (isAttacking && canAttackRightNow) {
                                    isCritSetup = true;
                                    bypass.bruh -= 1;
                                    em.setGround(true);
                                }
                            }

                            if ((block) && (mc.thePlayer.inventory.getCurrentItem() != null) && ((mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword))) {
                                mc.thePlayer.setItemInUse(mc.thePlayer.getCurrentEquippedItem(), 0x11938);
                            }
                        } else {
                            if (AutoPot.haltTicks < 0) {
                                lastAngles.x = em.getYaw();
                                lastAngles.y = em.getPitch();
                            }/* else {
                                em.setYaw(lastAngles.x);
                            }*/
                        }
                    }
                } else {
                    if (!isBlocking)
                        blockTimer.reset();
                    lastAngles.x = mc.thePlayer.rotationYaw;
                    lastAngles.y = mc.thePlayer.rotationPitch;
                    deltaHashMap.clear();
                    target = null;
                    wait = 0;
                    stepDelay = 0;
                    setupTick = 0;
                    wantsToStep = false;
                }

                boolean packetMode = Client.getModuleManager().isEnabled(NoSlowdown.class);
                if (mc.thePlayer.isBlocking() && isBlocking && packetMode && PlayerUtil.isMoving() && mc.thePlayer.ticksExisted % 2 == 0 && !AutoSoup.isHealing) {
                    isBlocking = false;
                    NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    blockTimer.reset();
                }
            } else if (em.isPost() && (loaded.size() > 0) && (loaded.get(Math.min(loaded.size() - 1, index)) != null) && target != null && !disable) {

                boolean alwaysCrit = (!Client.getModuleManager().isEnabled(LongJump.class) && !Client.getModuleManager().isEnabled(Fly.class) && (boolean) critModule.getSetting("ALWAYS-CRIT").getValue());

                boolean canCrit = (mc.thePlayer.fallDistance > 0.0625F && !mc.thePlayer.onGround);

                boolean isCriticalAttack = canCrit || isCritSetup;

                boolean isOptimalAttack = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : (target.hurtTime <= 5));

                boolean twoTickCritsGood = !mc.thePlayer.onGround || (!PlayerUtil.isOnLiquid() && (attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 1 :
                        (target.waitTicks <= 1 || (target.hurtResistantTime <= 11 && target.hurtResistantTime >= 6) || target.hurtTime > 6))) && isCritSetup);

                boolean threeTickCritsGood = !mc.thePlayer.onGround || (!PlayerUtil.isOnLiquid() && (isOptimalAttack || target.waitTicks <= 1) && (isCritSetup));

                boolean criticalsAreSet = !crits || ((critModule.isNewCrits() ? threeTickCritsGood : critModule.isOldCrits() ? twoTickCritsGood : critModule.isPacket()));

                boolean shouldAttack = alwaysCrit ? isCriticalAttack : criticalsAreSet;

                boolean setupCrits = critModule.isOldCrits() || (target.hurtTime <= 0 && target.waitTicks >= 6) || (target.waitTicks <= 0);

                double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionTicks.getValue().doubleValue());

                double distance = mc.thePlayer.getDistance(target.posX + p[0], target.posY + p[1], target.posZ + p[2]);

                boolean isAttacking = distance <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);

                boolean canAttackRightNow = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                if (isAttacking && shouldAttack && isBlocking && canAttackRightNow && !AutoSoup.isHealing) {
                    isBlocking = false;
                    NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    blockTimer.reset();
                }

                if (isAttacking && !isBlocking && (!antiLag.getValue() || !Client.instance.isLagging())) {
                    Vec3 v = getDirection(em.getYaw(), em.getPitch());
                    double off = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), v, target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                            HypixelUtil.isInGame("DUEL") ? 1.85 :
                                    HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                    if (((Number) settings.get(ANGLESTEP).getValue()).intValue() == 0 || (off <= 0.11 || (off <= 1 && off >= 0.22 && MathUtils.getIncremental(angleTimer.getDifference(), 50) < 100))) {

                        if (crits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && critModule.isPacket() && setupCrits && isCritSetup) {
                            if (HypixelUtil.isVerifiedHypixel() && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                                NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0076092939542 - (0.0000000002475776F) * Math.random(), mc.thePlayer.posZ, false));
                            } else {
                                Criticals.doCrits();
                            }
                        }

                        if (canAttackRightNow && shouldAttack) {
                            if (!Client.instance.is1_9orGreater()) {
                                if (!(boolean) noswing.getValue()) {
                                    mc.thePlayer.swingItem();
                                } else {
                                    mc.thePlayer.swingItemFake();
                                }
                            }

                            NetUtil.sendPacketNoEvents(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

                            if (Client.instance.is1_9orGreater()) {
                                if (!(boolean) noswing.getValue()) {
                                    mc.thePlayer.swingItem();
                                } else {
                                    mc.thePlayer.swingItemFake();
                                }
                            }

                            if (target.waitTicks <= 0) {
                                boolean b = Angle.INSTANCE.check(new Location(mc.thePlayer.posX, em.getY(), mc.thePlayer.posZ, em.getYaw(), 0), target);
                                target.waitTicks = 10;
                            }

                            delay.reset();
                            nextRandom = (int) Math.round((20 / randomInt(min, max)));
                            if (crits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && critModule.isPacket2() && isCritSetup) {
                                NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                            }
                        } else {
                            if (!attack.equals("Precise") && fakeSwingTimer.delay(nextRandom * 50)) {
                                mc.thePlayer.swingItemFake();
                                fakeSwingTimer.setDifference(nextRandom > 2 ? 100 : 0);
                            }
                        }

                        isCritSetup = false;

                        if ((boolean) settings.get(/*PARTICLES*/decodeByteArray(new byte[]{80, 65, 82, 84, 73, 67, 76, 69, 83})).getValue()) {
                            float sharpLevel = EnchantmentHelper.func_152377_a(mc.thePlayer.inventory.getCurrentItem(), target.getCreatureAttribute());
                            if (sharpLevel > 0)
                                mc.thePlayer.onEnchantmentCritical(target);
                            if ((mc.thePlayer.onGround && Client.getModuleManager().isEnabled(Criticals.class)) || (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 0.66)) {
                                mc.thePlayer.onCriticalHit(target);
                            }
                        }
                    } /*else {
                        float yawDiff = RotationUtils.getYawChangeGiven(target.posX + p[0], target.posZ + p[2], em.getYaw());
                        double offOrig = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), v, target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height, 1.2);

                        boolean willViolate = target.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, em.getYaw() + yawDiff, 0), target);

                        ChatUtil.debug("\247a" + yawDiff + " \247b" + off + " \247c" + offOrig + " \247d" + willViolate);
                        ChatUtil.debug("\247a" + mc.thePlayer.getDistance(target.posX + p[0], target.posY + p[1], target.posZ + p[2]) + " " + angleTimer.getDifference());
                        ChatUtil.debug("----------------------------------");
                    }*/
                }/* else {
                    if (isAttacking) {
                        ChatUtil.printChat("Blocking? " + isBlocking + " " + mc.thePlayer.ticksExisted);
                    }
                    if (isCritSetup) {
                        ChatUtil.printChat("Crit setup? " + isCritSetup + " " + isBlocking + " " + delay.roundDelay(50 * nextRandom) + " " + mc.thePlayer.ticksExisted);
                    }
                }*/

                if (wait <= 0 && mc.thePlayer.isBlocking() && !isBlocking && !AutoSoup.isHealing) {
                    isBlocking = true;
                    NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                }

            }
            if (em.isPost()) {
                wait--;

                if (loaded.isEmpty() && target == null && isBlocking && (blockTimer.delay(50)) && block && !AutoSoup.isHealing) {
                    // Unblock, set next random blockWait
                    blockTimer.reset();
                    isBlocking = false;
                    NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }

            }
        } else if ("bruh".equals( /*switch*/decodeByteArray(new byte[]{115, 119, 105, 116, 99, 104}))) {
            if (em.isPre()) {
                tickEntities();
                target = getOptimalTarget();
                if (target != null) {
                    float[] r = RotationUtils.getRotations(target);
                    em.setYaw(r[0]);
                    em.setPitch(r[1]);
                    if (block && mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
                        mc.thePlayer.setItemInUse(mc.thePlayer.inventory.getCurrentItem(), 0x11938);
                    }
                }
            } else {
                if (target != null) {
                    if (mc.thePlayer.isBlocking() && target.waitTicks <= 1 && isBlocking && !AutoSoup.isHealing) {
                        isBlocking = false;
                        NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                    if (target.waitTicks <= 0 && !isBlocking && delay.delay(200)) {
                        target.waitTicks = 10;

                        this.attack(target, crits);

                        float sharpLevel = EnchantmentHelper.func_152377_a(mc.thePlayer.inventory.getCurrentItem(), target.getCreatureAttribute());
                        if (sharpLevel > 0.0F && (Boolean) settings.get(/*PARTICLES*/decodeByteArray(new byte[]{80, 65, 82, 84, 73, 67, 76, 69, 83})).getValue()) {
                            mc.thePlayer.onEnchantmentCritical(target);
                            if (crits || (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 0.66)) {
                                mc.thePlayer.onCriticalHit(target);
                            }
                        }

                        if (mc.thePlayer.isBlocking() && !isBlocking) {
                            isBlocking = true;
                            NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        }
                        delay.reset();
                    }
                } else if (isBlocking && !AutoSoup.isHealing) {
                    // Unblock
                    isBlocking = false;
                    NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
            }
        }
    }

    private boolean shouldntCrit(EntityLivingBase target) {
        if (!HypixelUtil.isInGame("PIT")) {
            return false;
        }

        if (target instanceof EntityPlayer) {
            if (target.getEquipmentInSlot(2) != null) {
                for (String pitEnchant : HypixelUtil.getPitEnchants(target.getEquipmentInSlot(2))) {
                    if ((pitEnchant.contains("Crit") && pitEnchant.contains("Funk")) || pitEnchant.contains("Retro")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCritFunky(EntityLivingBase target) {
        if (!HypixelUtil.isInGame("PIT")) {
            return false;
        }

        if (target instanceof EntityPlayer) {
            if (target.getEquipmentInSlot(2) != null) {
                for (String pitEnchant : HypixelUtil.getPitEnchants(target.getEquipmentInSlot(2))) {
                    if (pitEnchant.contains("Crit") && pitEnchant.contains("Funk")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public float getLastYaw() {
        return lastAngles.y > 90 || lastAngles.y < -90 ? lastAngles.x + 180 : lastAngles.x;
    }

    private final double[] ZERO = new double[]{0, 0, 0};

    private double[] getPrediction(EntityLivingBase player, int ticks, double scale) {
        if (!prediction.getValue() || !deltaHashMap.containsKey(player)) {
            return ZERO;
        }

        Killaura.EntityDelta delta = deltaHashMap.get(player);
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

            motionY -= 0.08D;
            motionY *= 0.9800000190734863D;

            lastMotionY = motionY;
        }

        return new double[]{finalX * scale, finalY * scale, finalZ * scale};
    }

    private class EntityDelta {
        private final ArrayBlockingQueue<double[]> deltas = new ArrayBlockingQueue<>(5);
        private int lastUpdatedTick;

        private EntityDelta(double initialDeltaX, double initialDeltaY) {
            deltas.add(new double[]{initialDeltaX, initialDeltaY});
        }

        private EntityDelta logDeltas(double deltaX, double deltaY, int currentTick) {
            if (currentTick - lastUpdatedTick > 5) {
                deltas.clear();
            }
            if (deltas.remainingCapacity() == 0) {
                deltas.remove();
            }

            lastUpdatedTick = currentTick;
            deltas.add(new double[]{deltaX, deltaY});
            return this;
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

    public Vec3 getDirection(float yaw, float pitch) {
        double y = -Math.sin(Math.toRadians(pitch));

        double xz = Math.cos(Math.toRadians(pitch));

        double x = -xz * Math.sin(Math.toRadians(yaw));
        double z = xz * Math.cos(Math.toRadians(yaw));

        return new Vec3(x, y, z);
    }

    public boolean isPosOnGround(double posX, double posY, double posZ) {

        boolean isOnSlab = MathUtils.roundToPlace((posY - (int) posY), 1) == 0.5;

        Block nextBlockUnder = mc.theWorld.getBlockState(new BlockPos(posX, posY - (isOnSlab ? 0 : 0.1), posZ)).getBlock();

        boolean feetBlockAir = isOnSlab ? nextBlockUnder.getMaterial() == Material.air : (nextBlockUnder instanceof BlockSlab && !nextBlockUnder.isFullBlock()) || nextBlockUnder.getMaterial() == Material.air;

        return !feetBlockAir;
    }

    public boolean isNextTickGround() {
        boolean forceTrue = !((boolean) settings.get(/*STEPCOMPAT*/decodeByteArray(new byte[]{83, 84, 69, 80, 67, 79, 77, 80, 65, 84})).getValue()) || !PlayerUtil.isMoving();
        boolean nextTickGround = false;

        double motionX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double motionZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

        double[][] offsets = new double[][]{new double[]{-0.351, -0.351}, new double[]{-0.351, 0.351}, new double[]{0.351, 0.351}, new double[]{0.351, -0.351}};
        for (double[] offset : offsets) {
            double offsetX = offset[0];
            double offsetZ = offset[1];

            double posX = offsetX + mc.thePlayer.posX + motionX;
            double posY = -0.45 + mc.thePlayer.posY;
            double posZ = offsetZ + mc.thePlayer.posZ + motionZ;

            if (isPosOnGround(posX, posY, posZ)) {
                nextTickGround = true;
                break;
            }
        }
        boolean isGround = forceTrue || nextTickGround;
        return isGround;
    }

    public boolean blockStep() {
        return (boolean) settings.get(/*STEPCOMPAT*/decodeByteArray(new byte[]{83, 84, 69, 80, 67, 79, 77, 80, 65, 84})).getValue() && getCurrentTarget() != null && Client.getModuleManager().isEnabled(Criticals.class) && stepDelay < 0;
    }

    private void tickEntities() {
        for (Entity ent : mc.theWorld.getLoadedEntityList()) {
            if (ent instanceof EntityLivingBase) {
                ((EntityLivingBase) ent).waitTicks--;
            }
        }
    }

    private void incrementIndex() {
        index += 1;
        if (index >= loaded.size()) {
            index = 0;
        }
    }

    public EntityLivingBase getCurrentTarget() {
        if (target != null) {
            return target;
        } else {
            if (!loaded.isEmpty() && index < loaded.size()) {
                return loaded.get(index);
            }
        }
        return null;
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    public void attack(Entity ent, boolean crits) {
        if (!(boolean) noswing.getValue())
            mc.thePlayer.swingItem();
        else
            mc.thePlayer.swingItemFake();
        if (crits) {
            Criticals.doCrits();
        } else {
            NetUtil.sendPacket(new C03PacketPlayer(true));
        }
        NetUtil.sendPacket(new C02PacketUseEntity(ent, C02PacketUseEntity.Action.ATTACK));
        float sharpLevel = EnchantmentHelper.func_152377_a(mc.thePlayer.inventory.getCurrentItem(), target.getCreatureAttribute());
        if (sharpLevel > 0.0F) {
            mc.thePlayer.onEnchantmentCritical(target);
        }
    }

    private EntityLivingBase getOptimalTarget() {
        return null;
    }

    public boolean validEntity(EntityLivingBase entity) {
        if (entity == null)
            return false;
        MultiBool multi = ((MultiBool) settings.get(TARGETING).getValue());
        boolean players = (Boolean) multi.getSetting(/*PLAYERS*/decodeByteArray(new byte[]{80, 76, 65, 89, 69, 82, 83})).getValue();
        boolean animals = (Boolean) multi.getSetting(/*PASSIVE*/decodeByteArray(new byte[]{80, 65, 83, 83, 73, 86, 69})).getValue();
        boolean mobs = (Boolean) multi.getSetting(/*MOBS*/decodeByteArray(new byte[]{77, 79, 66, 83})).getValue();
        boolean villager = (Boolean) multi.getSetting(/*VILLAGERS*/decodeByteArray(new byte[]{86, 73, 76, 76, 65, 71, 69, 82, 83})).getValue();
        boolean golems = (Boolean) multi.getSetting(decodeByteArray(new byte[]{71, 79, 76, 69, 77, 83})).getValue();
        boolean friends = (Boolean) multi.getSetting("FRIENDS").getValue();
        boolean invis = (Boolean) multi.getSetting(/*INVISIBLES*/decodeByteArray(new byte[]{73, 78, 86, 73, 83, 73, 66, 76, 69, 83})).getValue();
        boolean teams = (Boolean) multi.getSetting(/*TEAMS*/decodeByteArray(new byte[]{84, 69, 65, 77, 83})).getValue();
        boolean armor = (Boolean) multi.getSetting(/*ARMOR*/decodeByteArray(new byte[]{65, 82, 77, 79, 82})).getValue();

        float range = ((Number) settings.get(RANGE).getValue()).floatValue();
        float focusRange = range >= ((Number) blockRange.getValue()).floatValue() ? (mc.thePlayer.canEntityBeSeen(entity) ? range : Math.min(3, range)) : ((Number) blockRange.getValue()).floatValue();
        if ((mc.thePlayer.getHealth() > 0) && (entity.getHealth() > 0 && !entity.isDead && entity.deathTime <= 0) || Float.isNaN(entity.getHealth())) {
            boolean raytrace = (!((Boolean) settings.get(RAYTRACE).getValue())) || (mc.thePlayer.canEntityBeSeen(entity));
            if (mc.thePlayer.getDistanceToEntity(entity) <= focusRange && raytrace && entity.ticksExisted > ((Number) settings.get(TICK).getValue()).intValue()) {
                if (!isInFOV(entity))
                    return false;
                if (entity instanceof EntityPlayer && players) {
                    if (AntiBot.isBot(entity) || entity.isPlayerSleeping())
                        return false;
                    EntityPlayer ent = (EntityPlayer) entity;
                    return !(TeamUtils.isTeam(mc.thePlayer, ent) && teams) && !(ent.isInvisible() && !invis) && !(armor && !hasArmor(ent)) && (friends || !FriendManager.isFriend(ent.getName()));
                }
                if (mobs && entity instanceof EntityWither && teams) {
                    return !TeamUtils.isTeam(mc.thePlayer, entity);
                }
                return (entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityGhast || entity instanceof EntityDragon) && mobs ||
                        ((entity instanceof EntityAnimal || entity instanceof EntitySnowman || entity instanceof EntitySquid) && animals) ||
                        (entity instanceof EntityVillager && villager) || (entity instanceof EntityGolem && golems);
            }
        }
        return false;
    }

    private boolean hasArmor(EntityPlayer player) {
        return (player.inventory.armorInventory[0] != null) || (player.inventory.armorInventory[1] != null) || (player.inventory.armorInventory[2] != null) || (player.inventory.armorInventory[3] != null);
    }

    private void sortList() {
        String current = ((Options) settings.get(TARGETMODE).getValue()).getSelected();
        if (current.equalsIgnoreCase(/*Range*/ decodeByteArray(new byte[]{82, 97, 110, 103, 101}))) {
            loaded.sort(Comparator.comparingDouble(o -> o.getDistanceToEntity(mc.thePlayer)));
        } else if (current.equalsIgnoreCase(/*Health*/ decodeByteArray(new byte[]{72, 101, 97, 108, 116, 104}))) {
            loaded.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
        } else if (current.equalsIgnoreCase(/*Bounty*/ decodeByteArray(new byte[]{66, 111, 117, 110, 116, 121}))) {
            loaded.sort(Comparator.comparingDouble(o -> o.getHealth() + (o instanceof EntityPlayer && TargetESP.isPriority((EntityPlayer) o) ? -100D : 0))); // Prioritize bounties over normal players, still puts lowest health bounty first.
        } else if (current.equalsIgnoreCase(/*FOV*/ decodeByteArray(new byte[]{70, 79, 86}))) {
            loaded.sort(Comparator.comparingDouble(o -> (Math.abs(RotationUtils.getYawChange(o.posX, o.posZ)))));
        } else if (current.equalsIgnoreCase(/*Angle*/ decodeByteArray(new byte[]{65, 110, 103, 108, 101}))) {
            loaded.sort(Comparator.comparingDouble(o -> o.getDistanceToEntity(mc.thePlayer)));
            loaded.sort(Comparator.comparingDouble(o -> MathUtils.roundToPlace(RotationUtils.getRotations(o)[0], 15)));
        } else if (current.equalsIgnoreCase(/*Armor*/ decodeByteArray(new byte[]{65, 114, 109, 111, 114}))) {
            loaded.sort(Comparator.comparingInt(o -> (o instanceof EntityPlayer ? ((EntityPlayer) o).inventory.getTotalArmorValue() : (int) o.getHealth())));
        } else if (current.equals("Health Vamp")) {
            loaded.sort(Comparator.comparingDouble(this::getTargetWeighted));
        } else if (current.equals("Bounty Vamp")) {
            loaded.sort(Comparator.comparingDouble(this::getTargetWeightedBounty));
        }

        int maxTargets = this.maxTargets.getValue().intValue();
        loaded = loaded.subList(0, Math.min(maxTargets, loaded.size()));

    }

    private double getTargetWeightedBounty(EntityLivingBase entityLivingBase) {
        double weight = entityLivingBase.getHealth();

        if (entityLivingBase instanceof EntityPlayer) {
            if (TargetESP.isPriority((EntityPlayer) entityLivingBase) && entityLivingBase.hurtTime < 7) {
                weight -= 20;
            }
        }

        if (mc.thePlayer.getHealth() <= 19.5) {
            weight += Math.max(entityLivingBase.waitTicks, 0);
            // If the player is hurt, we don't get any benefit?
            if (entityLivingBase.hurtTime >= 6) {
                weight += 10;
            }

            float estimatedYawChange;

            float forwardYawDiff = Math.abs(MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x), -180, 180));
            if (forwardYawDiff > 90) {
                estimatedYawChange = Math.abs(MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x + 180), -180, 180));
            } else {
                estimatedYawChange = forwardYawDiff;
            }

            estimatedYawChange = (float) MathUtils.getIncremental(estimatedYawChange, 20);

            // The bigger the difference, the less we prefer to swap to them.
            if (estimatedYawChange >= 60) {
                weight += estimatedYawChange / 20;
            }
        }

        return weight;
    }


    // This is for Health Vampire mode
    private double getTargetWeighted(EntityLivingBase entityLivingBase) {
        double weight = entityLivingBase.getHealth();

        if (mc.thePlayer.getHealth() <= 19.5) {
            weight += Math.max(entityLivingBase.waitTicks, 0);
            // If the player is hurt, we don't get any benefit?
            if (entityLivingBase.hurtTime >= 6) {
                weight += 10;
            }

            float estimatedYawChange;

            float forwardYawDiff = Math.abs(MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x), -180, 180));
            if (forwardYawDiff > 90) {
                estimatedYawChange = Math.abs(MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x + 180), -180, 180));
            } else {
                estimatedYawChange = forwardYawDiff;
            }

            estimatedYawChange = (float) MathUtils.getIncremental(estimatedYawChange, 20);

            // The bigger the difference, the less we prefer to swap to them.
            if (estimatedYawChange >= 60) {
                weight += estimatedYawChange / 20;
            }
        }

        return weight;
    }


    private List<EntityLivingBase> getTargets() {
        List<EntityLivingBase> targets = new ArrayList<>();
        boolean priorityOnly = false;
        boolean allowPriorityOnly = !((Options) settings.get(TARGETMODE).getValue()).getSelected().equals("Bounty Vamp");
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) o;
                if (validEntity(entity)) {
                    if (allowPriorityOnly) {
                        if (vip == entity) {
                            targets.clear();
                            targets.add(entity);
                            return targets;
                        }
                        if (entity instanceof EntityPlayer && PriorityManager.isPriority((EntityPlayer) entity)) {
                            if (!priorityOnly)
                                targets.clear();
                            priorityOnly = true;
                            targets.add(entity);
                        }
                    }
                    if (!priorityOnly)
                        targets.add(entity);
                }
            }
        }
        return targets;
    }

    private boolean isInFOV(EntityLivingBase entity) {
        int fov = ((Number) settings.get(FOVCHECK).getValue()).intValue();
        return Math.abs(RotationUtils.getYawChange(entity.posX, entity.posZ)) <= fov && Math.abs(RotationUtils.getPitchChange(entity, entity.posY)) <= fov;
    }

}