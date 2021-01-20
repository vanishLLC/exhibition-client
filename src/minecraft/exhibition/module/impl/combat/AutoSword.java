package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/**
 * Created by Arithmo on 6/22/2017 at 8:38 PM.
 */
public class AutoSword extends Module {

    private Timer timer = new Timer();
    private String SWAP = "SWAP";
    private String GAPPLES = "GAPPLES";
    private Setting<Boolean> useSwap = new Setting<>("USE-SWAP", false, "Swaps to your sword even if you are using an item/eating.");
    private Setting<Boolean> replace = new Setting<>("REPLACE", true, "Replaces your sword slot with the best sword. Gapples too.");

    public AutoSword(ModuleData data) {
        super(data);
        settings.put(SWAP, new Setting<>(SWAP, false, "Swaps to sword when attacking."));
        settings.put(GAPPLES, new Setting<>(GAPPLES, false, "Automatically moves gapples to the desired slot."));
        addSetting(useSwap);
        addSetting(replace);
        settings.put("SLOT", new Setting<>("SLOT", 1, "Slot number to put your sword in.", 1, 1, 9));
        settings.put("GAPPLE-SLOT", new Setting<>("GAPPLE-SLOT", 1, "Slot number to put your gapples in.", 1, 1, 9));
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    @Override
    public void onToggle() {
        swapped = false;
    }

    private static boolean swapped;

    public static boolean swapped() {
        return swapped;
    }

    @RegisterEvent(events = {EventTick.class})
    public void onEvent(Event event) {
        if (AutoArmor.swapped() || mc.currentScreen instanceof GuiContainer || AutoSoup.isHealing || AutoPot.potting || mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer) {
            timer.setDifference(0);
            swapped = false;
            return;
        }
        swapped = false;

        if (mc.currentScreen == null) {
            int slot = ((Number) settings.get("SLOT").getValue()).intValue() - 1;

            if (replace.getValue()) {
                boolean foundSword = false;
                for (int i = 9; i < 45; i++)
                    if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                        Item item = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem();
                        if (item instanceof ItemSword) {
                            float itemDamage = getAttackDamage(mc.thePlayer.inventoryContainer.getSlot(i).getStack());
                            float currentDamage = getAttackDamage(mc.thePlayer.inventoryContainer.getSlot(36 + slot).getStack());
                            if (itemDamage > currentDamage && (i - 36 != slot)) {
                                foundSword = true;
                                if (timer.delay(300)) {
                                    swap(i, slot);
                                    timer.reset();
                                    swapped = true;
                                    return;
                                }
                                break;
                            }
                        }
                    }

                boolean foundGapple = false;
                if ((boolean) settings.get(GAPPLES).getValue()) {
                    int gslot = ((Number) settings.get("GAPPLE-SLOT").getValue()).intValue() - 1;

                    for (int i = 9; i < 45; i++)
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            Item item = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem();
                            if (item instanceof ItemAppleGold && (!mc.thePlayer.inventoryContainer.getSlot(36 + gslot).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(36 + gslot).getStack().getItem() instanceof ItemAppleGold))) {
                                foundGapple = true;
                                if (timer.delay(300)) {
                                    swap(i, gslot);
                                    timer.reset();
                                    swapped = true;
                                    return;
                                }
                                break;
                            }
                        }
                }

                if (!foundSword && !foundGapple) {
                    timer.reset();
                }
            }

            if ((boolean) settings.get(SWAP).getValue() && mc.thePlayer.inventory.currentItem != slot && !AutoSoup.isHealing && !AutoPot.potting) {
                if (useSwap.getValue() || !mc.thePlayer.isUsingItem())
                    for (int i = 36; i < 45; i++)
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            Item item = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem();
                            if (item instanceof ItemSword && (Killaura.getTarget() != null || !((Killaura) Client.getModuleManager().get(Killaura.class)).loaded.isEmpty())) {
                                mc.thePlayer.inventory.currentItem = i - 36;
                                mc.playerController.updateController();
                                break;
                            }
                        }
            }

        }
    }

    public int getSwordSlot() {
        return ((Number) settings.get("SLOT").getValue()).intValue() - 1;
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSword)) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + ((ItemSword) stack.getItem()).getDamageVsEntity()
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.01f;
    }

}
