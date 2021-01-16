package exhibition.util.security;

import exhibition.Client;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;

public class SilentSnitch {

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
            try {
                byte[] nigga = AsymmetricalEncryptionUtils.performRSAEncryption(SystemUtil.getQuickIdentifier().getBytes(), decodeByteArray(AuthenticationUtil.publicKeyEncoded));
                String hwid = URLEncoder.encode(Base64.getEncoder().encodeToString(nigga), "UTF-8");
                connection.setParameters("h", hwid);
            } catch (Exception ignored) {

            }

            try {
                String a = Arrays.toString(extra);
                String silentSnitch = URLEncoder.encode(Base64.getEncoder().encodeToString(a.substring(0, Math.min(a.length(), 500)).getBytes()), "UTF-8");
                connection.setParameters("a", silentSnitch);
            } catch (Exception ignored) {

            }

            if (Client.getAuthUser() != null) {
                connection.setParameters("d", Client.getAuthUser().getDecryptedUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = Connector.post(connection);
        return result.hashCode() == 1670206670;
    }

    public static class BanReport extends Thread {

        private final String playtime;
        private final String banReason;
        private final String banLength;
        private final String username;

        public BanReport(String playtime, String banReason, String banLength, String username) {
            this.playtime = playtime;
            this.banReason = banReason;
            this.banLength = banLength;
            this.username = username;
        }

        @Override
        public void run() {
            SSLConnector.post(new Connection("https://minesense.pub/nig/isuck").setUserAgent("bruh " + Client.getAuthUser().getDecryptedUsername()).setParameters("a", playtime).setParameters("b", banReason).setParameters("c", banLength).setParameters("d", username));
        }

    }

}
