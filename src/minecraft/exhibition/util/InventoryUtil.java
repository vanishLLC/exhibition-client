package exhibition.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtil implements MinecraftUtil {
    public static void clickSlot(int slot, int mouseButton, boolean shiftClick) {
        mc.playerController.windowClick(mc.currentScreen == null ? 0 : mc.thePlayer.inventoryContainer.windowId, slot, mouseButton, shiftClick ? 1 : 0, mc.thePlayer);
    }

    public static void clickSlot(int slot, boolean shiftClick) {
        mc.playerController.windowClick(mc.currentScreen == null ? 0 : mc.thePlayer.inventoryContainer.windowId, slot, 0, shiftClick ? 1 : 0, mc.thePlayer);
    }

    public static void doubleClickSlot(int slot) {
        mc.playerController.windowClick(mc.currentScreen == null ? 0 : mc.thePlayer.inventoryContainer.windowId, slot, 0, 0, mc.thePlayer);
        mc.playerController.windowClick(mc.currentScreen == null ? 0 : mc.thePlayer.inventoryContainer.windowId, slot, 0, 6, mc.thePlayer);
        mc.playerController.windowClick(mc.currentScreen == null ? 0 : mc.thePlayer.inventoryContainer.windowId, slot, 0, 0, mc.thePlayer);
    }

    public static int findItemInInventory(Item item) {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int findItemInHotbar(Item item) {
        for (int i = 36; i < 45; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

}
