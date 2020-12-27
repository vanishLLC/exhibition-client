package exhibition.gui.generators.gui;

import exhibition.Client;
import exhibition.gui.altmanager.Alt;
import exhibition.gui.altmanager.AltManager;
import exhibition.gui.altmanager.Alts;
import exhibition.gui.screen.PanoramaScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AltGeneratorGUI extends PanoramaScreen {

    private GuiTextField apiKey;
    private GuiTextField altField;

    private GuiScreen previousScreen;

    public AltGeneratorGUI(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        switch (button.id) {
            case 0: {
                if (apiKey.getText().equals("") || !apiKey.getText().contains("-")) {
                    apiKey.setText("Invalid key!");
                    return;
                }
//                if (Client.altGenHandler.getApiKey() == (null)) {
//                    Client.altGenHandler.setApiKey(apiKey.getText());
//                    apiKey.setEnabled(false);
//                }
//                this.altField.setText(Client.altGenHandler.getAlt());
                break;
            }
            case 1: {
                if(!altField.getText().equals("") && altField.getText().contains(":")) {
                    String[] login = altField.getText().split(":");
                    AltManager.registry.add(new Alt(login[0],login[1]));
                    altField.setText("Added to alt manager!");
                    try {
                        Client.getFileManager().getFile(Alts.class).saveFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void drawScreen(final int i, final int j, final float f) {
        super.renderSkybox(i,j,f);
        this.apiKey.drawTextBox();
        this.altField.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, "Alt-Gen API Manager", this.width / 2, 20, -1);
        if (apiKey.getText().isEmpty() && !apiKey.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "API Key", this.width / 2 - 96, 66, -7829368);
        }
        if (altField.getText().isEmpty() && !altField.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Alt Information", this.width / 2 - 96, 106, -7829368);
        }
        super.drawScreen(i, j, f);

    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 80, "Generate Alt"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 80 + 22, "Add alt to manager"));

        this.altField = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.apiKey = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        apiKey.setMaxStringLength(128);
        altField.setMaxStringLength(128);
//        if(!(Client.altGenHandler.getApiKey() == null)) {
//            this.apiKey.setText(Client.altGenHandler.getApiKey());
//            apiKey.setEnabled(false);
//        }
    }

    @Override
    protected void keyTyped(final char par1, final int par2) {
        if(par2 == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(previousScreen);
        this.apiKey.textboxKeyTyped(par1, par2);
        this.altField.textboxKeyTyped(par1, par2);
        if (par1 == '\t' && (this.apiKey.isFocused() || this.altField.isFocused())) {
            this.apiKey.setFocused(!this.apiKey.isFocused());
            this.altField.setFocused(!this.altField.isFocused());
        }
        if (par1 == '\r') {
            this.actionPerformed((GuiButton) this.buttonList.get(1));
        }
    }

    @Override
    protected void mouseClicked(final int par1, final int par2, final int par3) {
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.apiKey.mouseClicked(par1, par2, par3);
        this.altField.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed() {
    }
}
