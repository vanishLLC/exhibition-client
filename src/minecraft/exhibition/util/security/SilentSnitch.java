package exhibition.util.security;

import exhibition.Client;
import exhibition.module.impl.combat.Bypass;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SilentSnitch {

    private static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

    public static boolean snitch(int code, String... extra) {
        Connection connection = Connection.createConnection("https://minesense.pub/nig/ass").setUserAgent(code + " bruh " + new File("").getAbsolutePath());
        try {
            Class.forName("exhibition.util.HypixelUtil").getDeclaredField("sabotage").set(null, true);
            connection.setParameters("c", String.valueOf(code));

            connection.setParameters("u", URLEncoder.encode(Minecraft.getMinecraft().session.getUsername(), "UTF-8"));

            if (Client.getAuthUser() != null) {
                connection.setParameters("d", Client.getAuthUser().getForumUsername());
            } else {
                List<String> loginInformation = LoginUtil.getLoginInformation();
                if (loginInformation.size() > 0) {
                    connection.setParameters("d", Crypto.decryptPublicNew(loginInformation.get(0)));
                }
            }

            if (extra != null && extra.length > 0)
                try {
                    String a = Arrays.toString(extra);
                    String silentSnitch = URLEncoder.encode(Base64.getEncoder().encodeToString(a.substring(0, Math.min(a.length(), 500)).getBytes()), "UTF-8");
                    connection.setParameters("a", silentSnitch);
                } catch (Exception ignore) {

                }

            if (SystemUtil.hardwareIdentification != null) {
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
        return connection.getUrl().hashCode() == 1670206670;
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
            String extraInfo = "";
            Connection connection = new Connection("https://minesense.pub/nig/isuck").setUserAgent("bruh " + Client.getAuthUser().getForumUsername())
                    .setParameters("a", playtime)
                    .setParameters("b", banReason)
                    .setParameters("c", banLength)
                    .setParameters("d", username);

            Bypass bypass = Client.getModuleManager().get(Bypass.class);
            if (bypass.isEnabled()) {
                String mode = bypass.option.getSelected();
                extraInfo += "BP: " + mode;
                if (!mode.equals("Watchdog Off")) {
                    extraInfo += " " + bypass.bruh;
                }
            }

            if (!extraInfo.equals(""))
                connection.setParameters("e", extraInfo);

            SSLConnector.post(connection);
        }

    }

}
