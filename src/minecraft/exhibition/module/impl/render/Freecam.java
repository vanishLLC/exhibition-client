/*
 * Time: 10:24:50 PM
 * Date: Jan 1, 2017
 * Creator: cool1
 */
package exhibition.module.impl.render;

import com.mojang.authlib.GameProfile;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiVelocity;
import exhibition.util.NetUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.UUID;

/**
 * @author cool1
 */
@SuppressWarnings("all")
public class Freecam extends Module {

    private String SPEED = "SPEED";
    private EntityOtherPlayerMP freecamEntity;

    /**
     * @param data
     */
    public Freecam(ModuleData data) {
        super(data);
        settings.put(SPEED, new Setting(SPEED, 1.0, "Movement speed."));
    }

    private Vec3 oldPos;

    @Override
    public void onDisable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (freecamEntity == null) {
            if (oldPos != null)
                mc.thePlayer.setPositionAndRotation(oldPos.getX(), oldPos.getY(), oldPos.getZ(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            return;
        }

        mc.thePlayer.positionUpdateTicks = ticks;
        mc.thePlayer.lastReportedYaw = lastReportedYaw;
        mc.thePlayer.lastReportedPitch = lastReportedPitch;
        mc.thePlayer.lastReportedPosX = lastReportedPosX;
        mc.thePlayer.lastReportedPosY = lastReportedPosY;
        mc.thePlayer.lastReportedPosZ = lastReportedPosZ;

        mc.thePlayer.lastTickPosX = this.freecamEntity.posX;
        mc.thePlayer.lastTickPosY = this.freecamEntity.posY;
        mc.thePlayer.lastTickPosZ = this.freecamEntity.posZ;

        mc.thePlayer.setPositionAndRotation(this.freecamEntity.posX, this.freecamEntity.posY, this.freecamEntity.posZ, this.freecamEntity.rotationYaw, this.freecamEntity.rotationPitch);
        mc.thePlayer.setVelocity(this.freecamEntity.motionX, this.freecamEntity.motionY, this.freecamEntity.motionZ);
        mc.theWorld.removeEntityFromWorld(69420);
        mc.renderGlobal.loadRenderers();
        mc.thePlayer.noClip = false;
    }

    private int ticks;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            toggle();
            return;
        }
        this.freecamEntity = new EntityOtherPlayerMP(mc.theWorld, new GameProfile(new UUID(69L, 96L), mc.thePlayer.getName() + " [Bot]"));
        mc.theWorld.addEntityToWorld(69420, this.freecamEntity);

        this.freecamEntity.inventory = mc.thePlayer.inventory;
        this.freecamEntity.inventoryContainer = mc.thePlayer.inventoryContainer;
        this.freecamEntity.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        this.freecamEntity.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        this.freecamEntity.rotationYawHead = mc.thePlayer.rotationYawHead;
        this.freecamEntity.onGround = mc.thePlayer.onGround;
        this.freecamEntity.motionX = mc.thePlayer.motionX;
        this.freecamEntity.motionY = mc.thePlayer.onGround ? 0 : mc.thePlayer.motionY;
        this.freecamEntity.motionZ = mc.thePlayer.motionZ;
        this.freecamEntity.capabilities = mc.thePlayer.capabilities;
        this.freecamEntity.noClip = false;

        ticks = mc.thePlayer.positionUpdateTicks;
        lastReportedYaw = mc.thePlayer.lastReportedYaw;
        lastReportedPitch = mc.thePlayer.lastReportedPitch;
        lastReportedPosX = mc.thePlayer.lastReportedPosX;
        lastReportedPosY = mc.thePlayer.lastReportedPosY;
        lastReportedPosZ = mc.thePlayer.lastReportedPosZ;

        oldPos = mc.thePlayer.getPositionVector();
        mc.renderGlobal.loadRenderers();
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventBlockBounds.class, EventMove.class, EventPushBlock.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        float speed = ((Number) settings.get(SPEED).getValue()).floatValue() / 5;
        if (event instanceof EventMotionUpdate) {
            mc.thePlayer.noClip = true;
            EventMotionUpdate em = event.cast();
        }
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet getPacket = ep.getPacket();
            if (freecamEntity == null) {
                return;
            }

            AntiVelocity velocity = (AntiVelocity) Client.getModuleManager().get(AntiVelocity.class);

