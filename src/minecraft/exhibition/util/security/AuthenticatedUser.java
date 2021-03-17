package exhibition.util.security;

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
    public static AuthenticatedUser create(Object[] args) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        try {
            Class var2 = Class.forName("java.lang.management.ManagementFactory");
            Object var3 = var2.getDeclaredMethod("getRuntimeMXBean").invoke(null);
            Method method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            List<String> list = (List) method.invoke(var3);
            authenticatedUser.jvmArguments = list;
        } catch (Exception e) {
        }

        try {
            File runTimeFile = new File((Minecraft.isIsRunningOnWindows ? "" : "/") + System.class.getResource("System.class").getPath().split("!")[0].replace("file:/", "").replace("%20", " "));
            Class md = Class.forName("java.security.MessageDigest");
            Object mdInstance = md.getMethod("getInstance", String.class).invoke(null, "SHA-256");
            try (InputStream in = new FileInputStream(runTimeFile)) {
                byte[] block = new byte[4096];
                int length;
                while ((length = in.read(block)) > 0) {
                    md.getMethod("update", byte[].class, int.class, int.class).invoke(mdInstance, block, 0, length);
                }
                String checkSum = (String) Class.forName("javax.xml.bind.DatatypeConverter").getMethod("printHexBinary", byte[].class).invoke(null, (byte[]) md.getMethod("digest").invoke(mdInstance));

                boolean bruh = Minecraft.isIsRunningOnWindows;

                if (183572818 != checkSum.hashCode() && checkSum.hashCode() != 589290158 && -927836280 != checkSum.hashCode() && 1791589503 != checkSum.hashCode() && bruh) {
                    Snitch.snitch(23, runTimeFile.getAbsolutePath(), checkSum, checkSum.hashCode() + ""); // checksum mismatch
                } else {
                    authenticatedUser.forumUsername = (String) args[4];
                    authenticatedUser.hwidHash = (String) args[6];
                    authenticatedUser.userID = Integer.parseInt((String) args[7]);

//                    this.disabledModules = (List<String>) args[8];
//                    this.alertModules = (List<String>) args[9];
//                    this.disabledSettings = (HashMap<String, String>) args[10];

                    try {
                        authenticatedUser.inputUsername = Crypto.decrypt(CryptManager.getSecretNew(), (String) args[2]);
                    } catch (Exception ignored) {
                    }

                    try {
                        String usedSomewhere = (String) args[123];
                        authenticatedUser.userID = Integer.parseInt(usedSomewhere + " " + args[1003].equals(args[54]));
                    } catch (Exception ignored) {
                    }

                    GuiLoginMenu loginInstance = (GuiLoginMenu) args[8];

                    int urlHash = (int) ReflectionUtil.getField(Class.forName("exhibition.util.security.AuthenticationUtil").getDeclaredField("hashCheckStub"), null);

                    String hashStr = Integer.toString(Arrays.hashCode((byte[]) ReflectionUtil.getField(Class.forName("exhibition.util.security.AuthenticationUtil").getDeclaredField("publicKeyEncoded"), null)) - urlHash);

                    if (!hashStr.equals("84983383")) {
                        loginInstance.setInvalid(false);
                        loginInstance.setProgress(0);
                        Notifications.getManager().post("Invalid HWID", "Request a HWID reset on the forums.", 5000, Notifications.Type.NOTIFY);
                        authenticatedUser = null;
                    }
                }
            } catch (Exception e) {
                exhibition.util.security.Snitch.snitch(22, e.getMessage(), e.getLocalizedMessage()); // ????
                KillProcess.killMC();
            }
        } catch (Exception e) {
        }
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

    public boolean isEverythingOk() {
        int i = 0;
        for (String a : jvmArguments) {
            if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */))
                i++;
        }
        return jvmArguments.size() > 0 && !jvmArguments.get(0).equalsIgnoreCase("XD") && i < 0x1 && ((Integer) i).equals(0x0) && (forumUsername).equals(inputUsername) && !(!(!Arrays.toString(forumUsername.getBytes()).equals(Arrays.toString("".getBytes()))));
    }

    public void setupClient(Castable instance) {
        try {
            Client client = instance.cast();
            client.setup();
            //Class.forName("exhibition.Client").getMethod("setup").invoke(instance);

            Class fieldClass = Class.forName("java.lang.reflect.Field");
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Object bruh = unsafeClass.getDeclaredField("theUnsafe");
            Object field = Class.forName("java.lang.System").getDeclaredField("err");
            fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
            Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) new Object[0]);

            Object oldInstance = ReflectionUtil.getField(Class.forName("exhibition.util.security.LoggerContainer").getDeclaredField("oldLoggerInstance"), null);

            unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class).invoke(unsafeInstance,
                    unsafeClass.getMethod("staticFieldBase", fieldClass).invoke(unsafeInstance, field),
                    unsafeClass.getMethod("staticFieldOffset", fieldClass).invoke(unsafeInstance, field),
                    oldInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object hardwareObject;

    public boolean hashCheck() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                runCheck();
            }
        };

        thread.start();

        return false;
    }

    public void runCheck() {
        try {
            if (hardwareObject == null)
                hardwareObject = new HardwareIdentification(Class.forName("java.lang.Class").getMethod("newInstance").invoke(Class.forName("oshi.SystemInfo")));

            HardwareIdentification hardwareIdentification = (HardwareIdentification) hardwareObject;

            String check = hardwareIdentification.cpuName
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

            if (isEverythingOk() && BCrypt.checkpw(check, hwidHash).detected)
                return;
        } catch (Exception e) {

        }
        try {
            HypixelUtil.sabotage = true;

            Class.forName("exhibition.util.security.SilentSnitch").getDeclaredMethod("snitch", int.class, String[].class).invoke(null, 501, new String[]{forumUsername, hwidHash});
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
