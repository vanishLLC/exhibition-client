/*
 * Time: 2:49:26 AM
 * Date: Jan 8, 2017
 * Creator: cool1
 */
package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.combat.Criticals;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import exhibition.util.security.BypassValues;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Speed extends Module {

    private String MODE = "MODE";
    private boolean lastGround;
    private double speed;
    private double lastDist;
    private double velocityBoost;
    public static int stage;
    public Setting<Boolean> step = new Setting<>("STEP", false, "Disables speed while stepping up multiple stairs/slabs.");
    public Setting<Boolean> water = new Setting<>("WATER", false, "Disables Speed while in water.");
    public Setting<Boolean> firstSlow = new Setting<>("FIRST-SLOW", false, "Makes the first jump slightly slower.");
    public Setting<Boolean> scaffold = new Setting<>("SCAFFOLD", true, "Disables Scaffold when Speed is Enabled.");
    public Setting<Boolean> strafeFix = new Setting<>("STRAFE-FIX", true, "Strafing fix for Hypixel. Use low steps if disabled.");
    public Setting<Boolean> fastFall = new Setting<>("FAST-FALL", false, "Speeds up falling to make you jump sooner.");
    public Setting<Number> retard = new Setting<>("STEPS", 40.3, "Allows you to smooth strafing. (Higher values require Strafe Fix)", 0.1, 1, 180);
    public Setting<Number> boostScale = new Setting<>("VEL-BOOST", 0.5, "Boosts your speed when you take KB.", 0.01, 0, 1);

    //public Setting<Number> jump_offset = new Setting<>("JUMP-OFFSET", 0, "Allows you to increase/decrease your jump height.", 0.01, -0.5, 0.5);
    public Setting<Number> timer_boost = new Setting<>("TIMER-BOOST", 0, "Speeds up the game to move \"faster.\"", 0.1, 0, 3);
    public Options boost_mode = new Options("Boost Mode", "Normal", "Normal", "Dynamic");

    public int hops = 0;
    private int ticks = 0;
    private boolean reset = false;

    private float currentYaw;

    private boolean modifiedTimer = false;

    public Speed(ModuleData data) {
        super(data);
        addSetting(step);
        addSetting(water);
        addSetting(scaffold);
        addSetting(retard);
        addSetting(boostScale);
        addSetting(strafeFix);
        addSetting(fastFall);
        addSetting(firstSlow);

        addSetting(timer_boost);
        //addSetting(jump_offset);
        addSetting(new Setting<>("BOOST MODE", boost_mode, "Timer boost method."));

        addSetting(new Setting<>(MODE, new Options("Speed Mode", "HypixelHop", /*"StrafeTest", */"HypixelHop", "HypixelLow", "HypixelHopOld", "HypixelOld", "Mineplex", "Hop", "OnGround", "YPort", "OldHop", "OldSlow"), "Speed bypass method."));

    }

    private double defaultSpeed() {
        double baseSpeed = 0.28730000691562896;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && (!((Options) settings.get(MODE).getValue()).getSelected().startsWith("HypixelHop") || mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.15D * (amplifier + 1));
        }
        return baseSpeed;
    }

    @Override
    public void onEnable() {
        reset = false;
        if (mc.thePlayer != null) {
            speed = defaultSpeed();
        }
        velocityBoost = 0;
        lastDist = 0.0;
        stage = 2;
        hops = 0;
        Module[] modules = new Module[]{Client.getModuleManager().get(Phase.class), Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(LongJump.class), Client.getModuleManager().get(Scaffold.class), Client.getModuleManager().get(Phase.class)};
        boolean disabled = false;
        for (Module module : modules) {
            if (module.isEnabled() && (module != Client.getModuleManager().get(Scaffold.class) || scaffold.getValue())) {
                module.toggle();
                disabled = true;
            }
        }
        if (disabled) {
            Notifications.getManager().post("Movement Check", "Disabled extra modules.", 1000, Notifications.Type.NOTIFY);
        }

        Killaura killaura = (Killaura) Client.getModuleManager().get(Killaura.class);
        if (killaura.getCurrentTarget() != null && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && Client.getModuleManager().isEnabled(Criticals.class)) {
            stage = -1;
        }
        String currentMode = ((Options) settings.get(MODE).getValue()).getSelected();
        if (currentMode.contains("Hypixel")) {
            stage = 1;
        }
        if (mc.thePlayer != null) {
            this.currentYaw = mc.thePlayer.rotationYaw;
        }
        lastGround = false;
    }

    int steps;

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        lastGround = false;
    }

    @RegisterEvent(events = {EventMove.class, EventPacket.class, EventMotionUpdate.class, EventStep.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        String currentMode = ((Options) settings.get(MODE).getValue()).getSelected();
        setSuffix(currentMode);
        if (mc.thePlayer.isDead)
            return;

        if (stage > 3) {
            reset = false;
        }

        if (stage < 0 && event instanceof EventRenderGui && !currentMode.equalsIgnoreCase("onground")) {
            ScaledResolution res = new ScaledResolution(mc);
            //Renders block count. TODO: Make text clearer.
            int color = Colors.getColor(255, 0, 0, 255);

            String bruh = "Speed disabled " + MathUtils.roundToPlace((Math.abs(stage) / 20F), 1) + "s";
            reset = true;

            GlStateManager.enableBlend();
            Depth.pre();
            Depth.mask();
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F, res.getScaledHeight() / 2F - 37, color);
            Depth.render(GL11.GL_LESS);
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F + 1, res.getScaledHeight() / 2F - 37, Colors.getColor(0, 230));
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F, res.getScaledHeight() / 2F - 37 + 1, Colors.getColor(0, 230));
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F - 1, res.getScaledHeight() / 2F - 37, Colors.getColor(0, 230));
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F, res.getScaledHeight() / 2F - 37 - 1, Colors.getColor(0, 230));
            Depth.post();
            mc.fontRendererObj.drawString(bruh, res.getScaledWidth() / 2F - mc.fontRendererObj.getStringWidth(bruh) / 2F, res.getScaledHeight() / 2F - 37, color);
            GlStateManager.disableBlend();
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S08PacketPlayerPosLook) {
                stage = -15;
                speed = 0;
                velocityBoost = 0;
                lastDist = 0;
            }

            if (packet instanceof S45PacketTitle) {
                S45PacketTitle titlePacket = ((S45PacketTitle) packet);
                if (titlePacket.getType().equals(S45PacketTitle.Type.TITLE)) {
                    String text = StringUtils.stripControlCodes(titlePacket.getMessage().getFormattedText());
                    if ((text.toLowerCase().contains("died") || text.toLowerCase().contains("game over")) && isEnabled()) {
                        lastGround = true; // rename to shouldToggle
                        Notifications.getManager().post("Speed Death", "Speed disabled due to death.");
                    }
                }
            }

            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) packet;
                if (velocity.getEntityID() == mc.thePlayer.getEntityId()) {
                    double x = (double) velocity.getMotionX() / 8000.0D;
                    double y = (double) velocity.getMotionY() / 8000.0D;
                    double z = (double) velocity.getMotionZ() / 8000.0D;
                    if (x != 0 && y != 0 && z != 0 && velocityBoost == 0)
                        velocityBoost = MathHelper.sqrt_double(x * x + z * z) * boostScale.getValue().doubleValue();
                }
            }
            if (packet instanceof S27PacketExplosion) {
                S27PacketExplosion velocity = (S27PacketExplosion) packet;
                if (velocity.xMotion != 0 && velocity.yMotion != 0 && velocity.zMotion != 0 && velocityBoost == 0)
                    velocityBoost = MathHelper.sqrt_double(velocity.xMotion * velocity.xMotion + velocity.zMotion * velocity.zMotion) * boostScale.getValue().doubleValue();
            }
            return;
        }
        if (lastGround) {
            toggle();
            return;
        }
        if (event instanceof EventStep && step.getValue()) {
            EventStep eventStep = event.cast();
            if (eventStep.isPre()) {
                steps++;
                if (steps > 4)
                    Speed.stage = -1;
            }
            return;
        }
        if ((water.getValue() && PlayerUtil.isInLiquid()) || (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))) {
            return;
        }

        float boost = timer_boost.getValue().floatValue();
        if (boost > 0 && stage > 0) {
            modifiedTimer = true;
            if (event instanceof EventMotionUpdate) {
                EventMotionUpdate em = event.cast();
                if (em.isPre()) {
                    float add = boost + ((boost / 10F) * (float) Math.random());
                    if (boost_mode.getSelected().equals("Dynamic")) {
                        if (hops > 1 && mc.thePlayer.onGround) {
                            mc.timer.timerSpeed = (1 - boost / 7F) + (0.05F * (float) Math.random());
                        } else {
                            mc.timer.timerSpeed = 1 + add / (stage);
                        }
                    } else if (boost_mode.getSelected().equals("Normal")) {
                        mc.timer.timerSpeed = 1 + add;
                    }
                }
            }
        } else if (modifiedTimer) {
            modifiedTimer = false;
            mc.timer.timerSpeed = 1;
        }

        switch (currentMode) {
            case "HypixelHop": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (stage < 0) {
                        stage++;
                        lastDist = 0;
                        velocityBoost = 0;
                        hops = 0;
                        break;
                    }

                    if (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))
                        return;

                    if (steps > 2)
                        steps = 0;

                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
                        speed = defaultSpeed();
                        velocityBoost = 0;
                    }

                    if (AutoPot.wantsToPot) {
                        stage = 0;
                        hops = 0;
                        return;
                    }

                    boolean canSprint = mc.thePlayer.getFoodStats().getFoodLevel() >= 6;

                    double moveSpeed = speed = (defaultSpeed()) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 : (mc.thePlayer.isSneaking()) ? 0.8 : (PlayerUtil.isInLiquid() ? 0.54 : (reset) ? 0.45 : ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 : canSprint ? (ticks == 1 && firstSlow.getValue()) ? 0.793 : 1.0 : 0.765)));

                    int current = stage;
