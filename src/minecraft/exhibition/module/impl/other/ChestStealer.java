package exhibition.module.impl.other;

/**
 * Time: 11:39:31 PM
 * Date: Dec 25, 2016
 * Creator: cool1
 */

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRenderGuiLast;
import exhibition.event.impl.EventScreenDisplay;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.RotationUtils;
import exhibition.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;

import java.util.*;

public class ChestStealer extends Module {

    private String DELAY = "DELAY";
    private String CLOSE = "CLOSE";
    private String CHESTAURA = "CHESTAURA";
    private String IGNORE = "IGNORE";
    private String TRASH = "TRASH";

    private Options mode = new Options("Mode", "Normal", "Silent", "Render", "Normal");

    private Setting<Options> silent = new Setting<>("MODE", mode, "Chest stealer mode.");
    private Setting<Boolean> randomMiss = new Setting<>("MISS", false, "Randomly miss clicks.");
    private Setting<Boolean> rayTrace = new Setting<>("RAYTRACE", false, "Visible check for target.");
    private Setting<Boolean> tools = new Setting<>("TOOLS", false, "Takes better tools from chests.");
    private Setting<Boolean> pitMode = new Setting<>("PIT-MODE", false, "Ignores Gold in chests. Prioritizes Fresh pants over anything.");

    private Timer timer = new Timer();
    private Timer stealTimer = new Timer();
    public static boolean isStealing;

    private boolean canMissAgain = false;

    private Random random = new Random();
    private List<Integer> slotList = Arrays.asList(0, 1, 2, 3);

    public ChestStealer(ModuleData data) {
        super(data);
        settings.put(DELAY, new Setting<>(DELAY, 2, "Tick delay before grabbing next item.", 1, 1, 5));
        settings.put(CLOSE, new Setting<>(CLOSE, true, "Auto closes chests when done."));
        settings.put(CHESTAURA, new Setting<>(CHESTAURA, false, "Auto opens chests near you."));
        settings.put(IGNORE, new Setting<>(IGNORE, true, "Ignores chests that are not loot. (Menus)"));
        settings.put(TRASH, new Setting<>(TRASH, true, "Ignores trash items in minigame servers."));
        addSetting(tools);
        addSetting(silent);
        addSetting(rayTrace);
        addSetting(randomMiss);
        addSetting(pitMode);
    }

    private TileEntityChest chest = null;

    private GuiChest chestContainer = null;

