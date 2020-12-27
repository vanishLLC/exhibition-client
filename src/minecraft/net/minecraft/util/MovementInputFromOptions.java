package net.minecraft.util;

import exhibition.Client;
import exhibition.gui.click.components.CategoryButton;
import exhibition.gui.click.components.ConfigTextBox;
import exhibition.gui.click.components.TextBox;
import exhibition.module.impl.player.AutoFish;
import exhibition.module.impl.player.InventoryWalk;
import exhibition.module.impl.player.Scaffold;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        InventoryWalk x = (InventoryWalk) Client.getModuleManager().get(InventoryWalk.class);
        boolean bad = false;
        for (CategoryButton button : Client.getClickGui().mainPanel.typeButton) {
            for (TextBox textBox : button.categoryPanel.textBoxes) {
                if (textBox.isTyping || textBox.isFocused)
                    bad = true;
            }
            if (button.categoryPanel.configTextBox != null) {
                ConfigTextBox textBox = button.categoryPanel.configTextBox;
                if (textBox.isTyping || textBox.isFocused)
                    bad = true;
            }
        }
        if (x.isEnabled() && !bad && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            if (Keyboard.isKeyDown(this.gameSettings.keyBindForward.getKeyCode())) {
                this.moveForward += 1.0F;
            }
            if (Keyboard.isKeyDown(this.gameSettings.keyBindBack.getKeyCode())) {
                this.moveForward -= 1.0F;
            }
            if (Keyboard.isKeyDown(this.gameSettings.keyBindLeft.getKeyCode())) {
                this.moveStrafe += 1.0F;
            }
            if (Keyboard.isKeyDown(this.gameSettings.keyBindRight.getKeyCode())) {
                this.moveStrafe -= 1.0F;
            }
            this.jump = Keyboard.isKeyDown(this.gameSettings.keyBindJump.getKeyCode());
            this.sneak = !Client.getModuleManager().isEnabled(Scaffold.class) && this.gameSettings.keyBindSneak.isKeyDown();
            if (this.sneak) {
                this.moveStrafe = ((float) (this.moveStrafe * 0.3D));
                this.moveForward = ((float) (this.moveForward * 0.3D));
            }
        } else {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;
            if (this.gameSettings.keyBindForward.isKeyDown()) {
                this.moveForward += 1.0F;
            }
            if (this.gameSettings.keyBindBack.isKeyDown()) {
                this.moveForward -= 1.0F;
            }
            if (this.gameSettings.keyBindLeft.isKeyDown()) {
                this.moveStrafe += 1.0F;
            }
            if (this.gameSettings.keyBindRight.isKeyDown()) {
                this.moveStrafe -= 1.0F;
            }
            this.jump = this.gameSettings.keyBindJump.isKeyDown();
            this.sneak = !Client.getModuleManager().isEnabled(Scaffold.class) && this.gameSettings.keyBindSneak.isKeyDown();
            if (this.sneak) {
                this.moveStrafe = ((float) (this.moveStrafe * 0.3D));
                this.moveForward = ((float) (this.moveForward * 0.3D));
            }
        }
        if(Client.getModuleManager().isEnabled(AutoFish.class) && Minecraft.getMinecraft().thePlayer != null) {
            if(Minecraft.getMinecraft().thePlayer.fishEntity != null) {
                int steps = Minecraft.getMinecraft().thePlayer.ticksExisted % 30;
                if(steps <= 14) {
                    Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe = 1;
                } else {
                    Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe = -1;
                }
            }
        }
    }
}
