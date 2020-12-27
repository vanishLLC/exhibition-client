package exhibition.gui.click.virtue;

import exhibition.Client;
import exhibition.gui.click.virtue.component.Console;
import exhibition.gui.click.virtue.component.window.ModuleWindow;
import exhibition.gui.click.virtue.component.window.Window;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.module.data.ModuleData;
import exhibition.util.FileUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VirtueClickGui extends GuiScreen {

    private final File GUI_DIR;
    private static final float SCALE = 2.0f;
    private CopyOnWriteArrayList<ModuleWindow> windows;
    private static VirtueClickGui instance;
    private static Console console;
    private boolean binding;

    public static void start() {
        VirtueClickGui.instance = new VirtueClickGui();
    }

    private VirtueClickGui() {
        GUI_DIR = FileUtils.getConfigFile("VirtueGui");
        this.windows = new CopyOnWriteArrayList<>();
        double x = 20.0;
        final double y = 50.0;
        double width = 0.0;

        ModuleData.Type[] values = ModuleData.Type.values();
        for (int length = values.length, i = 0; i < length; ++i) {
            final  ModuleData.Type category = values[i];
            width = Math.max((double) Client.virtueFont.getStringWidth(category.name()) * 1.1, width);
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType().equals(category)) {
                    width = Math.max(width, Client.virtueFont.getStringWidth(module.getName()));
                }
            }
            final ModuleWindow window = new ModuleWindow(category, x, y, width);
            this.windows.add(window);
            x += 100.0;
        }
        this.load();
        this.save();
        VirtueClickGui.console = new Console();
    }

    public void load() {
        List fileContent = FileUtils.read(GUI_DIR);
        Iterator var3 = fileContent.iterator();

        while(var3.hasNext()) {
            String line = (String)var3.next();
            String[] split = line.split(":");
            String windowName = split[0];
            String windowPosX = split[1];
            String windowPosY = split[2];
            String windowExtended = split[3];
            Window window = this.getWindow(windowName);
            int posX = Integer.parseInt(windowPosX);
            int posY = Integer.parseInt(windowPosY);
            Boolean extended = Boolean.parseBoolean(windowExtended);
            window.setStartOffset(new double[]{0.0D, 0.0D});
            window.setDragging(true);
            if (window.getHandle() != null) {
                window.getHandle().drag(posX, posY, 0);
            }

            window.setDragging(false);
            window.setExtended(extended);
        }

    }

    public void save() {
        List fileContent = new ArrayList();
        Iterator var3 = this.windows.iterator();

        while(var3.hasNext()) {
            ModuleWindow window = (ModuleWindow)var3.next();
            String windowName = window.getParent().name();
            String windowPosX = "" + (int)window.getX();
            String windowPosY = "" + (int)window.getY();
            String windowExtended = Boolean.toString(window.isExtended());
            fileContent.add(String.format("%s:%s:%s:%s", windowName, windowPosX, windowPosY, windowExtended));
        }

        FileUtils.write(GUI_DIR, fileContent, true);
    }


    private ModuleWindow getWindow(final String name) {
        for (final ModuleWindow window : this.windows) {
            if (((ModuleData.Type)window.getParent()).name().equalsIgnoreCase(name)) {
                return window;
            }
        }
        return null;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.pushMatrix();
        for (final Window window : this.windows) {
            window.draw(mouseX, mouseY);
        }
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        VirtueClickGui.console.draw(mouseX, mouseY);
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        for (final Window window : this.getWindows()) {
            window.click(mouseX, mouseY, mouseButton);
        }
        VirtueClickGui.console.click(mouseX, mouseY, mouseButton);
        this.save();
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        for (final Window window : this.windows) {
            if (window.getHandle() != null) {
                window.getHandle().drag(mouseX, mouseY, clickedMouseButton);
            }
        }
        this.save();
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        for (final Window window : this.windows) {
            window.release(mouseX, mouseY, state);
        }
        this.save();
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if (!this.binding && !VirtueClickGui.console.keyType(keyCode, typedChar)) {
            super.keyTyped(typedChar, keyCode);
        }
        for (final Window window : this.windows) {
            window.keyPress(keyCode, typedChar);
        }
    }

    @Override
    public void onGuiClosed() {
        ModuleManager.saveStatus();
        ModuleManager.saveSettings();
        super.onGuiClosed();
    }

    public Window getTopWindow(final int mouseX, final int mouseY) {
        final List<ModuleWindow> hoveringWindows = new ArrayList<ModuleWindow>();
        for (final ModuleWindow window : this.windows) {
            if (window.hovering(mouseX, mouseY) || window.getHandle().hovering(mouseX, mouseY)) {
                hoveringWindows.add(window);
            }
        }
        if (!hoveringWindows.isEmpty()) {
            final ModuleWindow lastWindow = (ModuleWindow)hoveringWindows.get(hoveringWindows.size() - 1);
            this.windows.remove(lastWindow);
            this.windows.add(lastWindow);
            return lastWindow;
        }
        return null;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static VirtueClickGui getInstance() {
        return VirtueClickGui.instance;
    }

    public CopyOnWriteArrayList<ModuleWindow> getWindows() {
        return this.windows;
    }

    public boolean isBinding() {
        return this.binding;
    }

    public void setBinding(final boolean binding) {
        this.binding = binding;
    }

}
