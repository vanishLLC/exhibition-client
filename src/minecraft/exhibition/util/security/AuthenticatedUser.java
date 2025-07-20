package exhibition.util.security;

import com.google.gson.JsonObject;
import exhibition.Client;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.HypixelUtil;
import exhibition.util.security.hwid.DiskIdentifiers;
import exhibition.util.security.hwid.DisplayIdentifiers;
import exhibition.util.security.hwid.HardwareIdentification;
import net.minecraft.client.Minecraft;
import net.minecraft.util.CryptManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static exhibition.util.security.AuthenticationUtil.getHwid;

public class AuthenticatedUser extends Castable {

    public int userID;
    public boolean isBeta;
    private String forumUsername;
    private String inputUsername;
    private String hwidHash;
    private List<String> jvmArguments;

    // These are modules that are forcefully disabled
    private List<String> disabledModules;

    // These are modules that alert the user that they may not function
    private List<String> alertModules;

    // These are settings that should not be enabled
    private HashMap<String, String> disabledSettings;

    // Ensures that the constructor is hidden in native
    public static Object create(Object[] args) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();

        authenticatedUser.forumUsername = (String) args[0];
        authenticatedUser.userID = 0;
        authenticatedUser.isBeta = false;
        return authenticatedUser;
    }

    public AuthenticatedUser setUserID(int userID) {
        this.userID = userID;
        return this;
    }

    public AuthenticatedUser setForumUsername(String forumUsername) {
        this.forumUsername = forumUsername;
        return this;
    }

    public AuthenticatedUser setInputUsername(String inputUsername) {
        this.inputUsername = inputUsername;
        return this;
    }

    public AuthenticatedUser setHwidHash(String hwidHash) {
        this.hwidHash = hwidHash;
        return this;
    }

    public AuthenticatedUser setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
        return this;
    }

    public void setupClient(Castable instance) {
        try {
            Client client = instance.cast();
            client.setup();
            //Class.forName("exhibition.Client").getMethod("setup").invoke(instance);

            if (getHwid() != 32161752) {
//                Class fieldClass = Class.forName("java.lang.reflect.Field");
//                Class unsafeClass = Class.forName("sun.misc.Unsafe");
//                Object theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
//                Object sysErrField = Class.forName("java.lang.System").getDeclaredField("err");
//                fieldClass.getMethod("setAccessible", boolean.class).invoke(theUnsafeField, true);
//                Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(theUnsafeField, (Object) new Object[0]);
//
//                Object oldInstance = ReflectionUtil.getField(Class.forName("exhibition.util.security.LoggerContainer").getDeclaredField("oldLoggerInstance"), null);
//
//                unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class).invoke(unsafeInstance,
//                        unsafeClass.getMethod("staticFieldBase", fieldClass).invoke(unsafeInstance, sysErrField),
//                        unsafeClass.getMethod("staticFieldOffset", fieldClass).invoke(unsafeInstance, sysErrField),
//                        oldInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object hardwareObject;

    public boolean hashCheck() {

//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                runCheck();
//            }
//        };
//
//        thread.start();

        return false;
    }

    public void runCheck() {
        String check = "";
        try {
            if (hardwareObject == null)
                hardwareObject = new HardwareIdentification(Class.forName("java.lang.Class").getMethod("newInstance").invoke(Class.forName("oshi.SystemInfo")));

            HardwareIdentification hardwareIdentification = (HardwareIdentification) hardwareObject;

            check += hardwareIdentification.cpuName
                    + hardwareIdentification.baseboardIdentifiers.getModel()
                    + hardwareIdentification.baseboardIdentifiers.getSerial()
                    + hardwareIdentification.systemIdentifiers.getModel()
                    + hardwareIdentification.systemIdentifiers.getSerial();

            for (DisplayIdentifiers.DisplayContainer displayContainer : hardwareIdentification.displayIdentifiers.getDisplayContainers()) {
                check += displayContainer.getSerial();
            }

            for (DiskIdentifiers.DiskContainer diskContainer : hardwareIdentification.diskIdentifiers.getDiskContainers()) {
                check += diskContainer.getSerial();
            }

            if (BCrypt.checkpw(check, hwidHash).detected)
                return;
        } catch (Exception e) {
            try {
                // snitch(502
                Class.forName("exhibition.util.security.SilentSnitch").getDeclaredMethod("snitch", int.class, String[].class).invoke(null, 502, new String[]{hwidHash, check});
            } catch (Exception ignored) {

            }
            return;
        }
        try {
            HypixelUtil.sabotage = true;
            // snitch(501
            Class.forName("exhibition.util.security.SilentSnitch").getDeclaredMethod("snitch", int.class, String[].class).invoke(null, 501, new String[]{hwidHash, check});
        } catch (Exception e) {

        }
    }

    public String getForumUsername() {
        return forumUsername;
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

}
