package exhibition.util.security;

import exhibition.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.util.CryptManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static exhibition.util.security.AuthenticationUtil.getHwid;

public class AuthenticatedUser {

    private int userID;

    private String decryptedUsername;
    private String decryptedPassword;
    private String encryptedUsername;
    private String encryptedPassword;
    private String inputUsername;
    private String inputPassword;
    private String usernameHash;
    private String passwordHash;
    private String hwidHash;
    private List<String> jvmArguments;

    public AuthenticatedUser(Object[] args) {
        try {
            Class var2 = Class.forName("java.lang.management.ManagementFactory");
            Object var3 = var2.getDeclaredMethod("getRuntimeMXBean").invoke(null);
            Method method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            List<String> list = (List) method.invoke(var3);
            this.jvmArguments = list;
        } catch (Exception e) {
        }

        try {
            File runTimeFile = new File((Minecraft.isIsRunningOnWindows ?  "" : "/") + System.class.getResource("System.class").getPath().split("!")[0].replace("file:/", "").replace("%20", " "));
            Class md = Class.forName("java.security.MessageDigest");
            Object mdInstance = md.getMethod("getInstance", String.class).invoke(null, "SHA-256");
            try (InputStream in = new FileInputStream(runTimeFile)) {
                byte[] block = new byte[4096];
                int length;
                while ((length = in.read(block)) > 0) {
                    md.getMethod("update", byte[].class, int.class, int.class).invoke(mdInstance, block, 0, length);
                }
                String checkSum = (String) Class.forName("javax.xml.bind.DatatypeConverter").getMethod("printHexBinary", byte[].class).invoke(null, (byte[]) md.getMethod("digest").invoke(mdInstance));

                boolean bruh = (boolean)(Class.forName("net.minecraft.client.Minecraft").getDeclaredField("isIsRunningOnWindows").get(null));

                if (183572818 != checkSum.hashCode() && -927836280 != checkSum.hashCode() && 1791589503 != checkSum.hashCode() && bruh) {
                    Snitch.snitch(23, runTimeFile.getAbsolutePath(), checkSum, checkSum.hashCode() + ""); // checksum mismatch

                } else {
                    this.decryptedUsername = (String)args[0];
                    this.decryptedPassword = (String)args[1];
                    this.encryptedUsername = (String)args[2];
                    this.encryptedPassword = (String)args[3];
                    this.usernameHash = (String)args[4];
                    this.passwordHash = (String)args[5];
                    this.hwidHash = (String)args[6];
                    this.userID = Integer.parseInt((String)args[7]);

                    try {
                        this.inputUsername = Crypto.decrypt(CryptManager.getSecretNew(), (String)args[2]);
                        this.inputPassword = Crypto.decrypt(CryptManager.getSecretNew(), (String)args[3]);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                exhibition.util.security.Snitch.snitch(22, e.getMessage(), e.getLocalizedMessage()); // ????
                KillProcess.killMC();
            }
        } catch (Exception e) {
        }
    }

    public int getUserID() {
        return userID;
    }

    public boolean isEverythingOk() {
        int i = 0;
        for (String a : jvmArguments) {
            if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */))
                i++;
        }
        return jvmArguments.size() > 0 && !jvmArguments.get(0).equalsIgnoreCase("XD") && i < 0x1 && ((Integer) i).equals(0x0) && (decryptedUsername + decryptedPassword).equals(inputUsername + inputPassword) && !(!(!Arrays.toString(decryptedUsername.getBytes()).equals(Arrays.toString("".getBytes())))) && !(!(!Arrays.toString(decryptedPassword.getBytes()).equals(Arrays.toString("".getBytes()))));
    }

    public boolean testEncryption() {
        try {
            String t1 = Crypto.decrypt(CryptManager.getSecretNew(), getEncryptedUsername());
            String t2 = Crypto.decrypt(CryptManager.getSecretNew(), getEncryptedPassword());
            return t1.equals(inputUsername) && t2.equals(inputPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public void setupClient(Object instance) {
        try {
            Class.forName("exhibition.Client").getMethod("setup").invoke(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hashCheck() {
        int a = BCrypt.checkpw(Crypto.decryptPublicNew(encryptedUsername), Crypto.decryptPublicNew(usernameHash)).detected ? 1 : 0;
        boolean password = BCrypt.checkpw(Crypto.decryptPublicNew(encryptedPassword), Crypto.decryptPublicNew(passwordHash)).detected;
        boolean hwid = BCrypt.checkpw(SystemUtil.getHardwareIdentifiers(), Crypto.decryptPublicNew(hwidHash)).detected;
        return testEncryption() && isEverythingOk() && a == 1 && password && hwid;
    }

    public boolean doAuth() {
        return false;
    }

    public String getDecryptedUsername() {
        return decryptedUsername;
    }

    public String getDecryptedPassword() {
        return decryptedPassword;
    }

    public boolean justMakingSure() {
        AtomicInteger isOkay = new AtomicInteger(0);
        jvmArguments.forEach(a -> {
            if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */))
                isOkay.getAndAdd(1);
        });
        return jvmArguments.size() > 0 && !jvmArguments.get(0).equalsIgnoreCase("XD") && isOkay.get() < 0x1 && ((Integer) isOkay.get()).equals(0x0) && (decryptedUsername + decryptedPassword).equals(inputUsername + inputPassword) && !(!(!Arrays.toString(decryptedUsername.getBytes()).equals(Arrays.toString("".getBytes())))) && !(!(!Arrays.toString(decryptedPassword.getBytes()).equals(Arrays.toString("".getBytes()))));
    }

    public String getEncryptedUsername() {
        return encryptedUsername;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

}
