package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class AutoPaper extends Module {

    private final Timer timer = new Timer();

    public AutoPaper(ModuleData data) {
        super(data);
    }

    public boolean isEnabled = false;

    public void worldChange() {
        isEnabled = false;
    }

    @RegisterEvent(events = {EventRenderGui.class})
    public void onEvent(Event event) {
        if (isEnabled)
            return;

        if (event instanceof EventRenderGui) {
            if (HypixelUtil.isVerifiedHypixel() && HypixelUtil.isInGame("SKYWARS") && HypixelUtil.isGameStarting()) {
                if (mc.currentScreen instanceof GuiChest) {
                    GuiChest chest = (GuiChest) mc.currentScreen;
                    ItemStack stack = chest.lowerChestInventory.getStackInSlot(7);
                    if (stack != null) {
                        if (stack.getItem() == Items.paper) {
                            if (!isEnabled) {
                                Slot slot = chest.inventorySlots.inventorySlots.get(7);
                                chest.handleMouseClick(slot, slot.slotNumber, 0, 0);
                                timer.reset();
                                isEnabled = true;
                            }
                            mc.thePlayer.closeScreen();
                        }
                    }
                } else {
                    int challengeSlot = 7;
                    if (timer.delay(2000)) {
                        if (mc.thePlayer.inventoryContainer.getSlot(36 + challengeSlot).getHasStack()) {
                            Item item = mc.thePlayer.inventoryContainer.getSlot(36 + challengeSlot).getStack().getItem();
                            if (item == Items.blaze_powder) {
                                int oldSlot = mc.thePlayer.inventory.currentItem;
                                NetUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = challengeSlot));
                                if (mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem())) {
                                    mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                                }
                                NetUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = oldSlot));
                                timer.reset();
                            }
                        }
                    }
                }
            }
        }
    }

}