//                    double offsetY = mc.thePlayer.posY - (int) mc.thePlayer.posY;
//
//                    ChatUtil.printChat(stage + (mc.thePlayer.onGround ? " GROUND " : " ") + offsetY + " " + mc.thePlayer.motionY);

                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        speed = lastDist;
                        ticks = 1;
                        hops = 0;
                    } else if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        double gay = 0 /*jump_offset.getValue().doubleValue() == 0 ? 0 : jump_offset.getValue().floatValue() / 20.5F*/;
                        BypassValues.offsetJump(em, this);
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = (em.getY() + gay));
                        velocityBoost /= 5;

                        speed = moveSpeed * 2.13050398;

                        {
                            double forward = mc.thePlayer.movementInput.moveForward;
                            double strafe = mc.thePlayer.movementInput.moveStrafe;
                            TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                            float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                            boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                            if (forward == 0.0f && strafe == 0.0f) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                                mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                                em.setX(0);
                                em.setZ(0);
                                if (mc.thePlayer.onGround)
                                    currentYaw = mc.thePlayer.rotationYaw;
                            } else if (!isCircleStrafing) {
                                if (forward != 0.0D) {
                                    if (forward < 0.0D) {
                                        yaw -= 180;
                                    }

                                    if (strafe > 0.0D) {
                                        yaw += (forward > 0.0D ? -43.51F : 43.51F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (forward > 0.0D ? 43.51F : -43.51F);
                                    }
                                } else {
                                    if (strafe > 0.0D) {
                                        yaw += (-88.58F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (88.58F);
                                    }
                                }
                            }

                            float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                            float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : 180;

                            if (Math.abs(difference) >= cap) {
                                difference = MathHelper.clamp_float(difference, -cap, cap);
                            }

                            currentYaw += difference;
                        }
                    } else if (stage == 3) {
//                        double baseSpeed = 0.25999999999999997;
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
//                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
//                            baseSpeed *= (1.0D + 0.135D * (amplifier + 1));
//                        }
//
//                        double bruh = 0.7666;
//
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
//                            if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
//                                bruh = 0.71193D;
//                            } else {
//                                bruh = 0.678D;
//                            }
//                        }
//
//                        final double difference = bruh * (lastDist - baseSpeed);

                        double baseSpeed = 0.3303950079529733;
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && (!((Options) settings.get(MODE).getValue()).getSelected().startsWith("HypixelHop") || mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10)) {
                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                            baseSpeed *= (1.0D + 0.15D * (amplifier + 1));
                        }

                        speed = lastDist - (0.666 * (lastDist - (ticks == 1 ? defaultSpeed() : baseSpeed)) - (0.00000125F / Math.max(hops, 1)));

//                        speed = lastDist * (ticks == 1 ? 0.59989892348 : 0.587622177);

                    } else {
                        if (stage == 7 && fastFall.getValue()) {
                            em.setY(mc.thePlayer.motionY += -(0.18999F + (0.00000004F * Math.random())));
                        }

                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                            if (stage == 0) {
                                ticks = 1;
                            } else {
                                ticks = 0;
                            }
                        }

//                        List<Double> list = new ArrayList<>();
//
//                        double a = lastDist - lastDist / 160;
//                        double b = lastDist - (lastDist - defaultSpeed()) / 33.3;
//                        double c = lastDist - (lastDist - moveSpeed) * 0.020000000000000018D;
//
//                        list.add(a);
//                        list.add(b);
//                        list.add(c);
//
//                        list.sort(Double::compare);

                        this.speed = moveSpeed * 1.255 * Math.pow(0.99, (current - 1)) - (0.0001125F / Math.max(1, hops));

