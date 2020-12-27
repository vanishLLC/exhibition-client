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
		load();
		if (getClass().equals(ClientMainMenu.class)) {
			display();
		}
	}

	private void load() {
		String file = "";
		try {
			file = FileUtils.readFileToString(getFile());
		} catch (IOException e) {
			return;
		}
		for (String line : file.split("\n")) {
			if (line.contains("key")) {
				String[] split = line.split(":");
				if (split.length > 1) {
					try {
						key = Integer.parseInt(split[1]);
					} catch (NumberFormatException ignored) {}
				}
			}
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
		save();
	}

	public void save() {
		try {
			FileUtils.write(getFile(), "Swap key (Toggles menus):" + key);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		File file = new File(getFolder().getAbsolutePath() + File.separator + "MainMenu.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public File getFolder() {
		File folder = new File(Client.getDataDir().getAbsolutePath() + File.separator + SubFolder.Other.getFolderName());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
}
