package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.gui.click.components.CategoryButton;
import exhibition.gui.click.components.TextBox;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.NetUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import org.lwjgl.input.Keyboard;

public class InventoryWalk extends Module {

    private String CARRY = "CARRY";

    private Setting autoCraft = new Setting<>("AUTOCRAFT", false, "Automatically crafts logs into planks.");

    public InventoryWalk(ModuleData data) {
        super(data);
        settings.put(CARRY, new Setting(CARRY, false, "Carry items in crafting slots."));
        settings.put(autoCraft.getName(), autoCraft);
    }

    @Override
    @RegisterEvent(events = {EventPacket.class, EventTick.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        boolean bad = false;
        for (CategoryButton button : Client.getClickGui().mainPanel.typeButton) {
            for (TextBox textBox : button.categoryPanel.textBoxes) {
                if (textBox.isTyping || textBox.isFocused)
                    bad = true;
            }
        }
        if (mc.currentScreen instanceof GuiChat || bad) {
            return;
        }
        if (event instanceof EventTick) {
            if ((boolean) autoCraft.getValue()) {
                boolean full = true;
                ItemStack[] arrayOfItemStack;
                int j = (arrayOfItemStack = mc.thePlayer.inventory.mainInventory).length;
                for (int i = 0; i < j; i++) {
                    ItemStack item = arrayOfItemStack[i];
                    if (item == null) {
                        full = false;
                        break;
                    }
                }

                if (!full)
                    for (int i = 9; i < 45; i++) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                            if (item.getItem() != null) {
                                if (item.getItem() instanceof ItemBlock) {
                                    ItemBlock itemBlock = (ItemBlock) item.getItem();
                                    if (itemBlock.getBlock() == Blocks.log || itemBlock.getBlock() == Blocks.log2) {
                                        NetUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                                        mc.playerController.windowClick(0, i, 0, 0, mc.thePlayer);
                                        int slot = 4;
                                        mc.playerController.windowClick(0, slot, 0, 0, mc.thePlayer);
                                        mc.playerController.windowClick(0, 0, 0, 1, mc.thePlayer);
                                        NetUtil.sendPacket(new C0DPacketCloseWindow(0));
                                        break;
                                    }
                                }
                            }
                        }
                    }
            }

            if (mc.currentScreen != null) {
                if (Keyboard.isKeyDown(200)) {
                    mc.thePlayer.rotationPitch = (mc.thePlayer.rotationPitch - 0.25F);
                }
                if (Keyboard.isKeyDown(208)) {
                    mc.thePlayer.rotationPitch = (mc.thePlayer.rotationPitch + 0.25F);
                }
                if (Keyboard.isKeyDown(203)) {
                    mc.thePlayer.rotationYaw = (mc.thePlayer.rotationYaw - 0.5F);
                }
                if (Keyboard.isKeyDown(205)) {
                    mc.thePlayer.rotationYaw = (mc.thePlayer.rotationYaw + 0.5F);
                }
            }
        }
        if (event instanceof EventPacket && ((Boolean) settings.get(CARRY).getValue())) {
            EventPacket ep = (EventPacket) event;
            if (ep.isOutgoing() && ep.getPacket() instanceof C0DPacketCloseWindow) {
                ep.setCancelled(true);
            }
        }
    }
}
