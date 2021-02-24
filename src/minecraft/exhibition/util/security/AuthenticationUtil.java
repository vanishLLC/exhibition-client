package exhibition.util.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import exhibition.Client;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.notifications.usernotification.Notifications;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
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

    public static int authListPos = -1;

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
    // Expected Str Hash: -2112696710

    public static Object isAuth(final GuiLoginMenu.AuthenticationThread authenticationThread, final GuiLoginMenu loginInstance, final String encryptedUsername, final String encryptedPassword, final String hashedUsername, final String hashedPassword) {
        Object authUser = null;
        try {
            loginInstance.setProgress(0.1);
            String hardware = SystemUtil.getHardwareIdentifiers();
            try {
                String name = "";
                if (Boolean.parseBoolean(decodeByteArray(new byte[]{116, 114, 117, 101})) && !((Stupid) TEMPPROTECT0()).detected) {
                    // Real Auth
                    String data = "";
                    String URL = Crypto.decryptPrivate("2HERJVjF6EBt5M4ohHRYEf7M8uAcxCzhBEiMJwl3vcpqH+yU4C7/d/bnvCWFbzYB");
                    Connection connection = new Connection("https://minesense.pub/nig/bruh", " E. " + Crypto.decryptPublicNew(encryptedUsername) + " U: " + Minecraft.getMinecraft().session.getUsername());
                    loginInstance.setProgress(0.2);
                    // Hardware

                    authListPos += connection.getUrl().hashCode();

                    byte[] hardwareBytes = new byte[Math.min(hardware.getBytes().length, 501)];
                    for (int i = 0; i < 501 && i < hardware.getBytes().length; i++) {
                        hardwareBytes[i] = hardware.getBytes()[i];
                    }

                    String uid = "";

                    if(AuthenticationUtil.authListPos + -139796159 == decodeByteArray((byte[])publicKeyEncoded).hashCode()) {
                        String rebuilt = decodeByteArray(hardwareBytes);
                        connection.setParameters("aooga", URLEncoder.encode(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(rebuilt.getBytes(), decodeByteArray((byte[]) publicKeyEncoded))), "UTF-8"));

                        // Username
                        connection.setParameters("ooga", URLEncoder.encode(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedUsername).getBytes(), decodeByteArray((byte[]) publicKeyEncoded))), "UTF-8"));
                        loginInstance.setProgress(0.3);
                        // Password
                        connection.setParameters("booga", URLEncoder.encode(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedPassword).getBytes(), decodeByteArray((byte[]) publicKeyEncoded))), "UTF-8"));
                        loginInstance.setProgress(0.4);
                    }

                    String ciphered = "";

                    SSLConnector.post(connection);


                    //System.out.println(result);

                    String test = "";
                    loginInstance.setProgress(0.5);
                    if (((JsonObject) JsonParser.parseString(connection.getResponse())).has("response")) {
                        for (Map.Entry<String, JsonElement> stringJsonElementEntry : ((JsonObject) JsonParser.parseString(connection.getResponse())).entrySet()) {
                            try {
                                String key = stringJsonElementEntry.getKey();
                                String decrypted = decodeByteArray(AsymmetricalEncryptionUtils.performRSADecryption(Base64.decode(key), decodeByteArray((byte[])publicKeyEncoded)));
                                switch (decrypted) {
                                    case "ciphered":
                                        ciphered = key;
                                        break;
                                    case "test":
                                        test = key;
                                        for (Map.Entry<String, JsonElement> jsonElementEntry : stringJsonElementEntry.getValue().getAsJsonObject().entrySet()) {
                                            String key2 = jsonElementEntry.getKey();
                                            String decrypted2 = decodeByteArray(AsymmetricalEncryptionUtils.performRSADecryption(Base64.decode(key2), decodeByteArray((byte[])publicKeyEncoded)));
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

                        switch (((JsonObject) JsonParser.parseString(connection.getResponse())).get("response").getAsString()) {
                            case "success":
                                if (((JsonObject) JsonParser.parseString(connection.getResponse())).has(ciphered)) {
                                    String cipherText = ((JsonObject) JsonParser.parseString(connection.getResponse())).get(ciphered).getAsString();
                                    String decryptedData = AsymmetricalEncryptionUtils.performRSADecryption(cipherText, decodeByteArray((byte[])publicKeyEncoded));
                                    String[] parsed = decryptedData.split(":");

                                    try {
                                        if (Crypto.decryptPublicNew(encryptedUsername).equals(parsed[0])) {
                                            loginInstance.setProgress(0.6);
                                            if (BCrypt.checkpw(Crypto.decryptPublicNew(encryptedPassword), parsed[1]).detected) {
                                                loginInstance.setProgress(0.7);
                                                if (BCrypt.checkpw(hardware, parsed[2]).detected) {
                                                    loginInstance.setProgress(0.8);

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

                                                        while (bruh.hasNext()) {
                                                            if (bruh.next().startsWith("META-INF/")) {
                                                                bruh.remove();
                                                            }
                                                        }

                                                        if (unsigned.size() > 0) {
                                                            Snitch.snitch(44, unsigned.toArray(new String[]{}));
                                                            return authUser;
                                                        }

                                                        Set<String> missing = new HashSet<>(signed);
                                                        missing.removeAll(entries);

                                                        if (missing.size() == 0 && !(((Stupid) RuntimeVerification.isClassPathModified(missing.toString()))).isThisJointDetected()) {
                                                            if (((JsonObject) JsonParser.parseString(connection.getResponse())).has(test)) {
                                                                JsonObject classData = ((JsonObject) JsonParser.parseString(connection.getResponse())).get(test).getAsJsonObject();
                                                                authUser = classData;
                                                                Heartbeat.loadUselessClass(AsymmetricalEncryptionUtils.performRSADecryption(classData.get(name).getAsString(), decodeByteArray((byte[])publicKeyEncoded)), classData.get(data).getAsString());
                                                            }
                                                            loginInstance.setProgress(0.9);
                                                            Object[] objectArray = new Object[]{parsed[0], Crypto.decryptPublicNew(encryptedPassword), encryptedUsername, encryptedPassword, parsed[0], parsed[1], parsed[2], AsymmetricalEncryptionUtils.performRSADecryption(((JsonObject) JsonParser.parseString(connection.getResponse())).get(uid).getAsString(), decodeByteArray((byte[])publicKeyEncoded)), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
                                                            authUser = AuthenticatedUser.class.getConstructor(Object[].class).newInstance((Object) objectArray);
                                                            loginInstance.setProgress(1.0);
                                                        } else {
                                                            Snitch.snitch(4, missing.toArray(new String[]{}));
                                                            return authUser;
                                                        }
                                                    }
                                                    try {
                                                        authUser = Class.forName("Retard").getMethod("retard").invoke(Class.forName("Retard").newInstance());
                                                        loginInstance.setProgress(0.9);
                                                        authUser = Class.forName("exhibition.util.security.AuthenticatedUser").getConstructor(Object[].class).newInstance((Object) new Object[]{parsed[0], Crypto.decryptPublicNew(encryptedPassword), encryptedUsername, encryptedPassword, parsed[0], parsed[1], parsed[2], AsymmetricalEncryptionUtils.performRSADecryption(((JsonObject) JsonParser.parseString(connection.getResponse())).get(uid).getAsString(), decodeByteArray((byte[])publicKeyEncoded))});
                                                        loginInstance.setProgress(1);
                                                    } catch (Exception e) {
                                                        //e.printStackTrace();
                                                    }
                                                } else {
                                                    loginInstance.setInvalidHWID();
                                                    loginInstance.setProgress(0);
                                                    return authUser;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        LoginUtil.cachedLogin = -1;
                                    }

                                }
                                break;
                            case "invalid_password":
                                loginInstance.setInvalid(true);
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Password", "Notify an admin if you forgot your password.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case "invalid_hwid":
                                loginInstance.setInvalid(false);
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid HWID", "Request a HWID reset on the forums.", 5000, Notifications.Type.NOTIFY);
                                return authUser;
                            case "invalid_user":
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Username", "Make sure your HWID has been set.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case "login_failed":
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Login Failed", "Make sure your HWID has been set.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            default:
                                SilentSnitch.snitch(11, ((JsonObject) JsonParser.parseString(connection.getResponse())).get("response").getAsString(), Crypto.decryptPublicNew(encryptedUsername), Crypto.decryptPublicNew(encryptedPassword), hardware);
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

    @SuppressWarnings("unchecked")
    public static Object TEMPPROTECT0() {
        // TODO: REMOVE ON UPDATE
        if (getHwid() != 32161752)
            try {
                final Field classesField = ClassLoader.class.getDeclaredField("classes");
                classesField.setAccessible(true);
                final Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(ClassLoader.getSystemClassLoader());

                boolean detected = false;
                List<String> detectedClasses = new ArrayList<>();
                for (final Class<?> clazz : new Vector<>(classes)) {
                    for (final Class<?> interfaceClazz : clazz.getInterfaces()) {
                        if (interfaceClazz.getName().startsWith("java.lang.instrument"))
                            if (!detectedClasses.contains(clazz.getName())) {
                                detectedClasses.add(clazz.getName());
                                detected = true;
                            }
                    }

                    try {
                        for (final Field field : clazz.getFields()) {
                            if (field.getType().getName().startsWith("java.lang.instrument"))
                                if (!detectedClasses.contains(clazz.getName())) {
                                    detectedClasses.add(clazz.getName());
                                    detected = true;
                                }
                        }
                    } catch (Throwable ignored) {

                    }

                    try {
                        for (final Method method : clazz.getMethods()) {
                            final List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
                            final List<Class<?>> exceptionTypes = Arrays.asList(method.getExceptionTypes());

                            if (parameterTypes.stream().anyMatch(c -> c.getName().startsWith("java.lang.instrument")) || exceptionTypes.stream().anyMatch(c -> c.getName().startsWith("java.lang.instrument")))
                                if (!detectedClasses.contains(clazz.getName())) {
                                    detectedClasses.add(clazz.getName());
                                    detected = true;
                                }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    if (detected) {
                        exhibition.util.security.Snitch.snitch(2, detectedClasses.toArray(new String[]{}));
                    }
                    return new Stupid(detectedClasses, detected);
                }
            } catch (Exception ignored) {
            }
        // TODO: REMOVE ON UPDATE
        return new Stupid(new ArrayList<>(), getHwid() != 32161752);
    }

    public static Object isHWIDValid(Object string, boolean b) {
        try {
            Object[] array = new Object[4 + (int)(Short.MAX_VALUE / 5 * Math.random())];
            array[0] = string;
            array[1] = b;
            return HWIDCheck.isHWIDValid(array);
        } catch (Exception ignored) {
        }
        return null;
    }
}