//                        this.speed = list.get(2) - 0.0000125F;
                    }

                    speed = Math.max(speed, moveSpeed);

                    if (velocityBoost != 0 && velocityBoost <= 0.05) {
                        velocityBoost = 0;
                    }
                    if (velocityBoost != 0 && stage > 2) {
                        speed += velocityBoost;
                    }
                    if (stage > 2)
                        velocityBoost *= 0.66;

                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
                    if (current > 1 && stage > 0) {
                        double forward = mc.thePlayer.movementInput.moveForward;
                        double strafe = mc.thePlayer.movementInput.moveStrafe;
                        TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                        if (forward == 0.0f && strafe == 0.0f) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                            em.setX(0);
                            em.setZ(0);
                            if (mc.thePlayer.onGround)
                                currentYaw = mc.thePlayer.rotationYaw;
                        } else if (!isCircleStrafing) {
                            if (forward != 0.0D) {
                                double oldForward = forward;
                                if (forward > 0.0D) {
                                    forward = 1;
                                } else if (forward < 0.0D) {
                                    forward = 0.85F;
                                    yaw -= 179.83;
                                }

                                if (strafe != 0) {
                                    forward = 0.985F;
                                    if (strafe > 0.0D) {
                                        yaw += (oldForward > 0.0D ? -44.5933 : 44.5933);
                                    } else if (strafe < 0.0D) {
                                        yaw += (oldForward > 0.0D ? 44.5933 : -44.5933);
                                    }
                                }
                            } else {
                                if (strafe > 0.0D) {
                                    yaw += (-89.453);
                                } else if (strafe < 0.0D) {
                                    yaw += (89.453);
                                }
                                forward = 0.75F;
                            }
                        }

                        float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                        float cap = isCircleStrafing ? Math.min(retard.getValue().floatValue(), ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue()) : retard.getValue().floatValue();

                        if (isCircleStrafing) {
                            forward = 0.985F;
                        }

                        if (Math.abs(difference) >= cap) {
                            difference = MathHelper.clamp_float(difference, -cap, cap);
                        }

                        currentYaw += difference;

                        double mx = Math.cos(Math.toRadians(currentYaw + 90));
                        double mz = Math.sin(Math.toRadians(currentYaw + 90));

                        em.setX((forward * this.speed * mx));
                        em.setZ((forward * this.speed * mz));
                    }
                    //If the player is moving, step the stage up.
                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                        if (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel() && stage > 1 && lastDist > 0 && !PlayerUtil.isOnLiquid()) {
                            if (em.isOnground()) {
                                hops++;
                                if (hops > 1) {
                                    BypassValues.offsetGround(em, mc.thePlayer);
                                }
                            }
                        }

                    }
                }
                break;
            }
            case "HypixelLow": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (stage < 0) {
                        stage++;
                        lastDist = 0;
                        velocityBoost = 0;
                        hops = 0;
                        break;
                    }

                    if (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))
                        return;

                    if (steps > 2)
                        steps = 0;

                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
                        speed = defaultSpeed();
                        velocityBoost = 0;
                    }

                    if (AutoPot.wantsToPot) {
                        stage = 0;
                        hops = 0;
                        return;
                    }

                    boolean canSprint = mc.thePlayer.getFoodStats().getFoodLevel() >= 6;

                    double moveSpeed = speed = (defaultSpeed()) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 :
                            (mc.thePlayer.isSneaking()) ? 0.8 :
                                    (PlayerUtil.isInLiquid() ? 0.54 :
                                            (reset) ? 0.45 :
                                                    ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 :
                                                            canSprint ? (ticks == 1 && firstSlow.getValue()) ? 0.793 :
                                                                    Client.getModuleManager().isEnabled(Scaffold.class) ? 0.8 : 1.0 : 0.765)));

                    int current = stage;
//                    double offsetY = mc.thePlayer.posY - (int) mc.thePlayer.posY;
//
//                    ChatUtil.printChat(stage + (mc.thePlayer.onGround ? " GROUND " : " ") + offsetY + " " + mc.thePlayer.motionY);

                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        speed = lastDist;
                        ticks = 1;
                        hops = 0;
                    } else if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        double gay = 0 /*jump_offset.getValue().doubleValue() == 0 ? 0 : jump_offset.getValue().floatValue() / 20.5F*/;
                        BypassValues.offsetJumpNovo(em, this);
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = (em.getY() + gay));
                        velocityBoost /= 5;

                        speed = lastDist * 2.13050398;

                        {
                            double forward = mc.thePlayer.movementInput.moveForward;
                            double strafe = mc.thePlayer.movementInput.moveStrafe;
                            TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                            float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                            boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                            if (forward == 0.0f && strafe == 0.0f) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                                mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                                em.setX(0);
                                em.setZ(0);
                                if (mc.thePlayer.onGround)
                                    currentYaw = mc.thePlayer.rotationYaw;
                            } else if (!isCircleStrafing) {
                                if (forward != 0.0D) {
                                    if (forward < 0.0D) {
                                        yaw -= 180;
                                    }

                                    if (strafe > 0.0D) {
                                        yaw += (forward > 0.0D ? -43.51F : 43.51F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (forward > 0.0D ? 43.51F : -43.51F);
                                    }
                                } else {
                                    if (strafe > 0.0D) {
                                        yaw += (-88.58F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (88.58F);
                                    }
                                }
                            }

                            float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                            float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : 180;

                            if (Math.abs(difference) >= cap) {
                                difference = MathHelper.clamp_float(difference, -cap, cap);
                            }

                            currentYaw += difference;
                        }
                    } else if (stage == 3) {
//                        double baseSpeed = 0.25999999999999997;
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
//                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
//                            baseSpeed *= (1.0D + 0.135D * (amplifier + 1));
//                        }
//
//                        double bruh = 0.7666;
//
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
//                            if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
//                                bruh = 0.71193D;
//                            } else {
//                                bruh = 0.678D;
//                            }
//                        }
//
//                        final double difference = bruh * (lastDist - baseSpeed);

                        double baseSpeed = 0.3303950079529733;
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && (!((Options) settings.get(MODE).getValue()).getSelected().startsWith("HypixelHop") || mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10)) {
                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                            baseSpeed *= (1.0D + 0.15D * (amplifier + 1));
                        }

                        //speed = lastDist - (0.666 * (lastDist - (ticks == 1 ? defaultSpeed() : baseSpeed)) - (0.00000125F / Math.max(hops, 1)));

                        this.speed = this.lastDist - 0.819999 * (this.lastDist - moveSpeed);

//                        speed = lastDist * (ticks == 1 ? 0.59989892348 : 0.587622177);

                    } else {
                        if (stage == 7 && fastFall.getValue()) {
                            em.setY(mc.thePlayer.motionY += -(0.18999F + (0.00000004F * Math.random())));
                        }

                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                            if (stage == 0) {
                                ticks = 1;
                            } else {
                                ticks = 0;
                            }
                        }

//                        List<Double> list = new ArrayList<>();
//
//                        double a = lastDist - lastDist / 160;
//                        double b = lastDist - (lastDist - defaultSpeed()) / 33.3;
//                        double c = lastDist - (lastDist - moveSpeed) * 0.020000000000000018D;
//
//                        list.add(a);
//                        list.add(b);
//                        list.add(c);
//
//                        list.sort(Double::compare);

                        // this.speed = moveSpeed * 1.255 * Math.pow(0.99, (current - 1)) - (0.0001125F / Math.max(1, hops));

                        double c = lastDist - (lastDist / 50);

                        this.speed = c;

