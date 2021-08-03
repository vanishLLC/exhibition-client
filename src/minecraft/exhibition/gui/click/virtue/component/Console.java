package exhibition.gui.click.virtue.component;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.management.command.CommandManager;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class Console extends Component
{
    private GuiTextField textField;
    private boolean autoCompleteNext;

    public Console() {
        super(null, 0.0, 0.0, 0.0, 0.0);
        this.textField = new GuiTextField(-69, Client.virtueFont, 0, 0, 0, 0);
    }

    @Override
    public void draw(final int mouseX, final int mouseY) {
        final ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft());
        this.textField.xPosition = (int)(scaledRes.getScaledWidth_double() / 4.0);
        this.textField.width = (int)(scaledRes.getScaledWidth_double() / 2.0);
        this.textField.yPosition = 2;
        this.textField.height = 18;
        this.textField.drawTextBox();
    }

    @Override
    public void click(final int mouseX, final int mouseY, final int button) {
        this.textField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drag(final int mouseX, final int mouseY, final int button) {
    }

    @Override
    public void release(final int mouseX, final int mouseY, final int button) {
    }

    @Override
    public void keyPress(final int keyInt, final char keyChar) {
    }

    public boolean keyType(final int keyInt, final char keyChar) {
        if (this.textField.isFocused() && 1 == keyInt) {
            this.textField.setText("");
            this.textField.setFocused(false);
            return true;
        }
        if (!Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54) && 53 == keyInt) {
            this.textField.setText("");
            this.textField.setFocused(true);
        }
        else if (28 == keyInt) {
            if(!this.textField.getText().equals("")) {
                String commandBits[] = textField.getText().split(" ");
                String commandName = commandBits[0].toLowerCase();
                //Get the command and fire it with arguments
                Command command = CommandManager.commandMap.get(commandName);
                if (command == null) {
                    ChatUtil.printChat(Command.chatPrefix + "Command \"" + commandName + "\" does not exist!");
                    this.textField.setText("");
                    return false;
                }
                if (commandBits.length > 1) {
                    String[] commandArguments = Arrays.copyOfRange(commandBits, 1, commandBits.length);
                    command.fire(commandArguments);
                } else {
                    command.fire(null);
                }
                this.textField.setText("");
            }
        }
        else if (this.textField.isFocused()) {
            this.autoCompleteNext = false;
            this.textField.textboxKeyTyped(keyChar, keyInt);
        }
        return false;
    }
}

