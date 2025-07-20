package exhibition.util.security;

import exhibition.Client;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static exhibition.util.security.AuthenticationUtil.getHwid;

public class AuthenticationThread extends Thread implements Runnable {

    public AuthenticationThread(GuiLoginMenu loginInstance) {
        this.loginInstance = loginInstance;
    }

    public final GuiLoginMenu loginInstance;

    public boolean hasFailed;
    public boolean isRunning;

    public boolean isRunning() {
        return isRunning;
    }

    public void stopThread() {
        isRunning = false;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            if (!Objects.equals(loginInstance.username.getText(), "") && !Objects.equals(loginInstance.password.getText(), ""))
                try {
                    loginInstance.status = GuiLoginMenu.Status.AUTHENTICATING;
                    try {
                        Object nigga = AuthenticatedUser.create(new Object[] {loginInstance.username.getText()});
                        Client.setAuthUser(nigga);
                        Client.getAuthUser().setupClient(Client.instance);
                        loginInstance.status = GuiLoginMenu.Status.SUCCESS;
                        loginInstance.setProgress(1.0);
                        LoginUtil.saveLogin(loginInstance.username.getText(), "password");
                    } catch (Exception e) {
                        loginInstance.setError();
                        loginInstance.setProgress(0.455);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    loginInstance.setProgress(0.47);
                    loginInstance.setError();
                    hasFailed = true;
                    e.printStackTrace();
                }
        } catch (Exception e) {
            e.printStackTrace();
            loginInstance.setProgress(0.48);
            loginInstance.setError();
            hasFailed = true;
        }
        isRunning = false;
    }


}