//                        this.speed = list.get(2) - 0.0000125F;
                    }

                    if(stage == 7) {
                        em.setY(mc.thePlayer.motionY = -0.18517061160392762);
                    }


                    speed = Math.max(speed, moveSpeed);

                    if (velocityBoost != 0 && velocityBoost <= 0.05) {
                        velocityBoost = 0;
                    }
                    if (velocityBoost != 0 && stage > 2) {
                        speed += velocityBoost;
                    }
                    if (stage > 2)
                        velocityBoost *= 0.66;

                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
                    if (current > 1 && stage > 0) {
                        double forward = mc.thePlayer.movementInput.moveForward;
                        double strafe = mc.thePlayer.movementInput.moveStrafe;
                        TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                        if (forward == 0.0f && strafe == 0.0f) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                            em.setX(0);
                            em.setZ(0);
                            if (mc.thePlayer.onGround)
                                currentYaw = mc.thePlayer.rotationYaw;
                        } else if (!isCircleStrafing) {
                            if (forward != 0.0D) {
                                double oldForward = forward;
                                if (forward > 0.0D) {
                                    forward = 1;
                                } else if (forward < 0.0D) {
                                    forward = 0.85F;
                                    yaw -= 179.83;
                                }

                                if (strafe != 0) {
                                    forward = 0.985F;
                                    if (strafe > 0.0D) {
                                        yaw += (oldForward > 0.0D ? -44.5933 : 44.5933);
                                    } else if (strafe < 0.0D) {
                                        yaw += (oldForward > 0.0D ? 44.5933 : -44.5933);
                                    }
                                }
                            } else {
                                if (strafe > 0.0D) {
                                    yaw += (-89.453);
                                } else if (strafe < 0.0D) {
                                    yaw += (89.453);
                                }
                                forward = 0.75F;
                            }
                        }

                        float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                        float cap = isCircleStrafing ? Math.min(retard.getValue().floatValue(), ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue()) : retard.getValue().floatValue();

                        if (isCircleStrafing) {
                            forward = 0.985F;
                        }

                        if (Math.abs(difference) >= cap) {
                            difference = MathHelper.clamp_float(difference, -cap, cap);
                        }

                        currentYaw += difference;

                        double mx = Math.cos(Math.toRadians(currentYaw + 90));
                        double mz = Math.sin(Math.toRadians(currentYaw + 90));

                        em.setX((forward * this.speed * mx));
                        em.setZ((forward * this.speed * mz));
                    }
                    //If the player is moving, step the stage up.
                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                        if (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel() && lastDist > 0) {
                            boolean ground = em.isOnground();
                            if (hops > 0) {
                                BypassValues.novolineStrafeFix(em, mc.thePlayer);
                            }
                            if (ground) {
                                hops++;
                            }
                        }

//                        if (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel() && stage > 1 && lastDist > 0 && !PlayerUtil.isOnLiquid()) {
//                            if (em.isOnground()) {
//                                hops++;
//                                if (hops > 1) {
//                                    BypassValues.offsetGround(em, mc.thePlayer);
//                                }
//                            }
//                        }

                    }
                }
                break;
            }
            case "HypixelHopOld": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (stage < 0) {
                        stage++;
                        lastDist = 0;
                        velocityBoost = 0;
                        break;
                    }

                    if (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))
                        return;

                    if (steps > 2)
                        steps = 0;

                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
                        speed = defaultSpeed();
                        velocityBoost = 0;
                    }

                    if (AutoPot.wantsToPot) {
                        stage = 0;
                        return;
                    }

                    boolean canSprint = mc.thePlayer.getFoodStats().getFoodLevel() >= 6;

                    double defaultSpeed = 0.27999999999999997;
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
                        int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                        defaultSpeed *= (1.0D + 0.135D * (amplifier + 1));
                    }

                    double moveSpeed = speed = (defaultSpeed) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 : (mc.thePlayer.isSneaking()) ? 0.8 : (PlayerUtil.isInLiquid() ? 0.54 : (reset) ? 0.45 : ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 : canSprint ? (ticks == 1 && firstSlow.getValue()) ? 0.793 : 1.0 : 0.765)));

                    int current = stage;

                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        speed = lastDist;
                        ticks = 1;
                    } else if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        double gay = /*jump_offset.getValue().doubleValue() == 0 ? */0/* : jump_offset.getValue().floatValue() / 20.5F*/;
                        BypassValues.offsetJump(em, this);
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = (em.getY() + gay));
                        velocityBoost /= 5;

                        speed = moveSpeed * 2.19959724;

                        {
                            double forward = mc.thePlayer.movementInput.moveForward;
                            double strafe = mc.thePlayer.movementInput.moveStrafe;
                            TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                            float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                            boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                            if (forward == 0.0f && strafe == 0.0f) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                                mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                                em.setX(0);
                                em.setZ(0);
                                if (mc.thePlayer.onGround)
                                    currentYaw = mc.thePlayer.rotationYaw;
                            } else if (!isCircleStrafing) {
                                if (forward != 0.0D) {
                                    if (forward < 0.0D) {
                                        yaw -= 180;
                                    }

                                    if (strafe > 0.0D) {
                                        yaw += (forward > 0.0D ? -43.51F : 43.51F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (forward > 0.0D ? 43.51F : -43.51F);
                                    }
                                } else {
                                    if (strafe > 0.0D) {
                                        yaw += (-88.58F);
                                    } else if (strafe < 0.0D) {
                                        yaw += (88.58F);
                                    }
                                }
                            }

                            float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                            float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : 180;

                            if (Math.abs(difference) >= cap) {
                                difference = MathHelper.clamp_float(difference, -cap, cap);
                            }

                            currentYaw += difference;
                        }
                    } else if (stage == 3) {
                        double baseSpeed = 0.25999999999999997;
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                            baseSpeed *= (1.0D + 0.135D * (amplifier + 1));
                        }

                        double bruh = 0.7666;

                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
                            if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
                                bruh = 0.71193D;
                            } else {
                                bruh = 0.678D;
                            }
                        }

                        if (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel())
                            em.setY(mc.thePlayer.motionY -= 0.0088425F + (0.0000000325 * Math.random()));

                        final double difference = ((firstSlow.getValue() && ticks == 1 ? 1.02089224 : 1) * bruh) * (lastDist - baseSpeed);
                        speed = lastDist - difference;
                    } else {
                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                            if (stage == 0) {
                                ticks = 1;
                            } else {
                                ticks = 0;
                            }
                        }

                        double baseSpeed = 0.25999999999999997;
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 10) {
                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                            baseSpeed *= (1.0D + 0.135D * (amplifier + 1));
                        }

                        List<Double> list = new ArrayList<>();

                        double a = lastDist - lastDist / 160;
                        double b = lastDist - (lastDist - baseSpeed) / 33.3;
                        double c = lastDist - (lastDist - baseSpeed) * 0.020000000000000018D;

                        list.add(a);
                        list.add(b);
                        list.add(c);

                        list.sort(Double::compare);

                        this.speed = list.get(2) - 0.0000125F;
                    }

                    speed = Math.max(speed, moveSpeed);

                    if (velocityBoost != 0 && velocityBoost <= 0.05) {
                        velocityBoost = 0;
                    }
                    if (velocityBoost != 0 && stage > 2) {
                        speed += velocityBoost;
                    }
                    if (stage > 2)
                        velocityBoost *= 0.66;

                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
                    if (current > 1 && stage > 0) {
                        double forward = mc.thePlayer.movementInput.moveForward;
                        double strafe = mc.thePlayer.movementInput.moveStrafe;
                        TargetStrafe targetStrafe = Client.getModuleManager().get(TargetStrafe.class);
                        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                        if (forward == 0.0f && strafe == 0.0f) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                            em.setX(0);
                            em.setZ(0);
                            if (mc.thePlayer.onGround)
                                currentYaw = mc.thePlayer.rotationYaw;
                        } else if (!isCircleStrafing) {
                            if (forward != 0.0D) {
                                double oldForward = forward;
                                if (forward > 0.0D) {
                                    forward = 1;
                                } else if (forward < 0.0D) {
                                    forward = 1;
                                    yaw -= 179.83;
                                }

                                if (strafe != 0) {
                                    forward = 0.985F;
                                    if (strafe > 0.0D) {
                                        yaw += (oldForward > 0.0D ? -44.5933 : 44.5933);
                                    } else if (strafe < 0.0D) {
                                        yaw += (oldForward > 0.0D ? 44.5933 : -44.5933);
                                    }
                                }
                            } else {
                                if (strafe > 0.0D) {
                                    yaw += (-89.453);
                                } else if (strafe < 0.0D) {
                                    yaw += (89.453);
                                }
                                forward = 0.985F;
                            }
                        }

                        float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                        float cap = isCircleStrafing ? Math.min(retard.getValue().floatValue(), ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue()) : retard.getValue().floatValue();

                        if (Math.abs(difference) >= cap) {
                            difference = MathHelper.clamp_float(difference, -cap, cap);
                        }

                        currentYaw += difference;

                        double mx = Math.cos(Math.toRadians(currentYaw + 90));
                        double mz = Math.sin(Math.toRadians(currentYaw + 90));

                        em.setX((forward * this.speed * mx));
                        em.setZ((forward * this.speed * mz));
                    }
                    //If the player is moving, step the stage up.
                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                        if (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel() && stage > 0 && lastDist > 0 && !PlayerUtil.isOnLiquid()) {
                            if (em.isOnground()) {
                                hops++;
                                BypassValues.offsetGround(em, mc.thePlayer);
                            }
                        }
                    }
                }
                break;
            }
