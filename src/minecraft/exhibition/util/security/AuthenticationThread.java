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
            Class var2 = Class.forName("java.lang.management.ManagementFactory");
            Object var3 = var2.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke((Object) null, new Object[0]);
            Method method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            List<String> list = (List) method.invoke(var3, new Object[0]);
            for (String a : list) {
                if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */)) {
                    try {
                        exhibition.util.security.Snitch.snitch(0, list.toArray(new String[]{}));
                        loginInstance.oldInstance = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (loginInstance.oldInstance != null && !Objects.equals(loginInstance.username.getText(), "") && !Objects.equals(loginInstance.password.getText(), ""))
                try {
                    loginInstance.status = GuiLoginMenu.Status.AUTHENTICATING;
                    String one = loginInstance.getCrypted(loginInstance.username.getText());
                    String two = loginInstance.getCrypted(loginInstance.password.getText());
                    String three = loginInstance.getCrypted(loginInstance.username.getText().hashCode() + "");
                    String four = loginInstance.getCrypted(loginInstance.password.getText().hashCode() + "");
                    try {
                        Object nigga = AuthenticationUtil.isAuth(this, loginInstance, one, two, three, four);
                        if (nigga != null && RuntimeVerification.argumentsMatch(list).isEmpty()) {
                            if (Client.instance == null) {
                                Client.instance = loginInstance.oldInstance;
                                Client.setAuthUser(nigga);
                                Client.getAuthUser().setupClient(Client.instance);
                            }
                            loginInstance.status = GuiLoginMenu.Status.SUCCESS;
                            loginInstance.setProgress(1.0);
                            LoginUtil.saveLogin(one, two);
                        }
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

