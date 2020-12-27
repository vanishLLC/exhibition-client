package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoArmor;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.combat.AutoSword;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Arithmo on 9/6/2017 at 11:00 PM.
 */
public class InventoryCleaner extends Module {

    private String TOGGLE = "TOGGLE";
    private String BLOCKCAP = "BLOCKCAP";
    private String ARCHERY = "ARCHERY";
    private String FOOD = "FOOD";
    private String TOOLS = "TOOLS";
    private Random random = new Random();


    public InventoryCleaner(ModuleData data) {
        super(data);
        settings.put(TOGGLE, new Setting<>(TOGGLE, false, "Turn off when finished."));
        settings.put(ARCHERY, new Setting<>(ARCHERY, false, "Clean bows and arrows."));
        settings.put(FOOD, new Setting<>(FOOD, false, "Clean food. Keeps Golden Apples."));
        settings.put(TOOLS, new Setting<>(TOOLS, false, "Clean all tools."));
        settings.put(BLOCKCAP, new Setting<>(BLOCKCAP, 128, "Max stacks of blocks allowed in your inventory.", 8, 0, 1728));
    }

    private static boolean isCleaning;

    public static boolean isCleaning() {
        return isCleaning;
    }

    @Override
    public void onToggle() {
        isCleaning = false;
    }

    @Override
    public void onEnable() {
        if ((Boolean) settings.get(TOGGLE).getValue())
            firstEnable = true;
    }

    private boolean firstEnable;

