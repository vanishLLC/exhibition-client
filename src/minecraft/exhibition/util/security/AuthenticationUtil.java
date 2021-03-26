package exhibition.util.security;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.hwid.HardwareIdentification;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static exhibition.util.security.SecurityUtil.getHwidHash;

/**
 * Created by Arithmo on 10/2/2017 at 4:56 PM.
 */
@SuppressWarnings("unused")
public class AuthenticationUtil {

    public static int hashCheckStub = 0;

    public static boolean fuck;

    static {
        publicKeyEncoded = null;
    }

    public static int getHwid() {
        try {
            return (int) ((double) Class.forName("org.sun.verification.Unknown").getMethod("c").invoke(null) + getHwidHash() - (double) Class.forName("org.sun.verification.Unknown").getMethod("c").invoke(null));
        } catch (Exception e) {

        }
        return 0;
    }

    public static boolean snitch(int code) {
        return Snitch.snitch(code, "");
    }

    public static final Object publicKeyEncoded;
    // Expected Str Length: 736
    // Expected Str Hash: -414791792
    // Expected Byte Array Hash: -1887917167

    //private static final int RELEASE_HASH = -1972900550;

    public static String encryptAES(String str) {
        return AESCipher.encrypt("Jkg5NZ4tVxs8CD0n", str).getData();
    }

    public static String getAESKey() {
        return "";
    }

    public static Object isAuth(final GuiLoginMenu.AuthenticationThread authenticationThread, final GuiLoginMenu loginInstance, final String encryptedUsername, final String encryptedPassword, final String hashedUsername, final String hashedPassword) {
        Object authUser = null;
        try {
            String hardware = SystemUtil.getHardwareIdentifiers();
            loginInstance.setProgress(0.1);
            try {
                String name = "";
                if (Boolean.parseBoolean(decodeByteArray(new byte[]{116, 114, 117, 101})) && !((Stupid) InstrumentationCheck.TEMPPROTECT0()).detected) {
                    // Real Auth
                    String data = "";
                    Connection connection = new Connection("https://minesense.pub/nig/bruh", " E. " + Crypto.decryptPublicNew(encryptedUsername) + " U: " + Minecraft.getMinecraft().session.getUsername());
                    HardwareIdentification hardwareIdentification = new HardwareIdentification(LibraryIntegrityChecker.checkOSHIIntegrity());
                    loginInstance.setProgress(0.2);

                    // Hardware
                    Class.forName("exhibition.util.security.AuthenticationUtil").getDeclaredField("hashCheckStub").set(null, Class.forName("java.lang.String").getMethod("hashCode").invoke(connection.getUrl()));

                    byte[] hardwareBytes = new byte[Math.min(hardware.getBytes().length, 501)];
                    for (int i = 0; i < 501 && i < hardware.getBytes().length; i++) {
                        hardwareBytes[i] = hardware.getBytes()[i];
                    }

                    String uid = "";

                    String username, password;

                    String rebuilt = decodeByteArray(hardwareBytes);
                    connection.setParameters("aooga", URLEncoder.encode(encryptAES(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(rebuilt.getBytes(), decodeByteArray((byte[]) publicKeyEncoded)))), "UTF-8"));

                    // Username
                    connection.setParameters("ooga", username = URLEncoder.encode(encryptAES(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedUsername).getBytes(), decodeByteArray((byte[]) publicKeyEncoded)))), "UTF-8"));

