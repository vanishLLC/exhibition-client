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
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
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
    private String DELAY = "DELAY";
    private String HEALTH = "HEALTH";

    private final Setting<Boolean> soup = new Setting<>("SOUP", true),
            heads = new Setting<>("HEADS", true),
            mutton = new Setting<>("MUTTON", true),
            gapples = new Setting<>("GAPPLES", true),
            steak = new Setting<>("STEAK", true),
            bread = new Setting<>("BREAD", true);

    private final Setting<Number> resHealth = new Setting<>("RES-HEALTH", 8.75, "Maximum health before consuming Mutton.", 0.25, 0.5, 10);

    public AutoSoup(ModuleData data) {
        super(data);
        settings.put(HEALTH, new Setting<>(HEALTH, 15.5, "Maximum health before healing.", 0.5, 1, 20));
        settings.put(DELAY, new Setting<>(DELAY, 350, "Delay before healing again.", 50, 100, 1000));
        addSetting(new Setting<>("CONSUMABLES", new MultiBool("Consumables", soup, heads, mutton, gapples, steak, bread)));
        addSetting(resHealth);
    }

    public static boolean isHealing = false;
    private int lastItem = -1;
    private int lastCurrentItem = -1;

    @Override
    public Priority getPriority() {
        return Priority.FIRST;
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                int soupSlot = getSoupFromInventory();

                float minHealth = ((Number) settings.get(HEALTH).getValue()).floatValue();

                boolean shouldEat = soupSlot != -1 && mc.thePlayer.inventoryContainer.getSlot(soupSlot).getHasStack() && mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack().getItem() == Items.cooked_beef;

                boolean shouldResistance = soupSlot != -1 && mc.thePlayer.inventoryContainer.getSlot(soupSlot).getHasStack() && mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack().getItem() == Items.mutton
                        && mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() / 2F <= resHealth.getValue().floatValue() : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= resHealth.getValue().floatValue() / 10F;

                boolean shouldHeal = (mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= minHealth : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= minHealth / 20F) ||
                        (shouldEat && mc.thePlayer.getFoodStats().needFood()) || shouldResistance;

                Killaura killaura = Client.getModuleManager().get(Killaura.class);

                if (lastItem != -1 && isHealing) {
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer(em.isOnground()));
                    NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange((lastItem + 1) % 9));
                    NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(lastItem));
                    NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    if (killaura.isEnabled() && killaura.isBlocking && Killaura.getTarget() != null) {
                        killaura.isBlocking = false;
                    }
                    isHealing = false;
                    lastItem = -1;
                    lastCurrentItem = -1;
                    return;
                }

                if (soupSlot != -1 && shouldHeal && timer.delay(((Number) settings.get(DELAY).getValue()).longValue())) {
                    int swapTo = 6;
                    if (soupSlot > 36)
                        swapTo = soupSlot - 36;
                    else
                        swap(soupSlot, 6);

                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack();

                    if (Client.instance.is1_9orGreater() && HypixelUtil.isVerifiedHypixel() && stack != null && notInstantUse(stack.getItem())) {
                        lastItem = swapTo;
                        lastCurrentItem = mc.thePlayer.inventory.currentItem;
                        if ((killaura.isEnabled() && killaura.isBlocking) || mc.thePlayer.isBlocking()) {
                            NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }

                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(lastItem));
                        NetUtil.sendPacketNoEvents(new C08PacketPlayerBlockPlacement(stack));
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange((lastItem + 1) % 9));
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(lastItem));
                    } else {
                        int currentItem = mc.thePlayer.inventory.currentItem;
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = swapTo));
                        NetUtil.sendPacketNoEvents(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = currentItem));
                    }
                    isHealing = true;
                    timer.reset();
                } else {
                    isHealing = false;
                }

//                if ((boolean) settings.get(DROP).getValue()) {
//                    for (int i = 9; i < 45; i++) {
//                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
//                            ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
//                            Item item = is.getItem();
//                            if (item == Items.bowl) {
//                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 0, 4, mc.thePlayer);
//                                break;
//                            }
//                        }
//                    }
//                }
            } else {
                if (lastItem != -1 && lastCurrentItem == 1) {

                }
            }
        }
    }

    private final Item[] instantItems = new Item[]{Items.skull, Items.baked_potato, Items.magma_cream, Items.mutton};

    private boolean notInstantUse(Item item) {
        for (Item instantItem : instantItems) {
            if (item == instantItem) {
                return false;
            }
        }
        return true;
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    private int getSoupFromInventory() {
        Minecraft mc = Minecraft.getMinecraft();
        int slot = -1;
        int priority = -1;
        /*
         * Soup = 0
         * Golden Heads = 1 (all pit healing items)
         * Golden Apples = 3
         * Mutton = 2
         * Steak = 0 normal 2 pit
         * Bread = 0 normal 2 pit
         *
         * We want to Gapple first then consume steak/breads
         */

        boolean isInPit = HypixelUtil.isInGame("PIT");


        float minHealth = ((Number) settings.get(HEALTH).getValue()).floatValue();
        boolean shouldHeal = (mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= minHealth : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= minHealth / 20F);

        boolean needsRegenOrAbsorption = (!mc.thePlayer.isPotionActive(Potion.regeneration) || (mc.thePlayer.getAbsorptionAmount() <= 0) ||
                (mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getDuration() < 5)) && shouldHeal;

        boolean shouldResistance = mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() / 2F <= resHealth.getValue().floatValue() : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= resHealth.getValue().floatValue() / 10F;

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();

                boolean shouldMutton = mutton.getValue() && item == Items.mutton && shouldResistance && !mc.thePlayer.isPotionActive(Potion.resistance);

                boolean shouldInstantHead = heads.getValue() && needsRegenOrAbsorption && (item == Items.skull || item == Items.baked_potato || item == Items.magma_cream);

                if (priority < 1 && shouldInstantHead) {
                    slot = i;
                    priority = 1;
                    continue;
                }
                if (priority < 3 && shouldMutton) {
                    slot = i;
                    priority = 3;
                    continue;
                }

                if (Client.instance.is1_9orGreater() && (item == Items.golden_apple || item == Items.cooked_beef || item == Items.bread)) {
                    if (isInPit) {
                        if (gapples.getValue() && priority < 3 && needsRegenOrAbsorption && item == Items.golden_apple) {
                            slot = i;
                            priority = 3;
                            continue;
                        }
                        if (bread.getValue() && priority < 2 && item == Items.bread) {
                            if (!mc.thePlayer.isPotionActive(Potion.regeneration) || (mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getAmplifier() < 2)) {
                                slot = i;
                                priority = 2;
                                continue;
                            }
                        }
                        if (steak.getValue() && priority < 2 && needsRegenOrAbsorption  && item == Items.cooked_beef) {
                            slot = i;
                            priority = 2;
                            continue;
                        }
                    } else {
                        if (gapples.getValue() && priority < 3 && needsRegenOrAbsorption && item == Items.golden_apple) {
                            slot = i;
                            priority = 3;
                            continue;
                        }

                        if (steak.getValue() && priority < 1 && mc.thePlayer.getFoodStats().needFood() && item == Items.cooked_beef) {
                            slot = i;
                            priority = 1;
                            continue;
                        }
                        if (bread.getValue() && priority < 1 && mc.thePlayer.getFoodStats().needFood() && item == Items.bread) {
                            slot = i;
                            priority = 1;
                            continue;
                        }
                    }
                }

                if (priority == -1 && item == Items.mushroom_stew) {
                    slot = i;
                    priority = 0;
                }
            }
        }
        return slot;
    }

}
