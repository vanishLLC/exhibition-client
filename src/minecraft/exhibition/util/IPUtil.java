package exhibition.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;

import java.util.ArrayList;
import java.util.List;

public class IPUtil {

    private static List<String> usedAddresses = new ArrayList<>();

    private static String getIPAddress() {
        Connection connection = new Connection("https://api.ipify.org/?format=json");
        String response = Connector.get(connection);
        JsonObject jsonObject = (JsonObject)new JsonParser().parse(response);
        if(jsonObject != null && jsonObject.has("ip")) {
            return jsonObject.get("ip").getAsString();
        }
        return null;
    }

    public static void checkIP() {
        try {
            String currentIP = getIPAddress();
            if (currentIP != null) {
                if (usedAddresses.contains(currentIP)) {
                    Notifications.getManager().post("\247cIP Check", "This IP has been used before, please change IP!", Notifications.Type.WARNING);
                } else {
                    Notifications.getManager().post("\247aIP Check", "This IP is not currently marked as banned.", Notifications.Type.OKAY);
                }
            } else {
                Notifications.getManager().post("IP Check", "Could not check current IP.", Notifications.Type.NOTIFY);
            }
        } catch (Exception e) {
            Notifications.getManager().post("IP Check", "Could not check current IP.", Notifications.Type.NOTIFY);
        }
    }

    public static void setIPBanned() {
        String currentIP = getIPAddress();
        if(currentIP != null) {
            if(!usedAddresses.contains(currentIP)) {
                Notifications.getManager().post("IP Banned", "This IP is now temp banned, please change IP!", Notifications.Type.WARNING);
                usedAddresses.add(currentIP);
            }
        }
    }

}
