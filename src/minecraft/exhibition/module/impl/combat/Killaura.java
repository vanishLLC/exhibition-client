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
import net.minecraft.init.Items;
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

    // Timers
    private final Timer delay = new Timer();
    private final Timer fakeSwingTimer = new Timer();
    private final Timer deathTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private final Timer blockTimer = new Timer();
    private final Timer angleTimer = new Timer();

    // Numbers
    private final Setting<Number> range = new Setting<>("RANGE", 4.5, "Range for killaura.", 0.1, 1, 7);
    private final Setting<Number> blockRange = new Setting<>("BLOCK-RANGE", 4.5, "Range for killaura.", 0.1, 1, 10);
    private final Setting<Number> fov = new Setting<>("FOV", 180, "Targets must be in FOV.", 1, 1, 180);
    private final Setting<Number> angleStep = new Setting<>("ANGLESTEP", 180, "The amount of degrees KillAura can step per tick. -1 = Your real yaw", 5, -1, 180);
    private final Setting<Number> existed = new Setting<>("EXISTED", 50, "Existed ticks before attacking.", 5, 1, 120);
    private final Setting<Number> minAPS = new Setting<>("MINAPS", 5, "Minimum APS.", 1, 1, 20);
    private final Setting<Number> maxAPS = new Setting<>("MXAXAPS", 15, "Maximum APS.", 1, 1, 20);
    private final Setting<Number> predictionTicks = new Setting<>("PTICKS", 1, "The amount of ticks to predict. 1 tick = 50ms", 1, 1, 10);
    private final Setting<Number> predictionScale = new Setting<>("PSCALE", 1.0, "The scale of how much prediction is applied.", 0.05, 0, 2);
    private final Setting<Number> maxTargets = new Setting<>("SWITCH-SIZE", 4, "The maximum amount of targets to switch through.", 1, 1, 10);
    private final Setting<Number> switchDelay = new Setting<>("SWITCH", 300, "Switch speed delay.", 50, 50, 1000);

    // Options/MultiBool
    private final Options priority = new Options("Priority", "Angle", "Angle", "Range", "FOV", "Armor", "Health", "Bounty", "Health Vamp", "Bounty Vamp");
    private final Options attackMode = new Options("Attack Mode", "Smart", "Precise", "Smart", "Always");

    // Booleans
    private final Setting<Boolean> exclusiveMode = new Setting<>("PRIORITY-ONLY", false, "Only targets a priority targets.");
    private final Setting<Boolean> autoBlock = new Setting<>("AUTOBLOCK", true, "Automatically blocks for you.");
    private final Setting<Boolean> autoShovel = new Setting<>("AUTOSHOVEL", false, "Swaps to a shovel silently. Intended for PIT.");
    private final Setting<Boolean> pitSpawn = new Setting<>("PIT-SPAWN", true, "Disables Killaura when in PIT spawn.");
    private final Setting<Boolean> antiLag = new Setting<>("ANTI-LAG", true, "Prevents the Killaura from flagging you when lagging.");
    private final Setting<Boolean> reduce = new Setting<>("REDUCE", false, "Reduces your rotations to prevent flags.");
    private final Setting<Boolean> antiCritFunky = new Setting<>("ANTI-CF", false, "Attacks players without proccing Critically Funky.");
    private final Setting<Boolean> prediction = new Setting<>("PREDICTION", true, "Predicts where the player will be on server side.");
    private final Setting<Boolean> noswing = new Setting<>("NOSWING", false, "Blocks swinging server sided.");
    private final Setting<Boolean> indicator = new Setting<>("INDICATOR", false, "Renders an indicator on target.");
    private final Setting<Boolean> deathCheck = new Setting<>("DEATH", true, "Disables Killaura on death.");
    private final Setting<Boolean> raytrace = new Setting<>("RAYTRACE", true, "Visible check for target.");

    private final Setting<Boolean> particles = new Setting<>("PARTICLES", false, "Show enchant/crit particles when attacking. (Client Side)");
    private final Setting<Boolean> stepCompat = new Setting<>("STEPCOMPAT", true, "Adds extra compatibility when stepping up blocks with Criticals.");

    private final HashMap<EntityLivingBase, EntityDelta> deltaHashMap = new HashMap<>();

    private int index;
    public static int setupTick;
    private boolean isCritSetup;
    private int nextRandom = -1;
    public static boolean blockJump;
    public static boolean wantedToJump;
    public Vector2f lastAngles = new Vector2f(0, 0);
    public int swapped = -1;

    private final UUID[] whitelistedUUIDs = {
            UUID.fromString("3ea40f14-cbcf-4191-ba3b-c126ca334714"), // WatchdogOff
            UUID.fromString("ff33c84a-03de-4919-9160-b461f10f4657"), // Fouo
            UUID.fromString("fb6d1e25-9c98-4865-ac77-6a307493abfa"), // PoopSock9
            /*UUID.fromString("")*/};

    private static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

    public Killaura(ModuleData data) {
        super(data);
        addSettings(range, fov, existed, autoBlock, autoShovel, range, blockRange, minAPS, maxAPS,
                angleStep, deathCheck, raytrace, particles, stepCompat, switchDelay, noswing,
                indicator, reduce, prediction, predictionTicks, predictionScale, maxTargets,
                antiLag, pitSpawn, antiCritFunky);

        Setting[] filters = new Setting[]{
                new Setting<>("PLAYERS", true),
                new Setting<>("MOBS", false),
                new Setting<>("PASSIVE", false),
                new Setting<>("VILLAGERS", false),
                new Setting<>("GOLEMS", false),
                new Setting<>("INVISIBLES", false),
                new Setting<>("TEAMS", false),
                new Setting<>("ARMOR-ONLY", false),
                new Setting<>("FRIENDS", false)};
        settings.put("TARGETING", new Setting<>("TARGETING", new MultiBool("Target Filter", filters), "Filters certain entities/properties when acquiring targets."));

        addSetting(new Setting<>("PRIORITY", priority, "Target mode priority."));
        addSetting(new Setting<>("ATTACK-MODE", attackMode, "Customizes the Killaura attack mode."));

        //randomSeed = randomNumber(0.0000005F, -0.0000005F);
    }

