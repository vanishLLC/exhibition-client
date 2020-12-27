package exhibition.module.impl.gta;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.management.command.Command;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.NetUtil;
import exhibition.util.RotationUtils;
import exhibition.util.Timer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arithmo on 9/15/2017 at 11:36 PM.
 */
public class AutoBandage extends Module {
    public static boolean shouldBand;
    public boolean preBandage;
    private int fakeHoldTicks;
    public float possibleDamage;
    Timer timer = new Timer();
    private List<EntityThrowable> eggs = new ArrayList();

    public AutoBandage(ModuleData data) {
        super(data);
        settings.put("HEALTH", new Setting<>("HEALTH", 17, "Thing health", 1, 1, 10));
        settings.put("DELAY", new Setting<>("DELAY", 100, "Delay MS", 50, 0, 1000));
        settings.put("HOLDTICKS", new Setting<>("HOLDTICKS", 5, "Fake holding ticks.", 1, 1, 20));
    }

    public void onEnable() {
        shouldBand = false;
    }

    public void onDisable() {
        shouldBand = false;
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event e) {
        EventMotionUpdate em = (EventMotionUpdate) e;
        int currentItem = mc.thePlayer.inventory.currentItem;
        int fakeTicks = ((Number) (settings.get("HOLDTICKS")).getValue()).intValue();
        int health = ((Number) (settings.get("HEALTH")).getValue()).intValue();
        if (mc.thePlayer.isDead)
            return;
        if (em.isPre()) {
            int bandageSlot = getBandageSlot();
            if ((hotbarBandCount() < 8) && (hotbarBandCount() != -1)) {
                swapStacks(bandageSlot);
            }
            if (preBandage && shouldBand) {
                possibleDamage = 0;
                preBandage = false;
            }
            if ((fakeHoldTicks < fakeTicks) && (fakeHoldTicks != -1)) {
                fakeHoldTicks += 1;
            }
            if ((shouldBand) && (fakeHoldTicks >= fakeTicks) && (fakeHoldTicks != -1)) {
                NetUtil.sendPacket(new C09PacketHeldItemChange(currentItem));
                shouldBand = false;
            }
            for (Object o : mc.theWorld.getLoadedEntityList()) {
                if ((o instanceof EntityEgg)) {
                    EntityEgg egg = (EntityEgg) o;
                    float var2 = (float) (mc.thePlayer.lastTickPosX - egg.posX);
                    float var3 = (float) (mc.thePlayer.lastTickPosY - egg.posY);
                    float var4 = (float) (mc.thePlayer.lastTickPosZ - egg.posZ);
                    float distance = MathHelper.sqrt_float(var2 * var2 + var3 * var3 + var4 * var4);
                    if (mc.thePlayer.getDistanceToEntity(egg) <= 2.7 && mc.thePlayer.getDistanceToEntity(egg) > 0.5 && egg.ticksExisted >= 0 && (!eggs.contains(egg))) {
                        float yawDelta = RotationUtils.getDistanceBetweenAngles(Math.abs(MathHelper.wrapAngleTo180_float(Math.abs(egg.rotationYaw))), Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)));
                        System.out.println("---EGG DATA---\n");
                        System.out.println("Rotation to Egg: " + Math.abs(RotationUtils.getYawChange(egg.posX, egg.posZ))
                                + "\nDistance from Player: " + distance + " " + mc.thePlayer.getDistanceToEntity(egg) +
                                "\nEgg - Player Yaw Delta: " + yawDelta +
                                "\nEgg Rotation Yaw: " + Math.abs(egg.rotationYaw) + "\nPlayer Rotations: " +  Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)) + " " +  Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationYaw)) +
                                "\nMotions: " + egg.motionX + " " + egg.motionZ +
                                "\nTicks Existed: " + egg.ticksExisted +
                                "\nY Positions: " + (mc.thePlayer.posY - egg.posY));
                        System.out.println("\n---END DATA---\n");
                        if (egg.rotationYaw == 0 && egg.ticksExisted <= 1 && (mc.thePlayer.posY - egg.posY == -1.59375 || mc.thePlayer.posY - egg.posY == -1.53125) && mc.thePlayer.getDistanceToEntity(egg) <= 2) {
                            DevNotifications.getManager().post(Command.chatPrefix + "YChange " + Math.abs(RotationUtils.getYawChange(egg.posX, egg.posZ))
                                    + " " + mc.thePlayer.getDistanceToEntity(egg));
                            continue;
                        }

                        if ((yawDelta < 30 && (Math.abs(RotationUtils.getYawChange(egg.posX, egg.posZ)) < 50) && egg.ticksExisted <= 1)) {
                            System.out.println("Ignored due to angle checks.");
                            continue;
                        }
                        if ((mc.thePlayer.posY - egg.posY == -1.59375 || mc.thePlayer.posY - egg.posY == -1.53125) && egg.ticksExisted <= 1 && mc.thePlayer.getDistanceToEntity(egg) <= 2) {
                            continue;
                        }
                        eggs.add(egg);
                        possibleDamage += 0.25;
                        if (mc.thePlayer.getHealth() - possibleDamage * 2 <= health * 2) {
                            preBandage = true;
                            shouldBand = true;
                            fakeHoldTicks = (fakeTicks == 0 ? -1 : 0);
                        }
                        DevNotifications.getManager().post("\2476\247l" + possibleDamage + " will be expected. ");
                    }
                }
            }
            int delay = ((Number) (settings.get("DELAY")).getValue()).intValue();
            if ((mc.thePlayer.getHealth() <= health * 2) && (bandageSlot != -1) && (timer.delay(delay)) && (mc.thePlayer.isEntityAlive()) && !preBandage && !shouldBand) {
                eggs.clear();
                shouldBand = true;
                fakeHoldTicks = (fakeTicks == 0 ? -1 : 0);
            }
            if ((shouldBand) && (fakeHoldTicks == 0) && (bandageSlot != -1)) {
                NetUtil.sendPacket(new C09PacketHeldItemChange(bandageSlot));
                NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                timer.reset();
            } else if ((shouldBand) && (fakeHoldTicks > 0) && (bandageSlot != -1)) {
                NetUtil.sendPacket(new C09PacketHeldItemChange(bandageSlot));
            }
        }
    }

    private void swapStacks(int slot) {
        for (int i = 9; i <= 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if ((stack != null) && (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem(Items.paper)) &&
                    (stack.stackSize > 40)) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, slot, 2,
                        mc.thePlayer);
            }
        }
    }

    private int hotbarBandCount() {
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if ((stack != null) && (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem(Items.paper))) {
                return stack.stackSize;
            }
        }
        return -1;
    }

    private int getBandageSlot() {
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if ((stack != null) && (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem(Items.paper))) {
                return i;
            }
        }
        return -1;
    }

}