    @Override
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    @RegisterEvent(events = EventMotionUpdate.class)
    public void onEvent(Event event) {
        if (!mc.thePlayer.capabilities.allowEdit || AutoArmor.swapped() || AutoSword.swapped() || mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer || AutoPot.potting) {
            return;
        }

        if(HypixelUtil.isGameStarting())
            return;

        EventMotionUpdate em = (EventMotionUpdate) event;
        if (em.isPre() && mc.thePlayer != null && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiInventory) && random.nextInt(2) == 0) {
            for (int i = 9; i < 45; i++) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (isBad(is) && (is != mc.thePlayer.getCurrentEquippedItem())) {
                        if (!isCleaning) {
                            isCleaning = true;
                            NetUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        }
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 1, 4, mc.thePlayer);
                        firstEnable = false;
                        break;
                    }
                }
                if (i == 44 && (isCleaning || firstEnable)) {
                    isCleaning = false;
                    NetUtil.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
                    if ((Boolean) settings.get(TOGGLE).getValue()) toggle();
                }
            }
        }
    }

    private float getEfficiency(ItemStack itemTool) {
        float var2 = ((ItemTool) itemTool.getItem()).efficiencyOnProperMaterial;

        if (var2 > 1.0f) {
            final int var6 = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemTool);
            var2 += var6 * var6 + 1;
        }
        return var2;
    }

    private boolean isToolWorst(ItemStack item) {
        List<ItemAxe> axeList = new ArrayList<>();
        List<ItemPickaxe> pickaxeList = new ArrayList<>();
        List<ItemSpade> shovelList = new ArrayList<>();

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemTool) {
                    ItemTool itemTool = (ItemTool) is.getItem();
                    if (itemTool instanceof ItemPickaxe && item.getItem() instanceof ItemPickaxe && axeList.size() < 2 && shovelList.size() < 2) {
                        pickaxeList.add((ItemPickaxe) itemTool);
                    } else if (itemTool instanceof ItemAxe && item.getItem() instanceof ItemAxe && pickaxeList.size() < 2 && shovelList.size() < 2) {
                        axeList.add((ItemAxe) itemTool);
                    } else if (itemTool instanceof ItemSpade && pickaxeList.size() < 2 && axeList.size() < 2) {
                        shovelList.add((ItemSpade) itemTool);
                    }

                }
            }
        }

        if (pickaxeList.size() > 1 && item.getItem() instanceof ItemPickaxe) {
            ItemStack bestStack = null;
            float currentItemDamage = getEfficiency(item);
            float bestDamage = -1;
            for (int i = 9; i < 45; i++)
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    Item itemTool = stack.getItem();
                    if (itemTool instanceof ItemPickaxe) {
                        float itemDamage = getEfficiency(stack);
                        if (itemDamage >= bestDamage) {
                            bestDamage = itemDamage;
                            bestStack = stack;
                        }
                    }
                }
            return currentItemDamage < bestDamage || (currentItemDamage == bestDamage && !item.equals(bestStack));
        }

        if (axeList.size() > 1 && item.getItem() instanceof ItemAxe) {
            ItemStack bestStack = null;
            float currentItemDamage = getEfficiency(item);
            float bestDamage = -1;
            for (int i = 9; i < 45; i++)
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    Item itemTool = stack.getItem();
                    if (itemTool instanceof ItemAxe) {
                        float itemDamage = getEfficiency(stack);
                        if (itemDamage >= bestDamage) {
                            bestDamage = itemDamage;
                            bestStack = stack;
                        }
                    }
                }
            return currentItemDamage < bestDamage || (currentItemDamage == bestDamage && !item.equals(bestStack));
        }

        if (shovelList.size() > 1 && item.getItem() instanceof ItemSpade) {
            ItemStack bestStack = null;
            float currentItemDamage = getEfficiency(item);
            float bestDamage = -1;
            for (int i = 9; i < 45; i++)
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    Item itemTool = stack.getItem();
                    if (itemTool instanceof ItemSpade) {
                        float itemDamage = getEfficiency(stack);
                        if (itemDamage >= bestDamage) {
                            bestDamage = itemDamage;
                            bestStack = stack;
                        }
                    }
                }
            return currentItemDamage < bestDamage || (currentItemDamage == bestDamage && !item.equals(bestStack));
        }
        return false;
    }

    private boolean isBad(ItemStack item) {
        if (item == null)
            return false;

        int swordCount = 0;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemSword) {
                    swordCount++;
                }
            }
        }
        int swordSlot = -1;
        if (swordCount > 1 && item.getItem() instanceof ItemSword) {
            float bestDamage = -1;
            for (int i = 9; i < 45; i++)
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    Item itemSword = stack.getItem();
                    if (itemSword instanceof ItemSword) {
                        float itemDamage = getDamage(mc.thePlayer.inventoryContainer.getSlot(i).getStack());
                        if (itemDamage > bestDamage) {
                            bestDamage = itemDamage;
                            if (stack == item)
                                swordSlot = i - 36;
                        }
                    }
                }
            return getDamage(item) < bestDamage || (getDamage(item) == bestDamage && swordSlot != ((AutoSword) Client.getModuleManager().get(AutoSword.class)).getSwordSlot());
        }

        if (item.getItem() instanceof ItemTool) {
            return ((boolean) settings.get(TOOLS).getValue() || isToolWorst(item));
        }

        if (item.getItem() instanceof ItemArmor) {
            if (!betterCheck(item) && !canEquip(item)) {
                return true;
            }
        }

        return (item.getItem().getUnlocalizedName().contains("tnt") || item.getItem().getUnlocalizedName().contains("stick") || item.getItem().getUnlocalizedName().contains("egg") || item.getItem().getUnlocalizedName().contains("string") || item.getItem().getUnlocalizedName().contains("flint") || item.getItem().getUnlocalizedName().contains("compass") || item.getItem().getUnlocalizedName().contains("feather") || item.getItem().getUnlocalizedName().contains("bucket") || item.getItem().getUnlocalizedName().contains("chest") && !item.getDisplayName().toLowerCase().contains("collect") || item.getItem().getUnlocalizedName().contains("snow") || item.getItem().getUnlocalizedName().contains("fish") || item.getItem().getUnlocalizedName().contains("enchant") || item.getItem().getUnlocalizedName().contains("exp") || item.getItem().getUnlocalizedName().contains("shears") || item.getItem().getUnlocalizedName().contains("anvil") || item.getItem().getUnlocalizedName().contains("torch") || item.getItem().getUnlocalizedName().contains("seeds") || item.getItem().getUnlocalizedName().contains("leather") || item.getItem() instanceof ItemGlassBottle || item.getItem().getUnlocalizedName().contains("piston") || item.getItem().getUnlocalizedName().contains("potion") && isBadPotion(item) || item.getItem() instanceof ItemBlock && getBlockCount() > ((Number) settings.get(BLOCKCAP).getValue()).intValue() || item.getItem() instanceof ItemFood && (Boolean) settings.get(FOOD).getValue() && !(item.getItem() instanceof ItemAppleGold) || ((item.getItem() instanceof ItemBow || item.getItem().getUnlocalizedName().contains("arrow")) && (Boolean) settings.get(ARCHERY).getValue()));
    }

    private double getProtectionValue(ItemStack stack) {
        if ((stack.getItem() instanceof ItemArmor)) {//floor ( [ 6 + level ^ 2 ] * .75 / 3 )​
            return ((ItemArmor) stack.getItem()).damageReduceAmount + (100 - ((ItemArmor) stack.getItem()).damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.0075D;
        }
        return 0;
    }

    private boolean betterCheck(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            if (mc.thePlayer.getEquipmentInSlot(3) != null && stack.getUnlocalizedName().contains("chestplate")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(3))) {
                    return true;
                }
            }
            if (mc.thePlayer.getEquipmentInSlot(2) != null && stack.getUnlocalizedName().contains("leggings")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(2))) {
                    return true;
                }
            }
            if (mc.thePlayer.getEquipmentInSlot(1) != null && stack.getUnlocalizedName().contains("boots")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(1))) {
                    return true;

                }
            }
            if (mc.thePlayer.getEquipmentInSlot(4) != null && stack.getUnlocalizedName().contains("helmet")) {
                if (getProtectionValue(stack) > getProtectionValue(mc.thePlayer.getEquipmentInSlot(4))) {
                    return true;
                }
            }
        }
        return false;
    }

    private float bestDamage() {
        float bestDamage = 0;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemSword && getDamage(is) > bestDamage)
                    bestDamage = getDamage(is);
            }
        }
        return bestDamage;
    }

    private boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof ItemArmor && ((mc.thePlayer.getEquipmentInSlot(1) == null && stack.getUnlocalizedName().contains("boots"))
                || (mc.thePlayer.getEquipmentInSlot(2) == null && stack.getUnlocalizedName().contains("leggings"))
                || (mc.thePlayer.getEquipmentInSlot(3) == null && stack.getUnlocalizedName().contains("chestplate"))
                || (mc.thePlayer.getEquipmentInSlot(4) == null && stack.getUnlocalizedName().contains("helmet")));
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock && !Scaffold.getBlacklistedBlocks().contains(((ItemBlock) item).getBlock())) {
                    blockCount += is.stackSize;
                }
            }
        }
        return blockCount;
    }

    private boolean isBadPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            final ItemPotion potion = (ItemPotion) stack.getItem();
            if (ItemPotion.isSplash(stack.getItemDamage())) {
                for (final Object o : potion.getEffects(stack)) {
                    final PotionEffect effect = (PotionEffect) o;
                    if (effect.getPotionID() == Potion.poison.getId() || effect.getPotionID() == Potion.harm.getId() || effect.getPotionID() == Potion.moveSlowdown.getId() || effect.getPotionID() == Potion.weakness.getId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private float getDamage(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSword)) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + ((ItemSword) stack.getItem()).getDamageVsEntity()
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.01f;
    }

}

