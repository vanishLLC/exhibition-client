/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.generators.gui;

import exhibition.Client;
import exhibition.gui.altmanager.Alt;
import exhibition.gui.altmanager.AltManager;
import exhibition.gui.altmanager.Alts;
import exhibition.gui.generators.handlers.altening.AlteningGenHandler;
import exhibition.gui.generators.handlers.altening.workers.GenAltsThread;
import exhibition.gui.screen.PanoramaScreen;
import exhibition.management.notifications.usernotification.Notifications;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AlteningGeneratorGUI extends PanoramaScreen {

    private GuiTextField apiKey;
    private GuiTextField altField;

    private GuiScreen previousScreen;

    private AlteningGenHandler.AlteningGenUser genUser;

    private AlteningGenHandler handler;

    private AlteningGenHandler.AlteningGenAlt currentAlt;

    private GenAltsThread genAltsThread;

    private String generatingThreadStatus;

    public AlteningGeneratorGUI(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
        this.handler = Client.alteningGenHandler;
        this.genUser = Client.alteningGenHandler.getUser();
        new Thread(() -> {
            if (genUser == null && handler.getAPIKey() != null) {
                genUser = handler.checkUserLicense();
            }
        });
        this.generatingThreadStatus = "\2477Idle...";
    }

    public void setThreadStatus(String string) {
        this.generatingThreadStatus = string;
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        switch (button.id) {
            case 0: {
                String key = apiKey.getText();
                if (key.equals("") || !key.startsWith("api-") || key.length() != 18) {
                    apiKey.setText("Invalid Key");
                    return;
                }
                if (handler.setAPIKey(key)) {
                    if (handler.getUser().isLimited()) {
                        altField.setText("You have reached your daily limit!");
                        return;
                    }
                    if (handler.getUser().canGenAlts()) {
                        this.currentAlt = handler.getAltLogin();
                        if (currentAlt == null) {
                            this.altField.setText("Could not generate alt!");
                        } else
                            this.altField.setText("Username: " + currentAlt.getUsername());
                        if (currentAlt != null) {
                            AltManager.registry.add(new Alt(currentAlt.getEmail(), "password", currentAlt.getUsername(), Alt.Status.Unchecked, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)));
                            Notifications.getManager().post("Added Alt", "Added " + currentAlt.getUsername() + " to alt manager!");
                            try {
                                Client.getFileManager().getFile(Alts.class).saveFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        this.altField.setText("You do not have a subscription!");
                    }
                } else {
                    this.altField.setText("Error: Invalid license");
                }
                break;
            }
            case 1: {
                String key = apiKey.getText();
                if (key.equals("") || !key.startsWith("api-") || key.length() != 18) {
                    apiKey.setText("Invalid Key");
                    return;
                }
                if (handler.setAPIKey(key)) {
                    if (handler.getUser().isLimited()) {
                        altField.setText("You have reached your daily limit!");
                        return;
                    }
                    if (handler.getUser().canGenAlts()) {
                        if (this.genAltsThread == null) {
                            this.genAltsThread = new GenAltsThread(handler, this);
                        }
                        if (!this.genAltsThread.isRunning())
                            this.genAltsThread.start();
                    } else {
                        this.altField.setText("You do not have a subscription!");
                    }
                } else {
                    this.altField.setText("Error: Invalid license");
                }
                break;
            }
            case 2: {
                if (this.genAltsThread != null && this.genAltsThread.isRunning())
                    this.genAltsThread.stopThread();
                break;
            }
        }
    }

    @Override
    public void drawScreen(final int i, final int j, final float f) {
        super.renderSkybox(i, j, f);
        this.apiKey.drawTextBox();
        this.altField.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, "AlteningGen API", this.width / 2, 20, -1);
        this.drawCenteredString(this.fontRendererObj, generatingThreadStatus, this.width / 2, 30, -1);
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
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 80 + 22, 100, 20, "Generate Max Alts"));
        this.buttonList.add(new GuiButton(2, this.width / 2, this.height / 4 + 80 + 22, 100, 20, "Stop Generating"));


        this.altField = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.apiKey = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20) {

            @Override
            public void drawTextBox() {
                String oldString = this.text;
                boolean isLength = oldString.length() >= 14;
                if(isLength)
                    this.text = oldString.substring(0, 3) + oldString.substring(3,14).replaceAll("[a-zA-Z0-9]", "*") + (oldString.length() > 14 ? oldString.substring(14) : "");
                super.drawTextBox();
                this.text = oldString;
            }
        };
        apiKey.setMaxStringLength(18);
        altField.setMaxStringLength(128);
        if (!(handler.getAPIKey() == null)) {
            this.apiKey.setText(handler.getAPIKey());
        }
    }

    @Override
    protected void keyTyped(final char par1, final int par2) {
        if (par2 == Keyboard.KEY_ESCAPE)
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
        if (this.genAltsThread != null && this.genAltsThread.isRunning())
            this.genAltsThread.stopThread();
    }


}
