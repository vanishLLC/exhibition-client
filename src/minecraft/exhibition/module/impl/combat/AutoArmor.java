package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.other.ChestStealer;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

public class AutoArmor extends Module {

    private Timer timer = new Timer();
    private boolean isOpen;

    private Setting<Boolean> openOnly = new Setting<>("INV-ONLY", true, "Only clean when inventory is open.");
    private Setting<Boolean> attackCheck = new Setting<>("ATTACKING", true, "Does not remove armor when attacking players.");

    public AutoArmor(ModuleData data) {
        super(data);
        settings.put(attackCheck.getName(), attackCheck);
    }

    private static boolean swapped;

    public static boolean swapped() {
        return swapped;
    }

    @Override
    public void onToggle() {
        swapped = false;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (!mc.thePlayer.capabilities.allowEdit || AutoSword.swapped() || mc.currentScreen instanceof GuiContainer || AutoPot.potting || mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer || ChestStealer.isStealing || HypixelUtil.scoreboardContains("Game end")) {
            timer.reset();
            swapped = false;
            return;
        }
        EventMotionUpdate em = event.cast();
        if(!em.isPre())
            return;

        swapped = false;

        Killaura k = (Killaura) Client.getModuleManager().get(Killaura.class);
        boolean attacking = (boolean) attackCheck.getValue() && k.isEnabled() && Killaura.getTarget() != null;
        boolean inInventory = mc.currentScreen instanceof GuiInventory;
        boolean canClean = ((openOnly.getValue() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat)) || inInventory);

        if (mc.thePlayer != null && canClean) {
            int slotID = -1;
            double maxProt = -1.0D;
            int switchArmor = -1;
            boolean isEquipping = false;
            for (int i = 9; i < 45; i++) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack != null && canEquip(stack)) {
                    double protValue = getProtectionValue(stack);
                    if (protValue > maxProt) {
                        isEquipping = true;
                        slotID = i;
                        maxProt = protValue;
                    }
                }
            }

            if (!isEquipping) {
                maxProt = -1.0D;
                for (int i = 9; i < 45; i++) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (stack != null && betterCheck(stack)) {
                        if (switchArmor == -1 & !attacking) {
                            switchArmor = betterSwap(stack);
                        }
                        double protValue = getProtectionValue(stack);
                        if (protValue > maxProt) {
                            slotID = i;
                            maxProt = protValue;
                        }
                    }
                }
            }
            if (slotID != -1) {
                swapped = true;
                if (timer.delay((long)(50 + (Math.random() * 200)))) {
                    if (!isOpen && !inInventory) {
                        NetUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        isOpen = true;
                    }
                    if (switchArmor != -1) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 4 + switchArmor, 0, 4, mc.thePlayer);
                        timer.setDifference(-100);
                    } else {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotID, 0, 1, mc.thePlayer);
                        timer.reset();
                    }
                }
            } else {
                if (isOpen && !inInventory) {
                    NetUtil.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
                    isOpen = false;
                }
                timer.reset();
                swapped = false;
            }
        } else {
            swapped = false;
            timer.reset();
        }
    }


    private boolean betterCheck(ItemStack stack) {
        try {
            if (stack.getItem() instanceof ItemArmor) {
                if (mc.thePlayer.getEquipmentInSlot(1) != null && stack.getUnlocalizedName().contains("boots")) {
                    if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(1))) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(2) != null && stack.getUnlocalizedName().contains("leggings")) {
                    if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(2))) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(3) != null && stack.getUnlocalizedName().contains("chestplate")) {
                    if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(3))) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(4) != null && stack.getUnlocalizedName().contains("helmet")) {
                    if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(4))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private int betterSwap(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            if (mc.thePlayer.getEquipmentInSlot(3) != null && stack.getUnlocalizedName().contains("chestplate")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(3))) {
                    return 2;
                }
            }
            if (mc.thePlayer.getEquipmentInSlot(2) != null && stack.getUnlocalizedName().contains("leggings")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(2))) {
                    return 3;
                }
            }
            if (mc.thePlayer.getEquipmentInSlot(1) != null && stack.getUnlocalizedName().contains("boots")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(1))) {
                    return 4;
                }
            }
            if (mc.thePlayer.getEquipmentInSlot(4) != null && stack.getUnlocalizedName().contains("helmet")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(4))) {
                    return 1;
                }
            }
        }
        return -1;
    }

    private boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof ItemArmor && ((mc.thePlayer.getEquipmentInSlot(1) == null && stack.getUnlocalizedName().contains("boots"))
                || (mc.thePlayer.getEquipmentInSlot(2) == null && stack.getUnlocalizedName().contains("leggings"))
                || (mc.thePlayer.getEquipmentInSlot(3) == null && stack.getUnlocalizedName().contains("chestplate"))
                || (mc.thePlayer.getEquipmentInSlot(4) == null && stack.getUnlocalizedName().contains("helmet")));
    }

    private double getProtectionValue(ItemStack stack) {
        if ((stack.getItem() instanceof ItemArmor)) {//floor ( [ 6 + level ^ 2 ] * .75 / 3 )​
            return ((ItemArmor) stack.getItem()).damageReduceAmount + (100 - ((ItemArmor) stack.getItem()).damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.0075D;
        }
        return 0;
    }
}