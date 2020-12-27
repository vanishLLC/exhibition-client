package exhibition.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtil {

    public static int getHwidHash() {
        String s = "";
        try {
            String main = (System.getenv(Crypto.decryptPrivate("C1KvKeE8A2WKZ90JD4HBy0PLTIwGdQ9SstTcxiaUikc")) +
                    System.getenv(Crypto.decryptPrivate("SgSyMcFiuys0Jwjdoz6bSw==")) +
                    System.getProperty(Crypto.decryptPrivate("+uP3OUfHhbnQH28rONuMNw=="))).trim();
            byte[] bytes = main.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md2 = md.digest(bytes);
            int i = 0;
            byte[] array;
            int length = (array = md2).length;
            for (int j = 0; j < length; j++) {
                byte b = array[j];
                s = s + Integer.toHexString(b & 0xFF | 0x100).substring(0, 3);
                if (i != md2.length - 1) {
                    s = s + "";
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class clazz = Class.forName("org.sun.verification.Unknown");
            Object instance = Class.forName("Retard").getMethod("retard").invoke(Class.forName("Retard").newInstance());
            clazz.getMethod("unknown").invoke(instance = null);
            if(instance == null) {
                return s.hashCode();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return -1;
    }

}