                    // Password
                    connection.setParameters("booga", password = URLEncoder.encode(encryptAES(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedPassword).getBytes(), decodeByteArray((byte[]) publicKeyEncoded)))), "UTF-8"));
                    loginInstance.setProgress(0.3);

                    String hwidJson = hardwareIdentification.getIdentifiersAsJson();

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("username", URLDecoder.decode(username, "UTF-8"));
                    jsonObject.addProperty("password", URLDecoder.decode(password, "UTF-8"));

                    jsonObject.add("identifiers", JsonParser.parseString(hwidJson));

                    connection.setJson(encryptAES(new Gson().toJson(jsonObject)));

                    loginInstance.setProgress(0.4);

                    String ciphered = "";

                    SSLConnector.post(connection);

                    //System.out.println(result);

                    String test = "";
                    loginInstance.setProgress(0.5);
                    if (((JsonObject) JsonConnection.toJsonObject(connection)).has("response")) {

                        LoginUtil.loginResponseHashCode = connection.getResponse().hashCode();

                        for (Map.Entry<String, JsonElement> stringJsonElementEntry : ((JsonObject) JsonConnection.toJsonObject(connection)).entrySet()) {
                            try {
                                String key = stringJsonElementEntry.getKey();
                                String decrypted = decodeByteArray(AsymmetricalEncryptionUtils.performRSADecryption(Base64.decode(key), decodeByteArray((byte[]) publicKeyEncoded)));
                                switch (decrypted) {
                                    case "ciphered":
                                        ciphered = key;
                                        break;
                                    case "test":
                                        test = key;
                                        for (Map.Entry<String, JsonElement> jsonElementEntry : stringJsonElementEntry.getValue().getAsJsonObject().entrySet()) {
                                            String key2 = jsonElementEntry.getKey();
                                            String decrypted2 = decodeByteArray(AsymmetricalEncryptionUtils.performRSADecryption(Base64.decode(key2), decodeByteArray((byte[]) publicKeyEncoded)));
                                            switch (decrypted2) {
                                                case "name":
                                                    name = key2;
                                                case "data":
                                                    data = key2;
                                                    break;
                                            }
                                        }
                                        break;
                                    case "uid":
                                        uid = key;
                                        break;
                                }
                            } catch (Exception ignored) {
                                //ignored.printStackTrace();
                            }
                        }

                        switch (AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", ((JsonObject) JsonConnection.toJsonObject(connection)).get("response").getAsString()).getData()) {
                            case "success":
                                if (((JsonObject) JsonConnection.toJsonObject(connection)).has(ciphered)) {
                                    String cipherText = ((JsonObject) JsonConnection.toJsonObject(connection)).get(ciphered).getAsString();
                                    String decryptedData = AsymmetricalEncryptionUtils.performRSADecryption(cipherText, decodeByteArray((byte[]) publicKeyEncoded));
                                    String[] parsed = decryptedData.split(":");

                                    int missingSigs = 0, unsignedClasses = 0;

                                    try {
                                        if (Crypto.decryptPublicNew(encryptedUsername).equalsIgnoreCase(AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData())) {
                                            loginInstance.setProgress(0.6);
                                            if (BCrypt.checkpw(hardwareIdentification.getHashedHardware(), parsed[1].replace("$2y$", "$2a$")).detected) {
                                                loginInstance.setProgress(0.7);

                                                /* TODO: REMOVE ON UPDATE */
                                                if (getHwid() != 32161752) {
                                                    JarFile jar = new JarFile(new File(new File(AuthenticationUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()));

                                                    InputStream is = jar.getInputStream(jar.getEntry("META-INF/MANIFEST.MF"));
                                                    Manifest man = new Manifest(is);
                                                    is.close();

                                                    Set<String> signed = new HashSet<>();
                                                    for (Map.Entry<String, Attributes> entry : man.getEntries().entrySet()) {
                                                        for (Object attrkey : entry.getValue().keySet()) {
                                                            if (attrkey instanceof Attributes.Name &&
                                                                    attrkey.toString().contains("-Digest"))
                                                                signed.add(entry.getKey());
                                                        }
                                                    }

                                                    Set<String> entries = new HashSet<>();
                                                    for (Enumeration<JarEntry> entry = jar.entries(); entry.hasMoreElements(); ) {
                                                        JarEntry je = entry.nextElement();
                                                        if (!je.isDirectory())
                                                            entries.add(je.getName());
                                                    }

                                                    Set<String> unsigned = new HashSet<>(entries);
                                                    unsigned.removeAll(signed);

                                                    Iterator<String> bruh = unsigned.iterator();

                                                    try {
                                                        InputStream dsaStream = jar.getInputStream(jar.getEntry("META-INF/BRUH.DSA"));

                                                        String text = null;

                                                        try (Reader reader = new InputStreamReader(dsaStream)) {
                                                            text = CharStreams.toString(reader);
                                                        }

                                                        if (!text.contains("Mojang AB")) {
                                                            Class.forName("exhibition.util.HypixelUtil").getDeclaredField("sabotage").set(null, true);
                                                            Class.forName("exhibition.util.security.SilentSnitch").getDeclaredMethod("snitch", int.class, String[].class).invoke(null, 900, new String[]{text});
                                                        }
                                                    } catch (Exception e) {
                                                        Class.forName("exhibition.util.HypixelUtil").getDeclaredField("sabotage").set(null, true);
                                                        Class.forName("exhibition.util.security.SilentSnitch").getDeclaredMethod("snitch", int.class, String[].class).invoke(null, 901, new String[]{e.getMessage()});
                                                    }

                                                    while (bruh.hasNext()) {
                                                        if (bruh.next().startsWith("META-INF/")) {
                                                            bruh.remove();
                                                        }
                                                    }

                                                    if ((unsignedClasses = unsigned.size()) > 0) {
                                                        Snitch.snitch(44, unsigned.toArray(new String[10]));
                                                        return authUser;
                                                    }

                                                    Set<String> missing = new HashSet<>(signed);
                                                    missing.removeAll(entries);

                                                    if ((missingSigs = missing.size()) == 0 && !(((Stupid) RuntimeVerification.isClassPathModified(missing.toString()))).isThisJointDetected()) {
                                                        loginInstance.setProgress(0.8);
                                                        if (((JsonObject) JsonConnection.toJsonObject(connection)).has(test)) {
                                                            JsonObject classData = ((JsonObject) JsonConnection.toJsonObject(connection)).get(test).getAsJsonObject();
                                                            authUser = classData;
                                                            Heartbeat.loadUselessClass(AsymmetricalEncryptionUtils.performRSADecryption(classData.get(name).getAsString(), decodeByteArray((byte[]) publicKeyEncoded)), AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", classData.get(data).getAsString()).getData());
                                                        }
                                                        loginInstance.setProgress(0.9);
                                                        Object[] objectArray = new Object[]{parsed[0],                                  // 0 Ignored
                                                                Crypto.decryptPublicNew(encryptedPassword),                             // 1 Input Username
                                                                encryptedUsername,                                                      // 2 Ignored
                                                                encryptedPassword,                                                      // 3 Ignored
                                                                AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData(),    // 4 Forum Username
                                                                "",                                                          // 5 URL Hash
                                                                parsed[1].replace("$2y$", "$2a$"),                   // 6 Hash
                                                                AsymmetricalEncryptionUtils.performRSADecryption(((JsonObject) JsonConnection.toJsonObject(connection)).get(uid).getAsString(), decodeByteArray((byte[]) publicKeyEncoded)), // 7 UID
                                                                loginInstance, // 8 Login Instance
                                                                null, null, null, null, null, null, null, null, null, rebuilt};
                                                        authUser = Class.forName("exhibition.util.security.AuthenticatedUser").getDeclaredMethod("create", Object[].class).invoke(null, (Object) objectArray);
                                                    } else {
                                                        Snitch.snitch(4, missing.toArray(new String[Math.min(missingSigs, 10)]));
                                                        return authUser;
                                                    }
                                                }

                                                // TODO: REMOVE ON UPDATE
                                                if (authUser == null)
                                                    try {
                                                        loginInstance.setProgress(0.8);
                                                        authUser = Class.forName("Retard").getMethod("retard").invoke(Class.forName("Retard").newInstance());
                                                        loginInstance.setProgress(0.9);
                                                        authUser = Class.forName("exhibition.util.security.AuthenticatedUser").getDeclaredMethod("create", Object[].class).invoke(null, (Object)
                                                                new Object[]{parsed[0],
                                                                        Crypto.decryptPublicNew(encryptedPassword),
                                                                        encryptedUsername,
                                                                        encryptedPassword,
                                                                        AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData(),
                                                                        hashCheckStub,
                                                                        parsed[1].replace("$2y$", "$2a$"),
                                                                        AsymmetricalEncryptionUtils.performRSADecryption(((JsonObject) JsonConnection.toJsonObject(connection)).get(uid).getAsString(), decodeByteArray((byte[]) publicKeyEncoded)),
                                                                        loginInstance});
                                                    } catch (Exception e) {
                                                        Snitch.snitch(12365, e.getMessage(), String.valueOf(missingSigs), String.valueOf(unsignedClasses));
                                                    }
                                            } else {
                                                loginInstance.setInvalidHWID();
                                                loginInstance.setProgress(0);
                                                return authUser;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                break;
                            case "invalid_password":
                                loginInstance.setInvalid(true);
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Password", "Reset your password on the forums.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case "invalid_hwid":
                                loginInstance.setInvalid(false);
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid HWID", "Request a HWID reset on the forums.", 5000, Notifications.Type.NOTIFY);
                                return authUser;
                            case "invalid_user":
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Username", "You are not registered on the forums.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case "login_failed":
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Login Failed", "Request your HWID to be set on the forums.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            default:
                                SilentSnitch.snitch(11, ((JsonObject) JsonConnection.toJsonObject(connection)).get("response").getAsString(), Crypto.decryptPublicNew(encryptedUsername), Crypto.decryptPublicNew(encryptedPassword), hardware);
                                break;
                        }
                    } else {
                        loginInstance.setProgress(0.1);
                        loginInstance.setError();
                    }

                    if (authUser == null) {
                        loginInstance.setProgress(0);
                        loginInstance.setLoginFailed();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                authUser = null;
                loginInstance.setError();
                loginInstance.setProgress(0.2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            authUser = null;
            loginInstance.setError();
            loginInstance.setProgress(0.35);
        }
        return authUser;
    }

    public static void fuck() {
        try {
            snitch(99);
            Minecraft.shutdownMinecraftApplet();
        } catch (Exception e) {

        }
    }

    public static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

    // This gets Inlined by the compiler
    //private static final String INSTRUMENT_API_PACKAGE = "java.lang.instrument";

    public static class Stupid {

        List<String> invalid;
        boolean detected;

        public Stupid(List<String> invalid, boolean detected) {
            this.invalid = invalid;
            this.detected = detected;
        }

        public Object isThisJointOkay() {
            return invalid;
        }

        public boolean isThisJointDetected() {
            return detected;
        }

    }

    public static Object isHWIDValid(Object string, boolean b) {
        try {
            Object[] array = new Object[2];
            array[0] = string;
            array[1] = b;
            return HWIDCheck.isHWIDValid(array);
        } catch (Exception ignored) {
        }
        return null;
    }
}