//            case "HypixelSlow": {
//                if (event instanceof EventMove) {
//                    EventMove em = (EventMove) event;
//                    if (stage < 0) {
//                        stage++;
//                        lastDist = 0;
//                        velocityBoost = 0;
//                        break;
//                    }
//                    double oldY = em.getY();
//                    if (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))
//                        return;
//
//                    if (steps > 2)
//                        steps = 0;
//
//                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
//                        speed = defaultSpeed();
//                    }
//                    double e = MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3);
//                    boolean didthebruh = false;
//                    if (lowhop.getValue() && !PlayerUtil.isInLiquid() && !mc.gameSettings.keyBindJump.getIsKeyPressed() && ((boolean) lowhopTarget.getValue() || Killaura.getTarget() == null) && !mc.thePlayer.isPotionActive(Potion.jump) && em.getY() > 0 && !mc.thePlayer.isCollidedHorizontally &&
//                            (e == MathUtils.roundToPlace(0.42, 3) ||
//                                    e == MathUtils.roundToPlace(0.92, 3) ||
//                                    e == MathUtils.roundToPlace(0.545, 3) ||
//                                    e == MathUtils.roundToPlace(0.482, 3))) {
//                        em.setY(mc.thePlayer.motionY -= 0.5F);
//                        didthebruh = true;
//                    }
//
//                    if (AutoPot.wantsToPot) {
//                        stage = 0;
//                        return;
//                    }
//
//                    double moveSpeed = speed = (defaultSpeed()) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 : (mc.thePlayer.isSneaking()) ? 0.8 : (PlayerUtil.isInLiquid() ? 0.6 : (reset) ? 0.53 : ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 : 1.0)));
//
//                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                        stage = 2;
//                    }
//                    if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                        double jumpVelocity = 0.42F;
//                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
//                            jumpVelocity += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
//                        }
//                        em.setY(mc.thePlayer.motionY = jumpVelocity);
//                        velocityBoost /= 2;
//                        mc.thePlayer.isAirBorne = true;
//                        speed = moveSpeed * 1.84445949644;
//
//                        {
//                            double forward = mc.thePlayer.movementInput.moveForward;
//                            double strafe = mc.thePlayer.movementInput.moveStrafe;
//                            TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
//                            float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
//                            boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
//                            if (forward == 0.0f && strafe == 0.0f) {
//                                mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
//                                mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
//                                em.setX(0);
//                                em.setZ(0);
//                                if (mc.thePlayer.onGround)
//                                    currentYaw = mc.thePlayer.rotationYaw;
//                            } else if (!isCircleStrafing) {
//                                if (forward != 0.0D) {
//                                    if (forward < 0.0D) {
//                                        yaw -= 180;
//                                    }
//
//                                    if (strafe > 0.0D) {
//                                        yaw += (forward > 0.0D ? -43.51F : 43.51F);
//                                    } else if (strafe < 0.0D) {
//                                        yaw += (forward > 0.0D ? 43.51F : -43.51F);
//                                    }
//                                } else {
//                                    if (strafe > 0.0D) {
//                                        yaw += (-88.58F);
//                                    } else if (strafe < 0.0D) {
//                                        yaw += (88.58F);
//                                    }
//                                }
//                            }
//
//                            float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));
//
//                            float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : 180;
//
//                            if (Math.abs(difference) >= cap) {
//                                difference = MathHelper.clamp_float(difference, -cap, cap);
//                            }
//
//                            currentYaw += difference;
//                        }
//                    } else if (stage == 3) {
//                        final double difference = 0.6375 * (lastDist - moveSpeed);
//                        speed = lastDist - difference;
//                    } else {
//                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
//                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
//                            stage = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
//                        }
//
//                        List<Double> list = new ArrayList<>();
//
//                        double a = lastDist - lastDist / 160;
//                        double b = lastDist - (lastDist - moveSpeed) / 33.3;
//                        double c = lastDist - (lastDist - moveSpeed) / 50;
//
//                        list.add(a);
//                        list.add(b);
//                        list.add(c);
//
//                        list.sort(Double::compare);
//
//                        this.speed = list.get(2) - 0.0000125F;
//
//                    }
//                    speed = Math.max(speed, moveSpeed);
//
//                    if (velocityBoost != 0 && stage >= 3) {
//                        speed += velocityBoost;
//                    }
//                    velocityBoost *= 0.66;
//
//                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
//                    if (stage > 0) {
//                        double forward = mc.thePlayer.movementInput.moveForward;
//                        double strafe = mc.thePlayer.movementInput.moveStrafe;
//                        TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
//                        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
//                        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
//                        if (forward == 0.0f && strafe == 0.0f) {
//                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
//                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
//                            em.setX(0);
//                            em.setZ(0);
//                            if (mc.thePlayer.onGround)
//                                currentYaw = mc.thePlayer.rotationYaw;
//                        } else if (!isCircleStrafing) {
//                            if (forward != 0.0D) {
//                                double oldForward = forward;
//                                if (forward > 0.0D) {
//                                    forward = 1;
//                                } else if (forward < 0.0D) {
//                                    forward = 1;
//                                    yaw -= 180;
//                                }
//
//                                if (strafe > 0.0D) {
//                                    yaw += (oldForward > 0.0D ? -43.51F : 43.51F);
//                                } else if (strafe < 0.0D) {
//                                    yaw += (oldForward > 0.0D ? 43.51F : -43.51F);
//                                }
//                            } else {
//                                if (strafe > 0.0D) {
//                                    yaw += (-88.58F);
//                                } else if (strafe < 0.0D) {
//                                    yaw += (88.58F);
//                                }
//                                forward = 0.985F;
//                            }
//                        }
//
//                        float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));
//
//                        float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : mc.thePlayer.hurtTime > 7 ? Math.max(((Number) retard.getValue()).floatValue(), 90) : ((Number) retard.getValue()).floatValue();
//
//                        if (Math.abs(difference) >= cap) {
//                            difference = MathHelper.clamp_float(difference, -cap, cap);
//                        }
//
//                        currentYaw += difference;
//
//                        double mx = Math.cos(Math.toRadians(currentYaw + 90));
//                        double mz = Math.sin(Math.toRadians(currentYaw + 90));
//
//                        double oldX = em.getX();
//                        double oldZ = em.getZ();
//
//                        em.setX((forward * this.speed * mx));
//                        em.setZ((forward * this.speed * mz));
//
//                        double var41 = 0.0625F;
//
//
//                        if (didthebruh) {
//                            List bruh = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(em.getX() * 1.513 * 3.25, em.getY(), em.getZ() * 1.513 * 3.25).contract(var41, var41, var41));
//                            List bruh2 = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(em.getX() * 1.513 * 2.25, em.getY(), em.getZ() * 1.513 * 2.25).contract(var41, var41, var41));
//                            List bruh3 = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(em.getX() * 1.513, em.getY(), em.getZ() * 1.513).contract(var41, var41, var41));
//
//                            List touchGround = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(em.getX() + (em.getX() - em.getX() / 159.0D), em.getY() - 0.41999998688697815, em.getZ() + (em.getZ() - em.getZ() / 159.0D)).contract(var41, var41, var41));
//                            if (!bruh.isEmpty() || !bruh2.isEmpty() || !bruh3.isEmpty() || (touchGround.isEmpty() && !Client.getModuleManager().isEnabled(Scaffold.class))) {
//                                em.setY(mc.thePlayer.motionY = oldY);
//                            }
//                        }
//
//                    }
//                    //If the player is moving, step the stage up.
//                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
//                        ++stage;
//                    }
//                }
//                if (event instanceof EventMotionUpdate) {
//                    EventMotionUpdate em = (EventMotionUpdate) event;
//                    if (em.isPre()) {
//                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
//                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
//                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
//                        if (PlayerUtil.isMoving() && stage > 0) {
//                            if (em.getY() % 0.015625 == 0) {
//                                em.setY(em.getY() + 0.00053424);
//                                em.setGround(false);
//                            }
//
//                            if (mc.thePlayer.motionY > 0.3) {
//                                em.setGround(true);
//                            }
//                        }
//                    }/* else {
//                        ChatUtil.debug((em.getY() - 64) + " " + ((mc.thePlayer.posY - mc.thePlayer.lastTickPosY)) + " " + (mc.thePlayer.posY - (int) mc.thePlayer.posY) + " " + mc.thePlayer.motionY);
//                    }*/
//                }
//                break;
//            }
            case "HypixelOld": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (stage < 0) {
                        stage++;
                        lastDist = 0;
                        velocityBoost = 0;
                        break;
                    }
                    if (Killaura.blockJump && Client.getModuleManager().isEnabled(Killaura.class))
                        return;

                    if (steps > 2)
                        steps = 0;

                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
                        speed = defaultSpeed();
                    }

                    if (AutoPot.wantsToPot) {
                        stage = 0;
                        return;
                    }

                    double moveSpeed = speed = (defaultSpeed()) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 : (mc.thePlayer.isSneaking()) ? 0.8 : (PlayerUtil.isInLiquid() ? 0.54 : (reset) ? 0.53 : ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 : 1.0)));

                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        stage = 2;
                    }
                    if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        double gay = 0.42F;
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = gay);
                        mc.thePlayer.isAirBorne = true;
                        velocityBoost /= 2;

                        speed = moveSpeed * 2.2554453949644;

                    } else if (stage == 3) {

                        double bruh = 0.655D;


                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
                                bruh = 0.63D;
                            } else {
                                bruh = 0.58D;
                            }
                        }

                        final double difference = bruh * (lastDist - moveSpeed);
                        speed = lastDist - difference;
                    } else {

                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                        }
                        List<Double> list = new ArrayList<>();

                        double a = lastDist - lastDist / 160;
                        double b = lastDist - (lastDist - moveSpeed) / 33.3;
                        double c = lastDist - (lastDist - moveSpeed) / 50;

                        list.add(a);
                        list.add(b);
                        list.add(c);

                        list.sort(Double::compare);

                        this.speed = list.get(2) - 0.0000125F;

                    }
                    speed = Math.max(speed, moveSpeed);

                    if (velocityBoost != 0 && stage >= 3) {
                        speed += velocityBoost;
                    }
                    velocityBoost *= 0.66;

                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
                    if (stage > 0) {
                        double forward = mc.thePlayer.movementInput.moveForward;
                        double strafe = mc.thePlayer.movementInput.moveStrafe;
                        TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
                        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
                        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
                        if (forward == 0.0f && strafe == 0.0f) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
                            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
                            em.setX(0);
                            em.setZ(0);
                            if (mc.thePlayer.onGround)
                                currentYaw = mc.thePlayer.rotationYaw;
                        } else if (!isCircleStrafing) {
                            if (forward != 0.0D) {
                                double oldForward = forward;
                                if (forward > 0.0D) {
                                    forward = 1;
                                } else if (forward < 0.0D) {
                                    forward = 1;
                                    yaw -= 180;
                                }

                                if (strafe > 0.0D) {
                                    yaw += (oldForward > 0.0D ? -45 : 45);
                                } else if (strafe < 0.0D) {
                                    yaw += (oldForward > 0.0D ? 45 : -45);
                                }
                            } else {
                                if (strafe > 0.0D) {
                                    yaw += (-90);
                                } else if (strafe < 0.0D) {
                                    yaw += (90);
                                }
                                forward = 0.985F;
                            }
                        }

                        float difference = MathHelper.wrapAngleTo180_float(-(currentYaw - yaw));

                        float cap = isCircleStrafing ? ((Number) targetStrafe.getSetting("STEPS").getValue()).floatValue() : mc.thePlayer.hurtTime > 7 ? Math.max(((Number) retard.getValue()).floatValue(), 90) : ((Number) retard.getValue()).floatValue();

                        if (Math.abs(difference) >= cap) {
                            difference = MathHelper.clamp_float(difference, -cap, cap);
                        }

                        currentYaw += difference;

                        double mx = Math.cos(Math.toRadians(currentYaw + 90));
                        double mz = Math.sin(Math.toRadians(currentYaw + 90));

                        double oldX = em.getX();
                        double oldZ = em.getZ();

                        em.setX((forward * this.speed * mx));
                        em.setZ((forward * this.speed * mz));


                    }
                    //If the player is moving, step the stage up.
                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {

                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                        if (PlayerUtil.isMoving() && stage > 0 && (strafeFix.getValue() && HypixelUtil.isVerifiedHypixel())) {
                            if (em.getY() % 0.015625 == 0) {
                                em.setY(em.getY() + 0.00053424);
                                em.setGround(false);
                            }

                            if (mc.thePlayer.motionY > 0.3) {
                                em.setGround(true);
                            }
                        }
                    }
                }
                break;
            }
            case "Mineplex": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (mc.thePlayer.isInWater())
                        return;
                    if ((mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
                        double superlegitvaluethatmostanticheatswillacceptasnormaljumping = 0.42F;
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            superlegitvaluethatmostanticheatswillacceptasnormaljumping += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = superlegitvaluethatmostanticheatswillacceptasnormaljumping);
                    }
                    setMotion(em, 0.4399999976158142);
                }
                break;
            }
