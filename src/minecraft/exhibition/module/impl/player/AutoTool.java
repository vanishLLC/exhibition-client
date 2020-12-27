/**
 * Time: 1:14:15 AM
 * Date: Jan 2, 2017
 * Creator: cool1
 */
package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class AutoTool extends Module {

    public AutoTool(ModuleData data) {
        super(data);
        settings.put("SWAP-BACK", new Setting<>("SWAP-BACK", false, "Swaps back to your original slot."));
        settings.put("SLOT", new Setting<>("SLOT", 3, "Slot number to put your tool in.", 1, 1, 9));
    }

    int oldSlot = -1;

    @RegisterEvent(events = {EventTick.class})
    public void onEvent(Event event) {
        boolean swapback = (boolean)settings.get("SWAP-BACK").getValue();
        if (!mc.gameSettings.keyBindAttack.getIsKeyPressed() || mc.objectMouseOver == null) {
            if (oldSlot != -1 && swapback) {
                mc.thePlayer.inventory.currentItem = oldSlot;
                mc.playerController.updateController();
            }
            oldSlot = -1;
            return;
        }
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if (pos == null) {
            if (oldSlot != -1 && swapback) {
                mc.thePlayer.inventory.currentItem = oldSlot;
                mc.playerController.updateController();
            }
            oldSlot = -1;
            return;
        }

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float strength = 1.0F;
        int bestItemIndex = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null) {
                continue;
            }
            float tStrength = itemStack.getStrVsBlock(block);
            if ((tStrength > strength)) {
                strength = tStrength;
                bestItemIndex = i;
            }
        }
        if (bestItemIndex > 8) {
            int slot = ((Number) settings.get("SLOT").getValue()).intValue() - 1;
            swap(bestItemIndex, slot);
            return;
        }
        if (bestItemIndex != -1) {
            if (oldSlot == -1 && swapback)
                oldSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = bestItemIndex;
            mc.playerController.updateController();
        }
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

}