            if (getPacket instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook packetIn = (S08PacketPlayerPosLook) getPacket;
                EntityPlayer entityplayer = freecamEntity;
                double d0 = packetIn.getX();
                double d1 = packetIn.getY();
                double d2 = packetIn.getZ();
                float f = packetIn.getYaw();
                float f1 = packetIn.getPitch();

                if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X)) {
                    d0 += entityplayer.posX;
                } else {
                    entityplayer.motionX = 0.0D;
                }

                if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
                    d1 += entityplayer.posY;
                } else {
                    entityplayer.motionY = 0.0D;
                }

                if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
                    d2 += entityplayer.posZ;
                } else {
                    entityplayer.motionZ = 0.0D;
                }

                if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
                    f1 += entityplayer.rotationPitch;
                }

                if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
                    f += entityplayer.rotationYaw;
                }

                entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
                NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch, false));
                event.setCancelled(true);
            }

            if (getPacket instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) getPacket;
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    double x = (double) packet.getMotionX() / 8000.0D;
                    double y = (double) packet.getMotionY() / 8000.0D;
                    double z = (double) packet.getMotionZ() / 8000.0D;

                    double hP = 1;
                    double vP = 1;
                    if (velocity.isEnabled()) {
                        int vertical = ((Number) velocity.getSettings().get("VERTICAL").getValue()).intValue();
                        int horizontal = ((Number) velocity.getSettings().get("HORIZONTAL").getValue()).intValue();
                        hP = horizontal / 100D;
                        vP = vertical / 100D;
                    }
                    freecamEntity.setVelocity(x * hP, y * vP, z * hP);
                }
            } else if (getPacket instanceof S27PacketExplosion) {
                S27PacketExplosion packet = (S27PacketExplosion) getPacket;
                if (velocity.isEnabled()) {
                    double vertical = ((Number) velocity.getSettings().get("VERTICAL").getValue()).doubleValue();
                    double horizontal = ((Number) velocity.getSettings().get("HORIZONTAL").getValue()).doubleValue();
                    if (vertical != 0 || horizontal != 0) {
                        packet.xMotion *= (horizontal / 100D);
                        packet.yMotion *= (vertical / 100D);
                        packet.zMotion *= (horizontal / 100D);
                    }
                }
                freecamEntity.motionX += packet.getMotionX();
                freecamEntity.motionY += packet.getMotionY();
                freecamEntity.motionZ += packet.getMotionZ();
            }

            if (getPacket instanceof C03PacketPlayer || getPacket.getClass().isAssignableFrom(C03PacketPlayer.class) || getPacket.getClass().isAssignableFrom(C03PacketPlayer.C04PacketPlayerPosition.class) ||
                    getPacket.getClass().isAssignableFrom(C03PacketPlayer.C05PacketPlayerLook.class) || getPacket.getClass().isAssignableFrom(C03PacketPlayer.C06PacketPlayerPosLook.class)) {
                onUpdateWalkingPlayer(ep, freecamEntity);
            }

            if (getPacket instanceof C0BPacketEntityAction || getPacket instanceof C08PacketPlayerBlockPlacement || getPacket instanceof C07PacketPlayerDigging) {
                event.setCancelled(true);
            }

        }
        if (event instanceof EventBlockBounds) {
            EventBlockBounds ebb = (EventBlockBounds) event;
            ebb.setBounds(null);
        }
        if (event instanceof EventMove) {
            EventMove em = (EventMove) event;
            if (mc.theWorld.isAreaLoaded(new BlockPos(oldPos), 2)) {
                moveEntityWithHeading(freecamEntity, 0, 0);
            }
            if (mc.thePlayer.movementInput.jump) {
                em.setY(mc.thePlayer.motionY = speed / 2);
            } else if (mc.thePlayer.movementInput.sneak) {
                em.setY(mc.thePlayer.motionY = -speed / 2);
            } else {
                em.setY(mc.thePlayer.motionY = 0.0D);
            }
            speed = (float) Math.max(speed, getBaseMoveSpeed());
            double forward = mc.thePlayer.movementInput.moveForward;
            double strafe = mc.thePlayer.movementInput.moveStrafe;
            float yaw = mc.thePlayer.rotationYaw;
            if ((forward == 0.0D) && (strafe == 0.0D)) {
                em.setX(0.0D);
                em.setZ(0.0D);
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        strafe = 1;
                        yaw += (forward > 0.0D ? -45 : 45);
                    } else if (strafe < 0.0D) {
                        yaw += (forward > 0.0D ? 45 : -45);
                    }
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1;
                    } else {
                        forward = -1;
                    }
                }
                em.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F))
                        + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
                em.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0F))
                        - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
            }
        }
        if (event instanceof EventPushBlock) {
            EventPushBlock ebp = (EventPushBlock) event;
            ebp.setCancelled(true);
        }
    }

    float lastReportedYaw, lastReportedPitch;
    double lastReportedPosX, lastReportedPosY, lastReportedPosZ;

    public void onUpdateWalkingPlayer(EventPacket ep, EntityPlayer player) {
        double var3 = player.posX - lastReportedPosX;
        double var5 = player.getEntityBoundingBox().minY - lastReportedPosY;
        double var7 = player.posZ - lastReportedPosZ;
        double var9 = player.rotationYaw - lastReportedYaw;
        double var11 = player.rotationPitch - lastReportedPitch;
        boolean var13 = var3 * var3 + var5 * var5 + var7 * var7 > 9.0E-4D || ticks >= 20;
        boolean var14 = var9 != 0.0D || var11 != 0.0D;
        if (player.ridingEntity == null) {
            if (var13 && var14) {
                ep.setPacket(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.getEntityBoundingBox().minY, player.posZ, player.rotationYaw, player.rotationPitch, player.onGround));
            } else if (var13) {
                ep.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.getEntityBoundingBox().minY, player.posZ, player.onGround));
            } else if (var14) {
                ep.setPacket(new C03PacketPlayer.C05PacketPlayerLook(player.rotationYaw, player.rotationPitch, player.onGround));
            } else {
                ep.setPacket(new C03PacketPlayer(player.onGround));
            }
        } else {
            ep.setPacket(new C03PacketPlayer.C06PacketPlayerPosLook(player.motionX, -999.0D, player.motionZ, player.rotationYaw, player.rotationPitch, player.onGround));
            var13 = false;
        }
        ++ticks;
        if (var13) {
            lastReportedPosX = player.posX;
            lastReportedPosY = player.posY;
            lastReportedPosZ = player.posZ;
            ticks = 0;
        }
        if (var14) {
            lastReportedYaw = player.rotationYaw;
            lastReportedPitch = player.rotationPitch;
        }
    }

    public void moveEntityWithHeading(EntityPlayer player, float strafe, float forward) {
        if (true) {
            if (!player.isInWater() || player.capabilities.isFlying) {
                if (!player.isInLava() || player.capabilities.isFlying) {
                    float f4 = 0.91F;

                    if (player.onGround) {
                        f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(player.posZ))).getBlock().slipperiness * 0.91F;
                    }

                    float f = 0.16277136F / (f4 * f4 * f4);
                    float f5;

                    if (player.onGround) {
                        f5 = player.getAIMoveSpeed() * f;
                    } else {
                        f5 = player.jumpMovementFactor;
                    }

                    player.moveFlying(strafe, forward, f5);
                    f4 = 0.91F;

                    if (player.onGround) {
                        f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(player.posZ))).getBlock().slipperiness * 0.91F;
                    }

                    if (player.isOnLadder()) {
                        float f6 = 0.15F;
                        player.motionX = MathHelper.clamp_double(player.motionX, (double) (-f6), (double) f6);
                        player.motionZ = MathHelper.clamp_double(player.motionZ, (double) (-f6), (double) f6);
                        player.fallDistance = 0.0F;

                        if (player.motionY < -0.15D) {
                            player.motionY = -0.15D;
                        }

                        boolean flag = player.isSneaking() && player instanceof EntityPlayer;

                        if (flag && player.motionY < 0.0D) {
                            player.motionY = 0.0D;
                        }
                    }

                    player.moveEntity(player.motionX, player.motionY, player.motionZ);

                    if (player.isCollidedHorizontally && player.isOnLadder()) {
                        player.motionY = 0.2D;
                    }

                    if (mc.thePlayer.worldObj.isRemote && (!mc.thePlayer.worldObj.isBlockLoaded(new BlockPos((int) player.posX, 0, (int) player.posZ)) || !mc.thePlayer.worldObj.getChunkFromBlockCoords(new BlockPos((int) player.posX, 0, (int) player.posZ)).isLoaded())) {
                        if (player.posY > 0.0D) {
                            player.motionY = -0.1D;
                        } else {
                            player.motionY = 0.0D;
                        }
                    } else {
                        player.motionY -= 0.08D;
                    }

                    player.motionY *= 0.9800000190734863D;
                    player.motionX *= (double) f4;
                    player.motionZ *= (double) f4;
                } else {
                    double d1 = player.posY;
                    player.moveFlying(strafe, forward, 0.02F);
                    player.moveEntity(player.motionX, player.motionY, player.motionZ);
                    player.motionX *= 0.5D;
                    player.motionY *= 0.5D;
                    player.motionZ *= 0.5D;
                    player.motionY -= 0.02D;

                    if (player.isCollidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + d1, player.motionZ)) {
                        player.motionY = 0.30000001192092896D;
                    }
                }
            } else {
                double d0 = player.posY;
                float f1 = 0.8F;
                float f2 = 0.02F;
                float f3 = (float) EnchantmentHelper.getDepthStriderModifier(player);

                if (f3 > 3.0F) {
                    f3 = 3.0F;
                }

                if (!player.onGround) {
                    f3 *= 0.5F;
                }

                if (f3 > 0.0F) {
                    f1 += (0.54600006F - f1) * f3 / 3.0F;
                    f2 += (player.getAIMoveSpeed() * 1.0F - f2) * f3 / 3.0F;
                }

                player.moveFlying(strafe, forward, f2);
                player.moveEntity(player.motionX, player.motionY, player.motionZ);
                player.motionX *= (double) f1;
                player.motionY *= 0.800000011920929D;
                player.motionZ *= (double) f1;
                player.motionY -= 0.02D;

                if (player.isCollidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + d0, player.motionZ)) {
                    player.motionY = 0.30000001192092896D;
                }
            }
        }

        player.prevLimbSwingAmount = player.limbSwingAmount;
        double d2 = player.posX - player.prevPosX;
        double d3 = player.posZ - player.prevPosZ;
        float f7 = MathHelper.sqrt_double(d2 * d2 + d3 * d3) * 4.0F;

        if (f7 > 1.0F) {
            f7 = 1.0F;
        }

        player.limbSwingAmount += (f7 - player.limbSwingAmount) * 0.4F;
        player.limbSwing += player.limbSwingAmount;
    }

}
