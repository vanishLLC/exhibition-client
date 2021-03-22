package exhibition.gui.altmanager;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import exhibition.Client;
import exhibition.gui.screen.PanoramaScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.net.Proxy;

public class GuiAddAlt extends PanoramaScreen {
    private final GuiAltManager manager;
    private PasswordField password;
    private String status;
    private GuiTextField username;

    GuiAddAlt(final GuiAltManager manager) {
        this.status = EnumChatFormatting.GRAY + "Idle...";
        this.manager = manager;
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        switch (button.id) {
            case 0: {
                if (this.username.getText().contains("@alt.com")) {
                    this.password.setText("password");
                }
                final AddAltThread login = new AddAltThread(this.username.getText(), this.password.getText());
                login.start();
                break;
            }
            case 1: {
                this.mc.displayGuiScreen(this.manager);
                break;
            }
            case 2: {
                String data;
                try {
                    data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                } catch (Exception ignored) {
                    break;
                }
                if (data.contains(":")) {
                    String[] credentials = data.split(":");
                    username.setText(credentials[0]);
                    password.setText(credentials[1]);
                }
            }
        }
    }

    @Override
    public void drawScreen(final int i, final int j, final float f) {
        renderSkybox(i, j, f);
        this.username.drawTextBox();
        this.password.drawTextBox();
        this.drawCenteredString(this.fontRendererObj, "Add Alt", this.width / 2, 20, -1);
        if (this.username.getText().isEmpty() && !this.username.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Username / E-Mail", this.width / 2 - 96, 66, -7829368);
        }
        if (this.password.getText().isEmpty() && !this.password.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Password", this.width / 2 - 96, 106, -7829368);
        }
        this.drawCenteredString(this.fontRendererObj, this.status, this.width / 2, 30, -1);
        super.drawScreen(i, j, f);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 92 + 12, "Login"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 116 + 12, "Back"));
        buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 92 - 12, "Import user:pass"));
        this.username = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.username.setMaxStringLength(128);
        this.password = new PasswordField(this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
        this.password.setMaxStringLength(128);
        super.initGui();
    }

    @Override
    protected void keyTyped(final char par1, final int par2) {
        this.username.textboxKeyTyped(par1, par2);
        this.password.textboxKeyTyped(par1, par2);
        if (par1 == '\t' && (this.username.isFocused() || this.password.isFocused())) {
            this.username.setFocused(!this.username.isFocused());
            this.password.setFocused(!this.password.isFocused());
        }
        if (par1 == '\r') {
            this.actionPerformed((GuiButton) this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(final int par1, final int par2, final int par3) {
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(par1, par2, par3);
        this.password.mouseClicked(par1, par2, par3);
    }

    private static void setStatus(final GuiAddAlt guiAddAlt, final String status) {
        guiAddAlt.status = status;
    }

    private class AddAltThread extends Thread {
        private final String password;
        private final String username;

        AddAltThread(final String username, final String password) {
            this.username = username;
            this.password = password;
            GuiAddAlt.setStatus(GuiAddAlt.this, EnumChatFormatting.GRAY + "Idle...");
        }

        private void checkAndAddAlt(final String username, final String password) {
            final YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
            final YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
            auth.setUsername(username);
            auth.setPassword(password);
            try {
                auth.logIn();
                Alt alt = new Alt(username, password, auth.getSelectedProfile().getName(), Alt.Status.Working);
                AltManager.registry.add(alt);
                Minecraft.getMinecraft().session = new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
                AltManager.lastAlt = alt;
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (Exception ignored) {
                }
                GuiAddAlt.setStatus(GuiAddAlt.this, "Alt added. (" + username + ")");
            } catch (AuthenticationException e) {
                GuiAddAlt.setStatus(GuiAddAlt.this, EnumChatFormatting.RED + "Alt failed!");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (this.password.equals("")) {
                AltManager.registry.add(new Alt(this.username, ""));
                GuiAddAlt.setStatus(GuiAddAlt.this, EnumChatFormatting.GREEN + "Alt added. (" + this.username + " - offline name)");
                return;
            }
            GuiAddAlt.setStatus(GuiAddAlt.this, EnumChatFormatting.AQUA + "Trying alt...");
            this.checkAndAddAlt(this.username, this.password);
        }
    }
}
