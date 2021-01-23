// 
// Decompiled by Procyon v0.5.30
// 

package exhibition.gui.altmanager;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import exhibition.Client;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.SSLConnector;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import java.io.IOException;
import java.net.Proxy;

public final class AltLoginThread extends Thread {
    private Alt alt;
    private String status;
    private Minecraft mc;

    public AltLoginThread(Alt alt) {
        super("Alt Login Thread");
        this.mc = Minecraft.getMinecraft();
        this.alt = alt;
        this.status = EnumChatFormatting.GRAY + "Waiting...";
    }

    private Session createSession(final String username, final String password) {
        final YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        final YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();

//            AutoSkin autoSkin = (AutoSkin) Client.getModuleManager().get(AutoSkin.class);
//            if (autoSkin.isEnabled()) {
//                String skinUrl = autoSkin.stringURL.getValue();
//                boolean isSlim = autoSkin.slimSkin.getValue();
//
//                try {
//                    System.out.println(Client.mojang.getUUIDOfUsername(auth.getSelectedProfile().getName()));
//                    System.out.println(auth.getAuthenticatedToken());
////                    if (Client.mojang.getStatus(Mojang.ServiceType.AUTHSERVER_MOJANG_COM) == Mojang.ServiceStatus.GREEN) {
////                        Client.mojang.updateSkin(Client.mojang.getUUIDOfUsername(auth.getSelectedProfile().getName()), auth.getAuthenticatedToken(), Mojang.SkinType.SLIM, skinUrl);
////                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Notifications.getManager().post("Error", "Could not change skin.", 750, Notifications.Type.NOTIFY);
//                }
//            }

            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
        } catch (AuthenticationException localAuthenticationException) {
            localAuthenticationException.printStackTrace();
            return null;
        }
    }

    public String getStatus() {
        return this.status;
    }

    @Override
    public void run() {
        if (alt.getUsername().contains("@alt.com")) {
            if (Client.altService.isVanilla()) {
                this.status = EnumChatFormatting.BLUE + "Switching to correct service.";
                Client.altService.switchService();
            }
        } else if (!Client.altService.isVanilla()) {
            this.status = EnumChatFormatting.BLUE + "Switching to correct service.";
            Client.altService.switchService();
        }
        if (alt.getPassword().equals("")) {
            this.mc.session = new Session(alt.getUsername(), "", "", "mojang");
            this.status = EnumChatFormatting.GREEN + "Logged in. (" + alt.getUsername() + " - offline name)";
            return;
        }
        this.status = EnumChatFormatting.AQUA + "Logging in...";
        final Session auth = this.createSession(alt.getUsername(), alt.getPassword());
        if (auth == null) {
            this.status = EnumChatFormatting.RED + "Login failed!";
            if (!alt.getStatus().equals(Alt.Status.NotWorking)) {
                alt.setStatus(Alt.Status.NotWorking);
            }
            String name = alt.getMask().equals("") ? alt.getUsername() : alt.getMask();
            Notifications.getManager().post("Logging Failed!", "Failed logging into " + name, 2000, Notifications.Type.WARNING);
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            AltManager.lastAlt = alt;
            this.status = EnumChatFormatting.GREEN + "Logged in. (" + auth.getUsername() + ")";
            alt.setMask(auth.getUsername());
            this.mc.session = auth;
            if (alt.getStatus().equals(Alt.Status.Unchecked) || alt.getStatus().equals(Alt.Status.NotWorking)) {
                alt.setStatus(Alt.Status.Working);
            }
            Notifications.getManager().post("Logged in!", "Logged in as " + alt.getMask(), 2000, Notifications.Type.OKAY);
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SSLConnector.get(new Connection("https://minesense.pub/nig/C"));
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
