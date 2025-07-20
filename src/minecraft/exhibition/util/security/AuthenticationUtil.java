package exhibition.util.security;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import exhibition.Client;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.hwid.HardwareIdentification;
import net.minecraft.client.Minecraft;

import java.net.*;
import java.util.*;
import java.util.List;

/**
 * Created by Arithmo on 10/2/2017 at 4:56 PM.
 */
@SuppressWarnings("unused")
public class AuthenticationUtil {

    public static int hashCheckStub = 0;

    static {
        publicKeyEncoded = new byte[(int) Math.round(5 * Math.random())];
    }

    public static int getHwid() {
        return 32161752;
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

//    public static String getAESKey() {
//        return "";
//    }

    public static Object hardwareIdentification;

    public static String temporaryUsername = null;

    public static Object isAuth(final AuthenticationThread authenticationThread, final GuiLoginMenu loginInstance, final String encryptedUsername, final String encryptedPassword, final String hashedUsername, final String hashedPassword) {
        Object authUser = null;
        try {
            String hardware = SystemUtil.getHardwareIdentifiers();
            loginInstance.setProgress(0.1);
            try {
                String name = "";
                if (!((Stupid) InstrumentationCheck.TEMPPROTECT0()).detected) {
                    // Real Auth
                    String data = "";
                    Connection connection = new Connection("https://api2.minesense.pub/bruh", " E. " + Crypto.decryptPublicNew(encryptedUsername) + " U: " + Minecraft.getMinecraft().session.getUsername());
                    if (hardwareIdentification == null)
                        hardwareIdentification = new HardwareIdentification(LibraryIntegrityChecker.checkOSHIIntegrity());
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

                    temporaryUsername = Crypto.decryptPublicNew(encryptedUsername);

                    // Username
                    connection.setParameters("ooga", username = URLEncoder.encode(encryptAES(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedUsername).getBytes(), decodeByteArray((byte[]) publicKeyEncoded)))), "UTF-8"));

                    // Password
                    connection.setParameters("booga", password = URLEncoder.encode(encryptAES(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(Crypto.decryptPublicNew(encryptedPassword).getBytes(), decodeByteArray((byte[]) publicKeyEncoded)))), "UTF-8"));
                    loginInstance.setProgress(0.3);

                    String hwidJson = ((HardwareIdentification) hardwareIdentification).getIdentifiersAsJson();

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("username", URLDecoder.decode(username, "UTF-8"));
                    jsonObject.addProperty("password", URLDecoder.decode(password, "UTF-8"));

                    jsonObject.add("identifiers", JsonParser.parseString(hwidJson));

                    connection.setJson(encryptAES(new Gson().toJson(jsonObject)));

                    loginInstance.setProgress(0.4);

                    String ciphered = "";

                    SSLConnector.post(connection);

                    System.out.println(connection.getResponse());

                    boolean isBeta = false;

                    String test = "";
                    loginInstance.setProgress(0.5);

                    int responseHashCode = connection.getResponse().hashCode();

                    if (((JsonObject) JsonConnection.toJsonObject(connection)).has("response")) {

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
                                    case "beta":
                                        isBeta = true;
                                        break;
                                }
                            } catch (Exception ignored) {
                                //ignored.printStackTrace();
                            }
                        }

                        switch (AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", ((JsonObject) JsonConnection.toJsonObject(connection)).get("response").getAsString()).getData().hashCode()) {
                            case -1867169789: // success
                                if (Client.isBeta() && !isBeta) {
                                    loginInstance.setLoginFailed();
                                    loginInstance.setProgress(0);
                                    Notifications.getManager().post("Unauthorized User", "Action logged. Client will be closing in {s}s.", 5000, Notifications.Type.WARNING);
                                    SilentSnitch.snitch(77777, Crypto.decryptPublicNew(encryptedUsername), Client.version, Client.parsedVersion);
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(5100);
                                            } catch (Exception ignored) {
                                            }
                                            Minecraft.getMinecraft().shutdown();
                                        }
                                    }.start();
                                    return authUser;
                                }
                                if (((JsonObject) JsonConnection.toJsonObject(connection)).has(ciphered)) {
                                    String cipherText = ((JsonObject) JsonConnection.toJsonObject(connection)).get(ciphered).getAsString();
                                    String decryptedData = AsymmetricalEncryptionUtils.performRSADecryption(cipherText, decodeByteArray((byte[]) publicKeyEncoded));
                                    String[] parsed = decryptedData.split(":");

                                    int missingSigs = 0, unsignedClasses = 0;

                                    try {
                                        if (Crypto.decryptPublicNew(encryptedUsername).trim().equalsIgnoreCase(AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData())) {
                                            loginInstance.setProgress(0.6);
                                            if (BCrypt.checkpw(((HardwareIdentification) hardwareIdentification).getHashedHardware(), parsed[1].replace("$2y$", "$2a$")).detected) {
                                                loginInstance.setProgress(0.7);
                                                if (((JsonObject) JsonConnection.toJsonObject(connection)).has(test)) {
                                                    authUser = ((JsonObject) JsonConnection.toJsonObject(connection)).get(test).getAsJsonObject();
                                                }
                                                loginInstance.setProgress(0.8);

                                                Object[] objectArray = new Object[]{
                                                        parsed[0], // 0 Encrypted Username
                                                        Crypto.decryptPublicNew(encryptedPassword), // 1 Decrypted Password
                                                        encryptedUsername, // 2 Entered Username
                                                        encryptedPassword, // 3 Entered Password
                                                        AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData(), // 4 Username
                                                        hashCheckStub, // 5 Hash Check Stub
                                                        parsed[1].replace("$2y$", "$2a$"), // 6 HWID Hash
                                                        // 7 UID
                                                        AsymmetricalEncryptionUtils.performRSADecryption(((JsonObject) JsonConnection.toJsonObject(connection)).get(uid).getAsString(), decodeByteArray((byte[]) publicKeyEncoded)),
                                                        loginInstance, // 8 Login Instance
                                                        isBeta, // 9 isBeta
                                                        authUser, // 10 ClassData
                                                        name, // 11 class name
                                                        data, // 12 class data
                                                        null, null, null, null, null, // Some kind of integrity checking later?
                                                        rebuilt};
                                                loginInstance.setProgress(0.9);
                                                if (LoginUtil.getLastResponseCode() != responseHashCode) {
                                                    try {
                                                        authUser = AuthenticatedUser.create(objectArray);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Snitch.snitch(4, e.getMessage());
                                                    }
                                                } else {
                                                    Snitch.snitch(60, LoginUtil.getLastResponseCode() + "", responseHashCode + "", AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", parsed[0]).getData());
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
                            case -1470480413: // invalid_password
                                loginInstance.setInvalid(true);
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Password", "Action logged. Reset your password on the forums.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case 526875570: // invalid_hwid
                                loginInstance.setInvalidHWID();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid HWID", "Action logged. Please request a HWID reset on the forums.", 5000, Notifications.Type.NOTIFY);
                                return authUser;
                            case 527258899: // invalid_user
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Invalid Username", "Action logged. You are not registered on the forums.", 5000, Notifications.Type.WARNING);
                                return authUser;
                            case -545183277: // login_failed
                                loginInstance.setLoginFailed();
                                loginInstance.setProgress(0);
                                Notifications.getManager().post("Login Failed", "Action logged. Request your HWID to be set on the forums.", 5000, Notifications.Type.WARNING);
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
                } else {
                    snitch(30303030);
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

    public static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

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
