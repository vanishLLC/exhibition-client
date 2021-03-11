package exhibition.gui.screen.impl.mainmenu;

import exhibition.Client;
import exhibition.gui.screen.PanoramaScreen;
import exhibition.management.SubFolder;
import exhibition.util.security.AuthenticatedUser;
import exhibition.util.security.Crypto;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;

public class ClientMainMenu extends PanoramaScreen {
	private static int key = Keyboard.KEY_GRAVE;
	private static final GuiVanillaMainMenu menuVanilla = new GuiVanillaMainMenu();

	public ClientMainMenu() {
	}

	public void initGui() {
		if (getClass().equals(ClientMainMenu.class)) {
			display();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == key) {
			toggleVanilla();
			display();
		}
	}

	private void display() {
		if (Client.isHidden()) {
			Minecraft.getMinecraft().displayGuiScreen(menuVanilla);
		} else {//menuModded
			Minecraft.getMinecraft().displayGuiScreen(new GuiModdedMainMenu());
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void toggleVanilla() {
		Client.setHidden(!Client.isHidden());
	}


}
