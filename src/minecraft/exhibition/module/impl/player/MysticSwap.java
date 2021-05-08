package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.util.HypixelUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MysticSwap extends Module {

    private Setting<Boolean> swapMirrors = new Setting<>("MIRRORS", true);
    private Setting<Boolean> swapDiamond = new Setting<>("DIA LEGS", true);
    private Setting<Boolean> swapPebble = new Setting<>("PEBBLE", true);
    private Setting<Boolean> swapDarks = new Setting<>("DARKS", true);

    public MysticSwap(ModuleData data) {
        super(data);
        addSetting(new Setting<>("SWAP", new MultiBool("Swap", swapMirrors, swapDiamond, swapPebble, swapDarks)));
    }

    private final List<EntityItem> possibleGold = new CopyOnWriteArrayList<>();
    private final List<EntityItem> trackedGold = new CopyOnWriteArrayList<>();

    @Override
    public Priority getPriority() {
        return Priority.FIRST;
    }

    @RegisterEvent(events = {EventTick.class, EventPacket.class})
    public void onEvent(Event event) {

        boolean ignoreSwap = false;

        boolean isInPit = HypixelUtil.isInGame("THE HYPIXEL PIT");
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        if (!isInPit || y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
            ignoreSwap = true;
        }

        if (event instanceof EventTick) {
            int slotToSwap = -1;

            // PRIORITY: Diamond Armor (Venom'd/Misery) -> Mirrors (Perun 3/Gamble 3) -> Darks (Reg/Etc)
            // TODO: Allow the user to select what enchants triggers Mirrors/Darks
            if (swapMirrors.getValue() || swapDarks.getValue() || swapDiamond.getValue()) {

                // Check if the player has venom hearts
                if (swapDiamond.getValue() && mc.thePlayer.isPotionActive(Potion.poison)) {
                    // You can verify if you want to check for venoms near you or not
//                    if(notWearingDiamond) {
//                        slotToSwap = findDiamond;
//                    }
                }

                if (slotToSwap == -1)
                    for (EntityPlayer player : mc.theWorld.playerEntities) {
                        // The entity can be clicked on/valid and can be seen within range
                        double currentDistance = mc.thePlayer.getDistance(player.posX, mc.thePlayer.posY, player.posZ);
                        if (player.canBeCollidedWith() && currentDistance <= 10) {
                            if (AntiBot.isBot(player) || FriendManager.isFriend(player.getName()))
                                continue;
                            double previousDistance = mc.thePlayer.getDistance(player.lastTickPosX, mc.thePlayer.posY, player.lastTickPosZ);
                            // If not standing still AND is moving towards the player
                            if (currentDistance != previousDistance && previousDistance > currentDistance) {
                                // Check held item for Perun/Gamble 3

                                if (swapMirrors.getValue()) {
                                    ItemStack heldStack = player.getHeldItem();

                                    if (heldStack != null) {
                                        if (stackHasEnchant(heldStack, "Gamble III", "Perun") && !playerHasEnchant("Mirror")) {
                                            slotToSwap = findWithEnchant("Mirror");
                                        }
                                    }
                                }

                                // If the user doesn't need Mirror, check for Regularity/Misery/Venoms
                                if (slotToSwap != -1)
                                    break;

                                if (!swapDarks.getValue() && !swapDiamond.getValue())
                                    continue;

                                // Check the players pants
                                ItemStack leggings = player.getEquipmentInSlot(2);

                                if (leggings == null || !(leggings.getItem() instanceof ItemArmor))
                                    continue;

                                ItemArmor pants = (ItemArmor) leggings.getItem();

                                if (pants.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
                                    if (swapDarks.getValue() && stackHasEnchant(leggings, "Regularity")) {
                                        if (!playerHasEnchant("Somber")) {
                                            slotToSwap = findWithEnchant("Somber");
                                            if (slotToSwap != -1)
                                                break;
                                        }
                                    }

                                    if (swapDiamond.getValue() && stackHasEnchant(leggings, "Venom", "Misery")) {
                                        if (!playerHasEnchant("Somber")) {
                                            slotToSwap = findWithEnchant("Somber");
                                            if (slotToSwap != -1)
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
            }

            if (swapPebble.getValue()) {
                if (slotToSwap == -1) {
                    if (mc.thePlayer.ticksExisted < 50) {
                        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                            if (entity instanceof EntityItem) {
                                EntityItem entityItem = (EntityItem) entity;
                                if (entityItem.getEntityItem() != null && entityItem.getEntityItem().getItem() == Items.gold_ingot) {
                                    if (!trackedGold.contains(entityItem)) {
                                        possibleGold.remove(entityItem);
                                        trackedGold.add(entityItem);
                                    }
                                }
                            }
                        }
                    }

                    // Remove if the entity no longer exists
                    trackedGold.removeIf(trackedItem -> !mc.theWorld.getLoadedEntityList().contains(trackedItem));

                    // Check any newly updated entities and verify if they're gold or not
                    for (EntityItem updatedItem : new ArrayList<>(possibleGold)) {
                        if (mc.theWorld.getLoadedEntityList().contains(updatedItem)) {
                            ItemStack itemStack = updatedItem.getEntityItem();
                            if (itemStack != null && itemStack.getItem() == Items.gold_ingot) {
                                trackedGold.add(updatedItem);
                            }
                        }
                        possibleGold.remove(updatedItem);
                    }

                    // For every confirmed gold entity
                    for (EntityItem goldItem : trackedGold) {
                        if (mc.thePlayer.getDistanceToEntity(goldItem) < 7 && !playerHasEnchant("Pebble")) {
                            slotToSwap = findWithEnchant("Pebble");
                            // If Enchant is found
                            if (slotToSwap != -1) {
                                break;
                            }
                        }
                    }
                }
            } else {
                trackedGold.clear();
                possibleGold.clear();
            }

            if (slotToSwap != -1 && !ignoreSwap) {
                // TODO: Check if the item is in the hotbar or inventory, click accordingly
            }
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            if (mc.thePlayer == null || mc.theWorld == null || !swapPebble.getValue()) {
                return;
            }

            Packet packet = ep.getPacket();
            if (packet instanceof S1CPacketEntityMetadata) {
                S1CPacketEntityMetadata entityMetadata = (S1CPacketEntityMetadata) packet;
                Entity entity = mc.theWorld.getEntityByID(entityMetadata.getEntityId());
                if (entity instanceof EntityItem) {
                    if (!possibleGold.contains(entity))
                        possibleGold.add((EntityItem) entity);
                }
            }
        }
    }

    public int findWithEnchant(String enchant) {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stackHasEnchant(stack, enchant)) {
                return i;
            }
        }
        return -1;
    }

    public boolean stackHasEnchant(ItemStack stack, String... enchants) {
        if (stack != null) {
            for (String pitEnchant : HypixelUtil.getPitEnchants(stack)) {
                for (String enchant : enchants) {
                    if (pitEnchant.toLowerCase().contains(enchant.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean playerHasEnchant(String enchant) {
        ItemStack pantsStack = mc.thePlayer.getEquipmentInSlot(2);
        if (pantsStack == null || !(pantsStack.getItem() instanceof ItemArmor || pantsStack.getItem() instanceof ItemSword)) {
            return false;
        }
        return stackHasEnchant(pantsStack, enchant);
    }


}