//            case "HPort": {
//                if (stage < 1) {
//                    stage++;
//                    lastDist = 0;
//                    velocityBoost = 0;
//                    break;
//                }
//                if (event instanceof EventMove) {
//                    EventMove em = (EventMove) event;
//                    if (((mc.thePlayer.onGround) || (stage == 3))) {
//                        if (((!mc.thePlayer.isCollidedHorizontally) && (mc.thePlayer.moveForward != 0.0F)) || (mc.thePlayer.moveStrafing != 0.0F)) {
//                            if (stage == 2) {
//                                speed *= 1.374D;
//                                stage = 3;
//                            } else if (stage == 3) {
//                                stage = 2;
//                                double difference = 0.66D * (lastDist - defaultSpeed());
//                                speed = (lastDist - difference);
//                            } else {
//                                List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, mc.thePlayer.motionY, 0));
//                                if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
//                                    stage = 1;
//                                }
//                            }
//                        } else {
//                            mc.timer.timerSpeed = 1.0F;
//                        }
//                        speed = Math.max(speed, defaultSpeed());
//                        setMotion(em, speed);
//
//                    }
//                }
//                if (event instanceof EventMotionUpdate) {
//                    EventMotionUpdate em = (EventMotionUpdate) event;
//                    if (em.isPre()) {
//                        if (stage == 3) {
//                            double gay = 0.398936D;
//                            if (mc.thePlayer.isPotionActive(Potion.jump)) {
//                                gay = (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
//                            }
//                            em.setY(em.getY() + gay);
//                        }
//                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
//                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
//                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
//                    }
//                }
//                break;
//            }
            case "OldSlow": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (stage < 0) {
                        lastDist = 0;
                        velocityBoost = 0;
                        break;
                    }
                    if (steps > 2)
                        steps = 0;
                    if ((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F)) {
                        speed = defaultSpeed();
                    }
                    if ((stage == 1) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
                        speed = (0.25D + defaultSpeed() - 0.01D);
                    } else if ((stage == 2) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
                        em.setY(mc.thePlayer.motionY = 0.42F);
                        speed *= 1.749D;
                    } else if (stage == 3) {
                        double difference = 0.66D * (this.lastDist - defaultSpeed());
                        speed = (this.lastDist - difference);
                    } else {
                        List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
                        if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
                            if (stage > 0) {
                                if (1.35D * defaultSpeed() - 0.01D > speed) {
                                    stage = 0;
                                } else {
                                    stage = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                                }
                            }
                        }
                        speed = (this.lastDist - this.lastDist / 159.0D);
                    }
                    speed = Math.max(speed, defaultSpeed());
                    if (stage > 0) {
                        setMotion(em, speed);
                    }
                    if ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) || stage < 1) {
                        stage += 1;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                    }
                }
                break;
            }
            case "YPort": {
                if (stage <= 1) {
                    stage++;
                    lastDist = 0;
                    velocityBoost = 0;
                    break;
                }
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (((mc.thePlayer.onGround) || (stage == 3))) {
                        if (!mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F)) {
                            if (stage == 2) {
                                speed = defaultSpeed();
                                speed *= 1.633D;
                                stage = 3;
                            } else if (stage == 3) {
                                stage = 2;
                                double difference = 0.75 * (lastDist - defaultSpeed());
                                speed = (lastDist - difference);
                            } else {
                                List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
                                if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
                                    stage = 1;
                                }
                                speed = (this.lastDist - this.lastDist / 159.0D);
                            }
                        } else {
                            speed = defaultSpeed();
                            lastDist = 0;
                            velocityBoost = 0;
                        }

                        speed = Math.max(speed, defaultSpeed());
                        setMotion(em, speed);
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        if (stage == 3) {
                            double gay = 0.42F;
                            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                                gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                            }
                            em.setGround(false);
                            em.setY(em.getY() + gay);
                        }
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                    }
                }
                break;
            }
