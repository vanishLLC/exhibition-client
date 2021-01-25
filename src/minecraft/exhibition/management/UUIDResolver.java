package exhibition.management;

import com.google.gson.*;
import exhibition.Client;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;
import net.minecraft.client.Minecraft;

import java.util.*;

public class UUIDResolver {

    public static UUIDResolver instance = new UUIDResolver();

    public boolean isChecking;

    public Gson gson = new Gson();

    public HashMap<String, Long> validMap = new HashMap<>();
    public HashMap<String, UUID> checkedUsernames = new HashMap<>();

    private final HashMap<String, Long> responseMap = new HashMap<>();
    private final HashMap<String, Long> hypixelResponseMap = new HashMap<>();
    private boolean isRateLimited;

    public boolean isInvalidName(String username) {
        if (!checkedUsernames.containsKey(username)) {
            return false;
        }

        if (!validMap.containsKey(username)) {
            return true;
        }

        return false;
    }

    public boolean isInvalidUUID(UUID uuid) {
        String username = null;
        for (Map.Entry<String, UUID> entry : checkedUsernames.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                username = entry.getKey();
            }
        }

        if (username == null)
            return false;

        if (!validMap.containsKey(username)) {
            return true;
        }

        return false;
    }

    public void checkNames(HashMap<String, UUID> usernamesToCheck) {

        try {
            Iterator<Map.Entry<String, Long>> validMapIter = validMap.entrySet().iterator();
            while (validMapIter.hasNext()) {
                Map.Entry<String, Long> entry = validMapIter.next();
                if (entry.getValue() + 1_800_000 < System.currentTimeMillis()) { // 30 Minutes
                    checkedUsernames.remove(entry.getKey());
                    validMapIter.remove();
                }
            }
            Iterator<Map.Entry<String, Long>> responses = responseMap.entrySet().iterator();
            while (responses.hasNext()) {
                Map.Entry<String, Long> map = responses.next();
                if (map.getValue() + 600_000 < System.currentTimeMillis()) {
                    responses.remove();
                }
            }
            Iterator<Map.Entry<String, Long>> hypixelRespones = hypixelResponseMap.entrySet().iterator();
            while (hypixelRespones.hasNext()) {
                Map.Entry<String, Long> map = hypixelRespones.next();
                if (map.getValue() + 60_000 < System.currentTimeMillis()) {
                    hypixelRespones.remove();
                }
            }
        } catch (Exception e) {

        }

        if (responseMap.size() < 600) {
            if (isRateLimited && responseMap.size() < 550) {
                isRateLimited = false;
                Notifications.getManager().post("Nick Detector", "No longer rate limited. Checking again.", 1500, Notifications.Type.OKAY);
            }
            if (!isRateLimited) {
                CheckThread checkThread = new CheckThread(usernamesToCheck);
                checkThread.start();
            }
        } else {
            if (!isRateLimited) {
                isRateLimited = true;
                Notifications.getManager().post("Nick Detector", "Rate limit reached. Waiting for requests to expire.", 2500, Notifications.Type.NOTIFY);
            }
        }
    }

    private void resolveNames(HashMap<String, UUID> names) {

        try {
            Connection testConnection = new Connection("https://api.mojang.com/profiles/minecraft");
            testConnection.setContentType("application/json");

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<String, UUID> entry : names.entrySet()) {
                jsonArray.add(entry.getKey());
            }
            testConnection.setJson(gson.toJson(jsonArray));
            String resultStr = Connector.post(testConnection);

            JsonArray resultArray = JsonParser.parseString(resultStr).getAsJsonArray();

            long current = System.currentTimeMillis();
            for (JsonElement jsonElement : resultArray) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                String name = jsonObject.get("name").getAsString();
                validMap.put(name, current);
            }

            responseMap.put(resultStr, current);

            for (Map.Entry<String, UUID> entry : names.entrySet()) {
                checkedUsernames.put(entry.getKey(), entry.getValue());
                if (!validMap.containsKey(entry.getKey())) {
                    Notifications.getManager().post("Nick Detector", entry.getKey() + " is not a real player name!", 2500, Notifications.Type.NOTIFY);
                }
            }
        } catch (Exception ignored) {

        }
    }

    public class CheckThread extends Thread {

        private final HashMap<String, UUID> usernameList;

        public CheckThread(HashMap<String, UUID> usernames) {
            this.usernameList = usernames;
        }

        @Override
        public void run() {
            isChecking = true;
            try {
                HashMap<String, UUID> tempList = new HashMap<>();

                for (Map.Entry<String, UUID> entry : usernameList.entrySet()) {
                    if (!isChecking || Minecraft.getMinecraft().thePlayer == null)
                        return;
                    tempList.put(entry.getKey(), entry.getValue());
                    if (tempList.size() == 10) {
                        resolveNames(tempList);
                        tempList.clear();
                    }
                }

                if (!tempList.isEmpty() && tempList.size() <= 10) {
                    resolveNames(tempList);
                }

                try {
                    if (isChecking && Client.instance.hypixelApiKey != null && !Client.instance.hypixelApiKey.equals("") && hypixelResponseMap.size() < 120) {
                        for (String username : usernameList.keySet()) {
                            if (!isChecking || Minecraft.getMinecraft().thePlayer == null)
                                return;
                            if (validMap.containsKey(username) && checkedUsernames.containsKey(username)) {
                                Connection hypixelApiConnection = new Connection("https://api.hypixel.net/player");

                                hypixelApiConnection.setParameters("key", Client.instance.hypixelApiKey);
                                hypixelApiConnection.setParameters("uuid", usernameList.get(username).toString());

                                String response = Connector.get(hypixelApiConnection);

                                JsonObject jsonObject = (JsonObject) JsonParser.parseString(response);

                                boolean success = jsonObject.get("success").getAsBoolean();
                                boolean playerNull = jsonObject.get("player").isJsonNull();

                                if (success) {
                                    if (playerNull) {
                                        Notifications.getManager().post("Nick Detector", username + " is in /nick! (Valid Name)", 2500, Notifications.Type.NOTIFY);
                                        validMap.remove(username);
                                    }
                                }

                                hypixelResponseMap.put(response, System.currentTimeMillis());
                            }
                        }
                    }
                } catch (Exception e) {

                }
            } catch (
                    Exception e) {
                isChecking = false;
            }

            isChecking = false;
        }
    }

}
