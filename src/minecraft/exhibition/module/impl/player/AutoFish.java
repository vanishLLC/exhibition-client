/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;

public class AutoFish extends Module {

    private boolean fishNearby;
    private Timer recastTimer = new Timer();

    public AutoFish(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventTick.class, EventPacket.class, EventMotionUpdate.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPre() && mc.thePlayer.fishEntity != null && mc.thePlayer.ticksExisted % 3 == 0) {
                em.setYaw(mc.thePlayer.rotationYaw + ((mc.thePlayer.ticksExisted % 2 == 0) ? 1 : -1));
                float pitch = ((mc.thePlayer.ticksExisted % 2 == 0) ? 1 : -1);

                em.setPitch(MathHelper.clamp_float((float) (mc.thePlayer.rotationPitch - pitch * 0.15D), -90.0F, 90.0F));
            }
        } else if (event instanceof EventTick) {
            if (recastTimer.delay(1000) && mc.thePlayer.fishEntity == null) {
                if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFishingRod) {
                    ItemStack var5 = mc.thePlayer.getCurrentEquippedItem();
                    if (var5 != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, var5)) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                    }
                    //ChatUtil.printChat("Recasting");
                    fishNearby = false;
                    recastTimer.reset();
                } else {
                    for (int i = 36; i < 45; i++)
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            Item item = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem();
                            if (item instanceof ItemFishingRod && Killaura.getTarget() == null && Killaura.loaded.isEmpty()) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = i - 36));
                                mc.playerController.updateController();
                                return;
                            }
                        }
                }
            }
            if (mc.thePlayer.fishEntity != null) {
                if (mc.thePlayer.fishEntity.caughtEntity != null || mc.thePlayer.fishEntity.inGround) {
                    ItemStack var5 = mc.thePlayer.getCurrentEquippedItem();
                    if (var5 != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, var5))
                        mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                    fishNearby = false;
                    recastTimer.reset();
                }
            }

        } else if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {
                if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFishingRod) {
                    if (mc.thePlayer.fishEntity != null) {
                        S12PacketEntityVelocity entityVelocity = (S12PacketEntityVelocity) packet;
                        if (entityVelocity.getEntityID() == mc.thePlayer.fishEntity.getEntityId()) {
                            int velX = entityVelocity.getMotionX();
                            int velY = entityVelocity.getMotionY();
                            int velZ = entityVelocity.getMotionZ();
                            if (velX == 0 && velY < 0 && velZ == 0 && fishNearby) {
                                //ChatUtil.printChat("Reeling it in!");
                                ItemStack var5 = mc.thePlayer.getCurrentEquippedItem();
                                if (var5 != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, var5))
                                    mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                                fishNearby = false;
                                recastTimer.reset();
                            }
                        }
                    }
                }
            }
            if (packet instanceof S14PacketEntity && !(packet instanceof S14PacketEntity.S16PacketEntityLook)) {
                S14PacketEntity packetIn = (S14PacketEntity) packet;
                Entity var2 = packetIn.getEntity(mc.theWorld);
                if (var2 != null && var2 == mc.thePlayer.fishEntity) {
                    double var5 = (double) var2.serverPosY / 32.0D;
                    if (var5 < (var2.posY - 0.141) && fishNearby) {
                        ItemStack var52 = mc.thePlayer.getCurrentEquippedItem();
                        if (var52 != null && mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, var52))
                            mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                        fishNearby = false;
                        recastTimer.reset();
                    }
                }
            }
            if (packet instanceof S2APacketParticles) {
                S2APacketParticles packetParticles = (S2APacketParticles) packet;
                if (packetParticles.getParticleType() == EnumParticleTypes.WATER_WAKE) {
                    if (mc.thePlayer.fishEntity != null && !fishNearby) {
                        EntityFishHook entityFishHook = mc.thePlayer.fishEntity;
                        if (entityFishHook.getDistance(packetParticles.getXCoordinate(), packetParticles.getYCoordinate(), packetParticles.getZCoordinate()) < 1) {
                            fishNearby = true;
                            //ChatUtil.printChat("Fish is nearby!");
                        }
                    }
                }
            }
        }
    }
}
