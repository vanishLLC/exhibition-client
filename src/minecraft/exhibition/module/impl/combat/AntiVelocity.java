package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.LongJump;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.Vec3;

public class AntiVelocity extends Module {

    public AntiVelocity(ModuleData data) {
        super(data);
        settings.put(HORIZONTAL, new Setting<>(HORIZONTAL, 0, "Horizontal velocity percent.", 1, -200, 200));
        settings.put(VERTICAL, new Setting<>(VERTICAL, 0, "Vertical velocity percent.", 1, -200, 200));
        settings.put(receiveKBAlert.getName(), receiveKBAlert);
        settings.put(nearbyOnly.getName(), nearbyOnly);
    }

    private Vec3 velocity = new Vec3(0, 0, 0);
    private Timer ignore = new Timer();
    private boolean checkDamage;
    private Timer hurtDelay = new Timer();

    public static String HORIZONTAL = "HORIZONTAL";
    public static String VERTICAL = "VERTICAL";
    public Setting receiveKBAlert = new Setting<>("ALERT", true, "Alerts you when you are KB checked by staff.");
    public Setting<Boolean> nearbyOnly = new Setting<>("NEARBY-ONLY", false, "Only enables Anti-KB when enemies are nearby. Can be used for Hypixel KB checks.");

    boolean projectileNearby;

    @RegisterEvent(events = {EventPacket.class, EventTick.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventTick) {
            if ((boolean) receiveKBAlert.getValue()) {
                if (hurtDelay.delay(250) && mc.thePlayer.ticksExisted > 60) {
                    if (checkDamage && mc.thePlayer.hurtTime == 0 && mc.thePlayer.isAllowEdit()) {
                        Notifications.getManager().post("\247cSuspicious Knockback", "You may have been KB checked!", 5000, Notifications.Type.WARNING);
                        checkDamage = false;
                        mc.thePlayer.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
                        velocity = null;
                    } else {
                        checkDamage = false;
                    }
                } else {
                    if (checkDamage) {
                        if (mc.thePlayer.hurtTime > 0) {
                            checkDamage = false;
                        }
                    }
                }
            }

            projectileNearby = false;
            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                if (entity instanceof IProjectile) {
                    if (mc.thePlayer.getDistanceToEntity(entity) <= 5) {
                        projectileNearby = true;
                    }
                }
            }
            return;
        }

        // Check for incoming packets only
        EventPacket ep = (EventPacket) event;
        Packet castPacket = ep.getPacket();
        // If the packet handles velocity
        try {
            if (castPacket instanceof S45PacketTitle) {
                S45PacketTitle packet = ((S45PacketTitle) castPacket);
                if (packet.getType().equals(S45PacketTitle.Type.TITLE)) {
                    String text = packet.getMessage().getUnformattedText();
                    if (text.equals("YOU DIED")) {
                        ignore.reset();
                    }
                }
            }

            if (castPacket instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) castPacket;

                boolean disable = false;

                if (HypixelUtil.isInGame("THE HYPIXEL PIT")) {
                    double x = mc.thePlayer.posX;
                    double y = mc.thePlayer.posY;
                    double z = mc.thePlayer.posZ;
                    if (y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
                        disable = true;
                    }
                }

                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    double x = (double) packet.getMotionX() / 8000.0D;
                    double y = (double) packet.getMotionY() / 8000.0D;
                    double z = (double) packet.getMotionZ() / 8000.0D;

                    if (nearbyOnly.getValue()) {
                        boolean nearby = false;
                        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                            if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                continue;
                            if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                nearby = true;
                                break;
                            }
                        }
                        if (!nearby && !projectileNearby) {
                            if (!((LongJump) Client.getModuleManager().get(LongJump.class)).noShake() && ignore.delay(500) && x != 0 && y != 0 && z != 0) {
                                velocity = new Vec3((double) packet.getMotionX() / 8000.0D, (double) packet.getMotionY() / 8000.0D, (double) packet.getMotionZ() / 8000.0D);
                                checkDamage = true;
                                hurtDelay.reset();
                            }
                            return;
                        }
                    }

                    int vertical = ((Number) settings.get(VERTICAL).getValue()).intValue();
                    int horizontal = ((Number) settings.get(HORIZONTAL).getValue()).intValue();

                    if (!disable)
                        if (vertical != 0 || horizontal != 0) {
                            packet.motionX = (int) ((double) packet.motionX * (horizontal / 100F));
                            packet.motionY = (int) ((double) packet.motionY * (vertical / 100F));
                            packet.motionZ = (int) ((double) packet.motionZ * (horizontal / 100F));
                        } else {
                            event.setCancelled(true);
                        }


                    if (!((LongJump) Client.getModuleManager().get(LongJump.class)).noShake() && ignore.delay(500) && x != 0 && y != 0 && z != 0) {
                        velocity = new Vec3((double) packet.getMotionX() / 8000.0D, (double) packet.getMotionY() / 8000.0D, (double) packet.getMotionZ() / 8000.0D);
                        checkDamage = true;
                        hurtDelay.reset();
                    }
                }
            } else if (castPacket instanceof S27PacketExplosion) {
                S27PacketExplosion packet = (S27PacketExplosion) castPacket;

                boolean disable = false;

                if (HypixelUtil.isInGame("THE HYPIXEL PIT")) {
                    double x = mc.thePlayer.posX;
                    double y = mc.thePlayer.posY;
                    double z = mc.thePlayer.posZ;
                    if (y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
                        disable = true;
                    }
                }

                if (nearbyOnly.getValue() && packet.yMotion > -0.5) {
                    boolean nearby = false;
                    for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                        if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                            continue;
                        if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                            nearby = true;
                            break;
                        }
                    }
                    if (!nearby && !projectileNearby) {
                        if (!((LongJump) Client.getModuleManager().get(LongJump.class)).noShake() && ignore.delay(500) && packet.xMotion != 0 && packet.zMotion != 0 && packet.yMotion != 0) {
                            velocity = new Vec3(packet.xMotion, packet.yMotion, packet.zMotion);
                            checkDamage = true;
                            hurtDelay.reset();
                        }
                        return;
                    }
                }

                double vertical = ((Number) settings.get(VERTICAL).getValue()).doubleValue();
                double horizontal = ((Number) settings.get(HORIZONTAL).getValue()).doubleValue();

                if (!disable)
                    if (vertical != 0 || horizontal != 0) {
                        packet.xMotion *= (float) (horizontal / 100F);
                        packet.yMotion *= (float) (vertical / 100F);
                        packet.zMotion *= (float) (horizontal / 100F);
                    } else {
                        event.setCancelled(true);
                    }

                if (!((LongJump) Client.getModuleManager().get(LongJump.class)).noShake() && ignore.delay(500) && packet.xMotion != 0 && packet.zMotion != 0 && packet.yMotion != 0) {
                    velocity = new Vec3(packet.xMotion, packet.yMotion, packet.zMotion);
                    checkDamage = true;
                    hurtDelay.reset();
                }

            }

        } catch (Exception ignored) {
        }


    }
}
