/**
 * Time: 12:16:54 AM
 * Date: Dec 26, 2016
 * Creator: cool1
 */
package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Module {

    private Timer timer = new Timer();
    private String HEADS = "HEADS";
    private String DELAY = "DELAY";
    private String HEALTH = "HEALTH";
    private String DROP = "DROP";

    public AutoSoup(ModuleData data) {
        super(data);
        settings.put(HEALTH, new Setting<>(HEALTH, 3, "Maximum health before healing."));
        settings.put(DELAY, new Setting<>(DELAY, 350, "Delay before healing again.", 50, 100, 1000));
        settings.put(HEADS, new Setting<>(HEADS, false, "Use player heads."));
        settings.put(DROP, new Setting<>(DROP, true, "Drops empty soup bowls."));
    }

    public static boolean isHealing = false;

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                int soupSlot = getSoupFromInventory();

                float minHealth = ((Number) settings.get(HEALTH).getValue()).floatValue();

                double minimumPercent = minHealth / 20F;

                boolean shouldHeal = mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= minHealth : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= minimumPercent;

                if (soupSlot != -1 && shouldHeal && isHealing) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack();

                    if (Client.instance.is1_16_4() && HypixelUtil.isVerifiedHypixel() && stack != null && stack.getItem() == Items.golden_apple) {
                        int swapTo = 6;
                        if (soupSlot > 36)
                            swapTo = soupSlot - 36;
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange((swapTo + 1) % 9));
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(swapTo));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround));
                        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        isHealing = false;
                    }
                }

                if (soupSlot != -1 && shouldHeal && timer.delay(((Number) settings.get(DELAY).getValue()).longValue())) {
                    int swapTo = 6;
                    if (soupSlot > 36)
                        swapTo = soupSlot - 36;
                    else
                        swap(soupSlot, 6);

                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack();

                    if (Client.instance.is1_16_4() && HypixelUtil.isVerifiedHypixel() && stack != null && stack.getItem() == Items.golden_apple) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(swapTo));
                        mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(stack));
                    } else {
                        int currentItem = mc.thePlayer.inventory.currentItem;
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = swapTo));

                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = currentItem));
                    }
                    timer.reset();
                    isHealing = true;
                } else {
                    isHealing = false;
                }

                if ((boolean) settings.get(DROP).getValue()) {
                    for (int i = 9; i < 45; i++) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                            Item item = is.getItem();
                            if (item == Items.bowl) {
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 0, 4, mc.thePlayer);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    private int getSoupFromInventory() {
        Minecraft mc = Minecraft.getMinecraft();
        int soup = -1;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();

                boolean shouldMutton = Item.getIdFromItem(item) == Item.getIdFromItem(Items.mutton) && !mc.thePlayer.isPotionActive(Potion.resistance);

                boolean shouldApple = (boolean) settings.get(HEADS).getValue() && (((Item.getIdFromItem(item) == Item.getIdFromItem(Items.skull) ||
                        Item.getIdFromItem(item) == Item.getIdFromItem(Items.baked_potato) ||
                        Item.getIdFromItem(item) == Item.getIdFromItem(Items.magma_cream) ||
                        (Client.instance.is1_16_4() && Item.getIdFromItem(item) == Item.getIdFromItem(Items.golden_apple))) &&
                        (!mc.thePlayer.isPotionActive(Potion.regeneration) || (mc.thePlayer.getAbsorptionAmount() <= 0) ||
                                (mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getDuration() < 5))) || shouldMutton);
                if (Item.getIdFromItem(item) == 282 || shouldApple) {
                    soup = i;
                }
            }
        }
        return soup;
    }

}