//    private double randomNumber(double max, double min) {
//        return (Math.random() * (max - min)) + min;
//    }

    private float randomNumber(float max, float min) {
        return min + (float) (Math.random() * (max - min));
    }

    private double randomInt(int min, int max) {
        return min + (Math.random() * (max - min));
    }

    public List<EntityLivingBase> loaded = new CopyOnWriteArrayList<>();
    public boolean isBlocking;
    public EntityLivingBase target;
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
        swapped = -1;
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

    //private float randomSeed;

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventStep.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        Criticals critModule = Client.getModuleManager().get(Criticals.class);
        int maxTargets = this.maxTargets.getValue().intValue();

        if (event instanceof EventPacket) {
            EventPacket e = event.cast();
            Packet packet = e.getPacket();
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
                critWaitTicks = critModule.isPacket() ? 20 : 6;
                setupTick = 0;
            }

//                if(packet instanceof C07PacketPlayerDigging) {
//                    ChatUtil.debug("Unblocked " + mc.thePlayer.ticksExisted + " " + isBlocking);
//                }
//                if(packet instanceof C08PacketPlayerBlockPlacement) {
//                    ChatUtil.debug("Blocked " + mc.thePlayer.ticksExisted + " " + isBlocking);
//                }

//                if (packet instanceof C01PacketChatMessage) {
//                    C01PacketChatMessage chatMessage = (C01PacketChatMessage) packet;
//                    if (chatMessage.getMessage().contains("/spawn")) {
//                        ChatUtil.printChat("Spawn " + chatMessage.getMessage());
//                    }
//                }

            if (packet instanceof S45PacketTitle && deathCheck.getValue()) {
                S45PacketTitle titlePacket = ((S45PacketTitle) packet);
                if (titlePacket.getType().equals(S45PacketTitle.Type.TITLE)) {
                    String text = StringUtils.stripControlCodes(titlePacket.getMessage().getFormattedText());
                    if ((text.toLowerCase().contains("died") || text.toLowerCase().contains("game over")) && isEnabled()) {
                        shouldToggle = true;
                        Notifications.getManager().post("Aura Death", "Aura disabled due to death.");
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
            if (indicator.getValue()) {
                EventRender3D er = event.cast();
                GL11.glPushMatrix();
                RenderingUtil.pre3D();
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                try {
                    Iterator<EntityLivingBase> loadedIter = loaded.iterator();
                    while (loadedIter.hasNext()) {
                        GlStateManager.pushMatrix();
                        EntityLivingBase target = loadedIter.next();
                        double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionScale.getValue().doubleValue());

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
        int min = minAPS.getValue().intValue();
        int max = maxAPS.getValue().intValue();
        if (min > max) {
            minAPS.setValue(max);
        }
        if ((blockRange.getValue()).floatValue() < this.range.getValue().floatValue()) {
            blockRange.setValue(this.range.getValue());
        }
        if (nextRandom == -1)
            nextRandom = (int) Math.round((20 / randomInt(min, max)));

        boolean block = autoBlock.getValue();
        EventMotionUpdate em = event.cast();
        if (em.isPre()) {

//            double offsetY = mc.thePlayer.posY - (int) mc.thePlayer.posY;
//
//            if (offsetY != 0)
//                ChatUtil.debug(offsetY + " " + mc.thePlayer.posY);

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
            if (deathCheck.getValue()) {
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
        Scaffold scaffold = Client.getModuleManager().get(Scaffold.class);
        LongJump longjump = Client.getModuleManager().get(LongJump.class);
        boolean disable = false;
        if ((AutoPot.potting || AutoPot.haltTicks > 0) || longjump.allowAttack() || longjump.isUsingBow()/* || (Client.getModuleManager().get(FreecamTP.class).stage == 1)*/) {
            disable = true;
            setupTick = 0;
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

        float range = this.range.getValue().floatValue();
        boolean crits = (critModule.isEnabled() && critWaitTicks <= 0) &&
                ((!Client.getModuleManager().isEnabled(Speed.class) || (mc.thePlayer.onGround && !PlayerUtil.isMoving())) && !Client.getModuleManager().isEnabled(Fly.class) && !Client.getModuleManager().isEnabled(LongJump.class)) &&
                !(Client.getModuleManager().isEnabled(Jesus.class) && PlayerUtil.isOnLiquid());
        String attack = attackMode.getSelected();
        boolean single = maxTargets == 1;
        if (single) {
            index = 0;
        }
//        Bypass bypass = Client.getModuleManager().get(Bypass.class);
//
//        int bypassTicks = bypass.bruh - 10;
//
//        boolean allowInvalidAngles = bypass.allowBypassing() && (bypass.option.getSelected().equals("Watchdog Off") || (bypass.option.getSelected().equals("Dong") ?
//                bypassTicks > 25 && bypassTicks <= (27 + bypass.randomDelay) : bypass.bruh > 10 && bypass.bruh % 100 > 10 && bypass.bruh % 100 < 99)) && HypixelUtil.isVerifiedHypixel();

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

                    if (deltaHashMap.containsKey(e)) {
                        deltaHashMap.get(e).logDeltas(xDelta, zDelta, mc.thePlayer.ticksExisted);
                    } else {
                        deltaHashMap.put(e, new EntityDelta(xDelta, zDelta));
                    }
                }

                // Set the target each switch delay
                if (switchTimer.delay((switchDelay.getValue()).intValue()) && !single) {
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
                        double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionScale.getValue().doubleValue());
                        double xDiff = (target.posX + p[0]) - mc.thePlayer.posX;
                        double yDiff = (target.posY + p[1]) - mc.thePlayer.posY - 0.5;
                        double zDiff = (target.posZ + p[2]) - mc.thePlayer.posZ;

                        double yDifference = Math.abs((target.posY + p[2]) - mc.thePlayer.posY);

                        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);

                        double distance = MathUtils.roundToPlace(dist, 1);

                        boolean shouldReduce = reduce.getValue() && distance <= 3;

                        float targetYaw = MathHelper.clamp_float(RotationUtils.getYawChangeGiven(target.posX, target.posZ, lastAngles.x), -180, 180);
                        int maxAngleStep = angleStep.getValue().intValue();

                        if (maxAngleStep > 5 && maxAngleStep < 175) {
                            maxAngleStep += randomNumber(5, -5);
                        }

                        if (targetYaw > maxAngleStep) targetYaw = maxAngleStep;
                        else if (targetYaw < -maxAngleStep) targetYaw = -maxAngleStep;

                        if (maxAngleStep > -1 && !scaffold.isEnabled())
                            if (shouldReduce) {
                                float pitch = (float) -(Math.atan2(yDiff - (distance > 2.1 ? 1.25 : 1.5), dist) * 180.0D / 3.141592653589793D);
                                float newYaw = 0F;

                                if (distance <= (HypixelUtil.isInGame("DUEL") ? 1.12 : 0.75) && yDifference <= 1.5) {
                                    em.setYaw(lastAngles.x);
                                    em.setPitch(MathHelper.clamp_float(88.9F + (float) (0.5F * Math.random()), -89.5F, 89.5F));
                                } else {
                                    em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -89.5F, 89.5F));

//                                        if (lastAngles.y > 90 || lastAngles.y < -90) {
//                                            //em.setPitch(180 - em.getPitch());
//                                        }

                                    Vec3 v = getDirection(lastAngles.x, em.getPitch());
                                    double off = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), v,
                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

//                                    Vec3 backwardsBruh = getDirection(lastAngles.x, 180 - em.getPitch());
//                                    double backwardsOff = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), backwardsBruh,
//                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
//                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

//                                    if (allowInvalidAngles && em.getPitch() >= 0 && off >= 0.1 && backwardsOff < 0.1) {
//                                        boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
//                                        boolean canAttackRightNow = (attack.equals("Always")) ||
//                                                (attack.equals("Precise") ? target.waitTicks <= 0 :
//                                                        target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);
//
//                                        if (isAttacking && canAttackRightNow) {
//                                            em.setPitch(MathHelper.wrapAngleTo180_float(180 - em.getPitch()));
//                                        } else {
//                                            em.setPitch(em.getPitch() + 360);
//                                        }
//                                    } else {

                                    float tempNewYaw = (float) MathUtils.getIncremental(lastAngles.x + (targetYaw / 1.1F), 20);

                                    boolean willViolate = maxAngleStep > 0 && target.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tempNewYaw, 0), target);

                                    if ((angleTimer.roundDelay(1000) && off >= 0.11 && !willViolate) || (angleTimer.roundDelay(200) && !willViolate && off >= 0.2)) {
                                        newYaw += targetYaw;

//                                        float normalDiff = Math.abs(newYaw);
//                                        float backwardsDiff = Math.abs(MathHelper.wrapAngleTo180_float(newYaw + 180));
//
//                                        Vec3 vecReverse = getDirection((float) MathUtils.getIncremental(lastAngles.x + MathHelper.wrapAngleTo180_float((newYaw + 180)), 20), 180 - em.getPitch());
//                                        double newOffReverse = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), vecReverse,
//                                                target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
//                                                HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

//                                            if (allowInvalidAngles && em.getPitch() >= 0 && backwardsDiff < normalDiff && newOffReverse < 0.1 && normalDiff > 90) {
//                                                angleTimer.reset();
//                                                em.setYaw(lastAngles.x = ((float) MathUtils.getIncremental(lastAngles.x += MathHelper.wrapAngleTo180_float((newYaw + 180)), 20) + randomNumber(1, -1)));
//                                                em.setPitch(180 - em.getPitch());
//                                            } else {
                                        angleTimer.reset();
                                        em.setYaw(lastAngles.x = ((float) MathUtils.getIncremental(lastAngles.x += (newYaw), 20) + randomNumber(1, -1)));
                                        //}
                                    }
                                    //}

                                    em.setYaw(lastAngles.x);

                                }
                            } else {
                                // Allow reduced to still have your heads backwards
//                                if (reduce.getValue() && allowInvalidAngles) {
//                                    float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
//                                    em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -89.5F, 89.5F));
//
//                                    float normalDiff = Math.abs(targetYaw);
//                                    float backwardsDiff = Math.abs(MathHelper.wrapAngleTo180_float(targetYaw + 180));
//
//                                    Vec3 vecReverse = getDirection((float) MathUtils.getIncremental(lastAngles.x + MathHelper.wrapAngleTo180_float((targetYaw + 180)), 20), 180 - em.getPitch());
//                                    double newOffReverse = Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), vecReverse,
//                                            target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
//                                            HypixelUtil.isInGame("DUEL") ? 1.2 : HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);
//
//                                    if (pitch >= 0 && backwardsDiff < normalDiff && newOffReverse < 0.1 && normalDiff > 120) {
//                                        em.setYaw(lastAngles.x += MathHelper.wrapAngleTo180_float(targetYaw + 180) / 1.1F);
//
//                                        boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
//                                        boolean canAttackRightNow = (attack.equals("Always")) ||
//                                                (attack.equals("Precise") ? target.waitTicks <= 0 :
//                                                        target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);
//
//                                        // Only headsnap when attacking to reduce others figuring this out
//                                        if (isAttacking && canAttackRightNow) {
//                                            em.setPitch(MathHelper.wrapAngleTo180_float(180 - em.getPitch()));
//                                        } else {
//                                            em.setPitch(em.getPitch() + 360);
//                                        }
//                                    } else {
//                                        angleTimer.reset();
//                                        em.setYaw((lastAngles.x += targetYaw / 1.1F) + (float) randomNumber(2, -2));
//                                    }
//                                } else {
                                float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);

                                em.setYaw((lastAngles.x += targetYaw / 1.1F));
                                em.setPitch(MathHelper.clamp_float(pitch / 1.1F, -89.5F, 89.5F));
                                //}
                            }

                        lastAngles.y = em.getPitch();

                        boolean setupCrits = critModule.isGround() || target.hurtTime <= 1 || (target.waitTicks <= 1);

                        boolean dontCrit = antiCritFunky.getValue() && hasEnchant(target, "Crit", "Funk");

                        if (target instanceof EntityPlayer && antiCritFunky.getValue() && hasEnchant(target, "Retro")) {
                            int criticalHits = ((EntityPlayer) target).criticalHits;
                            if ((criticalHits == 0 || criticalHits > 3) || target.waitTicks > 0) {
                                if (criticalHits == 0) {
                                    ((EntityPlayer) target).criticalHits++;
                                }
                                crits = false;
                                dontCrit = true;
                            }
                        }

                        if (!dontCrit) {
                            if (crits) {
                                if (critModule.isPacketOld()) {
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
                                } else if (critModule.isGround()) {
                                    boolean canAttackRightNow = (attack.equals("Always")) ||
                                            (attack.equals("Precise") ? target.waitTicks <= 1 :
                                                    target.waitTicks <= 1 || (target.hurtResistantTime <= 11 && target.hurtResistantTime >= 6) || target.hurtTime > 6);

                                    if (isNextTickGround() && !Client.instance.isLagging()) {
                                        if (setupCrits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                            boolean shouldCrit = target.waitTicks > 6 || target.waitTicks <= 1;
                                            isCritSetup = true;
                                            if (HypixelUtil.isVerifiedHypixel() && shouldCrit) {
                                                if (canAttackRightNow && setupTick == 0) {
                                                    stepDelay = 5;
                                                    blockJump = true;
                                                    em.setY(em.getY() + 0.125F);
                                                    em.setGround(false);
                                                    em.setForcePos(true);
                                                } else if (setupTick >= 1) {
                                                    if (setupTick > 2 || PlayerUtil.isMoving())
                                                        isCritSetup = true;
                                                    em.setY(em.getY() + 0.125F);
                                                    switch (setupTick) {
                                                        case 1: {
                                                            blockJump = true;
                                                            em.setY(em.getY() + 0.046599998474120774);
                                                            break;
                                                        }
                                                        case 2: {
                                                            blockJump = true;
                                                            em.setY(em.getY() + 0.046599998474120774 - 0.07501);
                                                            break;
                                                        }
                                                        case 3: {
                                                            em.setY(em.getY() + (0.046599998474120774 - 0.07501 - 0.07501));
                                                            break;
                                                        }
                                                    }
                                                    em.setGround(false);
                                                    em.setForcePos(true);

                                                    //ChatUtil.printChat((em.getY() - mc.thePlayer.posY) + "");
                                                }
                                                setupTick++;
                                                if (setupTick >= 4) {
                                                    setupTick = -1;
                                                }
                                            }
                                        } else {
                                            setupTick = 0;
                                        }
                                    } else {
                                        setupTick = 0;
                                    }
                                } else if (critModule.isGroundOld()) {
                                    boolean canAttackRightNow = (attack.equals("Always")) ||
                                            (attack.equals("Precise") ? target.waitTicks <= 1 :
                                                    target.waitTicks <= 1 || (target.hurtResistantTime <= 11 && target.hurtResistantTime >= 6) || target.hurtTime > 6);

                                    if (isNextTickGround() && !Client.instance.isLagging()) {
                                        if (setupCrits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                            if (canAttackRightNow && setupTick == 0) {
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
                                } else if (critModule.isPacket() && HypixelUtil.isVerifiedHypixel()) {
                                    boolean isAttacking = mc.thePlayer.getDistanceToEntity(target) <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && delay.roundDelay(50 * nextRandom);
                                    boolean canAttackRightNow = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

                                    if (isAttacking && canAttackRightNow && isNextTickGround())
                                        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                            stepDelay = 2;
                                            blockJump = true;
                                            em.setY(em.getY() + 0.125F);
                                            em.setGround(false);
                                            em.setForcePos(true);
                                            isCritSetup = true;
                                        }
                                }
                            } else {
                                setupTick = 0;
                            }
                        } else {
                            setupTick = 0;
                            isCritSetup = true;
                        }

                        if (Bypass.shouldSabotage()) {
                            em.setGround(true);
                        }

                        if (!block && !mc.thePlayer.isBlocking() && isBlocking) {
                            isBlocking = false;
                            NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, C08PacketPlayerBlockPlacement.USE_ITEM_POS, EnumFacing.DOWN));
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

                int maxAngleStep = angleStep.getValue().intValue();

                if (maxAngleStep == -1) {
                    lastAngles.x = mc.thePlayer.rotationYaw;
                    lastAngles.y = mc.thePlayer.rotationPitch;
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
            if (mc.thePlayer.isBlocking() && (isBlocking) && packetMode && PlayerUtil.isMoving() && mc.thePlayer.ticksExisted % 2 == 0 && !AutoSoup.isHealing) {
                isBlocking = false;
                NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, C08PacketPlayerBlockPlacement.USE_ITEM_POS, EnumFacing.DOWN));
                blockTimer.reset();
            }

        } else if (em.isPost() && (loaded.size() > 0) && (loaded.get(Math.min(loaded.size() - 1, index)) != null) && target != null && !disable) {

            boolean alwaysCrit = (!Client.getModuleManager().isEnabled(LongJump.class) && !Client.getModuleManager().isEnabled(Fly.class) && (boolean) critModule.getSetting("ALWAYS-CRIT").getValue());

            boolean canCrit = (mc.thePlayer.fallDistance > 0.0625F && !mc.thePlayer.onGround);

            boolean isCriticalAttack = canCrit || isCritSetup;

            boolean isOptimalAttack = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : (target.hurtTime <= 5));

            boolean twoTickCritsGood = !mc.thePlayer.onGround || (!PlayerUtil.isOnLiquid() && (attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 1 :
                    (target.waitTicks <= 1 || (target.hurtResistantTime <= 11 && target.hurtResistantTime >= 6) || target.hurtTime > 6))) && isCritSetup);

            boolean threeTickCritsGood = !mc.thePlayer.onGround || (!PlayerUtil.isOnLiquid() && (isOptimalAttack || target.waitTicks <= 1) && isCritSetup);

            boolean criticalsAreSet = !crits || ((critModule.isPacketOld() ? threeTickCritsGood : critModule.isGround() ? twoTickCritsGood : critModule.isPacket()));

            boolean shouldAttack = alwaysCrit ? isCriticalAttack : criticalsAreSet;

            boolean setupCrits = critModule.isGround() || critModule.isGroundOld() || (target.hurtTime <= 0 && target.waitTicks >= 6) || (target.waitTicks <= 0);

            double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionScale.getValue().doubleValue());

            double distance = mc.thePlayer.getDistance(target.posX + p[0], target.posY + p[1], target.posZ + p[2]);

            long attackDelay = 50 * nextRandom;

            boolean isAttacking = distance <= (mc.thePlayer.canEntityBeSeen(target) ? range : Math.min(3, range)) && (mc.timer.timerSpeed <= 1 ? delay.roundDelay(attackDelay) : delay.delay(attackDelay));

            boolean canAttackRightNow = attack.equals("Always") || (attack.equals("Precise") ? target.waitTicks <= 0 : target.waitTicks <= 0 || (target.hurtResistantTime <= 10 && target.hurtResistantTime >= 7) || target.hurtTime > 7);

            if (scaffold.isEnabled()) {
                lastAngles.x = em.getYaw();
                lastAngles.y = em.getPitch();
            }

            boolean dontCrit = antiCritFunky.getValue() && hasEnchant(target, "Crit", "Funk");
            if (Client.getModuleManager().isEnabled(Speed.class) && dontCrit) {
                if (!mc.thePlayer.onGround && mc.thePlayer.motionY < 0) {
                    isAttacking = false;
                }
            }

            if (mc.thePlayer.isBlocking() && isAttacking && shouldAttack && isBlocking && canAttackRightNow && !AutoSoup.isHealing) {
                isBlocking = false;
                NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, C08PacketPlayerBlockPlacement.USE_ITEM_POS, EnumFacing.DOWN));
                blockTimer.reset();
            }

            if (isAttacking && !isBlocking && (!antiLag.getValue() || !Client.instance.isLagging())) {
                Vec3 v = getDirection(em.getYaw(), em.getPitch());
                int maxAngleStep = angleStep.getValue().intValue();

                double off = maxAngleStep <= 0 ? 0 : Direction.directionCheck(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.getEyeHeight(), v, target.posX + p[0], target.posY + p[1] + target.height / 2D, target.posZ + p[2], target.width, target.height,
                        HypixelUtil.isInGame("DUEL") ? 1.85 :
                                HypixelUtil.isInGame("HYPIXEL PIT") ? 0.85 : 1);

                if (angleStep.getValue().intValue() <= 0 || (off <= 0.11 || (off <= 1 && off >= 0.22 && MathUtils.getIncremental(angleTimer.getDifference(), 50) <= 100))) {

                    if (crits && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && (((critModule.isPacket() && setupCrits)) && isCritSetup) && !em.isOnground()) {
                        if (HypixelUtil.isVerifiedHypixel() && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                            NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.046599998474120774, mc.thePlayer.posZ, false));
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

                        boolean whitelisted = false;

                        for (UUID whitelistedUUID : whitelistedUUIDs) {
                            if (whitelistedUUID.equals(target.getUniqueID())) {
                                whitelisted = true;
                                break;
                            }
                        }


                        if (!whitelisted) {
                            // If the user is holding nothing or NOT a diamond shovel
                            boolean shouldSilentSwap = autoShovel.getValue() && HypixelUtil.isInGame("PIT") && (mc.thePlayer.inventory.getCurrentItem() == null || mc.thePlayer.inventory.getCurrentItem().getItem() != Items.diamond_shovel);
                            if (shouldSilentSwap && target.waitTicks <= 0) {
                                int swapTo = InventoryUtil.findItemInInventory(Items.diamond_shovel);
                                if (swapTo != -1) {
                                    // Server should be checking currentItem int, some servers may be different and need to set the item next tick
                                    swapped = swapTo;
                                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, swapped, mc.thePlayer.inventory.currentItem, 2, mc.thePlayer);
                                    mc.playerController.updateController();
                                }
                            }

                            NetUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

                            if (swapped != -1) {
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, swapped, mc.thePlayer.inventory.currentItem, 2, mc.thePlayer);
                                mc.playerController.updateController();
                                swapped = -1;
                            }
                        } else {
                            ChatUtil.printChat("No.");
                        }

                        if (Client.instance.is1_9orGreater()) {
                            if (!(boolean) noswing.getValue()) {
                                mc.thePlayer.swingItem();
                            } else {
                                mc.thePlayer.swingItemFake();
                            }
                        }

                        if (target != null && target.waitTicks <= 0) {
                            if (target instanceof EntityPlayer) {
                                EntityPlayer player = (EntityPlayer) target;
                                if ((!em.isOnground() && isCritSetup) || mc.thePlayer.fallDistance > 0) {
                                    player.criticalHits++;
                                } else {
                                    if (player.criticalHits > 1) {
                                        if (antiCritFunky.getValue() && hasEnchant(target, "Retro")) {
                                            player.criticalHits = 0;
                                        }
                                    }
                                }
                            }

                            //boolean b = Angle.INSTANCE.check(new Location(mc.thePlayer.posX, em.getY(), mc.thePlayer.posZ, em.getYaw(), 0), target);
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

                    if (particles.getValue()) {
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

            if (loaded.isEmpty() && target == null && (isBlocking) && (blockTimer.delay(50)) && block && !AutoSoup.isHealing) {
                // Unblock, set next random blockWait
                blockTimer.reset();
                isBlocking = false;
                NetUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }

        }
    }

    public boolean hasEnchant(EntityLivingBase target, String... enchants) {
        if (!HypixelUtil.isInGame("PIT")) {
            return false;
        }

        if (target instanceof EntityPlayer) {
            if (HypixelUtil.isItemMystic(target.getEquipmentInSlot(2))) {
                for (String pitEnchant : HypixelUtil.getPitEnchants(target.getEquipmentInSlot(2))) {
                    for (String enchant : enchants) {
                        if (pitEnchant.contains(enchant)) {
                            return true;
                        }
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

    private static class EntityDelta {
        private final ArrayBlockingQueue<double[]> deltas = new ArrayBlockingQueue<>(5);
        private int lastUpdatedTick;

        private EntityDelta(double initialDeltaX, double initialDeltaY) {
            deltas.add(new double[]{initialDeltaX, initialDeltaY});
        }

        private void logDeltas(double deltaX, double deltaY, int currentTick) {

            double distance = MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY);

            if (distance >= 5) {
                if (deltas.remainingCapacity() == 0) {
                    deltas.remove();
                }
                deltas.add(new double[]{0, 0});
                return;
            }

            if (currentTick - lastUpdatedTick > 5) {
                deltas.clear();
            }
            if (deltas.remainingCapacity() == 0) {
                deltas.remove();
            }

            lastUpdatedTick = currentTick;
            deltas.add(new double[]{deltaX, deltaY});
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
        boolean forceTrue = !(stepCompat.getValue()) || !PlayerUtil.isMoving();
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
        return forceTrue || nextTickGround;
    }

    public boolean blockStep() {
        return stepCompat.getValue() && getCurrentTarget() != null && Client.getModuleManager().isEnabled(Criticals.class) && stepDelay < 0;
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
        if (!noswing.getValue())
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

    public boolean validEntity(EntityLivingBase entity) {
        if (entity == null)
            return false;
        MultiBool multi = ((MultiBool) settings.get("TARGETING").getValue());
        boolean players = (Boolean) multi.getSetting("PLAYERS").getValue();
        boolean animals = (Boolean) multi.getSetting("PASSIVE").getValue();
        boolean mobs = (Boolean) multi.getSetting("MOBS").getValue();
        boolean villager = (Boolean) multi.getSetting("VILLAGERS").getValue();
        boolean golems = (Boolean) multi.getSetting("GOLEMS").getValue();
        boolean friends = (Boolean) multi.getSetting("FRIENDS").getValue();
        boolean invis = (Boolean) multi.getSetting("INVISIBLES").getValue();
        boolean teams = (Boolean) multi.getSetting("TEAMS").getValue();
        boolean armor = (Boolean) multi.getSetting("ARMOR-ONLY").getValue();

        float range = this.range.getValue().floatValue();
        float focusRange = range >= blockRange.getValue().floatValue() ? (mc.thePlayer.canEntityBeSeen(entity) ? range : Math.min(3, range)) : blockRange.getValue().floatValue();
        if ((mc.thePlayer.getHealth() > 0) && (entity.getHealth() > 0 && !entity.isDead && entity.deathTime <= 0) || Float.isNaN(entity.getHealth())) {
            boolean raytrace = (!this.raytrace.getValue()) || (mc.thePlayer.canEntityBeSeen(entity));
            if (mc.thePlayer.getDistanceToEntity(entity) <= focusRange && raytrace && entity.ticksExisted > existed.getValue().intValue()) {
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
        String current = priority.getSelected();
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
        int maxAngleStep = angleStep.getValue().intValue();

        if (entityLivingBase instanceof EntityPlayer) {
            if (TargetESP.isPriority((EntityPlayer) entityLivingBase) && entityLivingBase.hurtTime < 6) {
                weight -= 20;
            }
        }

        if ((mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= 0.975) {
            weight += Math.max(entityLivingBase.waitTicks, 0);
            // If the player is hurt, we don't get any benefit?
            if (entityLivingBase.hurtTime >= 6) {
                weight += 10;
            }

            if (maxAngleStep > 0) {
                float yawDiff = MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x), -180, 180);

                float estimatedYawChange;

//                Bypass bypass = Client.getModuleManager().get(Bypass.class);
//                int bypassTicks = bypass.bruh - 10;
//                boolean allowInvalidAngles = bypass.allowBypassing() && (bypass.option.getSelected().equals("Watchdog Off") || (bypass.option.getSelected().equals("Dong") ?
//                        bypassTicks > 25 && bypassTicks <= (27 + bypass.randomDelay) : bypass.bruh > 10 && bypass.bruh % 100 > 10 && bypass.bruh % 100 < 99)) && HypixelUtil.isVerifiedHypixel();

                float forwardYawDiff = Math.abs(yawDiff);
//                if (forwardYawDiff > 90 && allowInvalidAngles) {
//                    boolean willViolate = entityLivingBase.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, lastAngles.x + MathHelper.clamp_float(yawDiff + 180, -180, 180), 0), entityLivingBase);
//                    if (willViolate) {
//                        weight += 5;
//                    }
//                    estimatedYawChange = Math.abs(MathHelper.clamp_float(yawDiff + 180, -180, 180));
//                } else {
                boolean willViolate = entityLivingBase.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, lastAngles.x + yawDiff, 0), entityLivingBase);
                if (willViolate) {
                    weight += 5;
                }
                estimatedYawChange = forwardYawDiff;
//                }

                estimatedYawChange = (float) MathUtils.getIncremental(estimatedYawChange, 20);

                // The bigger the difference, the less we prefer to swap to them.
                if (estimatedYawChange >= 60) {
                    weight += estimatedYawChange / 20;
                }
            }
        }

        return weight;
    }


    // This is for Health Vampire mode
    private double getTargetWeighted(EntityLivingBase entityLivingBase) {
        double weight = entityLivingBase.getHealth();
        int maxAngleStep = angleStep.getValue().intValue();

        if (mc.thePlayer.getHealth() <= 19.5) {
            weight += Math.max(entityLivingBase.waitTicks, 0);
            // If the player is hurt, we don't get any benefit?
            if (entityLivingBase.hurtTime >= 6) {
                weight += 10;
            }

            if (maxAngleStep > 0) {
                float yawDiff = MathHelper.clamp_float(RotationUtils.getYawChangeGiven(entityLivingBase.posX, entityLivingBase.posZ, lastAngles.x), -180, 180);

                float estimatedYawChange;

//                Bypass bypass = Client.getModuleManager().get(Bypass.class);
//                int bypassTicks = bypass.bruh - 10;
//                boolean allowInvalidAngles = bypass.allowBypassing() && (bypass.option.getSelected().equals("Watchdog Off") || (bypass.option.getSelected().equals("Dong") ?
//                        bypassTicks > 25 && bypassTicks <= (27 + bypass.randomDelay) : bypass.bruh > 10 && bypass.bruh % 100 > 10 && bypass.bruh % 100 < 99)) && HypixelUtil.isVerifiedHypixel();

                float forwardYawDiff = Math.abs(yawDiff);
//                if (forwardYawDiff > 90 && allowInvalidAngles) {
//                    boolean willViolate = entityLivingBase.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, lastAngles.x + MathHelper.clamp_float(yawDiff + 180, -180, 180), 0), entityLivingBase);
//                    if (willViolate) {
//                        weight += 5;
//                    }
//                    estimatedYawChange = Math.abs(MathHelper.clamp_float(yawDiff + 180, -180, 180));
//                } else {
                boolean willViolate = entityLivingBase.waitTicks <= 0 && Angle.INSTANCE.willViolateYaw(new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, lastAngles.x + yawDiff, 0), entityLivingBase);
                if (willViolate) {
                    weight += 5;
                }
                estimatedYawChange = forwardYawDiff;
                //}

                estimatedYawChange = (float) MathUtils.getIncremental(estimatedYawChange, 20);

                // The bigger the difference, the less we prefer to swap to them.
                if (estimatedYawChange >= 60) {
                    weight += estimatedYawChange / 20;
                }
            }
        }

        return weight;
    }


    private List<EntityLivingBase> getTargets() {
        List<EntityLivingBase> targets = new ArrayList<>();
        boolean priorityOnly = false;
        boolean allowPriorityOnly = !priority.getSelected().equals("Bounty Vamp") || exclusiveMode.getValue();
        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) o;
                if (validEntity(entity)) {
                    if (allowPriorityOnly) {
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
        int fov = this.fov.getValue().intValue();
        return Math.abs(RotationUtils.getYawChange(entity.posX, entity.posZ)) <= fov && Math.abs(RotationUtils.getPitchChange(entity, entity.posY)) <= fov;
    }

}