//            case "TritonJump": {
//                if (event instanceof EventMove) {
//                    EventMove em = (EventMove) event;
//                    ++this.ticks;
//                    if (!mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                        this.ticks = 0;
//                    }
//                    if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils.roundToPlace(0.481, 3)) {
//                        final Block pos = mc.theWorld.getBlock((int) mc.thePlayer.posX, (int) mc.thePlayer.posY - 1, (int) mc.thePlayer.posZ);
//                        if (!(pos instanceof BlockAir)) {
//                            em.setY(mc.thePlayer.motionY = -0.075);
//                        }
//                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils.roundToPlace(0.40599999999999997, 3)) {
//                        final Block pos = mc.theWorld.getBlock((int) mc.thePlayer.posX, (int) mc.thePlayer.posY - 1, (int) mc.thePlayer.posZ);
//                        if (!(pos instanceof BlockAir)) {
//                            em.setY(mc.thePlayer.motionY = -0.1);
//                        }
//                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils.roundToPlace(0.306, 3) && !mc.thePlayer.isCollidedHorizontally && stage != 0) {
//                        final Block pos = mc.theWorld.getBlock((int) mc.thePlayer.posX, (int) mc.thePlayer.posY - 1, (int) mc.thePlayer.posZ);
//                        if (!(pos instanceof BlockAir) && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                            em.setY(mc.thePlayer.motionY = -8.0E-6);
//                        }
//                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils.roundToPlace(0.305, 3) || (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) || mc.thePlayer.isCollidedHorizontally) {
//                        stage = 0;
//                    }
//                    if (stage == 1 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                        stage = 2;
//                        speed = (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? (3.25 * defaultSpeed()) : (4.3 * defaultSpeed()));
//                    } else if (stage == 2) {
//                        stage = 3;
//                        setMotion(em, 0.15);
//                        em.setY(mc.thePlayer.motionY = 0.42F);
//                        speed *= 2.147;
//                    } else if (stage == 3) {
//                        stage = 4;
//                        final double difference = 0.66 * (this.lastDist - defaultSpeed());
//                        speed = this.lastDist - difference;
//                    } else {
//                        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)).size() > 0 || mc.thePlayer.isCollidedVertically) {
//                            if (this.ticks >= 6) {
//                                stage = 1;
//                            } else {
//                                stage = 0;
//                            }
//                        }
//                        speed = this.lastDist - this.lastDist / 159.0;
//                    }
//                    setMotion(em, (stage != 0) ? (speed = Math.max(speed, defaultSpeed())) : 0.15);
//                }
//                if (event instanceof EventMotionUpdate) {
//                    EventMotionUpdate em = (EventMotionUpdate) event;
//                    if (em.isPre()) {
//
//                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
//                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
//                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
//                    }
//                }
//                break;
//            }
//            case "Jump": {
//                if (event instanceof EventMove) {
//                    EventMove em = (EventMove) event;
//                    if (mc.thePlayer.isCollidedHorizontally || (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing != 0.0f)) {
//                        stage = 0;
//                        ticks = 5;
//                    } else {
//                        if (ticks > 0 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                            speed = 0.09;
//                            --ticks;
//                        } else if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                            speed = 1.0 + defaultSpeed() - 0.05;
//                        } else if (stage == 2 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                            em.setY(mc.thePlayer.motionY = 0.415);
//                            speed *= 2.13;
//                        } else if (stage == 3) {
//                            final double difference = 0.66 * (this.lastDist - defaultSpeed());
//                            speed = this.lastDist - difference;
//                        } else {
//                            speed = this.lastDist - this.lastDist / 159.0;
//                        }
//                        setMotion(em, speed);
//                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
//                        final List collidingList2 = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, -0.4, 0.0));
//                        if (!mc.thePlayer.isCollidedVertically && (collidingList.size() > 0 || collidingList2.size() > 0) && stage > 10) {
//                            if (stage >= 38) {
//                                em.setY(mc.thePlayer.motionY = -0.4);
//                                stage = 0;
//                                ticks = 5;
//                            } else {
//                                em.setY(mc.thePlayer.motionY = -0.001);
//                            }
//                        }
//                        if (ticks <= 0 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
//                            ++stage;
//                        }
//                    }
//                }
//                if (event instanceof EventMotionUpdate) {
//                    EventMotionUpdate em = (EventMotionUpdate) event;
//                    if (em.isPre()) {
//                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
//                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
//                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
//                    }
//                }
//                break;
//            }
            case "Hop": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) {
                        speed = defaultSpeed();
                    }

                    if (stage == 1 && mc.thePlayer.isCollidedVertically && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        speed = 1.35 + defaultSpeed() - 0.01;
                    }
                    if (stage == 2 && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                        em.setY(mc.thePlayer.motionY = 0.42F);
                        mc.thePlayer.isAirBorne = true;
                        speed *= 1.533D;
                    } else if (stage == 3) {
                        final double difference = 0.66 * (lastDist - defaultSpeed());
                        speed = lastDist - difference;
                    } else {
                        final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, mc.thePlayer.motionY, 0.0));
                        if ((collidingList.size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
                            stage = ((mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) ? 1 : 0);
                        }
                        speed = lastDist - lastDist / 159.0;
                    }
                    speed = Math.max(speed, defaultSpeed());

                    //Stage checks if you're greater than 0 as step sets you -6 stage to make sure the player wont flag.
                    if (stage > 0) {
                        //Set strafe motion.
                        setMotion(em, speed);
                    }
                    //If the player is moving, step the stage up.
                    if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                        ++stage;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                    }
                }
                break;
            }
            case "OnGround": {
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        mc.timer.timerSpeed = 1.065f + ((float) Math.random() * 0.2f);
                        double forward = mc.thePlayer.movementInput.moveForward;
                        double strafe = mc.thePlayer.movementInput.moveStrafe;
                        if ((forward != 0 || strafe != 0) && !mc.thePlayer.isJumping && !mc.thePlayer.isInWater() && !mc.thePlayer.isOnLadder() && (!mc.thePlayer.isCollidedHorizontally)) {
                            em.setY(mc.thePlayer.posY + (mc.thePlayer.ticksExisted % 2 != 0 ? 0.42F : 0));
                        }
                        speed = Math.max(mc.thePlayer.ticksExisted % 2 == 0 ? (2 + (0.084541487 + (int) (Math.random() * 0.09878956))) : (1 + (0.3291651713 + (int) (Math.random() * 0.396541521))), defaultSpeed());
                        float yaw = mc.thePlayer.rotationYaw;
                        if (mc.thePlayer.fallDistance > 0) {
                            speed = defaultSpeed();
                            mc.timer.timerSpeed = 0.7f;
                        }
                        if ((forward == 0.0D) && (strafe == 0.0D)) {
                            mc.thePlayer.motionX = (0.0D);
                            mc.thePlayer.motionZ = (0.0D);
                        } else {
                            if (forward != 0.0D) {
                                if (strafe > 0.0D) {
                                    yaw += (forward > 0.0D ? -45 : 45);
                                } else if (strafe < 0.0D) {
                                    yaw += (forward > 0.0D ? 45 : -45);
                                }
                                strafe = 0.0D;
                                if (forward > 0.0D) {
                                    forward = 0.15;
                                } else if (forward < 0.0D) {
                                    forward = -0.15;
                                }
                            }
                            if (strafe > 0) {
                                strafe = 0.15;
                            } else if (strafe < 0) {
                                strafe = -0.15;
                            }
                            double cos = Math.cos(Math.toRadians(yaw + 90.0F));
                            double sin = Math.sin(Math.toRadians(yaw + 90.0F));
                            mc.thePlayer.motionX = (forward * speed * cos + strafe * speed * sin);
                            mc.thePlayer.motionZ = (forward * speed * sin - strafe * speed * cos);
                        }
                    }
                }
                break;
            }
            case "OldHop": {
                if (event instanceof EventMove) {
                    EventMove em = (EventMove) event;
                    if ((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F)) {
                        speed = defaultSpeed();
                    }
                    if ((stage == 1) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
                        speed = (0.25D + defaultSpeed() - 0.01D);
                    } else if ((stage == 2) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
                        double gay = 0.42F;
                        if (mc.thePlayer.isPotionActive(Potion.jump)) {
                            gay += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                        }
                        em.setY(mc.thePlayer.motionY = gay);
                        speed *= 2.149D;
                    } else if (stage == 3) {
                        double difference = 0.66D * (this.lastDist - defaultSpeed());
                        speed = (this.lastDist - difference);
                    } else {
                        List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
                        if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
                            if (stage > 0) {
                                if (1.35D * defaultSpeed() - 0.01D > speed) {
                                    stage = 0;
                                } else {
                                    stage = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                                }
                            }
                        }
                        speed = (this.lastDist - this.lastDist / 159.0D);
                    }
                    speed = Math.max(speed, defaultSpeed());
                    if (stage > 0) {
                        setMotion(em, speed);
                    }
                    if ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F)) {
                        stage += 1;
                    }
                }
                if (event instanceof EventMotionUpdate) {
                    EventMotionUpdate em = (EventMotionUpdate) event;
                    if (em.isPre()) {
                        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                    }
                }
                break;
            }
        }
    }

    private void setMotion(EventMove em, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
        float yaw = strafe == 0 && forward > 0 ? targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, em.getY()) : mc.thePlayer.rotationYaw;
        boolean isCircleStrafing = mc.thePlayer.rotationYaw != yaw;
        if (forward == 0.0f && strafe == 0.0f) {
            mc.thePlayer.setPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY, mc.thePlayer.posZ + 1);
            mc.thePlayer.setPosition(mc.thePlayer.prevPosX, mc.thePlayer.posY, mc.thePlayer.prevPosZ);
            em.setX(0);
            em.setZ(0);
        } else if (forward != 0.0f && !isCircleStrafing) {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
        }
        em.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
        em.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
    }

}
