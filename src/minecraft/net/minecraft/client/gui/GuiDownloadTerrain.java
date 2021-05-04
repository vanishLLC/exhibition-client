package net.minecraft.client.gui;

import java.io.IOException;

import com.github.creeper123123321.viafabric.handler.CommonTransformer;
import com.github.creeper123123321.viafabric.handler.clientside.VRDecodeHandler;
import exhibition.Client;
import exhibition.management.GlobalValues;
import exhibition.management.PriorityManager;
import exhibition.management.command.impl.Teleport;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.combat.Bypass;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Phase;
import exhibition.module.impl.movement.Speed;
import exhibition.module.impl.other.AutoPaper;
import exhibition.module.impl.other.HackerDetect;
import exhibition.module.impl.player.AutoFish;
import exhibition.module.impl.player.Scaffold;
import exhibition.module.impl.render.MotionPrediction;
import exhibition.ncp.Angle;
import exhibition.util.security.DiscordUtil;
import io.netty.channel.ChannelHandler;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

public class GuiDownloadTerrain extends GuiScreen {
    private NetHandlerPlayClient netHandlerPlayClient;
    private int progress;
    ////private static final String __OBFID = "CL_00000708";

    public GuiDownloadTerrain(NetHandlerPlayClient p_i45023_1_) {
        this.netHandlerPlayClient = p_i45023_1_;
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
        Module[] modules = new Module[]{Client.getModuleManager().get(Killaura.class), Client.getModuleManager().get(Phase.class), Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(Speed.class), Client.getModuleManager().get(Scaffold.class), Client.getModuleManager().get(LongJump.class)};
        boolean disabled = false;
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.toggle();
                disabled = true;
            }
        }
        if (disabled) {
            Notifications.getManager().post("Server Change", "Disabled movement modules/aura.", 1000, Notifications.Type.NOTIFY);
        }

        if (Client.getModuleManager().isEnabled(AutoFish.class)) {
            Client.getModuleManager().get(AutoFish.class).toggle();
            Notifications.getManager().post("Server Change", "Blub Blub Blub.", 1500, Notifications.Type.NOTIFY);
        }

        if (Client.getModuleManager().isEnabled(HackerDetect.class)) {
            HackerDetect hackerDetect = Client.getModuleManager().get(HackerDetect.class).cast();
            hackerDetect.reset();
        }

        Angle.INSTANCE.clear();

        if (Teleport.isTeleporting)
            Teleport.cancel = true;

        AntiBot.clear();

        Client.getModuleManager().get(Bypass.class).worldChange();
        //Client.getModuleManager().get(AutoPaper.class).worldChange();

        Client.instance.differenceQueue.clear();

        if (Client.instance.hypixelApiKey != null && Client.instance.hypixelApiKey.equals(""))
            Client.instance.hypixelApiKey = null;

        if (!GlobalValues.keepPriority.getValue())
            PriorityManager.clearPriorityList();

        for (Module module : Client.getModuleManager().getArray()) {
            module.worldChange();
        }

        if (mc.getCurrentServerData() != null) {
            DiscordUtil.setDiscordPresence("In Game", "IP: " + mc.getCurrentServerData().serverIP);
        }

        this.buttonList.clear();
    }

    /**
     * Called from the bruh game loop to update the screen.
     */
    public void updateScreen() {
        ++this.progress;

        if (this.progress % 20 == 0) {
            this.netHandlerPlayClient.addToSendQueue(new C00PacketKeepAlive());
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingTerrain", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }
}