    @RegisterEvent(events = {EventMotionUpdate.class, EventScreenDisplay.class, EventRenderGuiLast.class})
    public void onEvent(Event event) {
        if (event instanceof EventRenderGuiLast) {
            if (silent.getValue().getSelected().equals("Render") && mc.currentScreen == null && chestContainer != null) {
                chestContainer.drawScreen(0, 0, mc.timer.renderPartialTicks);
            }
        }
        if (event instanceof EventScreenDisplay) {
            EventScreenDisplay esd = event.cast();
            if (esd.getGuiScreen() instanceof GuiChest) {
                GuiChest guiChest = (GuiChest) esd.getGuiScreen();
                IInventory inventry = guiChest.lowerChestInventory;
                String name = guiChest.lowerChestInventory.getDisplayName().getFormattedText().toLowerCase();
                boolean isVanillaChest = !inventry.hasCustomName() || name.equals(new ChatComponentTranslation("container.chest").getFormattedText().toLowerCase().trim()) || name.equalsIgnoreCase("low") || name.equalsIgnoreCase("Chest");
                chestContainer = guiChest;

                if ((boolean) settings.get(IGNORE).getValue() && !isVanillaChest) {
                    return;
                }

                if (!silent.getValue().getSelected().equals("Normal")) {
                    ScaledResolution scaledresolution = new ScaledResolution(mc);
                    int i = scaledresolution.getScaledWidth();
                    int j = scaledresolution.getScaledHeight();
                    chestContainer.setWorldAndResolution(mc, i, j);
                    mc.thePlayer.openContainer = chestContainer.inventorySlots;
                    esd.setCancelled(true);
                }
            } else if (esd.getGuiScreen() == null) {
                chestContainer = null;
            }
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (!mc.thePlayer.capabilities.allowEdit)
                return;

            boolean isPitMode = pitMode.getValue();

            if ((Boolean) settings.get(CHESTAURA).getValue() && (mc.currentScreen == null) && !isStealing && ((Killaura) Client.getModuleManager().get(Killaura.class)).loaded.isEmpty() && Killaura.getTarget() == null && (!HypixelUtil.isInGame("SKYWARS") || HypixelUtil.isGameActive())) {
                if (stealTimer.delay(2000) && isStealing) {
                    stealTimer.reset();
                    isStealing = false;
                }
                if (em.isPre())
                    for (Object o : mc.theWorld.loadedTileEntityList) {
                        if (o instanceof TileEntityChest) {
                            TileEntityChest chest = (TileEntityChest) o;
                            float x = chest.getPos().getX();
                            float y = chest.getPos().getY();
                            float z = chest.getPos().getZ();
                            if (Client.getModuleManager().get(Killaura.class).loaded.isEmpty() && chest.lidAngle < 1 && Math.abs(RotationUtils.getYawChange(x + 0.5, z + 0.5)) < 90) {
                                if (!rayTrace.getValue() || mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(x + 0.5, y + 0.9, z + 0.5)) == null) {
                                    if (!isStealing && !chest.isEmpty && mc.thePlayer.getDistance(x, y, z) < 4 && stealTimer.delay(1000) && mc.currentScreen == null) {
                                        this.chest = chest;
                                        isStealing = true;
                                        chest.isEmpty = true;
                                        stealTimer.reset();
                                        float[] rot = RotationUtils.getRotationFromPosition(x, y + 0.5, z);
                                        em.setYaw(mc.thePlayer.rotationYaw + RotationUtils.getYawChange(x + 0.5, z + 0.5));
                                        em.setPitch(rot[1]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                if (em.isPost() && chest != null) {
                    MovingObjectPosition hitResult = getHitResult(chest.getPos());
                    if(hitResult != null) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), chest.getPos(), hitResult.sideHit, hitResult.hitVec)) {
                            NetUtil.sendPacketNoEvents(new C0APacketAnimation());
                        }
                    }
                    chest = null;
                }
            }
            if (em.isPre()) {
                if (chestContainer != null) {
                    GuiChest guiChest = chestContainer;
                    IInventory inventry = guiChest.lowerChestInventory;
                    String name = guiChest.lowerChestInventory.getDisplayName().getFormattedText().toLowerCase();
                    boolean isVanillaChest = !inventry.hasCustomName() || name.equalsIgnoreCase(new ChatComponentTranslation("container.chest").getFormattedText().toLowerCase().trim()) || name.equalsIgnoreCase("low") || name.equalsIgnoreCase("Chest");
                    chestContainer = guiChest;

                    if ((boolean) settings.get(IGNORE).getValue() && !isVanillaChest) {
                        return;
                    }
                    if (!isStealing) {
                        canMissAgain = random.nextBoolean();
                    }
                    if (isPitMode || (!Client.instance.is1_9orGreater()) || !isStealing) {
                        this.slotList = new ArrayList<>();
                        for (int i = 0; i < guiChest.lowerChestInventory.getSizeInventory(); i++) {
                            ItemStack stack = guiChest.lowerChestInventory.getStackInSlot(i);

                            if (stack != null) {
                                this.slotList.add(i);
                                canMissAgain = random.nextBoolean();
                            } else if (!isPitMode && randomMiss.getValue() && canMissAgain && Math.random() > 0.95) {
                                canMissAgain = false;
                                this.slotList.add(i);
                            }
                        }
                        Collections.shuffle(this.slotList);

                        if (isPitMode) {
                            this.slotList.sort(Comparator.comparingDouble((o) -> {

                                double weight = 0;
                                ItemStack firstItem = guiChest.lowerChestInventory.getStackInSlot(o);
                                if (firstItem != null) {
                                    Item item = firstItem.getItem();
                                    if (item instanceof ItemArmor) {
                                        ItemArmor armor = (ItemArmor) item;
                                        weight -= armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER ? 10 : 3;
                                    }
                                    if (item instanceof ItemSword) {
                                        weight -= 2;
                                    }
                                    if (item instanceof ItemBow) {
                                        weight -= 1;
                                    }
                                }

                                return weight;
                            }));
                        }
                    }
                    isStealing = true;

                    boolean full = true;
                    int delayMS = 50 * ((Number) settings.get(DELAY).getValue()).intValue() + (random.nextInt(2));
                    ItemStack[] arrayOfItemStack;
                    int j = (arrayOfItemStack = mc.thePlayer.inventory.mainInventory).length;
                    for (int i = 0; i < j; i++) {
                        ItemStack item = arrayOfItemStack[i];
                        if (item == null) {
                            full = false;
                            break;
                        }
                    }
                    boolean containsItems = false;
                    if (!full) {
                        for (int index : slotList) {
                            ItemStack stack = guiChest.lowerChestInventory.getStackInSlot(index);
                            if (stack != null && !isBad(stack)) {
                                containsItems = true;
                                break;
                            }
                        }

                        if (containsItems) {
                            int remove = -1337;

                            for (int index : slotList) {
                                ItemStack stack = guiChest.lowerChestInventory.getStackInSlot(index);
                                if (timer.delay(delayMS)) {
                                    if (stack != null && !isBad(stack)) {
                                        timer.reset();
                                        Slot slot = guiChest.inventorySlots.inventorySlots.get(index);
//                                        if ((boolean) settings.get(DROP).getValue()) {
//                                            mc.playerController.windowClick(guiChest.inventorySlots.windowId, slot.slotNumber, 0, 4, mc.thePlayer);
//                                            //mc.playerController.windowClick(guiChest.inventorySlots.windowId, -999, 0, 0, mc.thePlayer);
//                                        } else {
//                                            guiChest.handleMouseClick(slot, slot.slotNumber, 0, 1);
//                                            //guiChest.handleMouseClick(slot, slot.slotNumber, 0, 6);
//                                        }
                                        guiChest.handleMouseClick(slot, slot.slotNumber, 0, 1);
                                        //guiChest.handleMouseClick(slot, slot.slotNumber, 0, 6);

                                        if (Client.instance.is1_9orGreater()) {
                                            remove = index;
                                        }

                                        break;
                                    }
                                }
                            }

                            if (remove != -1337) {
                                this.slotList.remove((Integer) remove);
                            }
                        } else if ((Boolean) settings.get(CLOSE).getValue() && timer.delay(100)) {
                            if (mc.currentScreen == chestContainer) {
                                mc.thePlayer.closeScreen();
                                chestContainer = null;
                            } else {
                                mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(chestContainer.inventorySlots.windowId));
                                mc.thePlayer.inventory.setItemStack(null);
                                mc.thePlayer.openContainer = mc.thePlayer.inventoryContainer;
                                chestContainer = null;
                            }
                            isStealing = false;
                        }
                    } else if ((Boolean) settings.get(CLOSE).getValue() && timer.delay(100)) {
                        if (mc.currentScreen == chestContainer) {
                            mc.thePlayer.closeScreen();
                            chestContainer = null;
                        } else {
                            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(chestContainer.inventorySlots.windowId));
                            mc.thePlayer.inventory.setItemStack(null);
                            mc.thePlayer.openContainer = mc.thePlayer.inventoryContainer;
                            chestContainer = null;
                        }
                        isStealing = false;
                    }
                } else {
                    timer.setDifference(50);
                    isStealing = false;
                }
            }
        }
    }

    private MovingObjectPosition getHitResult(final BlockPos pos) {
        IBlockState blockState = mc.theWorld.getBlockState(pos);
        Block block = blockState.getBlock();

        double centerX = block.getBlockBoundsMinX() + (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX())/2;
        double centerY = block.getBlockBoundsMinY() + (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY())/2;
        double centerZ = block.getBlockBoundsMinZ() + (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ())/2;
        return mc.theWorld.rayTraceBlocksIgnored(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(pos.getX() + centerX, pos.getY() + centerY, pos.getZ() + centerZ), pos);
    }

    private boolean isBad(ItemStack item) {
        if (!(boolean) settings.get(TRASH).getValue())
            return false;
        if (item == null)
            return true;
        if (item.getItem() instanceof ItemSword) {
            return getDamage(item) <= bestDamage();
        }
        if (item.getItem() instanceof ItemArmor) {
            if (!(canEquip(item) || (betterCheck(item) && !canEquip(item)))) return true;
        }
        if (item.getItem() instanceof ItemTool) {
            return !tools.getValue() || isToolWorst(item);
        }
        return !(item.getItem() instanceof ItemArmor) &&
                ((item.getItem().getUnlocalizedName().contains("tnt")) ||
                        (item.getItem().getUnlocalizedName().contains("stick")) ||
                        (item.getItem().getUnlocalizedName().contains("egg")) ||
                        (item.getItem().getUnlocalizedName().contains("string")) ||
                        (item.getItem().getUnlocalizedName().contains("flint")) ||
                        (item.getItem().getUnlocalizedName().contains("compass")) ||
                        (item.getItem().getUnlocalizedName().contains("feather")) ||
                        (item.getItem().getUnlocalizedName().contains("bucket")) ||
                        (item.getItem().getUnlocalizedName().contains("chest") && !item.getDisplayName().toLowerCase().contains("collect")) ||
                        (item.getItem().getUnlocalizedName().contains("snow")) ||
                        (item.getItem().getUnlocalizedName().contains("enchant")) ||
                        (item.getItem().getUnlocalizedName().contains("exp")) ||
                        (item.getItem().getUnlocalizedName().contains("shears")) ||
                        (item.getItem().getUnlocalizedName().contains("anvil")) ||
                        (item.getItem().getUnlocalizedName().contains("torch")) ||
                        (item.getItem().getUnlocalizedName().contains("seeds")) ||
                        (item.getItem().getUnlocalizedName().contains("leather")) ||
                        ((item.getItem() instanceof ItemGlassBottle)) ||
                        (item.getItem().getUnlocalizedName().contains("piston")) ||
                        ((item.getItem().getUnlocalizedName().contains("potion")) && (isBadPotion(item))));
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
                    if (itemTool instanceof ItemPickaxe && item.getItem() instanceof ItemPickaxe) {
                        pickaxeList.add((ItemPickaxe) itemTool);
                    } else if (itemTool instanceof ItemAxe && item.getItem() instanceof ItemAxe) {
                        axeList.add((ItemAxe) itemTool);
                    } else if (itemTool instanceof ItemSpade && item.getItem() instanceof ItemSpade) {
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

    private double getProtectionValue(ItemStack stack) {
        if ((stack.getItem() instanceof ItemArmor)) {//floor ( [ 6 + level ^ 2 ] * .75 / 3 )​
            return ((ItemArmor) stack.getItem()).damageReduceAmount + (100 - ((ItemArmor) stack.getItem()).damageReduceAmount) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.0075D;
        }
        return 0;
    }

    private float getEfficiency(ItemStack itemTool) {
        float var2 = ((ItemTool) itemTool.getItem()).efficiencyOnProperMaterial;

        if (var2 > 1.0f) {
            final int var6 = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemTool);
            var2 += var6 * var6 + 1;
        }
        return var2;
    }

    private boolean betterCheck(ItemStack stack) {
        try {
            if (stack.getItem() instanceof ItemArmor) {
                if (mc.thePlayer.getEquipmentInSlot(1) != null && stack.getUnlocalizedName().contains("boots")) {
                    assert mc.thePlayer.getEquipmentInSlot(1).getItem() instanceof ItemArmor;
                    if (getProtectionValue(stack) + ((ItemArmor) stack.getItem()).damageReduceAmount > getProtectionValue(mc.thePlayer.getEquipmentInSlot(1)) + ((ItemArmor) mc.thePlayer.getEquipmentInSlot(1).getItem()).damageReduceAmount) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(2) != null && stack.getUnlocalizedName().contains("leggings")) {
                    assert mc.thePlayer.getEquipmentInSlot(2).getItem() instanceof ItemArmor;
                    if (getProtectionValue(stack) + ((ItemArmor) stack.getItem()).damageReduceAmount > getProtectionValue(mc.thePlayer.getEquipmentInSlot(2)) + ((ItemArmor) mc.thePlayer.getEquipmentInSlot(2).getItem()).damageReduceAmount) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(3) != null && stack.getUnlocalizedName().contains("chestplate")) {
                    assert mc.thePlayer.getEquipmentInSlot(3).getItem() instanceof ItemArmor;
                    if (getProtectionValue(stack) + ((ItemArmor) stack.getItem()).damageReduceAmount > getProtectionValue(mc.thePlayer.getEquipmentInSlot(3)) + ((ItemArmor) mc.thePlayer.getEquipmentInSlot(3).getItem()).damageReduceAmount) {
                        return true;
                    }
                }
                if (mc.thePlayer.getEquipmentInSlot(4) != null && stack.getUnlocalizedName().contains("helmet")) {
                    assert mc.thePlayer.getEquipmentInSlot(4).getItem() instanceof ItemArmor;
                    if (getProtectionValue(stack) + ((ItemArmor) stack.getItem()).damageReduceAmount > getProtectionValue(mc.thePlayer.getEquipmentInSlot(4)) + ((ItemArmor) mc.thePlayer.getEquipmentInSlot(4).getItem()).damageReduceAmount) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private float bestDamage() {
        float bestDamage = -1;
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
        assert stack.getItem() instanceof ItemArmor;
        return ((mc.thePlayer.getEquipmentInSlot(1) == null) && (stack.getUnlocalizedName().contains("boots")))
                || ((mc.thePlayer.getEquipmentInSlot(2) == null) && (stack.getUnlocalizedName().contains("leggings")))
                || ((mc.thePlayer.getEquipmentInSlot(3) == null) && (stack.getUnlocalizedName().contains("chestplate")))
                || ((mc.thePlayer.getEquipmentInSlot(4) == null) && (stack.getUnlocalizedName().contains("helmet")));
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
